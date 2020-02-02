package me.TheTealViper.Quarries.systems;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.blocks.Construction;
import me.TheTealViper.Quarries.blocks.Marker;
import me.TheTealViper.Quarries.entities.QuarryArm;
import me.TheTealViper.Quarries.nms.v1_15_R1.CustomItems1_15;
import me.TheTealViper.Quarries.serializables.LocationSerializable;
import me.TheTealViper.Quarries.serializables.VectorSerializable;
import me.TheTealViper.Quarries.systems.enums.QuarrySystemTypes;
import me.TheTealViper.Quarries.systems.listeners.QuarrySystemListeners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"CanBeFinal", "deprecation"})
public class QuarrySystem implements Serializable {
    private static final long serialVersionUID = -3701643577709552290L;
    public static ConcurrentMap<Location, QuarrySystem> DATABASE = new ConcurrentHashMap<>();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public transient Block quarryBlock;
    public LocationSerializable quarryBlockLoc;
    public transient Location max, min;
    public boolean powered;
    @SuppressWarnings("unused")
    public QuarrySystemTypes type;
    public transient Vector miningArmShift;
    public boolean hitBedrock;
    public int mineDelay;
    @SuppressWarnings({"InstanceVariableMayNotBeInitializedByReadObject", "unused"})
    public transient boolean isActive = false;
    public transient boolean isAlive = true;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean hitAir;
    private LocationSerializable maxS, minS;
    private VectorSerializable masS;
    private transient List<SoftReference<QuarryArm>> arms = new ArrayList<>();
    private String world;

    public QuarrySystem(Block quarryBlock, Location max, Location min, boolean powered, QuarrySystemTypes type, Vector miningArmShift, boolean hitBedrock, int mineDelay) {
        DATABASE.put(quarryBlock.getLocation(), this);
        this.quarryBlock = quarryBlock;
        this.max = max;
        this.min = min;
        this.powered = powered;
        this.type = type;
        this.miningArmShift = miningArmShift;
        this.hitBedrock = hitBedrock;
        this.mineDelay = mineDelay;
        this.world = max.getWorld().getName();
        init();
    }

    public static void onEnable() {
        Quarries.plugin.getServer().getPluginManager().registerEvents(new QuarrySystemListeners(), Quarries.plugin);
    }

    @SuppressWarnings("EmptyMethod")
    public static void onDisable() {
    }

    public static QuarrySystem createQuarrySystem(Block quarryBlock, Location max, Location min, QuarrySystemTypes type) {
        return new QuarrySystem(quarryBlock, max, min, true, type, new Vector(0, 1, 0), false, 4);
    }

