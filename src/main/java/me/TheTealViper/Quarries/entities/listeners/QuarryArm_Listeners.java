package me.TheTealViper.Quarries.entities.listeners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.entities.QuarryArm;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings({"DeprecatedIsStillUsed", "removal"})
@Deprecated(forRemoval = true)
public class QuarryArm_Listeners implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        List<QuarryArm> dummy = new ArrayList<>(QuarryArm.DATABASE.values());
        Quarries.pool.execute(() -> {
            for (QuarryArm arm : dummy) {
                if (e.getBlock().getLocation().equals(arm.loc)) {
                    Quarries.scheduler.runSync(arm::breakQuarryArm);
                }
            }
        });
    }

}
