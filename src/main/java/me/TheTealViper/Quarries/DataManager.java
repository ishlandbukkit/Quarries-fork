package me.TheTealViper.Quarries;

import me.TheTealViper.Quarries.blocks.Construction;
import me.TheTealViper.Quarries.blocks.Marker;
import me.TheTealViper.Quarries.blocks.Quarry;
import me.TheTealViper.Quarries.entities.QuarryArm;
import me.TheTealViper.Quarries.serializables.LocationSerializable;
import me.TheTealViper.Quarries.systems.QuarrySystem;
import org.bukkit.Location;

import java.io.*;
import java.util.*;
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
            Iterator<Map.Entry<Location, Construction>> it = Construction.DATABASE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Location, Construction> entry = it.next();
                if (entry.getKey() == null || entry.getValue() == null ||
                        !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                    if (entry.getValue() != null)
                        entry.getValue().breakObj();
                    it.remove();
                }
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            Iterator<Map.Entry<Location, Quarry>> it = Quarry.DATABASE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Location, Quarry> entry = it.next();
                if (entry.getKey() == null || entry.getValue() == null ||
                        !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                    if (entry.getValue() != null)
                        entry.getValue().breakObj();
                    it.remove();
                }
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            Iterator<Map.Entry<Location, Marker>> it = Marker.DATABASE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Location, Marker> entry = it.next();
                if (entry.getKey() == null || entry.getValue() == null ||
                        !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                    if (entry.getValue() != null)
                        entry.getValue().breakObj();
                    it.remove();
                }
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            Iterator<Map.Entry<Location, QuarryArm>> it = QuarryArm.DATABASE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Location, QuarryArm> entry = it.next();
                if (entry.getKey() == null || entry.getValue() == null ||
                        !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                    if (entry.getValue() != null)
                        entry.getValue().breakObj();
                    it.remove();
                }
            }
        }));
        futures.add(Quarries.pool.submit(() -> {
            Iterator<Map.Entry<Location, QuarrySystem>> it = QuarrySystem.DATABASE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Location, QuarrySystem> entry = it.next();
                if (entry.getKey() == null || entry.getValue() == null ||
                        !entry.getValue().isAlive || !entry.getValue().checkAlive()) {
                    if (entry.getValue() != null)
                        entry.getValue().breakObj();
                    it.remove();
                }
            }
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
        if (quarryArmData.exists())
            futures.add(Quarries.pool.submit(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(quarryArmData));
                    ((Map<LocationSerializable, QuarryArm>) in.readObject())
                            .forEach((k, v) -> QuarryArm.DATABASE.put(k.toLocation(), v));
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
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(quarryArmData));
                Map<LocationSerializable, QuarryArm> data = new HashMap<>();
                QuarryArm.DATABASE.forEach((k, v) -> data.put(LocationSerializable.parseLocation(k), v));
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
        Quarries.plugin.getLogger().info("QuarryArm: " + QuarryArm.DATABASE.size());
        Quarries.plugin.getLogger().info("QuarrySystem: " + QuarryArm.DATABASE.size());
        Quarries.plugin.getLogger().info("End of statistics");
    }
}
