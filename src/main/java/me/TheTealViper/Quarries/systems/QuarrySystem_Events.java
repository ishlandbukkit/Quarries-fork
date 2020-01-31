package me.TheTealViper.Quarries.systems;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;

public class QuarrySystem_Events implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) throws IOException {
        if (QuarrySystem.DATABASE.containsKey(e.getBlock().getLocation())) {
            QuarrySystem.DATABASE.get(e.getBlock().getLocation()).destroy();
        }
    }

}
