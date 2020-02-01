package me.TheTealViper.Quarries.insidespawners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("deprecation")
public class Construction implements Serializable {

    private static final long serialVersionUID = -8433317554699901583L;
    public static ConcurrentMap<Location, Construction> DATABASE = new ConcurrentHashMap<>();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Location loc;
    public transient boolean isAlive = true;
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

    @SuppressWarnings("EmptyMethod")
    public static void onDisable() {

    }

    public static Construction getConstruction(Location loc) {
        return DATABASE.get(loc);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakConstruction, 1);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ls = LocationSerializable.parseLocation(loc);
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loc = ls.toLocation();
        isAlive = true;
    }

    public void breakConstruction() {
        DATABASE.remove(loc);
        boolean isLoaded = loc.getChunk().isLoaded();
        if (!isLoaded && !loc.getChunk().load()) {
            Quarries.plugin.getLogger().warning(
                    "Unable to load chunk " + loc.getChunk().getX() + "," + loc.getChunk().getZ() + ", task delayed.");
            Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakConstruction, 1);
            return;
        }
        loc.getBlock().setType(Material.AIR);

        loc = null;
    }

    public boolean checkAlive() {
        try {
            Block b = loc.getBlock();
            // Basic type detection
            if (!b.getType().equals(Material.SPAWNER)) {
                isAlive = false;
                return false;
            }
            // Get nms spawner
            TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) b.getLocation().getWorld()).getHandle()
                    .getTileEntity(new BlockPosition(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ()));
            // Unable to get nms spawner
            if (spawner == null) {
                isAlive = false;
                return false;
            }
            // Get entity spawned by spawner
            NBTTagCompound spawnerTag = spawner.b();
            // Get armor data of armor stand
            NBTTagList armorList = (NBTTagList) spawnerTag.get("ArmorItems");
            // Unable to get armor data
            if (armorList == null) {
                isAlive = false;
                return false;
            }
            // Get helmet data
            NBTTagCompound item = (NBTTagCompound) armorList.get(0);
            // Check type and count of helmet
            if (!Objects.equals(item.getString("id"), "minecraft:stone") ||
                    !Objects.equals(item.getShort("Count"), 1)) {
                isAlive = false;
                return false;
            }
            // Get detailed data of helmet
            NBTTagCompound tag = (NBTTagCompound) item.get("tag");
            if (tag == null) {
                isAlive = false;
                return false;
            }
            if (Objects.equals(tag.getInt("CustomModelData"), Quarries.TEXID_CONSTRUCTION)) {
                isAlive = false;
                return false;
            }
        } catch (Exception e) {
            // If any line of code throw an exception, assume false
            isAlive = false;
            return false;
        }
        // Passed all tests and it is truly valid, return true
        return true;
    }
}
