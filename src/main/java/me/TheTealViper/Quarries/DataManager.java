package me.TheTealViper.Quarries;

import me.TheTealViper.Quarries.insidespawners.Construction;
import me.TheTealViper.Quarries.insidespawners.Quarry;
import me.TheTealViper.Quarries.outsidespawners.Marker;
import me.TheTealViper.Quarries.outsidespawners.QuarryArm;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class DataManager {
    private static File dataDir = Quarries.plugin.getDataFolder();
    private static File constructionData = new File(Quarries.plugin.getDataFolder(), "construction.dat");
    private static File quarryData = new File(Quarries.plugin.getDataFolder(), "quarry.dat");
    private static File markerData = new File(Quarries.plugin.getDataFolder(), "marker.dat");
    private static File quarryArmData = new File(Quarries.plugin.getDataFolder(), "quarryArm.dat");
    private static File quarrySystemData = new File(Quarries.plugin.getDataFolder(), "quarrySystem.dat");

    private static ExecutorService pool = Executors.newCachedThreadPool();

    @SuppressWarnings("unchecked")
    public static void reload() throws IOException {
        if (!dataDir.exists())
            save();
        if (constructionData.exists())
            pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(constructionData));
                    Construction.DATABASE = (Map<Location, Construction>) in.readObject();
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                }
            });
        if (quarryData.exists())
            pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarryData));
                    Quarry.DATABASE = (Map<Location, Quarry>) in.readObject();
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                }
            });
        if (markerData.exists())
            pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(markerData));
                    Marker.DATABASE = (Map<Location, Marker>) in.readObject();
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                }
            });
        if (quarryArmData.exists())
            pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarryArmData));
                    QuarryArm.DATABASE = (Map<Location, QuarryArm>) in.readObject();
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                }
            });
        if (quarrySystemData.exists())
            pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarrySystemData));
                    QuarrySystem.DATABASE = (Map<Location, QuarrySystem>) in.readObject();
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                }
            });

    }

    public static void save() throws IOException {
        if (!dataDir.exists() && !dataDir.mkdirs())
            throw new IOException("Error while creating " + dataDir.getPath());
        pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(constructionData));
                out.writeObject(Construction.DATABASE);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
            }
        });
        pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarryData));
                out.writeObject(Quarry.DATABASE);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
            }
        });
        pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(markerData));
                out.writeObject(Marker.DATABASE);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
            }
        });
        pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarryArmData));
                out.writeObject(QuarryArm.DATABASE);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
            }
        });
        pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarrySystemData));
                out.writeObject(QuarrySystem.DATABASE);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
            }
        });
    }
}
