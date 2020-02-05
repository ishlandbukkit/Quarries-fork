package me.TheTealViper.Quarries.integration.protection;

import me.TheTealViper.Quarries.Quarries;
import org.bukkit.Location;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Protections {
    public static List<Protection> DATABASE = new ArrayList<>();
    private static final Reflections reflections = new Reflections("me.TheTealViper.Quarries.integration.protection.types");

    public static synchronized void reload() {
        Quarries.plugin.getLogger().info("Reloading protection modules...");
        List<Protection> list = new ArrayList<>();
        Set<Class<? extends Protection>> classes = reflections.getSubTypesOf(Protection.class);
        for (Class<? extends Protection> clazz : classes) {
            try {
                Protection protection = clazz.getDeclaredConstructor().newInstance();
                if (protection.isAvailable()) {
                    list.add(protection);
                    Quarries.plugin.getLogger().info("Hooked into " + protection.providerName());
                } else {
                    Quarries.plugin.getLogger().info(protection.providerName() + " not found");
                }
            } catch (Exception e) {
                Quarries.plugin.getLogger()
                        .log(Level.WARNING, "Unable to check the accessibility of " + clazz.getName());
            }
        }
        DATABASE = list;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canPlace(Location loc, String playerName) {
        boolean res = true;
        for (Protection protection : DATABASE)
            try {
                boolean result = protection.canPlace(loc, playerName);
                if (!result) {
                    if (Quarries.plugin.getLogger().isLoggable(Level.FINE))
                        Quarries.plugin.getLogger().log(Level.FINE,
                                protection.getClass().getName() +
                                        " (Provider name: " + protection.providerName() + ") " +
                                        "denied the request to break block at " + loc
                        );
                    res = false;
                }
            } catch (Exception e) {
                Quarries.plugin.getLogger()
                        .log(Level.WARNING, "Cannot pass canPlace() to module " +
                                protection.getClass().getName() + " Provider name: " +
                                protection.providerName(), e);
            }
        return res;
    }

    public static boolean canBreak(Location loc, String playerName) {
        boolean res = true;
        for (Protection protection : DATABASE)
            try {
                boolean result = protection.canBreak(loc, playerName);
                if (!result) {
                    if (Quarries.plugin.getLogger().isLoggable(Level.FINE))
                        Quarries.plugin.getLogger().log(Level.FINE,
                                protection.getClass().getName() +
                                        " (Provider name: " + protection.providerName() + ") " +
                                        "denied the request to place block at " + loc
                        );
                    res = false;
                }
            } catch (Exception e) {
                Quarries.plugin.getLogger()
                        .log(Level.WARNING, "Cannot pass canBreak() to module " +
                                protection.getClass().getName() + " Provider name: " +
                                protection.providerName(), e);
            }
        return res;
    }
}
