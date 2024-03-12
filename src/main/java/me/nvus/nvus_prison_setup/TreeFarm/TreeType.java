package me.nvus.nvus_prison_setup.TreeFarm;

import org.bukkit.Material;
import java.util.EnumSet;

public enum TreeType {
    OAK(Material.OAK_LOG, Material.OAK_SAPLING),
    SPRUCE(Material.SPRUCE_LOG, Material.SPRUCE_SAPLING),
    BIRCH(Material.BIRCH_LOG, Material.BIRCH_SAPLING),
    JUNGLE(Material.JUNGLE_LOG, Material.JUNGLE_SAPLING),
    ACACIA(Material.ACACIA_LOG, Material.ACACIA_SAPLING),
    DARK_OAK(Material.DARK_OAK_LOG, Material.DARK_OAK_SAPLING);

    private final Material logMaterial;
    private final Material saplingMaterial;

    TreeType(Material logMaterial, Material saplingMaterial) {
        this.logMaterial = logMaterial;
        this.saplingMaterial = saplingMaterial;
    }

    public Material getLogMaterial() {
        return logMaterial;
    }

    public Material getSaplingMaterial() {
        return saplingMaterial;
    }

    public static boolean isLog(Material material) {
        for (TreeType treeType : TreeType.values()) {
            if (treeType.getLogMaterial() == material) {
                return true;
            }
        }
        return false;
    }
}
