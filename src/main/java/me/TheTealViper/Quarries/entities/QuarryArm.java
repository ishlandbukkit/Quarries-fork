package me.TheTealViper.Quarries.entities;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.annotations.Synchronized;
import me.TheTealViper.Quarries.entities.listeners.QuarryArm_Listeners;
import me.TheTealViper.Quarries.integration.protection.Protections;
import me.TheTealViper.Quarries.serializables.LocationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Deprecated(forRemoval = true)
@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed", "removal"})
public class QuarryArm implements Serializable {

    public static final ConcurrentMap<Location, QuarryArm> DATABASE = new ConcurrentHashMap<>();
    private static final long serialVersionUID = 1004245770995582732L;
    private final String world;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Location loc;
    public UUID uuid;
    public transient boolean isAlive = true;
    private LocationSerializable ls;

    @Synchronized
    public QuarryArm(@NotNull Location loc, UUID uuid, boolean generateNew) {
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
        Quarries.plugin.getServer().getPluginManager().registerEvents(new QuarryArm_Listeners(), Quarries.plugin);
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
        this.uuid = Quarries.createOutsideSpawner(loc.getBlock(), Quarries.TEXID_QUARRYARM);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Quarries.scheduler.runSync(this::breakQuarryArm);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ls = LocationSerializable.parseLocation(loc);
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loc = ls.toLocation();
        Quarries.scheduler.runSync(this::regen);
        isAlive = true;
    }

    @Synchronized
    public void breakQuarryArm() {
        Quarries.plugin.getServer().createWorld(new WorldCreator(world));
        if (!isAlive || !checkAlive()) return;
        if (uuid != null) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        }
        if (loc != null)
            DATABASE.remove(loc);

        loc = null;
        uuid = null;
        isAlive = false;
    }

    public void breakObj() {
        Quarries.scheduler.runSync(this::breakQuarryArm);
    }

    public boolean checkAlive() {
        try {
            ArmorStand armorStand = (ArmorStand) loc.getWorld().getEntity(uuid);
            assert armorStand != null;
            /*
            ItemStack helmet = Objects.requireNonNull(armorStand.getEquipment()).getHelmet();
            assert helmet != null;
            ItemMeta meta = helmet.getItemMeta();
            if (meta.getCustomModelData() != Quarries.TEXID_QUARRYARM) {
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