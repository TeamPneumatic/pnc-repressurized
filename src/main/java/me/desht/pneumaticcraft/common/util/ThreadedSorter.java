package me.desht.pneumaticcraft.common.util;

import java.util.Comparator;
import java.util.List;

public class ThreadedSorter<T> extends Thread {

    private boolean isDone;
    private final List<T> list;
    private final Comparator<? super T> comparator;

    public ThreadedSorter(List<T> list, Comparator<? super T> comparator) {
        this.list = list;
        this.comparator = comparator;
        setName("PneumaticCraft Drone Area Sorting Thread");
        start();
    }

    @Override
    public void run() {
        list.sort(comparator);
        isDone = true;
    }

    public boolean isDone() {
        return isDone;
    }
}
