package info.faceland.loot.groups;

import info.faceland.loot.api.groups.ItemGroup;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public final class LootItemGroup implements ItemGroup {

    private final String name;
    private final boolean inverse;
    private final Set<Material> legalMaterials;

    public LootItemGroup(String name, boolean inv) {
        this.name = name;
        this.legalMaterials = new HashSet<>();
        this.inverse = inv;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Material> getMaterials() {
        return new HashSet<>(legalMaterials);
    }

    @Override
    public void addMaterial(Material material) {
        legalMaterials.add(material);
    }

    @Override
    public void removeMaterial(Material material) {
        legalMaterials.remove(material);
    }

    @Override
    public boolean hasMaterial(Material material) {
        return legalMaterials.contains(material);
    }

    @Override
    public boolean isInverse() {
        return inverse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LootItemGroup that = (LootItemGroup) o;

        return inverse == that.inverse && !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (inverse ? 1 : 0);
        return result;
    }

}