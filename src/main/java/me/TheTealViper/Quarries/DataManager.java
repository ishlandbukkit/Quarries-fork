package me.TheTealViper.Quarries;

import me.TheTealViper.Quarries.insidespawners.Construction;
import me.TheTealViper.Quarries.insidespawners.Quarry;
import me.TheTealViper.Quarries.misc.LocationSerializable;
import me.TheTealViper.Quarries.outsidespawners.Marker;
import me.TheTealViper.Quarries.outsidespawners.QuarryArm;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class DataManager {
    private static final File dataDir = new File(Quarries.plugin.getDataFolder(), "data");
    private static final File constructionData = new File(Quarries.plugin.getDataFolder(), "data/construction.dat");
    private static final File quarryData = new File(Quarries.plugin.getDataFolder(), "data/quarry.dat");
    private static final File markerData = new File(Quarries.plugin.getDataFolder(), "data/marker.dat");
    private static final File quarryArmData = new File(Quarries.plugin.getDataFolder(), "data/quarryArm.dat");
    private static final File quarrySystemData = new File(Quarries.plugin.getDataFolder(), "data/quarrySystem.dat");

    public static void registerTask() {
        Quarries.plugin.getServer().getScheduler().runTaskTimerAsynchronously(Quarries.plugin, () -> {
                    Quarries.plugin.getLogger().info("Cleaning up database...");
                    long startTime = System.nanoTime();
                    try {
                        cleanDB();
                    } catch (Exception e) {
                        Quarries.plugin.getLogger().log(Level.SEVERE, "Cleaning up database failed after " +
                                (System.nanoTime() - startTime) / 1000.0 / 1000.0 + "ms");
                    }
                    Quarries.plugin.getLogger().info("Cleaning up database completed after " +
                            (System.nanoTime() - startTime) / 1000.0 / 1000.0 + "ms");

                    Quarries.plugin.getLogger().info("Auto-saving system data... " +
                            "(Next Auto-save: " + Quarries.plugin.getConfig().getLong("Auto_Saving", 300) + "s)");
                    printStats();
                    startTime = System.nanoTime();
                    try {
                        save();
                    } catch (Exception e) {
                        Quarries.plugin.getLogger().log(Level.SEVERE, "Saving failed after " +
                                (System.nanoTime() - startTime) / 1000.0 / 1000.0 + "ms", e);
                    }
                    Quarries.plugin.getLogger().info("Saving completed after " +
                            (System.nanoTime() - startTime) / 1000.0 / 1000.0 + "ms");
                },
                Quarries.plugin.getConfig().getLong("Auto_Saving", 300) * 20,
                Quarries.plugin.getConfig().getLong("Auto_Saving", 300) * 20);
    }

    public static void cleanDB() throws ExecutionException, InterruptedException {
        List<Future<?>> futures = new ArrayList<>();
        futures.add(Quarries.pool.submit(() -> {
            Construction.DATABASE.entrySet().removeIf(
                    entry -> entry.getKey() == null || entry.getValue() == null ||
                            !entry.getValue().isAlive || !entry.getValue().checkAlive());
        }));
        futures.add(Quarries.pool.submit(() -> {
            Quarry.DATABASE.entrySet().removeIf(
                    entry -> entry.getKey() == null || entry.getValue() == null ||
                            !entry.getValue().isAlive || !entry.getValue().checkAlive());
        }));
        futures.add(Quarries.pool.submit(() -> {
            Marker.DATABASE.entrySet().removeIf(
                    entry -> entry.getKey() == null || entry.getValue() == null ||
                            !entry.getValue().isAlive || !entry.getValue().checkAlive());
        }));
        futures.add(Quarries.pool.submit(() -> {
            QuarryArm.DATABASE.entrySet().removeIf(
                    entry -> entry.getKey() == null || entry.getValue() == null ||
                            !entry.getValue().isAlive || !entry.getValue().checkAlive());
        }));
        futures.add(Quarries.pool.submit(() -> {
            QuarrySystem.DATABASE.entrySet().removeIf(
                    entry -> entry.getKey() == null || entry.getValue() == null ||
                            !entry.getValue().isAlive || !entry.getValue().checkAlive());
        }));
        for (Future<?> future : futures)
            future.get();
    }

    @SuppressWarnings("unchecked")
    public static void reload() throws IOException, ExecutionException, InterruptedException {
        if (!dataDir.exists())
            save();
        List<Future<?>> futures = new ArrayList<>();
        if (constructionData.exists())
            futures.add(Quarries.pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(constructionData));
                    ConcurrentMap<Location, Construction> data = new ConcurrentHashMap<>();
                    for (ConcurrentMap.Entry<LocationSerializable, Construction> entry :
                            ((ConcurrentMap<LocationSerializable, Construction>) in.readObject()).entrySet())
                        data.put(entry.getKey().toLocation(), entry.getValue());
                    Construction.DATABASE = data;
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                    throw new RuntimeException("Unable to load data", e);
                }
            }));
        if (quarryData.exists())
            futures.add(Quarries.pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarryData));
                    ConcurrentMap<Location, Quarry> data = new ConcurrentHashMap<>();
                    for (ConcurrentMap.Entry<LocationSerializable, Quarry> entry :
                            ((ConcurrentMap<LocationSerializable, Quarry>) in.readObject()).entrySet())
                        data.put(entry.getKey().toLocation(), entry.getValue());
                    Quarry.DATABASE = data;
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                    throw new RuntimeException("Unable to load data", e);
                }
            }));
        if (markerData.exists())
            futures.add(Quarries.pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(markerData));
                    ConcurrentMap<Location, Marker> data = new ConcurrentHashMap<>();
                    for (ConcurrentMap.Entry<LocationSerializable, Marker> entry :
                            ((ConcurrentMap<LocationSerializable, Marker>) in.readObject()).entrySet())
                        data.put(entry.getKey().toLocation(), entry.getValue());
                    Marker.DATABASE = data;
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                    throw new RuntimeException("Unable to load data", e);
                }
            }));
        if (quarryArmData.exists())
            futures.add(Quarries.pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarryArmData));
                    ConcurrentMap<Location, QuarryArm> data = new ConcurrentHashMap<>();
                    for (ConcurrentMap.Entry<LocationSerializable, QuarryArm> entry :
                            ((ConcurrentMap<LocationSerializable, QuarryArm>) in.readObject()).entrySet())
                        data.put(entry.getKey().toLocation(), entry.getValue());
                    QuarryArm.DATABASE = data;
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                    throw new RuntimeException("Unable to load data", e);
                }
            }));
        if (quarrySystemData.exists())
            futures.add(Quarries.pool.submit(() -> {
                try {
                    futures.get(1).get();
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarrySystemData));
                    QuarrySystem.DATABASE = (ConcurrentMap<Location, QuarrySystem>) in.readObject();
                    in.close();
                } catch (Exception e) {
                    Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to load data", e);
                    throw new RuntimeException("Unable to load data", e);
                }
            }));
        for (Future<?> future : futures)
            future.get();
    }

    public static void save() throws IOException, ExecutionException, InterruptedException {
        if (!dataDir.exists() && !dataDir.mkdirs())
            throw new IOException("Error while creating " + dataDir.getPath());
        List<Future<?>> futures = new ArrayList<>();
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(constructionData));
                ConcurrentMap<LocationSerializable, Construction> data = new ConcurrentHashMap<>();
                for (ConcurrentMap.Entry<Location, Construction> entry : Construction.DATABASE.entrySet())
                    data.put(LocationSerializable.parseLocation(entry.getKey()), entry.getValue());
                out.writeObject(data);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarryData));
                ConcurrentMap<LocationSerializable, Quarry> data = new ConcurrentHashMap<>();
                for (ConcurrentMap.Entry<Location, Quarry> entry : Quarry.DATABASE.entrySet())
                    data.put(LocationSerializable.parseLocation(entry.getKey()), entry.getValue());
                out.writeObject(data);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(markerData));
                ConcurrentMap<LocationSerializable, Marker> data = new ConcurrentHashMap<>();
                for (ConcurrentMap.Entry<Location, Marker> entry : Marker.DATABASE.entrySet())
                    data.put(LocationSerializable.parseLocation(entry.getKey()), entry.getValue());
                out.writeObject(data);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarryArmData));
                ConcurrentMap<LocationSerializable, QuarryArm> data = new ConcurrentHashMap<>();
                for (ConcurrentMap.Entry<Location, QuarryArm> entry : QuarryArm.DATABASE.entrySet())
                    data.put(LocationSerializable.parseLocation(entry.getKey()), entry.getValue());
                out.writeObject(data);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarrySystemData));
                ConcurrentMap<LocationSerializable, QuarrySystem> data = new ConcurrentHashMap<>();
                for (ConcurrentMap.Entry<Location, QuarrySystem> entry : QuarrySystem.DATABASE.entrySet())
                    data.put(LocationSerializable.parseLocation(entry.getKey()), entry.getValue());
                out.writeObject(data);
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));

        for (Future<?> future : futures)
            future.get();
    }

    public static void printStats() {
        Quarries.plugin.getLogger().info("Sizes of databases: ");
        Quarries.plugin.getLogger().info("Construction: " + Construction.DATABASE.size());
        Quarries.plugin.getLogger().info("Quarry: " + Quarry.DATABASE.size());
        Quarries.plugin.getLogger().info("Marker: " + Marker.DATABASE.size());
        Quarries.plugin.getLogger().info("QuarryArm: " + QuarryArm.DATABASE.size());
        Quarries.plugin.getLogger().info("QuarrySystem: " + QuarryArm.DATABASE.size());
        Quarries.plugin.getLogger().info("End of statistics");
    }
}
