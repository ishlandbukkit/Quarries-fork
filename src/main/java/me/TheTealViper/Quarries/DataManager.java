package me.TheTealViper.Quarries;

import me.TheTealViper.Quarries.blocks.Construction;
import me.TheTealViper.Quarries.blocks.Marker;
import me.TheTealViper.Quarries.blocks.Quarry;
import me.TheTealViper.Quarries.serializables.LocationSerializable;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
        LinkedBlockingQueue<Future<?>> futures = new LinkedBlockingQueue<>();
        futures.add(Quarries.pool.submit(() -> {
            for (Map.Entry<Location, Construction> entry : Construction.DATABASE.entrySet())
                futures.add(Quarries.pool.submit(() -> {
                            if (entry.getKey() == null || entry.getValue() == null ||
                                    !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                                if (entry.getValue() != null)
                                    entry.getValue().breakObj();
                                Construction.DATABASE.remove(entry.getKey());
                            }
                        }
                ));
        }));
        futures.add(Quarries.pool.submit(() -> {
            for (Map.Entry<Location, Quarry> entry : Quarry.DATABASE.entrySet()) {
                futures.add(Quarries.pool.submit(() -> {
                            if (entry.getKey() == null || entry.getValue() == null ||
                                    !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                                if (entry.getValue() != null)
                                    entry.getValue().breakObj();
                                Quarry.DATABASE.remove(entry.getKey());
                            }
                        }
                ));
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            for (Map.Entry<Location, Marker> entry : Marker.DATABASE.entrySet()) {
                futures.add(Quarries.pool.submit(() -> {
                            if (entry.getKey() == null || entry.getValue() == null ||
                                    !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                                if (entry.getValue() != null)
                                    entry.getValue().breakObj();
                                Marker.DATABASE.remove(entry.getKey());
                            }
                        }
                ));
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            for (Map.Entry<Location, QuarrySystem> entry : QuarrySystem.DATABASE.entrySet()) {
                futures.add(Quarries.pool.submit(() -> {
                            if (entry.getKey() == null || entry.getValue() == null ||
                                    !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                                if (entry.getValue() != null)
                                    entry.getValue().breakObj();
                                QuarrySystem.DATABASE.remove(entry.getKey());
                            }
                        }
                ));
            }
        }));
        while (true) {
            Future<?> future = futures.poll(100, TimeUnit.MILLISECONDS);
            if (future == null) break;
            future.get();
        }
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
                    ((Map<LocationSerializable, Construction>) in.readObject())
                            .forEach((k, v) -> Construction.DATABASE.put(k.toLocation(), v));
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
                    ((Map<LocationSerializable, Quarry>) in.readObject())
                            .forEach((k, v) -> Quarry.DATABASE.put(k.toLocation(), v));
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
                    ((Map<LocationSerializable, Marker>) in.readObject())
                            .forEach((k, v) -> Marker.DATABASE.put(k.toLocation(), v));
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
                    ((Map<LocationSerializable, QuarrySystem>) in.readObject())
                            .forEach((k, v) -> QuarrySystem.DATABASE.put(k.toLocation(), v));
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
                Map<LocationSerializable, Construction> data = new HashMap<>();
                Construction.DATABASE.forEach((k, v) -> data.put(LocationSerializable.parseLocation(k), v));
                out.writeObject(data);
                out.flush();
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarryData));
                Map<LocationSerializable, Quarry> data = new HashMap<>();
                Quarry.DATABASE.forEach((k, v) -> data.put(LocationSerializable.parseLocation(k), v));
                out.writeObject(data);
                out.flush();
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(markerData));
                Map<LocationSerializable, Marker> data = new HashMap<>();
                Marker.DATABASE.forEach((k, v) -> data.put(LocationSerializable.parseLocation(k), v));
                out.writeObject(data);
                out.flush();
                out.close();
            } catch (Exception e) {
                Quarries.plugin.getLogger().log(Level.SEVERE, "Unable to save data", e);
                throw new RuntimeException("Unable to save data", e);
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarrySystemData));
                Map<LocationSerializable, QuarrySystem> data = new HashMap<>();
                QuarrySystem.DATABASE.forEach((k, v) -> data.put(LocationSerializable.parseLocation(k), v));
                out.writeObject(data);
                out.flush();
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
        Quarries.plugin.getLogger().info("QuarrySystem: " + QuarrySystem.DATABASE.size());
        Quarries.plugin.getLogger().info("End of statistics");
    }
}
