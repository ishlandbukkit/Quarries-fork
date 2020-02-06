package me.TheTealViper.Quarries.integration.slimefun.blocks;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunBlockHandler;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.UnregisterReason;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class QuarryInterface extends SlimefunItem implements InventoryBlock {

    public static final Map<Block, MachineRecipe> processing = new HashMap<>();
    public static final Map<Block, Integer> progress = new HashMap<>();
    private static final int[] slot_border = {
            0, 1, 2, 3, 4, 5, 6, 7,
            9, 16, 18, 25, 27, 34, 36, 43,
            45, 46, 47, 48, 50, 51, 52
    };
    private static final int slot_progress = 49;
    public final int energyConsumption = 10;
    private final String invTitle = "Quarry Interface";

    public QuarryInterface(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        createPreset(this, this::constructMenu);

        registerBlockHandler(getID(), new SlimefunBlockHandler() {

            @Override
            public void onPlace(Player p, Block b, SlimefunItem item) {
                BlockStorage.addBlockInfo(b, "owner", p.getUniqueId().toString());
            }

            @Override
            public boolean onBreak(Player p, Block b, SlimefunItem item, UnregisterReason reason) {
                BlockMenu inv = BlockStorage.getInventory(b);

                if (inv != null) {
                    for (int slot : getOutputSlots()) {
                        if (inv.getItemInSlot(slot) != null) {
                            b.getWorld().dropItemNaturally(b.getLocation(), inv.getItemInSlot(slot));
                            inv.replaceExistingItem(slot, null);
                        }
                    }
                }
                progress.remove(b);
                processing.remove(b);
                return true;
            }
        });
    }

    protected void constructMenu(BlockMenuPreset preset) {
        for (int slot : slot_border) {
            preset.addItem(slot, new CustomItem(new ItemStack(Material.CYAN_STAINED_GLASS_PANE), " "),
                    (p, s, item, action) -> false);
        }
        preset.addItem(slot_progress, new CustomItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "),
                (p, s, item, action) -> false);

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, new ChestMenu.AdvancedMenuClickHandler() {

                @Override
                public boolean onClick(Player p, int slot, ItemStack cursor, ClickAction action) {
                    return false;
                }

                @Override
                public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor, ClickAction action) {
                    return cursor == null || cursor.getType() == Material.AIR;
                }
            });
        }
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @SuppressWarnings("deprecation")
            @Override
            public void tick(Block b, SlimefunItem sf, Config data) {
                QuarryInterface.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    private void tick(Block b) {

    }

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15,
                29, 20, 21, 22, 23, 24,
                28, 29, 30, 31, 32, 33,
                37, 38, 39, 40, 41, 42,
        };
    }

}
