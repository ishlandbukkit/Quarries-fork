package me.TheTealViper.Quarries.integration.slimefun;

import io.github.thebusybiscuit.slimefun4.core.attributes.MachineTier;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineType;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.abstractProviders.AIntegration;
import me.TheTealViper.Quarries.integration.slimefun.blocks.QuarryInterface;
import me.TheTealViper.Quarries.nms.v1_15_R1.CustomItems1_15;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SlimefunIntegration extends AIntegration {

    public static final SlimefunItemStack QUARRY_INTERFACE = new SlimefunItemStack(
            "QUARRY_INTERFACE",
            "6f68d509b5d1669b971dd1d4df2e47e19bcb1b33bf1a7ff1dda29bfc6f9ebf",
            "Quarry Interface",
            "",
            "&rGives quarry energy and get mined items from it",
            "",
            LoreBuilder.machine(MachineTier.ADVANCED, MachineType.MACHINE),
            LoreBuilder.powerBuffer(1024),
            "&r10J/block"
    );

    public static final Category QUARRIES = new Category(new CustomItem(Material.DIAMOND_PICKAXE, "Quarries"));

    @SuppressWarnings("FieldCanBeLocal")
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
                        null, CustomItems1_15.getItem(Quarries.TEXID_MARKER), null,
                        CustomItems1_15.getItem(Quarries.TEXID_MARKER), new ItemStack(Material.CHEST),
                        CustomItems1_15.getItem(Quarries.TEXID_MARKER),
                        null, CustomItems1_15.getItem(Quarries.TEXID_MARKER), null
                });
        quarryInterface.registerChargeableBlock(1024);
    }
}
