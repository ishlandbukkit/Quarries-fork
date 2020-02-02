package me.TheTealViper.Quarries.blocks.listeners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.blocks.Quarry;
import me.TheTealViper.Quarries.nms.ServerVersions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuarryListeners implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        List<Quarry> dummy = new ArrayList<>(Quarry.DATABASE.values());
        Quarries.pool.execute(() -> {
            for (Quarry q : dummy) {
                if (q.loc.equals(e.getBlock().getLocation())) {
                    Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, q::breakQuarry, 1);
                }
            }
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled())
            return;

        // NotNull
        ItemStack item = e.getItemInHand();
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData() &&
                item.getItemMeta().getCustomModelData() == Quarries.TEXID_QUARRY) {
//				e.setCancelled(true);
            if (Quarries.version == ServerVersions.v1_15_R1) {
                new Quarry(e.getBlockPlaced().getLocation(), Quarries.getFacing(e.getPlayer()), true);
            }
        }
    }

}
