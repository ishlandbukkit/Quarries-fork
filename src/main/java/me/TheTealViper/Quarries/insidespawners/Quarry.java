package me.TheTealViper.Quarries.insidespawners;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import me.TheTealViper.Quarries.outsidespawners.Marker;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("deprecation")
public class Quarry implements Serializable {
    private static final long serialVersionUID = -983597691394166322L;
    public static final ConcurrentMap<Location, Quarry> DATABASE = new ConcurrentHashMap<>();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Location loc;
    public transient boolean isAlive = true;
    private LocationSerializable ls;
    private final String world;

    public Quarry(Location loc, BlockFace face, boolean generateNew) {
        this.loc = loc;
        this.world = loc.getWorld().getName();
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

    @SuppressWarnings("EmptyMethod")
    public static void onDisable() {
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakQuarry, 0);
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

    public void breakQuarry() {
        DATABASE.remove(loc);
        Quarries.plugin.getServer().createWorld(new WorldCreator(world));
        loc.getBlock().setType(Material.AIR);

        loc = null;
        isAlive = false;
    }

    public void breakObj() {
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakQuarry, 1);
    }

    public boolean checkAlive() {
        try {
            Quarries.plugin.getServer().createWorld(new WorldCreator(world));
            Block b = loc.getBlock();
            // Basic type detection
            if (!b.getType().equals(Material.SPAWNER)) {
                isAlive = false;
                return false;
            }
            /*
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
            if (Objects.equals(tag.getInt("CustomModelData"), Quarries.TEXID_QUARRY)) {
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
