package me.TheTealViper.Quarries.blocks.listeners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.blocks.Marker;
import me.TheTealViper.Quarries.nms.ServerVersions;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MarkerListeners implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        List<Marker> dummy = new ArrayList<>(Marker.DATABASE.values());
        Quarries.pool.execute(() -> {
            for (Marker marker : dummy) {
                if (e.getBlock().getLocation().equals(marker.loc)) {
                    Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, marker::breakMarker, 1);
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
                item.getItemMeta().getCustomModelData() == Quarries.TEXID_MARKER) {
//				e.setCancelled(true);
            if (Quarries.version == ServerVersions.v1_15_R1) {
                Block b = e.getBlockPlaced();
                new Marker(b.getLocation(), null, true);
            }
        }
    }

}
