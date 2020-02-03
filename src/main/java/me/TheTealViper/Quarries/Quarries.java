package me.TheTealViper.Quarries;

import me.TheTealViper.Quarries.blocks.Construction;
import me.TheTealViper.Quarries.blocks.Marker;
import me.TheTealViper.Quarries.blocks.Quarry;
import me.TheTealViper.Quarries.entities.QuarryArm;
import me.TheTealViper.Quarries.nms.ServerVersions;
import me.TheTealViper.Quarries.nms.v1_15_R1.CustomSpawner1_15;
import me.TheTealViper.Quarries.protection.Protections;
import me.TheTealViper.Quarries.recipes.MarkerRecipe;
import me.TheTealViper.Quarries.recipes.QuarryRecipe;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Quarries extends JavaPlugin implements Listener {
    //Item ID's
    //1 is north version, 2 is east version, 3 is south version, 4 is west version
    public static final int TEXID_MARKER = 600020;
    public static final int TEXID_QUARRY = 600030;
    public static final int TEXID_CONSTRUCTION = 600010;
    public static final int TEXID_QUARRYARM = 600040;
    //general
    public static Quarries plugin;
    public static ServerVersions version;
    //markers
    public static int Marker_Check_Range;
    //thread pool
    public static ExecutorService pool = null;

    public static BlockFace getFacing(Player p) {
        float yaw = p.getLocation().getYaw();
        if (yaw >= -360 && yaw < -315) {
            return BlockFace.SOUTH;
        } else if (yaw >= -315 && yaw < -225) {
            return BlockFace.WEST;
        } else if (yaw >= -225 && yaw < -135) {
            return BlockFace.NORTH;
        } else if (yaw >= -135 && yaw < -45) {
            return BlockFace.EAST;
        } else if (yaw >= -45 && yaw < 45) {
            return BlockFace.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return BlockFace.NORTH;
        } else if (yaw >= 225 && yaw < 315) {
            return BlockFace.EAST;
        } else if (yaw >= 315 && yaw <= 360) {
            return BlockFace.SOUTH;
        } else {
            return null;
        }
    }

//Utilities ----------------------------------------------------------------------------------------------------------------------------------

    public static int facingToAddedInt(BlockFace face) {
        if (face.equals(BlockFace.NORTH))
            return 1;
        else if (face.equals(BlockFace.EAST))
            return 2;
        else if (face.equals(BlockFace.SOUTH))
            return 3;
        else if (face.equals(BlockFace.WEST))
            return 4;
        return 0;
    }

    public static Location[] getMinMax(Location l1, Location l2) {
        int minX = (int) (Math.min(l1.getX(), l2.getX()));
        int maxX = (int) (Math.max(l1.getX(), l2.getX()));
        int minY = (int) (Math.min(l1.getY(), l2.getY()));
        int maxY = (int) (Math.max(l1.getY(), l2.getY()));
        int minZ = (int) (Math.min(l1.getZ(), l2.getZ()));
        int maxZ = (int) (Math.max(l1.getZ(), l2.getZ()));
        return new Location[]{new Location(l1.getWorld(), minX, minY, minZ), new Location(l1.getWorld(), maxX, maxY, maxZ), new Location(l1.getWorld(), maxX - minX, maxY - minY, maxZ - minZ)};
    }

    public static void createInsideSpawner(Block b, int textureId) {
        if (version == ServerVersions.v1_15_R1)
            CustomSpawner1_15.createBlock(b, textureId);
    }

    public static UUID createOutsideSpawner(Block b, int textureId) {
        if (version == ServerVersions.v1_15_R1)
            return CustomSpawner1_15.createEntity(b, textureId);
        else
            return null;
    }

    public void onEnable() {
        getLogger().info("Early initialization");
        plugin = this;
        saveDefaultConfig();
        //Load values from config
        Marker_Check_Range = getConfig().getInt("Marker_Check_Range");
        Bukkit.getPluginManager().registerEvents(plugin, plugin);

        //Handle version
        String a = getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        if (version.equalsIgnoreCase("v1_15_R1")) {
            Quarries.version = ServerVersions.v1_15_R1;
        }
        getLogger().info("Detected server version: " + version);

        getLogger().info("Creating thread pool with size " + getConfig().getInt("Async_Threads", 8));
        //Init pool
        pool = Executors.newFixedThreadPool(getConfig().getInt("Async_Threads", 8));

        getLogger().info("Reloading data from disk...");
        //Load data
        try {
            DataManager.reload();
        } catch (Exception e) {
            throw new RuntimeException("Unable to init data storage", e);
        }
        DataManager.printStats();
        getLogger().info("Reloaded data.");

        getLogger().info("Enabling modules...");
        DataManager.registerTask();
        //Enable block types
        Marker.onEnable();
        Quarry.onEnable();
        Construction.onEnable();
        QuarrySystem.onEnable();
        QuarryArm.onEnable();
        Protections.reload();

        getLogger().info("Registering recipe...");
        //Register Recipes
        try {
            MarkerRecipe.register();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Unable to register recipe", e);
        }
        try {
            QuarryRecipe.register();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Unable to register recipe", e);
        }
        getLogger().info("Enabled");
    }

    public void onDisable() {
        getLogger().info("Disabling modules...");
        //Disable block types
        Marker.onDisable();
        Quarry.onDisable();
        Construction.onDisable();
        QuarrySystem.onDisable();
        QuarryArm.onDisable();

        getLogger().info("Saving data...");
        //Save data
        try {
            DataManager.save();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to save data", e);
        }
        DataManager.printStats();

        getLogger().info("Shutting down pool...");
        //Shutdown pool
        pool.shutdown();

        getLogger().info("Disabled");
    }

	/*
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
			if (e.getMessage().startsWith("give") && e.getPlayer().hasPermission("quarries.give")) {
				ItemStack item = CustomItems1_15.getItem(Integer.parseInt(e.getMessage().replace("give ", "")));
				e.getPlayer().getInventory().addItem(item);
			}
//			else if(e.getMessage().equals("test")) {
//				Block b = e.getPlayer().getTargetBlock(null, 100).getRelative(BlockFace.UP);
//				TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) b.getLocation().getWorld()).getHandle().getTileEntity(new BlockPosition(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ()));
//				NBTTagCompound spawnerTag = spawner.b();
//				Bukkit.broadcastMessage(spawnerTag.toString());
//			}
		}, 0);
	}
*/
}
