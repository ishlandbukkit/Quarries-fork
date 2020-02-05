package me.TheTealViper.Quarries.integration.slimefun;

import me.TheTealViper.Quarries.abstractProviders.AIntegration;
import me.TheTealViper.Quarries.integration.slimefun.blocks.QuarryInterface;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SlimefunIntegration extends AIntegration {

    public static final SlimefunItemStack QUARRY_INTERFACE = new SlimefunItemStack(
            "QUARRY_INTERFACE", Material.DISPENSER, "Quarry Interface");

    public static final Category QUARRIES = new Category(new CustomItem(Material.DIAMOND_PICKAXE, "Quarries"));

    private QuarryInterface quarryInterface;

    public SlimefunIntegration() {
        super();
    }

    @Override
    protected void preRegister() {

    }

    @Override
    protected void postRegister() {

    }

    @Override
    protected void registerInterface() {
        quarryInterface = new QuarryInterface(QUARRIES, QUARRY_INTERFACE,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{

                });
    }
}
