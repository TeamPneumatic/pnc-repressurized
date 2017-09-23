package igwmod.gui;

public interface IPageLink extends IClickable{

    /**
     * String that is being used by the search bar.
     * @return
     */
    String getName();

    String getLinkAddress();
}
