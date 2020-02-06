package me.TheTealViper.Quarries.systems.listeners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class QuarrySystemListeners implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (QuarrySystem.DATABASE.containsKey(e.getBlock().getLocation())) {
            QuarrySystem.DATABASE.get(e.getBlock().getLocation()).destroy();
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        Item itemEntity = e.getEntity();
        Location itemLoc = itemEntity.getLocation();
        ItemStack item = itemEntity.getItemStack();
        Quarries.pool.execute(() ->
        {
            for (Map.Entry<Location, QuarrySystem> entry : QuarrySystem.DATABASE.entrySet())
                Quarries.pool.execute(() -> {
                    QuarrySystem system = entry.getValue();
                    if (
                            system.min.getX() < itemLoc.getX() &&
                                    system.min.getZ() < itemLoc.getZ() &&
                                    system.max.getX() > itemLoc.getX() &&
                                    system.max.getZ() > itemLoc.getZ() &&
                                    system.max.getY() > itemLoc.getY()
                    )
                        Quarries.scheduler.runSync(() -> {
                            system.handleMinedItem(item);
                            itemEntity.remove();
                        });
                });
        });
    }

}
