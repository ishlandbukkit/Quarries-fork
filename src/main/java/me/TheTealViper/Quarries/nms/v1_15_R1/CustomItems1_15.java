package me.TheTealViper.Quarries.nms.v1_15_R1;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItems1_15 {

    public static ItemStack getItem(int textureId) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Quarries Item - " + textureId);
        meta.setCustomModelData(textureId);
        item.setItemMeta(meta);
        return item;
    }

    public static NBTTagCompound getItemNBT(int textureId) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("id", "minecraft:stone");
        compound.setShort("Count", (short) 1);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("CustomModelData", textureId);
        compound.set("tag", tag);

        return compound;
    }

//	public static ItemStack getMarker() {
//		ItemStack item = new ItemStack(Material.DIRT);
//		ItemMeta meta = item.getItemMeta();
////		meta.setCustomModelData(3324476);
//		meta.setCustomModelData(ViperFusion.TEXID_MARKER);
//		item.setItemMeta(meta);
//		return item;
//	}
//	public static NBTTagCompound getMarkerNBT() {
//		NBTTagCompound compound = new NBTTagCompound();
//		compound.setString("id", "minecraft:dirt");
//		compound.setShort("Count", (short) 1);
//		
//		NBTTagCompound tag = new NBTTagCompound();
////		tag.setInt("CustomModelData", 3324476);
//		tag.setInt("CustomModelData", ViperFusion.TEXID_MARKER);
//		compound.set("tag", tag);
//		
//		return compound;
//	}

}
