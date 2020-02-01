package me.TheTealViper.Quarries.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("CanBeFinal")
public class LocationSerializable implements Serializable {
    private static final long serialVersionUID = 9196768773185758132L;

    String world;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    public LocationSerializable(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static LocationSerializable parseLocation(Location location) {
        return new LocationSerializable(location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location toLocation() {
        return new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }
}
