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
package info.faceland.loot.items;

import com.tealcube.minecraft.bukkit.facecore.shade.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemBuilder;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.api.tier.Tier;
import info.faceland.loot.math.LootRandom;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LootItemBuilder implements ItemBuilder {

    private final LootPlugin plugin;
    private boolean built = false;
    private Tier tier;
    private Material material;
    private ItemGenerationReason itemGenerationReason = ItemGenerationReason.MONSTER;
    private LootRandom random;
    private double distance;

    public LootItemBuilder(LootPlugin plugin) {
        this.plugin = plugin;
        this.random = new LootRandom(System.currentTimeMillis());
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    @Override
    public HiltItemStack build() {
        if (isBuilt()) {
            throw new IllegalStateException("already built");
        }
        built = true;
        HiltItemStack hiltItemStack;
        int attempts = 0;
        while (tier == null && attempts < 10) {
            tier = chooseTier();
            if (material != null && tier != null && !tier.getAllowedMaterials().contains(material)) {
                tier = null;
            }
            attempts++;
        }
        if (tier == null) {
            throw new IllegalStateException("tier is null");
        }
        if (material == null) {
            Set<Material> set = tier.getAllowedMaterials();
            Material[] array = set.toArray(new Material[set.size()]);
            if (array.length == 0) {
                throw new RuntimeException("array length is 0 for tier: " + tier.getName());
            }
            material = array[random.nextInt(array.length)];
        }
        hiltItemStack = new HiltItemStack(material);
        hiltItemStack.setUnbreakable(true);
        hiltItemStack.setName(tier.getDisplayColor() + plugin.getNameManager().getRandomPrefix() + " " + plugin
                .getNameManager().getRandomSuffix() + tier.getIdentificationColor());
        List<String> lore = new ArrayList<>(tier.getBaseLore());
        lore.addAll(plugin.getSettings().getStringList("corestats." + material.name(),
                                                       new ArrayList<String>()));
        int bonusLore = random.nextIntRange(tier.getMinimumBonusLore(), tier.getMaximumBonusLore());
        for (int i = 0; i < bonusLore; i++) {
            lore.add(tier.getBonusLore().get(random.nextInt(tier.getBonusLore().size())));
        }
        if (tier.isEnchantable()) {
            lore.add("<blue>(Enchantable)");
        }
        int sockets = random.nextIntRange(tier.getMinimumSockets(), tier.getMaximumSockets());
        for (int i = 0; i < sockets; i++) {
            lore.add("<gold>(Socket)");
        }
        if (random.nextDouble() < tier.getExtendableChance()) {
            lore.add("<dark aqua>(+)");
        }
        hiltItemStack.setLore(TextUtils.color(lore));
        ItemMeta itemMeta = hiltItemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        hiltItemStack.setItemMeta(itemMeta);
        return hiltItemStack;
    }

    @Override
    public ItemBuilder withTier(Tier t) {
        tier = t;
        return this;
    }

    @Override
    public ItemBuilder withMaterial(Material m) {
        material = m;
        return this;
    }

    @Override
    public ItemBuilder withItemGenerationReason(ItemGenerationReason reason) {
        itemGenerationReason = reason;
        return this;
    }

    @Override
    public ItemBuilder withDistance(double d) {
        distance = d;
        return this;
    }

    private Tier chooseTier() {
        if (itemGenerationReason == ItemGenerationReason.IDENTIFYING) {
            double totalWeight = 0D;
            for (Tier t : plugin.getTierManager().getLoadedTiers()) {
                totalWeight += t.getIdentifyWeight() + ((distance / 10000D) * t.getDistanceWeight());
            }
            double chosenWeight = random.nextDouble() * totalWeight;
            double currentWeight = 0D;
            for (Tier t : plugin.getTierManager().getLoadedTiers()) {
                currentWeight += t.getIdentifyWeight() + ((distance / 10000D) * t.getDistanceWeight());
                if (currentWeight >= chosenWeight) {
                    return t;
                }
            }
            return null;
        }
        return plugin.getTierManager().getRandomTier(true, distance);
    }

}
