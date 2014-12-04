package pneumaticCraft.common.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ThreadedSorter<T> extends Thread{

    private boolean isDone;
    private final List<T> list;
    private final Comparator comparator;

    public ThreadedSorter(List<T> list, Comparator comparator){
        this.list = list;
        this.comparator = comparator;
        setName("PneumaticCraft Drone Area Sorting Thread");
        start();
    }

    @Override
    public void run(){
        Collections.sort(list, comparator);
        isDone = true;
    }

    public boolean isDone(){
        return isDone;
    }
}
