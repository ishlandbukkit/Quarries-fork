package me.TheTealViper.Quarries.nms.nms1_15;

import me.TheTealViper.Quarries.Quarries;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("unused")
public class CustomSpawner1_15 {

    public static void createInsideSpawner(Block b, int textureId) {
        b.getWorld().playSound(b.getLocation(), Sound.BLOCK_WOOD_PLACE, 1, 1);
        b.setType(Material.SPAWNER);
        CreatureSpawner cs = (CreatureSpawner) b.getState();
        cs.setRequiredPlayerRange(0);
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) b.getLocation().getWorld()).getHandle().getTileEntity(new BlockPosition(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ()));
        assert spawner != null;
        NBTTagCompound spawnerTag = spawner.b();

        spawnerTag.setShort("RequiredPlayerRange", (short) 0);

        NBTTagCompound spawnData = (NBTTagCompound) spawnerTag.get("SpawnData");
        assert spawnData != null;
        spawnData.setString("id", "armor_stand");
        spawnData.setInt("Invisible", 1);
        spawnData.setInt("Small", 0);
        spawnData.setInt("NoBasePlate", 0);
        spawnData.setInt("ShowArms", 0);
        spawnData.setInt("Marker", 1);

        NBTTagList armorList = new NBTTagList();
        NBTTagCompound mainHand = new NBTTagCompound();
        NBTTagCompound offHand = new NBTTagCompound();
        NBTTagCompound helmet = CustomItems1_15.getItemNBT(textureId);
        NBTTagCompound chestplate = new NBTTagCompound();
        NBTTagCompound leggings = new NBTTagCompound();
        NBTTagCompound boots = new NBTTagCompound();
        armorList.add(boots);
        armorList.add(leggings);
        armorList.add(chestplate);
        armorList.add(helmet);
        spawnData.set("ArmorItems", armorList);

        spawnerTag.set("SpawnData", spawnData);
        spawner.load(spawnerTag);
        b.getState().update();
    }

    public static UUID createOutsideSpawner(Block b, int textureId) {
        // b.getWorld().playSound(b.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        // b.setType(replacementBlock);
        ArmorStand e = (ArmorStand) b.getWorld().spawnEntity(b.getLocation().clone().add(.5, 0, .5), EntityType.ARMOR_STAND);
        e.setSmall(true);
        e.setArms(false);
        e.setBasePlate(false);
        e.setGravity(false);
        e.setCollidable(false);
        e.setVisible(false);
        e.setMarker(true);
        //noinspection deprecation
        e.setHelmet(CustomItems1_15.getItem(textureId));
        return e.getUniqueId();
    }

    @SuppressWarnings("unused")
    public static void big(Player p) {
        Block b = p.getTargetBlock(null, 100);
        b.setType(Material.SPAWNER);
        CreatureSpawner cs = (CreatureSpawner) b.getState();
        cs.setRequiredPlayerRange(0);
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) b.getLocation().getWorld()).getHandle().getTileEntity(new BlockPosition(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ()));
        assert spawner != null;
        NBTTagCompound spawnerTag = spawner.b();

        spawnerTag.setShort("RequiredPlayerRange", (short) 0);

        NBTTagCompound spawnData = (NBTTagCompound) spawnerTag.get("SpawnData");
        assert spawnData != null;
        spawnData.setString("id", "armor_stand");
        spawnData.setInt("Invisible", 1);
        spawnData.setInt("Small", 0);
        spawnData.setInt("NoBasePlate", 0);
        spawnData.setInt("ShowArms", 0);
        spawnData.setInt("Marker", 1);

        NBTTagList armorList = new NBTTagList();
        NBTTagCompound mainHand = new NBTTagCompound();
        NBTTagCompound offHand = new NBTTagCompound();
        NBTTagCompound helmet = CustomItems1_15.getItemNBT(Quarries.TEXID_MARKER);
        NBTTagCompound chestplate = new NBTTagCompound();
        NBTTagCompound leggings = new NBTTagCompound();
        NBTTagCompound boots = new NBTTagCompound();
        armorList.add(boots);
        armorList.add(leggings);
        armorList.add(chestplate);
        armorList.add(helmet);
        spawnData.set("ArmorItems", armorList);

        spawnerTag.set("SpawnData", spawnData);
        spawner.load(spawnerTag);
        b.getState().update();
    }

    public static void small(Player p) {
        Block b = p.getTargetBlock(null, 100);
        b.setType(Material.SPAWNER);
        CreatureSpawner cs = (CreatureSpawner) b.getState();
        cs.setRequiredPlayerRange(0);
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) b.getLocation().getWorld()).getHandle().getTileEntity(new BlockPosition(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ()));
        assert spawner != null;
        NBTTagCompound spawnerTag = spawner.b();

        spawnerTag.setShort("RequiredPlayerRange", (short) 0);

        NBTTagCompound spawnData = (NBTTagCompound) spawnerTag.get("SpawnData");
        assert spawnData != null;
        spawnData.setString("id", "armor_stand");
        spawnData.setInt("Invisible", 0);
        spawnData.setInt("Small", 1);
        spawnData.setInt("NoBasePlate", 1);
        spawnData.setInt("ShowArms", 0);

        NBTTagList armorList = new NBTTagList();
        NBTTagCompound mainHand = new NBTTagCompound();
        NBTTagCompound offHand = new NBTTagCompound();
        NBTTagCompound helmet = new NBTTagCompound();
        NBTTagCompound chestplate = new NBTTagCompound();
        NBTTagCompound leggings = new NBTTagCompound();
        NBTTagCompound boots = new NBTTagCompound();
        armorList.add(boots);
        armorList.add(leggings);
        armorList.add(chestplate);
        armorList.add(helmet);
        spawnData.set("ArmorItems", armorList);

        spawnerTag.set("SpawnData", spawnData);
        spawner.load(spawnerTag);
        b.getState().update();
    }
}
