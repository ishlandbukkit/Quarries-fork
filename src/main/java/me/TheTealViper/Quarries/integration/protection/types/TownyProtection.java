package me.TheTealViper.Quarries.integration.protection.types;

import com.palmergames.bukkit.towny.TownyAPI;
import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.integration.protection.Protection;
import org.bukkit.Location;

public class TownyProtection implements Protection {

    public boolean isAvailable() {
        return Quarries.plugin.getServer().getPluginManager().getPlugin("Towny") != null;
    }

    @Override
    public String providerName() {
        return "Towny";
    }

    public boolean canBreak(Location loc, String playerName) {
        return TownyAPI.getInstance().isWilderness(loc);
    }

    public boolean canPlace(Location loc, String playerName) {
        return TownyAPI.getInstance().isWilderness(loc);
    }
}
