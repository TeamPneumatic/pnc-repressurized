package igwmod.gui.tabs;

import java.util.ArrayList;
import java.util.List;

import igwmod.gui.GuiWiki;
import igwmod.gui.IPageLink;
import igwmod.gui.IReservedSpace;
import igwmod.gui.LocatedSectionString;
import igwmod.gui.LocatedString;

public abstract class BaseWikiTab implements IWikiTab{
    protected List<String> pageEntries = new ArrayList<String>();

    @Override
    public List<IReservedSpace> getReservedSpaces(){
        return null;
    }

    @Override
    public List<IPageLink> getPages(int[] pageIndexes){
        List<IPageLink> pages = new ArrayList<IPageLink>();
        if(pageIndexes == null) {
            for(int i = 0; i < pageEntries.size(); i++) {
                if(pageEntries.get(i).startsWith("#")) {
                    pages.add(new LocatedSectionString(getPageName(pageEntries.get(i)), 80, 64 + 11 * i, false));
                } else if(pageEntries.get(i).equals("")) {
                    pages.add(new LocatedString("", 80, 64 + 11 * i, 0, false));
                } else {
                    pages.add(new LocatedString(getPageName(pageEntries.get(i).toLowerCase()), 80, 64 + 11 * i, false, getPageLocation(pageEntries.get(i).toLowerCase())));
                }
            }
        } else {
            for(int i = 0; i < pageIndexes.length; i++) {
                if(pageEntries.get(pageIndexes[i]).startsWith("#")) {
                    pages.add(new LocatedSectionString(getPageName(pageEntries.get(pageIndexes[i]).toLowerCase()), 80, 64 + 11 * i, false).capTextWidth(pagesPerTab() > pageIndexes.length ? 100 : 77));
                } else if(pageEntries.get(pageIndexes[i]).equals("")) {
                    pages.add(new LocatedString("", 80, 64 + 11 * i, 0, false));
                } else {
                    pages.add(new LocatedString(getPageName(pageEntries.get(pageIndexes[i]).toLowerCase()), 80, 64 + 11 * i, false, getPageLocation(pageEntries.get(pageIndexes[i]).toLowerCase())).capTextWidth(pagesPerTab() > pageIndexes.length ? 100 : 77));
                }
            }
        }
        return pages;
    }

    protected void skipLine(){
        pageEntries.add("");
    }

    protected void addSectionHeader(String header){
        pageEntries.add("#" + header);
    }

    @Override
    public int pagesPerTab(){
        return 36;
    }

    @Override
    public int pagesPerScroll(){
        return 1;
    }

    @Override
    public int getSearchBarAndScrollStartY(){
        return 18;
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey){}

    @Override
    public void onPageChange(GuiWiki gui, String pageName, Object... metadata){}

    protected abstract String getPageName(String pageEntry);

    protected abstract String getPageLocation(String pageEntry);

}
