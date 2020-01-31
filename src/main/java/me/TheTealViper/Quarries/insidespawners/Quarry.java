package me.TheTealViper.Quarries.insidespawners;

import me.TheTealViper.Quarries.PluginFile;
import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import me.TheTealViper.Quarries.outsidespawners.Marker;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quarry implements Serializable {
	private static final long serialVersionUID = -983597691394166322L;
	public static Map<Location, Quarry> DATABASE = new HashMap<>();
	public static PluginFile PLUGINFILE = new PluginFile(Quarries.plugin, "data/quarrys");
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Location loc;
	private LocationSerializable ls;

	public Quarry(Location loc, BlockFace face, boolean generateNew) {
		this.loc = loc;
		DATABASE.put(loc, this);

		if (generateNew) {
			Quarries.createInsideSpawner(loc.getBlock(), Integer.parseInt(Quarries.TEXID_QUARRY + Quarries.facingToAddedInt(face)));

			if (Marker.DATABASE.containsKey(loc.getBlock().getRelative(BlockFace.NORTH).getLocation()))
				QuarrySystem.initCreateQuarrySystem(loc.getBlock(), loc.getBlock().getRelative(BlockFace.NORTH), BlockFace.NORTH);
			else if (Marker.DATABASE.containsKey(loc.getBlock().getRelative(BlockFace.EAST).getLocation()))
				QuarrySystem.initCreateQuarrySystem(loc.getBlock(), loc.getBlock().getRelative(BlockFace.EAST), BlockFace.EAST);
			else if (Marker.DATABASE.containsKey(loc.getBlock().getRelative(BlockFace.SOUTH).getLocation()))
				QuarrySystem.initCreateQuarrySystem(loc.getBlock(), loc.getBlock().getRelative(BlockFace.SOUTH), BlockFace.SOUTH);
			else if (Marker.DATABASE.containsKey(loc.getBlock().getRelative(BlockFace.WEST).getLocation()))
				QuarrySystem.initCreateQuarrySystem(loc.getBlock(), loc.getBlock().getRelative(BlockFace.WEST), BlockFace.WEST);
		}
	}

	public static void onEnable() {
		Quarries.plugin.getServer().getPluginManager().registerEvents(new Quarry_Events(), Quarries.plugin);
	}

	public static void onDisable() {
		List<String> stringList = new ArrayList<>();
		for (Quarry i : DATABASE.values()) {
			stringList.add(Quarries.locToString(i.loc));
		}
		PLUGINFILE.set("locs", stringList);
		PLUGINFILE.save();
	}

	public static Quarry getQuarry(Location loc) {
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

	public void breakQuarry() {
		DATABASE.remove(loc);
		loc.getBlock().setType(Material.AIR);

		loc = null;
	}
}
