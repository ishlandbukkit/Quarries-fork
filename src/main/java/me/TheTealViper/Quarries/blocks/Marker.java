package me.TheTealViper.Quarries.blocks;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.annotations.Synchronized;
import me.TheTealViper.Quarries.blocks.listeners.MarkerListeners;
import me.TheTealViper.Quarries.integration.protection.Protections;
import me.TheTealViper.Quarries.serializables.LocationSerializable;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("deprecation")
public class Marker implements Listener, Serializable {
    public static final ConcurrentMap<Location, Marker> DATABASE = new ConcurrentHashMap<>();
    private static final long serialVersionUID = 3511844179276547506L;
    private final String world;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Location loc;
    public UUID uuid;
    public transient boolean isAlive = true;
    private LocationSerializable ls;

    @Synchronized
    public Marker(@NotNull Location loc, UUID uuid, boolean generateNew) {
        this.loc = loc;
        this.uuid = uuid;
        this.world = loc.getWorld().getName();
        if (!Protections.canPlace(loc, null)) {
            this.isAlive = false;
            return;
        }
        DATABASE.put(loc, this);

        if (generateNew)
            regen();
    }

    @Synchronized
    public static void onEnable() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Quarries.plugin, () -> {
            for (Marker marker : DATABASE.values()) {
                marker.loc.getWorld().spawnParticle(Particle.REDSTONE, marker.loc.clone().add(.5, .75, .5), 0, new DustOptions(Color.AQUA, 1));
            }
        }, 0, 5);

        Quarries.plugin.getServer().getPluginManager().registerEvents(new MarkerListeners(), Quarries.plugin);
    }

    @Synchronized
    @SuppressWarnings("EmptyMethod")
    public static void onDisable() {
    }

    @Synchronized
    public void regen() {
        if (this.uuid != null && loc != null && loc.getWorld() != null && loc.getWorld().getEntity(this.uuid) != null)
            Objects.requireNonNull(loc.getWorld().getEntity(this.uuid)).remove();
        assert loc != null;
        this.uuid = Quarries.createOutsideSpawner(loc.getBlock(), Quarries.TEXID_MARKER);
        loc.getBlock().setType(Material.REDSTONE_TORCH);
    }

    public static Marker getMarker(Location loc) {
        return DATABASE.get(loc);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakMarker, 0);
    }

    private void writeObject(@NotNull ObjectOutputStream out) throws IOException {
        ls = LocationSerializable.parseLocation(loc);
        out.defaultWriteObject();
    }

    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loc = ls.toLocation();
        Bukkit.getScheduler().runTaskLater(Quarries.plugin, this::regen, 1);
        isAlive = true;
    }

    @Synchronized
    public void breakMarker() {
        if (loc != null) {
            DATABASE.remove(loc);
        }
        if (!isAlive || !checkAlive()) return;
        try {
            Quarries.plugin.getServer().createWorld(new WorldCreator(world));
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
            loc.getBlock().setType(Material.AIR);
        } catch (IllegalArgumentException e) {
            breakObj(); // Try to hunt with "world unloaded"
        }

        loc = null;
        uuid = null;
        isAlive = false;
    }

    public void breakObj() {
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakMarker, 1);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkAlive() {
        try {
            Quarries.plugin.getServer().createWorld(new WorldCreator(world));
            ArmorStand armorStand = (ArmorStand) loc.getWorld().getEntity(uuid);
            assert armorStand != null;
            /*
            assert armorStand != null;
            ItemStack helmet = Objects.requireNonNull(armorStand.getEquipment()).getHelmet();
            assert helmet != null;
            ItemMeta meta = helmet.getItemMeta();
            if (meta.getCustomModelData() != Quarries.TEXID_MARKER) {
                isAlive = false;
                return false;
            }

             */
        } catch (Exception e) {
            // If any line of code throw an exception, assume false
            isAlive = false;
            return false;
        }
        // Passed all tests and it is truly valid, return true
        return true;
    }

}
