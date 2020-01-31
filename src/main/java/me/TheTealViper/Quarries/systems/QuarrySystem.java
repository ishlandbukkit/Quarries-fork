package me.TheTealViper.Quarries.systems;

import me.TheTealViper.Quarries.CustomItems1_15;
import me.TheTealViper.Quarries.PluginFile;
import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.insidespawners.Construction;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import me.TheTealViper.Quarries.outsidespawners.Marker;
import me.TheTealViper.Quarries.outsidespawners.QuarryArm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;

public class QuarrySystem implements Serializable {
    private static final long serialVersionUID = -3701643577709552290L;
    public static Map<Location, QuarrySystem> DATABASE = new HashMap<>();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Block quarryBlock;
    public transient Location max, min;
    public boolean powered;
    public QuarrySystemType type;
    public Vector miningArmShift;
    public boolean hitBedrock;
    public int mineDelay;
    @SuppressWarnings("InstanceVariableMayNotBeInitializedByReadObject")
    public transient boolean isActive = false;
    private boolean hitAir;
    private LocationSerializable maxs, mins;

    public QuarrySystem(Block quarryBlock, Location max, Location min, boolean powered, QuarrySystemType type, Vector miningArmShift, boolean hitBedrock, int mineDelay) {
        DATABASE.put(quarryBlock.getLocation(), this);
        this.quarryBlock = quarryBlock;
        this.max = max;
        this.min = min;
        this.powered = powered;
        this.type = type;
        this.miningArmShift = miningArmShift;
        this.hitBedrock = hitBedrock;
        this.mineDelay = mineDelay;

    }

    public static void onEnable() {
        Quarries.plugin.getServer().getPluginManager().registerEvents(new QuarrySystem_Events(), Quarries.plugin);
    }

    public static void onDisable() {
        for (QuarrySystem qs : DATABASE.values()) {
            PluginFile pf = new PluginFile(Quarries.plugin, "data/systems/quarrysystem/" + Quarries.locToString(qs.quarryBlock.getLocation()));
            pf.set("max", Quarries.locToString(qs.max));
            pf.set("min", Quarries.locToString(qs.min));
            pf.set("powered", qs.powered);
            pf.set("type", qs.type.toString());
            pf.set("miningArmShift", Quarries.locToString(qs.miningArmShift.toLocation(qs.quarryBlock.getWorld())));
            pf.set("hitBedrock", qs.hitBedrock);
            pf.set("mineDelay", qs.mineDelay);
            pf.save();
        }
    }

    public static QuarrySystem getQuarrySystem(Location loc) {
        return DATABASE.get(loc);
    }

    public static QuarrySystem createQuarrySystem(Block quarryBlock, Location max, Location min, QuarrySystemType type) {
        return new QuarrySystem(quarryBlock, max, min, true, type, new Vector(0, 1, 0), false, 4);
    }

    public static QuarrySystem load(Block quarryBlock) {
        File file = new File(Quarries.plugin.getDataFolder(), "/data/systems/quarrysystem/" + Quarries.locToString(quarryBlock.getLocation()));
        if (!file.exists())
            return null;

        PluginFile pf = new PluginFile(Quarries.plugin, "data/systems/quarrysystem/" + Quarries.locToString(quarryBlock.getLocation()));
        Location max = Quarries.parseLoc(Objects.requireNonNull(pf.getString("max")));
        Location min = Quarries.parseLoc(Objects.requireNonNull(pf.getString("min")));
        boolean powered = pf.getBoolean("powered");
        QuarrySystemType type = QuarrySystemType.valueOf(pf.getString("type"));
        Location loc = Quarries.parseLoc(Objects.requireNonNull(pf.getString("miningArmShift")));
        boolean hitBedrock = pf.getBoolean("hitBedrock");
        int mineDelay = pf.getInt("mineDelay");
        return new QuarrySystem(quarryBlock, max, min, powered, type, loc.toVector(), hitBedrock, mineDelay);
    }

    public static QuarrySystem load(String fileName) {
        Block quarryBlock = Quarries.parseLoc(fileName).getBlock();
        return load(quarryBlock);
    }