    public static void initCreateQuarrySystem(Block quarryBlock, Block startingMarker, BlockFace face) {
//		Bukkit.broadcastMessage("checkRange:" + ViperFusion.Marker_Check_Range);
        Quarries.plugin.getServer().createWorld(new WorldCreator(quarryBlock.getWorld().getName()));
        List<Location> foundMarkers = new ArrayList<>();
        if (!face.equals(BlockFace.NORTH)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempBlock = startingMarker.getRelative(BlockFace.SOUTH, i);
                if (Marker.DATABASE.containsKey(tempBlock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempBlock.getLocation());
                    break;
                }
            }
        }
        if (!face.equals(BlockFace.EAST)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempBlock = startingMarker.getRelative(BlockFace.WEST, i);
                if (Marker.DATABASE.containsKey(tempBlock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempBlock.getLocation());
                    break;
                }
            }
        }
        if (!face.equals(BlockFace.SOUTH)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempBlock = startingMarker.getRelative(BlockFace.NORTH, i);
                if (Marker.DATABASE.containsKey(tempBlock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempBlock.getLocation());
                    break;
                }
            }
        }
        if (!face.equals(BlockFace.WEST)) {
            for (int i = 1; i <= Quarries.Marker_Check_Range; i++) {
                Block tempBlock = startingMarker.getRelative(BlockFace.EAST, i);
                if (Marker.DATABASE.containsKey(tempBlock.getLocation())) {
//					Bukkit.broadcastMessage("found!");
                    foundMarkers.add(tempBlock.getLocation());
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
            QuarrySystem QS = QuarrySystem.createQuarrySystem(quarryBlock, max, min, QuarrySystemTypes.Default);
            QuarrySystem.DATABASE.put(QS.quarryBlock.getLocation(), QS);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        maxS = LocationSerializable.parseLocation(max);
        minS = LocationSerializable.parseLocation(min);
        masS = VectorSerializable.parseVector(miningArmShift);
        quarryBlockLoc = LocationSerializable.parseLocation(quarryBlock.getLocation());
        out.defaultWriteObject();
        List<QuarryArm> list = new ArrayList<>();
        for (SoftReference<QuarryArm> ref : arms)
            if (ref.get() != null)
                list.add(ref.get());
        out.writeObject(list);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        List<QuarryArm> list = (List<QuarryArm>) in.readObject();
        arms = new ArrayList<>();
        for (QuarryArm arm : list)
            if (QuarryArm.DATABASE.get(arm.loc) != null)
                arms.add(new SoftReference<>(QuarryArm.DATABASE.get(arm.loc)));
        max = maxS.toLocation();
        min = minS.toLocation();
        miningArmShift = masS.toVector();
        quarryBlock = quarryBlockLoc.toLocation().getBlock();
        init();
        isAlive = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::destroy, 0);
    }

    public void init() {
        QuarrySystem QS = this;
        List<Integer> scheduleInt = new ArrayList<>();
        scheduleInt.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(Quarries.plugin, () -> {
            if (QS.powered && !QS.hitBedrock && DATABASE.containsKey(quarryBlock.getLocation()))
                mine();
            else {
                Bukkit.getScheduler().cancelTask(scheduleInt.get(0));
                isActive = false;
            }
        }, 0, QS.mineDelay));
        isActive = true;
    }

    public void destroy() {
        Quarries.plugin.getServer().createWorld(new WorldCreator(world));
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
        quarryBlock.getLocation().getWorld().dropItem(quarryBlock.getLocation(), CustomItems1_15.getItem(Quarries.TEXID_QUARRY));
    }

    public void mine() {
//		Bukkit.broadcastMessage("mining");
        Quarries.plugin.getServer().createWorld(new WorldCreator(world));
        //Check tool
        ItemStack tool = getTool();
        if (tool == null)
            return;

        //Handle mining arm placement
        Vector delta = max.clone().subtract(min.clone()).toVector();
        Block constructionBlock = min.clone().add(1 + miningArmShift.getBlockX(), delta.getBlockY() - miningArmShift.getBlockY(), 1 + miningArmShift.getBlockZ()).getBlock();
        //		ViperFusion.createConstruction(constructionBlock);
        Location oldCB = constructionBlock.getLocation();

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
                minedBlock.breakNaturally(tool, true);
                constructionBlock = updateArm(delta);
                break;
            }
        }

        // Update visual effects
        updateVisual(constructionBlock, oldCB.getBlock());
    }

    public void breakObj() {
        Quarries.plugin.getServer().getScheduler().runTaskLater(Quarries.plugin, this::destroy, 1);
    }

    private void updateVisual(Block constructionBlock, Block oldCB) {
        for (int y = max.getBlockY() - 1; y >= constructionBlock.getLocation().getBlockY(); y--) {
            Location newPos = constructionBlock.getLocation().clone();
            Location oldPos = oldCB.getLocation().clone();
            // Optimization - teleport oldPos ones instead breaking them and create one
            newPos.setX(newPos.getX() + 0.5);
            newPos.setZ(newPos.getZ() + 0.5);
            newPos.setY(y);
            oldPos.setY(y);
            oldPos.setX(oldPos.getX() + 0.5);
            oldPos.setZ(oldPos.getZ() + 0.5);
            QuarryArm arm = QuarryArm.DATABASE.get(oldPos);
            if (arm != null) {
                if (arm.uuid == null) {
                    arm.breakQuarryArm();
                    continue;
                }
                Entity blockEntity = Objects.requireNonNull(Bukkit.getEntity(arm.uuid));
                blockEntity.teleport(newPos);
                QuarryArm.DATABASE.put(newPos, QuarryArm.DATABASE.get(oldPos));
                QuarryArm.DATABASE.remove(oldPos);
            } else arms.add(new SoftReference<>(new QuarryArm(newPos, null, true)));
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

    private ItemStack getTool() {
        Block toolContainer = quarryBlock.getRelative(BlockFace.UP);
        if (toolContainer.getState() instanceof Container) {
            Inventory inv = ((Container) toolContainer.getState()).getInventory();
            return inv.getItem(0);
        }
        return null;
    }

    public void handleMinedItem(ItemStack item) {
        Quarries.plugin.getServer().createWorld(new WorldCreator(world));
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

        if (!handledAlready) {
            quarryBlock.getWorld().dropItem(quarryBlock.getLocation().clone().add(.5, 1.2, .5), item);
        }
    }

    public void breakQuarryArms() {
        for (SoftReference<QuarryArm> arm : arms)
            Objects.requireNonNull(arm.get()).breakQuarryArm();
        isAlive = false;
    }

    public boolean checkAlive() {
        return isAlive;
    }

}
