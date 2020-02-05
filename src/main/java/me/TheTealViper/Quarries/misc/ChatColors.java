package me.TheTealViper.Quarries.misc;

import org.bukkit.ChatColor;

/*
 * @author TheBusyBiscuit
 *
 */
public final class ChatColors {
    /**
     * This is just a simple shortcut for:
     * <code>ChatColor.translateAlternateColorCodes('&amp;', input)</code>
     *
     * @param input The String that should be colored
     * @return The colored String
     */
    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
