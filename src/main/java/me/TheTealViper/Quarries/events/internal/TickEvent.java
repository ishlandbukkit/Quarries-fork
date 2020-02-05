package me.TheTealViper.Quarries.events.internal;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TickEvent extends Event {

    public TickEvent() {
        super();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return new HandlerList();
    }
}
