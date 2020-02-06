package me.TheTealViper.Quarries.systems;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.annotations.Synchronized;
import me.TheTealViper.Quarries.blocks.Construction;
import me.TheTealViper.Quarries.blocks.Marker;
import me.TheTealViper.Quarries.integration.protection.Protections;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

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
    private String world;
    @SuppressWarnings("InstanceVariableMayNotBeInitializedByReadObject")
    private transient ItemStack currentTool = null;
    @SuppressWarnings("InstanceVariableMayNotBeInitializedByReadObject")
    private transient Future<?> lastShift = null;

    @Synchronized
    public QuarrySystem(@NotNull Block quarryBlock, @NotNull Location max,
                        @NotNull Location min, boolean powered, @NotNull QuarrySystemTypes type,
                        @NotNull Vector miningArmShift, boolean hitBedrock, int mineDelay) {
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

    @Synchronized
    public static void onEnable() {
        Quarries.plugin.getServer().getPluginManager().registerEvents(new QuarrySystemListeners(), Quarries.plugin);
    }

    @Synchronized
    @SuppressWarnings("EmptyMethod")
    public static void onDisable() {
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    @Synchronized
    public static QuarrySystem createQuarrySystem(Block quarryBlock, Location max, Location min, QuarrySystemTypes type) {
        return new QuarrySystem(quarryBlock, max, min, true, type, new Vector(0, 1, 0), false, 1);
    }

    @Synchronized
    public static void initCreateQuarrySystem(@NotNull Block quarryBlock, Block startingMarker, @NotNull BlockFace face) {
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

    private void writeObject(@NotNull ObjectOutputStream out) throws IOException {
        maxS = LocationSerializable.parseLocation(max);
        minS = LocationSerializable.parseLocation(min);
        masS = VectorSerializable.parseVector(miningArmShift);
        quarryBlockLoc = LocationSerializable.parseLocation(quarryBlock.getLocation());
        out.defaultWriteObject();
    }

    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
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
        Quarries.scheduler.runSync(this::destroy);
    }

    public void init() {
        QuarrySystem QS = this;
        List<Integer> scheduleInt = new ArrayList<>();
        scheduleInt.add(Bukkit.getScheduler().runTaskTimerAsynchronously(Quarries.plugin, () -> {
            if (QS.powered && !QS.hitBedrock && DATABASE.containsKey(quarryBlock.getLocation()))
                mine();
            else {
                Bukkit.getScheduler().cancelTask(scheduleInt.get(0));
                isActive = false;
            }
        }, 0, QS.mineDelay).getTaskId());
        isActive = true;
    }

    @Synchronized
    public void destroy() {
        Quarries.plugin.getServer().createWorld(new WorldCreator(world));
        if (!isAlive || !checkAlive()) return;
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

        //Remove from code
        DATABASE.remove(quarryBlock.getLocation());

        // Linus drop tips
        quarryBlock.getLocation().getWorld().dropItem(quarryBlock.getLocation(), CustomItems1_15.getItem(Quarries.TEXID_QUARRY));

        isAlive = false;
    }

    public void mine() {
//		Bukkit.broadcastMessage("mining");
        //Check tool
        ItemStack tool = getTool();
        if (tool == null)
            return;

        //Handle mining arm placement
        if (lastShift != null) {
            try {
                lastShift.get();
            } catch (Exception ignored) {
            }
        }
        Vector delta = max.clone().subtract(min.clone()).toVector();
        Block constructionBlock = min.clone().add(1 + miningArmShift.getBlockX(), delta.getBlockY() - miningArmShift.getBlockY(), 1 + miningArmShift.getBlockZ()).getBlock();
        //		ViperFusion.createConstruction(constructionBlock);

        //Handle mining
        Block minedBlock = constructionBlock.getRelative(BlockFace.DOWN);
        if (minedBlock.getType().equals(Material.BEDROCK)) {
            hitAir = false;
//			Bukkit.broadcastMessage("hit bedrock");
            hitBedrock = true;
        } else {
            hitAir = false;
            Quarries.scheduler.runSync(() -> minedBlock.breakNaturally(tool, true));
        }
        updateArm(delta);
    }

    public void breakObj() {
        Quarries.scheduler.runSync(this::destroy);
    }

    private void updateArm(@NotNull Vector delta) {
        //Handle updating mining arm
        Quarries.pool.submit(() -> {
            miningArmShift.add(new Vector(0, 0, 1));
            if (miningArmShift.getBlockZ() > delta.getBlockZ() - 2) {
                miningArmShift.setZ(0);
                miningArmShift.add(new Vector(1, 0, 0));
                if (miningArmShift.getBlockX() > delta.getBlockX() - 2) {
                    miningArmShift.setX(0);
                    miningArmShift.add(new Vector(0, 1, 0));
                }
            }
            miningArmShift.setY(0);
            while (true) {
                miningArmShift.add(new Vector(0, 1, 0));
                Block nextBlock = min.clone().add(
                        1 + miningArmShift.getBlockX(), delta.getBlockY() - miningArmShift.getBlockY(),
                        1 + miningArmShift.getBlockZ()).getBlock().getRelative(BlockFace.DOWN);
                /*Quarries.plugin.getLogger().info(nextBlock.getLocation().toString() +
                        nextBlock.getType().equals(Material.AIR) +
                        !Protections.canBreak(nextBlock.getLocation(), null) +
                        nextBlock.getType().equals(Material.BEDROCK) +
                        (nextBlock.getDrops(currentTool).size() == 0));

                 */
                if (nextBlock.getLocation().getBlockY() < 0) break;
                if (nextBlock.getType().equals(Material.AIR) || // Skip air
                        !Protections.canBreak(nextBlock.getLocation(), null) || // Skip protection
                        nextBlock.getType().equals(Material.BEDROCK) || // Skip bedrock
                        nextBlock.getDrops(currentTool).size() == 0) // Skip no drop

                    //noinspection UnnecessaryContinue
                    continue;
                else break;
            }
        });
    }

    @Synchronized
    @Nullable
    private ItemStack getTool() {
        Block toolContainer = quarryBlock.getRelative(BlockFace.UP);
        if (toolContainer.getState() instanceof Container) {
            Inventory inv = ((Container) toolContainer.getState()).getInventory();
            currentTool = inv.getItem(0);
            return currentTool;
        }
        currentTool = null;
        return null;
    }

    @Synchronized
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkAlive() {
        return isAlive;
    }

}
