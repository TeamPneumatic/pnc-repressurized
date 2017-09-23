package igwmod.gui;

import java.util.ArrayList;
import java.util.List;

import igwmod.gui.tabs.IWikiTab;

public class BrowseHistory{

    public final String link;
    public final IWikiTab tab;
    public final Object[] meta;
    public float scroll;
    private static int curIndex;

    private BrowseHistory(String link, IWikiTab tab, Object... meta){
        this.link = link;
        this.meta = meta;
        this.tab = tab;
    }

    public static void updateHistory(float scroll){
        if(history.size() > 0) history.get(history.size() - 1).scroll = scroll;
    }

    private static List<BrowseHistory> history = new ArrayList<BrowseHistory>();

    public static void addHistory(String link, IWikiTab tab, Object... meta){
        if(history.size() > 0) history = history.subList(0, curIndex + 1);
        curIndex = history.size();
        history.add(new BrowseHistory(link, tab, meta));
    }

    public static BrowseHistory previous(){
        if(canGoPrevious()) {
            curIndex--;
            return history.get(curIndex);
        } else {
            throw new IllegalArgumentException("It's not possible to go to the previous page here. Check for 'canGoPrevious()' first!");
        }
    }

    public static BrowseHistory next(){
        if(canGoNext()) {
            curIndex++;
            return history.get(curIndex);
        } else {
            throw new IllegalArgumentException("It's not possible to go to the next page here. Check for 'canGoNext()' first!");
        }
    }

    public static boolean canGoPrevious(){
        return curIndex > 0;
    }

    public static boolean canGoNext(){
        return curIndex + 1 < history.size();
    }
}
