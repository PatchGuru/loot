package info.faceland.loot.api.items;

import info.faceland.hilt.HiltItemStack;
import org.bukkit.Material;

import java.util.List;

public interface CustomItem {

    String getName();

    String getDisplayName();

    List<String> getLore();

    Material getMaterial();

    HiltItemStack toItemStack(int amount);

}