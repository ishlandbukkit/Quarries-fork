package me.TheTealViper.Quarries.misc;

import me.TheTealViper.Quarries.Quarries;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/*
 *
 * @author TheBusyBiscuit
 */
public final class SimpleHologram {

    public static void update(Block b, String name) {
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, () -> {
            ArmorStand hologram = getArmorStand(b, true);
            assert hologram != null;
            hologram.setCustomName(ChatColors.color(name));
        }, 1);
    }

    public static void remove(Block b) {
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, () -> {
            ArmorStand hologram = getArmorStand(b, false);
            if (hologram != null) hologram.remove();
        }, 1);
    }

    private static ArmorStand getArmorStand(Block b, boolean createIfNoneExists) {
        Location l = new Location(b.getWorld(), b.getX() + 0.5, b.getY() + 0.7F, b.getZ() + 0.5);

        for (Entity n : l.getChunk().getEntities()) {
            if (n instanceof ArmorStand && n.getCustomName() != null && l.distanceSquared(n.getLocation()) < 0.4D) {
                return (ArmorStand) n;
            }
        }

        if (!createIfNoneExists) return null;
        else return create(l);
    }

    public static ArmorStand create(Location l) {
        ArmorStand armorStand = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setSilent(true);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setRemoveWhenFarAway(false);
        return armorStand;
    }
}
