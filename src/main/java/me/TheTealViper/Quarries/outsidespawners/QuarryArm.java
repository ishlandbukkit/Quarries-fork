package me.TheTealViper.Quarries.outsidespawners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuarryArm implements Serializable {

    private static final long serialVersionUID = 1004245770995582732L;
    public static Map<Location, QuarryArm> DATABASE = new HashMap<>();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Location loc;
    public UUID uuid;
    private LocationSerializable ls;

    public QuarryArm(Location loc, UUID uuid, boolean generateNew) {
        this.loc = loc;
        this.uuid = uuid;
        DATABASE.put(loc, this);

        if (generateNew)
            this.uuid = Quarries.createOutsideSpawner(loc.getBlock(), Material.AIR, Quarries.TEXID_QUARRYARM);
    }

    public static void onEnable() {
        Quarries.plugin.getServer().getPluginManager().registerEvents(new QuarryArm_Events(), Quarries.plugin);
    }

    public static void onDisable() {
    }

    public static QuarryArm getQuarryArm(Location loc) {
        return DATABASE.get(loc);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ls = LocationSerializable.parseLocation(loc);
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loc = ls.toLocation();
    }

    public void breakQuarryArm() {
        Entity e = Bukkit.getEntity(uuid);
        if (e != null) e.remove();
        DATABASE.remove(loc);

        loc = null;
        uuid = null;
    }

}