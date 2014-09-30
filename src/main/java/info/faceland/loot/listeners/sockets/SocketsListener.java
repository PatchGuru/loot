package info.faceland.loot.listeners.sockets;

import info.faceland.hilt.HiltItemStack;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.enchantments.EnchantmentStone;
import info.faceland.loot.api.sockets.SocketGem;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.utils.StringListUtils;
import info.faceland.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SocketsListener implements Listener {

    private LootPlugin plugin;
    private LootRandom random;

    public SocketsListener(LootPlugin plugin) {
        this.plugin = plugin;
        this.random = new LootRandom(System.currentTimeMillis());
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
            if (attackerP.getItemInHand() != null && attackerP.getItemInHand().getType() != Material.AIR) {
                HiltItemStack inHand = new HiltItemStack(attackerP.getItemInHand());
                List<String> lore = inHand.getLore();
                List<String> strippedLore = StringListUtils.stripColor(lore);
                for (String key : strippedLore) {
                    SocketGem gem = plugin.getSocketGemManager().getSocketGem(key);
                    if (gem == null) {
                        continue;
                    }
                    attackerGems.add(gem);
                }
            }
        }
        if (defender instanceof Player) {
            Player defenderP = (Player) defender;
            for (ItemStack equipment : defenderP.getEquipment().getArmorContents()) {
                if (equipment == null || equipment.getType() == Material.AIR) {
                    continue;
                }
                HiltItemStack item = new HiltItemStack(equipment);
                List<String> lore = item.getLore();
                List<String> strippedLore = StringListUtils.stripColor(lore);
                for (String key : strippedLore) {
                    SocketGem gem = plugin.getSocketGemManager().getSocketGem(key);
                    if (gem == null) {
                        continue;
                    }
                    defenderGems.add(gem);
                }
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCursor() == null
            || event.getCurrentItem().getType() == Material.AIR || event.getCursor().getType() == Material.AIR) {
            return;
        }
        HiltItemStack currentItem = new HiltItemStack(event.getCurrentItem());
        HiltItemStack cursor = new HiltItemStack(event.getCursor());

        if (cursor.getName().startsWith(ChatColor.GOLD + "Socket Gem - ")) {
            String gemName = ChatColor.stripColor(cursor.getName().replace(ChatColor.GOLD + "Socket Gem - ", ""));
            SocketGem gem = plugin.getSocketGemManager().getSocketGem(gemName);

            if (gem == null) {
                return;
            }

            List<String> lore = currentItem.getLore();
            List<String> strippedLore = StringListUtils.stripColor(lore);
            if (!strippedLore.contains("(Socket)")) {
                return;
            }
            int index = strippedLore.indexOf("(Socket)");

            lore.set(index, ChatColor.GOLD + gem.getName());
            lore.addAll(index + 1, TextUtils.color(gem.getLore()));

            currentItem.setLore(lore);
        } else if (cursor.getName().startsWith(ChatColor.BLUE + "Enchantment Stone - ")) {
            String stoneName = ChatColor.stripColor(
                    cursor.getName().replace(ChatColor.BLUE + "Enchantment Stone - ", ""));
            EnchantmentStone stone = plugin.getEnchantmentStoneManager().getEnchantmentStone(stoneName);

            if (!isBlockWithinRadius(Material.ENCHANTMENT_TABLE, event.getWhoClicked().getLocation(), 5)) {
                return;
            }

            if (stone == null) {
                return;
            }

            List<String> lore = currentItem.getLore();
            List<String> strippedLore = StringListUtils.stripColor(lore);
            if (!strippedLore.contains("(Enchantable)")) {
                return;
            }
            int index = strippedLore.indexOf("(Enchantable)");

            List<String> added = new ArrayList<>();
            for (int i = 0; i < random.nextIntRange(stone.getMinStats(), stone.getMaxStats()); i++) {
                added.add(stone.getLore().get(random.nextInt(stone.getLore().size())));
            }

            lore.remove(index);
            lore.addAll(index, TextUtils.color(added));

            currentItem.setLore(lore);
        } else {
            return;
        }

        event.setCurrentItem(currentItem);
        event.setCursor(null);
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
    }

    private boolean isBlockWithinRadius(Material material, Location location, int radius) {
        int minX = location.getBlockX() - radius;
        int maxX = location.getBlockX() + radius;
        int minY = location.getBlockY() - radius;
        int maxY = location.getBlockY() + radius;
        int minZ = location.getBlockZ() - radius;
        int maxZ = location.getBlockZ() + radius;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    Block block = location.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == material) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
