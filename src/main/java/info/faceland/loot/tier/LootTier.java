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
package info.faceland.loot.tier;

import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.api.tier.Tier;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LootTier implements Tier {

    private final String name;
    private String displayName;
    private ChatColor displayColor;
    private ChatColor identificationColor;
    private double spawnWeight;
    private double identifyWeight;
    private int minimumSockets;
    private int maximumSockets;
    private int minimumBonusLore;
    private int maximumBonusLore;
    private Set<ItemGroup> itemGroups;
    private List<String> baseLore;
    private List<String> bonusLore;
    private double minimumDurability;
    private double maximumDurability;
    private double distanceWeight;
    private boolean enchantable;
    private boolean broadcast;
    private double extendableChance;

    public LootTier(String name) {
        this.name = name;
        this.itemGroups = new HashSet<>();
        this.baseLore = new ArrayList<>();
        this.bonusLore = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public ChatColor getDisplayColor() {
        return displayColor;
    }

    void setDisplayColor(ChatColor displayColor) {
        this.displayColor = displayColor;
    }

    @Override
    public ChatColor getIdentificationColor() {
        return identificationColor;
    }

    void setIdentificationColor(ChatColor identificationColor) {
        this.identificationColor = identificationColor;
    }

    @Override
    public double getMinimumDurability() {
        return minimumDurability;
    }

    void setMinimumDurability(double minimumDurability) {
        this.minimumDurability = minimumDurability;
    }

    @Override
    public double getMaximumDurability() {
        return maximumDurability;
    }

    @Override
    public double getSpawnWeight() {
        return spawnWeight;
    }

    void setSpawnWeight(double spawnWeight) {
        this.spawnWeight = spawnWeight;
    }

    @Override
    public double getIdentifyWeight() {
        return identifyWeight;
    }

    void setIdentifyWeight(double identifyWeight) {
        this.identifyWeight = identifyWeight;
    }

    @Override
    public double getDistanceWeight() {
        return distanceWeight;
    }

    @Override
    public List<String> getBaseLore() {
        return new ArrayList<>(baseLore);
    }

    void setBaseLore(List<String> baseLore) {
        if (baseLore == null) {
            this.baseLore.clear();
        } else {
            this.baseLore = baseLore;
        }
    }

    @Override
    public List<String> getBonusLore() {
        return new ArrayList<>(bonusLore);
    }

    @Override
    public Set<ItemGroup> getItemGroups() {
        return new HashSet<>(itemGroups);
    }

    @Override
    public int getMinimumSockets() {
        return minimumSockets;
    }

    void setMinimumSockets(int minimumSockets) {
        this.minimumSockets = minimumSockets;
    }

    @Override
    public int getMaximumSockets() {
        return maximumSockets;
    }

    void setMaximumSockets(int maximumSockets) {
        this.maximumSockets = maximumSockets;
    }

    @Override
    public int getMinimumBonusLore() {
        return minimumBonusLore;
    }

    void setMinimumBonusLore(int minimumBonusLore) {
        this.minimumBonusLore = minimumBonusLore;
    }

    @Override
    public int getMaximumBonusLore() {
        return maximumBonusLore;
    }

    @Override
    public Set<Material> getAllowedMaterials() {
        Set<Material> materials = new HashSet<>();
        for (ItemGroup ig : getItemGroups()) {
            if (!ig.isInverse()) {
                for (Material m : ig.getMaterials()) {
                    materials.add(m);
                }
            }
        }
        for (ItemGroup ig : getItemGroups()) {
            if (ig.isInverse()) {
                for (Material m : ig.getMaterials()) {
                    materials.remove(m);
                }
            }
        }
        return materials;
    }

    @Override
    public boolean isEnchantable() {
        return enchantable;
    }

    void setEnchantable(boolean enchantable) {
        this.enchantable = enchantable;
    }

    @Override
    public boolean isBroadcast() {
        return broadcast;
    }

    void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    @Override
    public double getExtendableChance() {
        return extendableChance;
    }

    void setExtendableChance(double extendableChance) {
        this.extendableChance = extendableChance;
    }

    void setMaximumBonusLore(int maximumBonusLore) {
        this.maximumBonusLore = maximumBonusLore;
    }

    void setItemGroups(Set<ItemGroup> itemGroups) {
        if (itemGroups == null) {
            this.itemGroups.clear();
        } else {
            this.itemGroups = itemGroups;
        }
    }

    void setBonusLore(List<String> bonusLore) {
        if (bonusLore == null) {
            this.bonusLore.clear();
        } else {
            this.bonusLore = bonusLore;
        }
    }

    void setDistanceWeight(double distanceWeight) {
        this.distanceWeight = distanceWeight;
    }

    void setMaximumDurability(double maximumDurability) {
        this.maximumDurability = maximumDurability;
    }

    @Override
    public int compareTo(Tier o) {
        if (o == null) {
            return 1;
        }
        if (this.equals(o)) {
            return 0;
        }
        return Double.compare(getSpawnWeight(), o.getSpawnWeight());
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LootTier lootTier = (LootTier) o;

        return !(name != null ? !name.equals(lootTier.name) : lootTier.name != null);
    }

}
