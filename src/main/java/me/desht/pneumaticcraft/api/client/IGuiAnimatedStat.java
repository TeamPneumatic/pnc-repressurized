package me.desht.pneumaticcraft.api.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.ITickableWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * This interface doesn't have to be implemented. In PneumaticCraft there already is a class which implements this interface
 * which is used many times in PneumaticCraft (GUI side tabs, Pneumatic Helmet 2D and 3D stats). You can get an instance of this
 * class via the various {@link IClientRegistry} getAnimatedStat() methods. Implementing your own version of animated
 * stats is also possible, and they will interact with the PneumaticCraft GuiAnimatedStats if you implement it correctly.
 */
public interface IGuiAnimatedStat extends ITickableWidget {
    /**
     * Returns true if the stat expands to the left.
     *
     * @return
     */
    boolean isLeftSided();

    /**
     * Returns true if the stat is done with expanding (when text will be displayed).
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
     * Sets the main text of this stat. Every line should be stored in a separate list element, but lines do not need
     * to be split manually; overlong lines will be automatically by wrapped to fit horizontally, and a scrollbar will
     * be added if necessary.
     *
     * @param text a list of text components
     * @return this, so you can chain calls.
     */
    IGuiAnimatedStat setText(List<ITextComponent> text);

    /**
     * Sets the main text of this stat. Every line should be stored in a separate list element, but lines do not need
     * to be split manually; overlong lines will be automatically by wrapped to fit horizontally, and a scrollbar will
     * be added if necessary.
     *
     * @param text a text component
     * @return this, so you can chain calls.
     */
    IGuiAnimatedStat setText(ITextComponent text);

    /**
     * Appends some more text to the existing text in this stat.  This method will split overlong lines, same as
     * {@link #setText(ITextComponent)}
     *
     * @param text a list of text components
     */
    void appendText(List<ITextComponent> text);

    /**
     * Defines what dimensions the stat should have when it is not expanded (default 17x17) and resets the stat to these
     * dimensions. Used in PneumaticCraft by the block/entity tracker stats, they are 0x0 when not expanded so it looks
     * like they expand (and appear) from nothing.
     *
     * @param minWidth the minimum width
     * @param minHeight the minimum height
     */
    void setMinimumContractedDimensions(int minWidth, int minHeight);

    /**
     * Set the minimum width that this stat should expand to, even if the stat's text isn't that wide or tall. Use this
     * if you need to ensure sufficient space for subwidgets.  You don't need to call this if you're not adding any
     * subwidgets, since the stat's expanded size will be automatically calculated from its text in that case.
     * <p>
     * Requesting a width wider than is available (given current screen resolution), or taller than 12 lines of text,
     * will be silently ignored, and clamped to those dimensions. The requested width does not include a 20-pixel
     * margin for drawing a possible scrollbar, and the requested height does not include a 20 pixel margin for drawing
     * the stat's title at the top.
     *
     * @param minWidth the desired width, may be 0
     * @param minHeight the desired height, may be 0
     */
    void setMinimumExpandedDimensions(int minWidth, int minHeight);

    /**
     * When this stat gets a parent stat assigned, the Y position of this stat will be auto-adjusted to be directly
     * beneath the parent stat. This will cause this stat to move up and down when the parent stat expands/moves.
     *
     * @param stat the parent stat
     */
    void setParentStat(IGuiAnimatedStat stat);

    /**
     * Change the background color of this stat.
     *
     * @param backgroundColor color, in ARGB format
     */
    void setBackgroundColor(int backgroundColor);

    /**
     * Set the foreground color of this stat, which is the color used to render any text which doesn't have explicit
     * formatting styles. The default foreground color is 0xFFFFFFFF, or white.
     *
     * @param foregroundColor the foreground color, in ARGB format
     */
    void setForegroundColor(int foregroundColor);

    /**
     * Set the title color of this stat, which is the color used to render the top title line of the stat. The default
     * title color is 0xFFFFFF00, or yellow.
     *
     * @param titleColor the title color, in ARGB format
     */
    void setTitleColor(int titleColor);

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
     * @param bevel true if a beveled edge should be drawn, false otherwise
     */
    void setBeveled(boolean bevel);

    /**
     * Sets the x location of this stat.
     *
     * @param x the X position
     */
    void setBaseX(int x);

    /**
     * Sets the base Y of this stat.
     *
     * @param y the Y position
     */
    void setBaseY(int y);

    /**
     * Returns the actual Y position of this stat. This is the same as getBaseY when there is no parent stat, but if
     * there is one, this method returns the value described in {@link #setParentStat(IGuiAnimatedStat)}.  This is the
     * position used to render the stat, and to define the area where keyboard and mouse input is checked for.
     *
     * @return the effective Y position
     */
    int getEffectiveY();

    int getBaseX();

    int getBaseY();

    /**
     * Returns the Y size of this stat.
     *
     * @return the stat's height
     */
    int getStatHeight();

    /**
     * Returns the X size of this stat.
     *
     * @return the stat's width
     */
    int getStatWidth();

    /**
     * Get a bounding box for this stat.
     *
     * @return a bounding box
     */
    Rectangle2d getBounds();

    /**
     * Should be called every render tick to render the stat.
     *
     * @param mouseX
     * @param mouseY
     * @param partialTicks
     */
    void renderStat(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

    /**
     * Forces the stat to close.
     */
    void closeStat();

    /**
     * Forces the stat to open.
     */
    void openStat();

    /**
     * Check if the stat is currently toggled open.
     *
     * @return true if the stat is open
     */
    boolean isStatOpen();

}