    public static void initCreateQuarrySystem(Block quarryBlock, Block startingMarker, BlockFace face) {
//		Bukkit.broadcastMessage("checkrange:" + ViperFusion.Marker_Check_Range);
        List<Location> foundMarkers = new ArrayList<>();
        if (!face.equals(BlockFace.NORTH)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempblock = startingMarker.getRelative(BlockFace.SOUTH, i);
                if (Marker.DATABASE.containsKey(tempblock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempblock.getLocation());
                    break;
                }
            }
        }
        if (!face.equals(BlockFace.EAST)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempblock = startingMarker.getRelative(BlockFace.WEST, i);
                if (Marker.DATABASE.containsKey(tempblock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempblock.getLocation());
                    break;
                }
            }
        }
        if (!face.equals(BlockFace.SOUTH)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempblock = startingMarker.getRelative(BlockFace.NORTH, i);
                if (Marker.DATABASE.containsKey(tempblock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempblock.getLocation());
                    break;
                }
            }
        }
        if (!face.equals(BlockFace.WEST)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempblock = startingMarker.getRelative(BlockFace.EAST, i);
                if (Marker.DATABASE.containsKey(tempblock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempblock.getLocation());
                    break;
                }
            }
        }

        if (foundMarkers.size() == 2) {
            //Handle markers
            Marker.getMarker(startingMarker.getLocation()).breakMarker();
            for (Location loc : foundMarkers)
                Marker.getMarker(loc).breakMarker();

            //Handle construction blocks
            Block constructionBlock;
            Location[] temp = Quarries.getMinMax(foundMarkers.get(0), foundMarkers.get(1).clone().add(0, 3, 0));
            Location min = temp[0];
            Location max = temp[1];
            Vector delta = temp[2].toVector();
            Location[] startingXPoints = new Location[]{min.clone(), min.clone().add(0, delta.getY(), 0), min.clone().add(0, 0, delta.getZ()), min.clone().add(0, delta.getY(), delta.getZ())};
//			Bukkit.broadcastMessage("made it 1");
            for (Location startingXPoint : startingXPoints) {
                for (int x = 0; x < delta.getBlockX(); x++) {
                    constructionBlock = startingXPoint.clone().add(x, 0, 0).getBlock();
                    new Construction(constructionBlock.getLocation(), true);
                }
            }
//			Bukkit.broadcastMessage("made it 2");
            Location[] startingYPoints = new Location[]{min.clone(), min.clone().add(delta.getX(), 0, 0), min.clone().add(0, 0, delta.getZ()), min.clone().add(delta.getX(), 0, delta.getZ())};
            for (Location startingYPoint : startingYPoints) {
                for (int y = 0; y < delta.getBlockY(); y++) {
                    constructionBlock = startingYPoint.clone().add(0, y, 0).getBlock();
                    new Construction(constructionBlock.getLocation(), true);
                }
            }
//			Bukkit.broadcastMessage("made it 3");
            Location[] startingZPoints = new Location[]{min.clone(), min.clone().add(delta.getX(), 0, 0), min.clone().add(0, delta.getY(), 0), min.clone().add(delta.getX(), delta.getY(), 0)};
            for (Location startingZPoint : startingZPoints) {
                for (int z = 0; z < delta.getBlockZ(); z++) {
                    constructionBlock = startingZPoint.clone().add(0, 0, z).getBlock();
                    new Construction(constructionBlock.getLocation(), true);
                }
            }
//			Bukkit.broadcastMessage("made it 4");
            for (int x = 1; x < delta.getBlockX(); x++) {
                for (int z = 1; z <= delta.getBlockZ(); z++) {
                    constructionBlock = min.clone().add(x, delta.getBlockY(), z).getBlock();
                    new Construction(constructionBlock.getLocation(), true);
                }
            }
//			Bukkit.broadcastMessage("made it 5");
            constructionBlock = min.clone().add(delta).getBlock();
            new Construction(constructionBlock.getLocation(), true);

            //Handle creating QuarrySystem
            QuarrySystem QS = QuarrySystem.createQuarrySystem(quarryBlock, max, min, QuarrySystemType.Default);
            QuarrySystem.DATABASE.put(QS.quarryBlock.getLocation(), QS);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        maxs = LocationSerializable.parseLocation(max);
        mins = LocationSerializable.parseLocation(min);
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        max = maxs.toLocation();
        min = mins.toLocation();
        init();
    }

    public void init() {
        final QuarrySystem QS = this;
        final List<Integer> scheduleInt = new ArrayList<>();
        scheduleInt.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(Quarries.plugin, () -> {
            if (QS.powered && !QS.hitBedrock && DATABASE.containsKey(quarryBlock.getLocation()))
                mine();
            else
                Bukkit.getScheduler().cancelTask(scheduleInt.get(0));
        }, 0, QS.mineDelay));
        isActive = true;
    }

    public void destroy() throws IOException {
        //Remove file
        File file = new File(Quarries.plugin.getDataFolder(), "/data/systems/quarrysystem/" + Quarries.locToString(quarryBlock.getLocation()));
        if (!file.delete()) {
            throw new IOException("Unable to delete " + file.getPath());
        }

        //Break construction blocks
        Vector delta = max.clone().subtract(min.clone()).toVector();
        Block constructionBlock;
        Location[] startingXPoints = new Location[]{min.clone(), min.clone().add(0, delta.getY(), 0), min.clone().add(0, 0, delta.getZ()), min.clone().add(0, delta.getY(), delta.getZ())};
        for (Location startingXPoint : startingXPoints) {
            for (int x = 0; x < delta.getBlockX(); x++) {
                constructionBlock = startingXPoint.clone().add(x, 0, 0).getBlock();
                Construction con = Construction.getConstruction(constructionBlock.getLocation());
                if (con != null)
                    con.breakConstruction();
            }
        }
        Location[] startingYPoints = new Location[]{min.clone(), min.clone().add(delta.getX(), 0, 0), min.clone().add(0, 0, delta.getZ()), min.clone().add(delta.getX(), 0, delta.getZ())};
        for (Location startingYPoint : startingYPoints) {
            for (int y = 0; y < delta.getBlockY(); y++) {
                constructionBlock = startingYPoint.clone().add(0, y, 0).getBlock();
                Construction con = Construction.getConstruction(constructionBlock.getLocation());
                if (con != null)
                    con.breakConstruction();
            }
        }
        Location[] startingZPoints = new Location[]{min.clone(), min.clone().add(delta.getX(), 0, 0), min.clone().add(0, delta.getY(), 0), min.clone().add(delta.getX(), delta.getY(), 0)};
        for (Location startingZPoint : startingZPoints) {
            for (int z = 0; z < delta.getBlockZ(); z++) {
                constructionBlock = startingZPoint.clone().add(0, 0, z).getBlock();
                Construction con = Construction.getConstruction(constructionBlock.getLocation());
                if (con != null)
                    con.breakConstruction();
            }
        }
        for (int x = 1; x < delta.getBlockX(); x++) {
            for (int z = 1; z <= delta.getBlockZ(); z++) {
                constructionBlock = min.clone().add(x, delta.getBlockY(), z).getBlock();
                Construction con = Construction.getConstruction(constructionBlock.getLocation());
                if (con != null)
                    con.breakConstruction();
            }
        }
        constructionBlock = max.getBlock();
        Construction con = Construction.getConstruction(constructionBlock.getLocation());
        if (con != null)
            con.breakConstruction();

        //Break quarry arm blocks
        breakQuarryArms();

        //Remove from code
        DATABASE.remove(quarryBlock.getLocation());

        // Linus drop tips
        quarryBlock.getLocation().getWorld().dropItem(quarryBlock.getLocation(), CustomItems1_15.getItem(576161));
    }

    public void mine() {
//		Bukkit.broadcastMessage("mining");

        //Handle mining arm placement
        Vector delta = max.clone().subtract(min.clone()).toVector();
        Block constructionBlock = min.clone().add(1 + miningArmShift.getBlockX(), delta.getBlockY() - miningArmShift.getBlockY(), 1 + miningArmShift.getBlockZ()).getBlock();
        Block oldCB = constructionBlock;
//		ViperFusion.createConstruction(constructionBlock);

        //Handle mining
        while (true) {
            Block minedBlock = constructionBlock.getRelative(BlockFace.DOWN);
            if (minedBlock.getType().equals(Material.BEDROCK)) {
                hitAir = false;
//			Bukkit.broadcastMessage("hit bedrock");
                hitBedrock = true;
                break;
            } else if (minedBlock.getType().equals(Material.AIR)) {
                hitAir = true;
                constructionBlock = updateArm(delta);
            } else {
                hitAir = false;
                Collection<ItemStack> drops = new ArrayList<>(minedBlock.getDrops());
                if (drops.size() == 0 && minedBlock.getType().equals(Material.STONE))
                    drops.add(new ItemStack(Material.COBBLESTONE, 1));
                minedBlock.setType(Material.AIR);
                for (ItemStack item : drops) {
                    handleMinedItem(item);
                }

                constructionBlock = updateArm(delta);
                break;
            }
        }

        // Update visual effects
        for (int y = max.getBlockY() - 1; y >= constructionBlock.getLocation().getBlockY(); y--) {
            Location newPos = constructionBlock.getLocation().clone();
            Location oldPos = oldCB.getLocation().clone();
            // Optimization - teleport oldPos ones instead breaking them and create one
            newPos.setY(y);
            oldPos.setY(y);
            QuarryArm arm = QuarryArm.DATABASE.get(oldPos);
            if (arm != null) {
                Entity blockEntity = Objects.requireNonNull(Bukkit.getEntity(arm.uuid));
                blockEntity.teleport(newPos);
                QuarryArm.DATABASE.put(newPos, QuarryArm.DATABASE.get(oldPos));
                QuarryArm.DATABASE.remove(oldPos);
            } else new QuarryArm(newPos, null, true);
        }
    }

    private Block updateArm(Vector delta) {
        //Handle updating mining arm
        miningArmShift.add(new Vector(0, 0, 1));
        if (miningArmShift.getBlockZ() > delta.getBlockZ() - 2) {
            miningArmShift.setZ(0);
            miningArmShift.add(new Vector(1, 0, 0));
            if (miningArmShift.getBlockX() > delta.getBlockX() - 2) {
                miningArmShift.setX(0);
                miningArmShift.add(new Vector(0, 1, 0));
            }
        }
        return min.clone().add(1 + miningArmShift.getBlockX(), delta.getBlockY() - miningArmShift.getBlockY(), 1 + miningArmShift.getBlockZ()).getBlock();
    }

    public void handleMinedItem(ItemStack item) {
        boolean handledAlready = false;
        Block testBlock;

        testBlock = quarryBlock.getRelative(BlockFace.NORTH);
        if (testBlock.getState() instanceof Container) {
            Container c = (Container) testBlock.getState();
            if (c.getInventory().firstEmpty() != -1) {
                handledAlready = true;
                c.getInventory().addItem(item);
            }
        }
        testBlock = quarryBlock.getRelative(BlockFace.EAST);
        if (!handledAlready && testBlock.getState() instanceof Container) {
            Container c = (Container) testBlock.getState();
            if (c.getInventory().firstEmpty() != -1) {
                handledAlready = true;
                c.getInventory().addItem(item);
            }
        }
        testBlock = quarryBlock.getRelative(BlockFace.SOUTH);
        if (!handledAlready && testBlock.getState() instanceof Container) {
            Container c = (Container) testBlock.getState();
            if (c.getInventory().firstEmpty() != -1) {
                handledAlready = true;
                c.getInventory().addItem(item);
            }
        }
        testBlock = quarryBlock.getRelative(BlockFace.WEST);
        if (!handledAlready && testBlock.getState() instanceof Container) {
            Container c = (Container) testBlock.getState();
            if (c.getInventory().firstEmpty() != -1) {
                handledAlready = true;
                c.getInventory().addItem(item);
            }
        }
        testBlock = quarryBlock.getRelative(BlockFace.UP);
        if (!handledAlready && testBlock.getState() instanceof Container) {
            Container c = (Container) testBlock.getState();
            if (c.getInventory().firstEmpty() != -1) {
                handledAlready = true;
                c.getInventory().addItem(item);
            }
        }
        testBlock = quarryBlock.getRelative(BlockFace.DOWN);
        if (!handledAlready && testBlock.getState() instanceof Container) {
            Container c = (Container) testBlock.getState();
            if (c.getInventory().firstEmpty() != -1) {
                handledAlready = true;
                c.getInventory().addItem(item);
            }
        }

        if (!handledAlready) {
            quarryBlock.getWorld().dropItem(quarryBlock.getLocation().clone().add(.5, 1.2, .5), item);
        }
    }

    public void breakQuarryArms() {
        boolean foundArm;
        for (int y = max.getBlockY() - 1; y >= 0; y--) {
            foundArm = false;
            for (int x = min.getBlockX() + 1; x <= max.getBlockX() - 1; x++) {
                for (int z = min.getBlockZ() + 1; z <= max.getBlockZ() - 1; z++) {
                    Location loc = new Location(min.getWorld(), x, y, z);
                    List<QuarryArm> dummy = new ArrayList<>(QuarryArm.DATABASE.values());
                    for (QuarryArm arm : dummy) {
                        if (arm.loc.equals(loc)) {
                            arm.breakQuarryArm();
                            foundArm = true;
                        }
                    }
                }
            }
            if (!foundArm)
                break;
//			else
//				Bukkit.broadcastMessage("found arm @ y=" + y);
        }
    }

}
