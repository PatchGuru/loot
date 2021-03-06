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

import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.CustomItemBuilder;
import org.bukkit.Material;

import java.util.List;

public final class LootCustomItemBuilder implements CustomItemBuilder {

    private boolean built = false;
    private LootCustomItem customItem;

    public LootCustomItemBuilder(String name) {
        this.customItem = new LootCustomItem(name);
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    @Override
    public CustomItem build() {
        if (isBuilt()) {
            throw new IllegalStateException("already built");
        }
        this.built = true;
        return customItem;
    }

    @Override
    public CustomItemBuilder withDisplayName(String displayName) {
        customItem.setDisplayName(displayName);
        return this;
    }

    @Override
    public CustomItemBuilder withLore(List<String> lore) {
        customItem.setLore(lore);
        return this;
    }

    @Override
    public CustomItemBuilder withMaterial(Material material) {
        customItem.setMaterial(material);
        return this;
    }

    @Override
    public CustomItemBuilder withWeight(double d) {
        customItem.setWeight(d);
        return this;
    }

    @Override
    public CustomItemBuilder withDistanceWeight(double d) {
        customItem.setDistanceWeight(d);
        return this;
    }

    @Override
    public CustomItemBuilder withBroadcast(boolean b) {
        customItem.setBroadcast(b);
        return this;
    }

}
