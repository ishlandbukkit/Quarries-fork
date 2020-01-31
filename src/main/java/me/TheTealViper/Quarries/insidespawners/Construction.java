package me.TheTealViper.Quarries.insidespawners;

import me.TheTealViper.Quarries.PluginFile;
import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Construction implements Serializable {

    private static final long serialVersionUID = -8433317554699901583L;
    public static Map<Location, Construction> DATABASE = new HashMap<>();
    public static PluginFile PLUGINFILE = new PluginFile(Quarries.plugin, "data/constructions");
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Location loc;
    private LocationSerializable ls;

    public Construction(Location loc, boolean generateNew) {
        this.loc = loc;
        DATABASE.put(loc, this);

        if (generateNew)
            Quarries.createInsideSpawner(loc.getBlock(), Quarries.TEXID_CONSTRUCTION);
    }

    public static void onEnable() {

        Quarries.plugin.getServer().getPluginManager().registerEvents(new Construction_Events(), Quarries.plugin);
    }

    public static void onDisable() {
        List<String> stringList = new ArrayList<>();
        for (Construction i : DATABASE.values()) {
            stringList.add(Quarries.locToString(i.loc));
        }
        PLUGINFILE.set("locs", stringList);
        PLUGINFILE.save();
    }

    public static Construction getConstruction(Location loc) {
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

    public void breakConstruction() {
        DATABASE.remove(loc);
        loc.getBlock().setType(Material.AIR);

        loc = null;
    }
}
