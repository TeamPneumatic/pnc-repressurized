package me.desht.pneumaticcraft.api.client;

import me.desht.pneumaticcraft.client.gui.widget.ITickableWidget;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.List;

/**
 * This interface doesn't have to be implemented. In PneumaticCraft there already is a class which implements this interface
 * which is used many times in PneumaticCraft (GUI side tabs, Pneumatic Helmet 2D and 3D stats). You can get an instance of this
 * class via the various {@link IClientRegistry} getAnimatedStat() methods. Implementing your own version of animated
 * stats is also possible, and they will interact with the PneumaticCraft GuiAnimatedStats if you implement it correctly.
 */
public interface IGuiAnimatedStat extends ITickableWidget {
    /**
     * When you call this method with a set of coordinates representing the button location and dimensions, you'll get
     * these parameters back scaled to the GuiAnimatedStat's scale.
     *
     * @param origX  Button start X.
     * @param origY  Button start Y.
     * @param width  Button width.
     * @param height Button height.
     * @return rectangle containing the new location and dimensions.
     */
    Rectangle2d getButtonScaledRectangle(int origX, int origY, int width, int height);

    /**
     * When passed 0.5F for example, the text of the stat will be half as big (so more text can fit into a certain area).
     *
     * @param scale
     */
    void scaleTextSize(float scale);

    /**
     * Returns true if the statistic expands to the left.
     *
     * @return
     */
    boolean isLeftSided();

    /**
     * Returns true if the statistic is done with expanding (when text will be displayed).
     *
     * @return
     */
    boolean isDoneExpanding();

    /**
     * Pass true if the statistic should expand to the left, otherwise false.
     *
     * @param leftSided
     */
    void setLeftSided(boolean leftSided);

    /**
     * Sets the main text of this stat. Every line should be stored in a separate list element. Upon rendering,
     * TextFormattings will be respected. When you call this method, Overlong lines will be divided into multiple
     * shorter ones to fit in the GUI.
     *
     * @param text
     * @return this, so you can chain calls.
     */
    IGuiAnimatedStat setText(List<String> text);

    /**
     * Sets the line to a single line. Upon rendering,
     * TextFormattings will be respected. When you call this method, Too-long lines will be divided into multiple
     * shorter ones to fit in the GUI.
     *
     * @param text
     * @return this, so you can chain calls.
     */
    IGuiAnimatedStat setText(String text);

    /**
     * Sets the main text of this stat. Every line should be stored in a separate list element. Upon rendering,
     * TextFormattings will be respected. This method doesn't split overlong lines.
     *
     * @param text
     */
    void setTextWithoutCuttingString(List<String> text);

    /**
     * Appends some more text to the existing text in this stat.  This method will split overlong lines.
     *
     * @param text
     */
    void appendText(List<String> text);

    /**
     * Sets the title of this stat. It will automatically get the yellow color assigned.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Returns the title of this stat (obviously without color prefix).
     *
     * @return
     */
    String getTitle();

    /**
     * Defines what dimensions the stat should have when it is not expanded (default 17x17) and resets the stat to these dimensions.
     * Used in PneumaticCraft by the block/entity tracker stats, they are 0x0 when not expanded so it looks like they expand
     * (and appear) from nothing.
     *
     * @param minWidth
     * @param minHeight
     */
    void setMinDimensionsAndReset(int minWidth, int minHeight);

    /**
     * When this stat gets a parent stat assigned, the y of this stat will be the same as the parent's plus this stat's
     * baseY. This will cause this stat to move up and down when the parent's stat expands/moves.
     *
     * @param stat
     */
    void setParentStat(IGuiAnimatedStat stat);

    /**
     * Pad the stat tab with some spacing to allow for widget placement.
     *
     * @param nRows rows of spacing
     * @param nCols columns of spacing
     */
    void addPadding(int nRows, int nCols);

    /**
     * Pad the stat tab with some spacing to allow for widget placement.
     *
     * @param text existing text to insert into the padding
     * @param nRows rows of spacing
     * @param nCols columns of spacing
     */
    void addPadding(List<String> text, int nRows, int nCols);

    /**
     * Change the background color of this stat.
     *
     * @param backgroundColor color, in ARGB format
     */
    void setBackgroundColor(int backgroundColor);

    /**
     * Get the background color of this stat.
     *
     * @return the background color
     */
    int getBackgroundColor();

    /**
     * Should this stat be drawn with a beveled edge, or a plain edge?  Default behaviour is a beveled edge
     * for GUI side tabs, plain edge for HUD stats.  The color of the plain edge is a darkened version of the stat's
     * background color.
     *
     * @param bevel
     */
    void setBeveled(boolean bevel);

    /**
     * Sets the x location of this stat.
     *
     * @param x
     */
    void setBaseX(int x);

    /**
     * Sets the base Y of this stat.
     *
     * @param y
     */
    void setBaseY(int y);

    /**
     * Returns the actual Y position of this stat. This is the same as getBaseY when there is no parent stat, but if
     * there is, this method returns the value described in {@link #setParentStat(IGuiAnimatedStat)}.
     *
     * @return
     */
    int getAffectedY();

    int getBaseX();

    int getBaseY();

    /**
     * Returns the Y size of this stat.
     *
     * @return the stat's height
     */
    int getHeight();

    /**
     * Returns the X size of this stat.
     *
     * @return the stat's width
     */
    int getWidth();

    /**
     * Get a bounding box for this stat.
     *
     * @return a bounding box
     */
    Rectangle2d getBounds();

    /**
     * Should be called every render tick when and where you want to render the stat.
     *
     * @param mouseX
     * @param mouseY
     * @param partialTicks
     */
    void render(int mouseX, int mouseY, float partialTicks);

    /**
     * Forces the stat to close.
     */
    void closeWindow();

    /**
     * Forces the stat to expand.
     */
    void openWindow();

    /**
     * Returns true if the stat is expanding.
     *
     * @return
     */
    boolean isClicked();

}
