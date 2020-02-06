package me.TheTealViper.Quarries.misc;

import me.TheTealViper.Quarries.Quarries;
import me.TheTealViper.Quarries.annotations.Synchronized;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler {
    private Queue<Runnable> runnableQueue = new ConcurrentLinkedQueue<>();

    public Scheduler() {
        Quarries.plugin.getServer().getScheduler().runTaskTimer(Quarries.plugin, this::runTasks, 0, 1);
    }

    public void runSync(Runnable runnable) {
        runnableQueue.add(runnable);
    }

    @Synchronized
    private void runTasks() {
        long startTime = System.nanoTime();
        while (((System.nanoTime() - startTime) / 1000.0 / 1000.0) < Quarries.serverThreadTime
                && !runnableQueue.isEmpty())
            runnableQueue.poll().run();
    }
}
