package me.TheTealViper.Quarries.integration.protection;

import org.bukkit.Location;

public interface Protection {

    boolean isAvailable();

    String providerName();

    boolean canPlace(Location loc, String playerName);

    boolean canBreak(Location loc, String playerName);
}
