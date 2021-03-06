/*
 * This file is part of Loot, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.loot.listeners.sockets;

import com.kill3rtaco.tacoserialization.SingleItemSerialization;
import com.tealcube.minecraft.bukkit.facecore.shade.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.Predicates;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.collect.Iterables;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.collect.Lists;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.math.Vec3;
import info.faceland.loot.api.sockets.SocketGem;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

public final class SocketsListener implements Listener {

    private LootPlugin plugin;
    private final Map<UUID, List<String>> gems;

    public SocketsListener(LootPlugin plugin) {
        this.plugin = plugin;
        this.gems = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled() || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Set<SocketGem> gems = getGems(((Player) event.getEntity().getShooter()).getItemInHand());
        List<String> names = new ArrayList<>();
        for (SocketGem gem : gems) {
            names.add(gem.getName());
        }
        event.getEntity().setMetadata("loot.gems", new FixedMetadataValue(plugin, names.toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        List<SocketGem> attackerGems = new ArrayList<>();
        List<SocketGem> defenderGems = new ArrayList<>();
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();
        if (attacker instanceof Player) {
            Player attackerP = (Player) attacker;
            attackerGems.addAll(getGems(attackerP.getEquipment().getItemInHand()));
        } else if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof Player) {
            attacker = (Player) ((Projectile) attacker).getShooter();
            if (event.getDamager().hasMetadata("loot.gems")) {
                for (MetadataValue val : event.getDamager().getMetadata("loot.gems")) {
                    if (!val.getOwningPlugin().equals(plugin)) {
                        continue;
                    }
                    String blah = val.asString().replace("[", "").replace("]", "");
                    for (String s : blah.split(",")) {
                        SocketGem gem = plugin.getSocketGemManager().getSocketGem(s.trim());
                        if (gem == null) {
                            continue;
                        }
                        attackerGems.add(gem);
                    }
                }
            }
        }
        if (defender instanceof Player) {
            Player defenderP = (Player) defender;
            for (ItemStack equipment : defenderP.getEquipment().getArmorContents()) {
                defenderGems.addAll(getGems(equipment));
            }
        }

        for (SocketGem gem : attackerGems) {
            for (SocketEffect effect : gem.getSocketEffects()) {
                switch (effect.getTarget()) {
                    case SELF:
                        if (attacker instanceof LivingEntity) {
                            effect.apply((LivingEntity) attacker);
                        }
                        break;
                    case OTHER:
                        if (defender instanceof LivingEntity) {
                            effect.apply((LivingEntity) defender);
                        }
                        break;
                    case AREA:
                        for (Entity e : defender
                                .getNearbyEntities(effect.getRadius(), effect.getRadius(), effect.getRadius())) {
                            if (e instanceof LivingEntity) {
                                effect.apply((LivingEntity) e);
                            }
                        }
                        if (defender instanceof LivingEntity) {
                            effect.apply((LivingEntity) defender);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        for (SocketGem gem : defenderGems) {
            for (SocketEffect effect : gem.getSocketEffects()) {
                switch (effect.getTarget()) {
                    case SELF:
                        if (defender instanceof LivingEntity) {
                            effect.apply((LivingEntity) defender);
                        }
                        break;
                    case OTHER:
                        if (attacker instanceof LivingEntity) {
                            effect.apply((LivingEntity) attacker);
                        }
                        break;
                    case AREA:
                        for (Entity e : attacker
                                .getNearbyEntities(effect.getRadius(), effect.getRadius(), effect.getRadius())) {
                            if (e instanceof LivingEntity) {
                                effect.apply((LivingEntity) e);
                            }
                        }
                        if (attacker instanceof LivingEntity) {
                            effect.apply((LivingEntity) attacker);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName()).equals("Socket Gem Combiner")) {
            return;
        }
        if (event.getInventory().getSize() > 9) {
            return;
        }
        HiltItemStack his = new HiltItemStack(event.getCurrentItem());
        if (!his.getName().startsWith(ChatColor.GOLD + "Socket Gem - ")) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
        his = new HiltItemStack(event.getCursor());
        if (!his.getName().startsWith(ChatColor.GOLD + "Socket Gem - ")) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
        if (event.isShiftClick()) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof Chest)) {
            return;
        }
        Vec3 loc = new Vec3(((Chest) holder).getWorld().getName(), ((Chest) holder).getX(), ((Chest) holder).getY(),
                ((Chest) holder).getZ());
        if (!plugin.getChestManager().getChestLocations().contains(loc)) {
            return;
        }
        event.setCancelled(true);
        Inventory toShow = Bukkit.createInventory(null, 9, "Socket Gem Combiner");
        toShow.setMaxStackSize(1);
        List<String> toAdd = new ArrayList<>();
        toAdd.addAll(gems.containsKey(event.getPlayer().getUniqueId()) ? gems.get(event.getPlayer().getUniqueId()) : new ArrayList<String>());
        gems.remove(event.getPlayer().getUniqueId());
        for (String s : toAdd) {
            toShow.addItem(SingleItemSerialization.getItem(s));
        }
        event.getPlayer().openInventory(toShow);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName()).equals("Socket Gem Combiner")) {
            return;
        }
        if (event.getInventory().getSize() > 9) {
            return;
        }
        List<ItemStack> newResults = new ArrayList<>();
        List<ItemStack> contents = Lists.newArrayList(Iterables.filter(Arrays.asList(event.getInventory().getContents()),
                Predicates.notNull()));
        for (ItemStack content : contents) {
            HiltItemStack his = new HiltItemStack(content);
            if (!his.getName().startsWith(ChatColor.GOLD + "Socket Gem - ")) {
                MessageUtils.sendMessage(event.getPlayer(), "<green>All items must be Socket Gems in order to transmute.");
                return;
            }
        }
        while (contents.size() >= 4) {
            contents = contents.subList(4, contents.size());
            newResults.add(plugin.getSocketGemManager().getRandomSocketGemByBonus().toItemStack(1));
        }
        newResults.addAll(contents);
        List<String> toAdd = new ArrayList<>();
        for (ItemStack is : newResults) {
            toAdd.add(SingleItemSerialization.serializeItemAsString(is));
        }
        if (toAdd.size() > 0) {
            HumanEntity c = event.getPlayer();
            c.getWorld().playEffect(c.getLocation().add(0, 1, 0), Effect.SPELL, 0);
            c.getWorld().playSound(c.getLocation().add(0, 1, 0), Sound.ENDERMAN_SCREAM, 1.0f, 1.0f);
            MessageUtils.sendMessage(event.getPlayer(), "<green>Open the chest again to get your new Socket Gems!");
        }
        gems.put(event.getPlayer().getUniqueId(), toAdd);
    }

    private Set<SocketGem> getGems(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return new HashSet<>();
        }
        Set<SocketGem> gems = new HashSet<>();
        HiltItemStack item = new HiltItemStack(itemStack);
        List<String> lore = item.getLore();
        List<String> strippedLore = stripColor(lore);
        for (String key : strippedLore) {
            SocketGem gem = plugin.getSocketGemManager().getSocketGem(key);
            if (gem == null) {
                for (SocketGem g : plugin.getSocketGemManager().getSocketGems()) {
                    if (key.equals(ChatColor.stripColor(TextUtils.color(
                            g.getTriggerText() != null ? g.getTriggerText() : "")))) {
                        gem = g;
                        break;
                    }
                }
                if (gem == null) {
                    continue;
                }
            }
            gems.add(gem);
        }
        return gems;
    }

    private List<String> stripColor(List<String> strings) {
        List<String> ret = new ArrayList<>();
        for (String s : strings) {
            ret.add(ChatColor.stripColor(s));
        }
        return ret;
    }

}
