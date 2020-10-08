package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.lib.Log;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Simple minded class which allows runnables to deferred until the end of this server tick
 * No provision for delaying tasks for any longer
 */
public enum DeferredTaskManager {
    INSTANCE;

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<>();

    public static DeferredTaskManager getInstance() {
        return INSTANCE;
    }

    public void scheduleTask(@Nonnull Runnable task) {
        taskQueue.add(Objects.requireNonNull(task));
    }

    public void runTasks() {
        while (taskQueue.peek() != null) {
            try {
                taskQueue.take().run();
            } catch (Exception e) {
                Log.error("Deferred task threw an expection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
