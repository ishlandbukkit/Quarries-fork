package me.TheTealViper.Quarries.recipes;

import me.TheTealViper.Quarries.nms.v1_15_R1.CustomItems1_15;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

public class QuarryRecipe {

    public static void register() {
        ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("quarry"), CustomItems1_15.getItem(576161));
        recipe.shape(
                "IRI",
                "GIG",
                "DHD"
        );
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('H', Material.HOPPER);
        Bukkit.addRecipe(recipe);
    }
}
