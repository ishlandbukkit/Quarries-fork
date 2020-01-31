package me.TheTealViper.Quarries.outsidespawners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Marker implements Listener, Serializable {
	private static final long serialVersionUID = 3511844179276547506L;
	public static Map<Location, Marker> DATABASE = new HashMap<>();
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Location loc;
	public UUID uuid;
	private LocationSerializable ls;

	public Marker(Location loc, UUID uuid, boolean generateNew) {
		this.loc = loc;
		this.uuid = uuid;
		DATABASE.put(loc, this);

		if (generateNew)
			this.uuid = Quarries.createOutsideSpawner(loc.getBlock(), Material.REDSTONE_TORCH, Quarries.TEXID_MARKER);
	}

	public static void onEnable() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Quarries.plugin, () -> {
			for (Marker marker : DATABASE.values()) {
				marker.loc.getWorld().spawnParticle(Particle.REDSTONE, marker.loc.clone().add(.5, .75, .5), 0, new DustOptions(Color.AQUA, 1));
			}
		}, 0, 5);

		Quarries.plugin.getServer().getPluginManager().registerEvents(new Marker_Events(), Quarries.plugin);
	}

	public static void onDisable() {
	}

	public static Marker getMarker(Location loc) {
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

	public void breakMarker() {
		Entity e = Bukkit.getEntity(uuid);
		if (e != null) e.remove();
		DATABASE.remove(loc);
		loc.getBlock().setType(Material.AIR);

		loc = null;
		uuid = null;
	}

}
