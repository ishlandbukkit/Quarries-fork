package me.TheTealViper.Quarries;

import me.TheTealViper.Quarries.insidespawners.Construction;
import me.TheTealViper.Quarries.insidespawners.Quarry;
import me.TheTealViper.Quarries.outsidespawners.Marker;
import me.TheTealViper.Quarries.outsidespawners.QuarryArm;
import me.TheTealViper.Quarries.recipes.MarkerRecipe;
import me.TheTealViper.Quarries.recipes.QuarryRecipe;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class Quarries extends JavaPlugin implements Listener {
    //Item ID's
    //1 is north version, 2 is east version, 3 is south version, 4 is west version
    public static final int TEXID_MARKER = 332447;
    public static final int TEXID_QUARRY = 576161;
    public static final int TEXID_CONSTRUCTION = 296370;
    public static final int TEXID_QUARRYARM = 750566;
    //general
    public static Quarries plugin;
    public static String LOG_PREFIX = "[ViperFusion] ";
    public static VersionType version;
    //markers
    public static int Marker_Check_Range;

    public static Location parseLoc(String s) {
        String[] args = s.split(",");
        return new Location(Bukkit.getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
    }

    public static String locToString(Location loc) {
        return loc.getWorld().getName() + "," + ((int) loc.getX()) + "," + ((int) loc.getY()) + "," + ((int) loc.getZ());
    }

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

    public static String facingToAddedInt(BlockFace face) {
        String addedint = "";
        if (face.equals(BlockFace.NORTH))
            addedint = "1";
        else if (face.equals(BlockFace.EAST))
            addedint = "2";
        else if (face.equals(BlockFace.SOUTH))
            addedint = "3";
        else if (face.equals(BlockFace.WEST))
            addedint = "4";
        return addedint;
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

    public static void createInsideSpawner(Block b, int textureid) {
        if (version == VersionType.v1_15_R1)
            CustomSpawner1_15.createInsideSpawner(b, textureid);
    }

    public static UUID createOutsideSpawner(Block b, Material replacementBlock, int textureid) {
        if (version == VersionType.v1_15_R1)
            return CustomSpawner1_15.createOutsideSpawner(b, textureid);
        else
            return null;
    }

    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(plugin, plugin);

        //Load data
        try {
            DataManager.reload();
        } catch (IOException e) {
            throw new RuntimeException("Unable to init data storage");
        }

        //Handle version
        String a = getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        if (version.equalsIgnoreCase("v1_15_R1")) {
            Quarries.version = VersionType.v1_15_R1;
        }

        //Load values from config
        Marker_Check_Range = getConfig().getInt("Marker_Check_Range");

        //Enable block types
        Marker.onEnable();
        Quarry.onEnable();
        Construction.onEnable();
        QuarrySystem.onEnable();
        QuarryArm.onEnable();

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
    }

    public void onDisable() {
        //Disable block types
        Marker.onDisable();
        Quarry.onDisable();
        Construction.onDisable();
        QuarrySystem.onDisable();
        QuarryArm.onDisable();
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
