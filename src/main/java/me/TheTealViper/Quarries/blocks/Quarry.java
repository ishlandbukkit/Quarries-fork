package me.TheTealViper.Quarries.blocks;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.annotations.Synchronized;
import me.TheTealViper.Quarries.blocks.listeners.QuarryListeners;
import me.TheTealViper.Quarries.protection.Protections;
import me.TheTealViper.Quarries.serializables.LocationSerializable;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("deprecation")
public class Quarry implements Serializable {
    public static final ConcurrentMap<Location, Quarry> DATABASE = new ConcurrentHashMap<>();
    private static final long serialVersionUID = -983597691394166322L;
    private final String world;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Location loc;
    public transient boolean isAlive = true;
    public BlockFace facing;
    private LocationSerializable ls;

    @Synchronized
    public Quarry(@NotNull Location loc, BlockFace face, boolean generateNew) {
        this.loc = loc;
        this.world = loc.getWorld().getName();
        this.facing = face;
        if (!Protections.canPlace(loc, null)) {
            this.isAlive = false;
            return;
        }
        DATABASE.put(loc, this);

        if (generateNew) {
            regen();

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

    @Synchronized
    public static void onEnable() {
        Quarries.plugin.getServer().getPluginManager().registerEvents(new QuarryListeners(), Quarries.plugin);
    }

    @Synchronized
    @SuppressWarnings("EmptyMethod")
    public static void onDisable() {
    }

    public void regen() {
        Quarries.createInsideSpawner(loc.getBlock(),
                Quarries.TEXID_QUARRY + // base id
                        Quarries.facingToAddedInt(facing) // facing
        );
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakQuarry, 0);
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
    public void breakQuarry() {
        if (loc != null) {
            DATABASE.remove(loc);
        }
        if (!isAlive || !checkAlive()) return;
        try {
            Quarries.plugin.getServer().createWorld(new WorldCreator(world));
            loc.getBlock().setType(Material.AIR);
        } catch (IllegalArgumentException e) {
            breakObj(); // Try to hunt with "world unloaded"
        }

        loc = null;
        isAlive = false;
    }

    public void breakObj() {
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::breakQuarry, 1);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
