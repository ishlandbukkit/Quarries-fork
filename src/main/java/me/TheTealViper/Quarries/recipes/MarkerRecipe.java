package me.TheTealViper.Quarries.recipes;

import me.TheTealViper.Quarries.CustomItems1_15;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class MarkerRecipe {

    public static void register() {
        ShapelessRecipe recipe = new ShapelessRecipe(NamespacedKey.minecraft("quarry_marker"), CustomItems1_15.getItem(332447));
        recipe.addIngredient(new ItemStack(Material.REDSTONE_TORCH, 1));
        recipe.addIngredient(new ItemStack(Material.LAPIS_LAZULI, 1));
        Bukkit.addRecipe(recipe);
    }
}
