package com.aqmoe.botdetector;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Locale;

public class OreBlocksFilter {
    public static boolean isOreBlock(Block block) {
        Material material = block.getType();
        String name = material.name().toLowerCase(Locale.ROOT);
        if (name.contains("_ore") || name.contains("deepslate")) {
            return true;
        }
        switch (name) {
            case "cobblestone":
            case "andesite":
            case "sand":
            case "diorite":
            case "granite":
            case "dirt":
            case "gravel":
            case "stone":
                return true;
        }
        return false;
    }
}
