/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * This interface doesn't have to be implemented. In PneumaticCraft there already is a widget which implements this
 * interface used in many places: GUI side tabs, Pneumatic Helmet 2D and 3D stats. You can get an instance of this
 * class via the various {@link IClientRegistry} getAnimatedStat() methods.
 */
public interface IGuiAnimatedStat extends ITickableWidget, GuiEventListener {
    /**
     * Check which direction this expands to when opened.
     *
     * @return true if the stat expands to the left (the right edge of the state is anchored to the X position),
     *         false if the stat expands to the right
     */
    boolean isLeftSided();

    /**
     * Set the direction this stat will expand in when opened.
     *
     * @param leftSided true if the stat should expand to the left, false if the stat should expand to the right
     */
    void setLeftSided(boolean leftSided);

    /**
     * Check if this stat is fully opened.
     *
     * @return true if the stat is done with expanding (when text and subwidgets will be displayed)
     */
    boolean isDoneExpanding();

    /**
     * Sets the main text of this stat. Every line should be stored in a separate list element, but lines do not need
     * to be split manually; overlong lines will be automatically by wrapped to fit horizontally (if auto-wrap is
     * enabled), and a scrollbar will be added if necessary.
     *
     * @param text a list of text components
     * @return this, so you can chain calls.
     */
    IGuiAnimatedStat setText(List<Component> text);

    /**
     * Sets the main text of this stat. Every line should be stored in a separate list element, but lines do not need
     * to be split manually; overlong lines will be automatically by wrapped to fit horizontally (if auto-wrap is
     * enabled), and a scrollbar will be added if necessary.
     *
     * @param text a text component
     * @return this, so you can chain calls.
     */
    IGuiAnimatedStat setText(Component text);

    /**
     * Appends some more text to the existing text in this stat.  This method will split overlong lines, same as
     * {@link #setText(Component)}
     *
     * @param text a list of text components
     */
    void appendText(List<Component> text);

    /**
     * Defines what dimensions the stat should have when it is not expanded (default 17x17, sufficient to display the
     * stat's icon) and resets the stat to these dimensions. Stats which should disappear completely when closed
     * (e.g. the Pneumatic Armor HUD stats) should be given a minimum size of 0x0.
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
     * When this stat gets a parent stat assigned, the effective Y position of this stat should be auto-adjusted to be
     * directly beneath the parent stat. This will cause this stat to move up and down when the parent stat
     * expands/moves.
     *
     * @param stat the parent stat
     */
    void setParentStat(IGuiAnimatedStat stat);

    /**
     * Set the background color of this stat.
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
     * Sets the x position of this stat.
     *
     * @param x the X position
     */
    void setBaseX(int x);

    /**
     * Sets the base Y position of this stat (see {@link #setParentStat(IGuiAnimatedStat)}.
     *
     * @param y the Y position
     */
    void setBaseY(int y);

    /**
     * Get the effective Y position of this stat. This is the same as getBaseY when there is no parent stat, but if
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
    Rect2i getBounds();

    /**
     * Render the stat in 2D (gui) context.
     *
     * @param graphics     the gui graphics context
     * @param mouseX       the mouse X position
     * @param mouseY       the mouse Y position
     * @param partialTicks partial ticks since last client tick
     */
    void renderStat(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    /**
     * Render the stat in 3D (in-world) context.
     *
     * @param matrixStack the matrix stack
     * @param buffer the render buffer
     * @param partialTicks partial ticks since last client tick
     */
    void renderStat(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks);

    /**
     * Forces the stat to close.
     */
    void closeStat();

    /**
     * Forces the stat to open.
     */
    void openStat();

    /**
     * Check if the stat is currently toggled open (but not necessarily fully-open - see {@link #isDoneExpanding()})
     *
     * @return true if the stat is open
     */
    boolean isStatOpen();

    /**
     * Enable/disable auto-line-wrapping functionality of the widget. This is enabled by default. When disabled, the
     * widget will make no effort to keep all text on screen; unwrapped text could extend off the right-hand edge of
     * the screen.
     *
     * @param wrap enablement of wrapping
     */
    void setAutoLineWrap(boolean wrap);

    /**
     * Get this stat's title line.
     *
     * @return the title
     */
    Component getTitle();

    /**
     * Set the title line for this stat; the text drawn on the top line. This text is never wrapped, so be mindful
     * of the length of this line.
     * @param title the title string
     */
    void setTitle(Component title);

    /**
     * This can be used to reserve one or more lines at the top of the stat; text will only drawn below the reserved
     * area.  Useful if you want to have a static area for widget display.
     *
     * @param reservedLines number of text lines to reserve
     */
    void setReservedLines(int reservedLines);

    /**
     * Set the texture to use for the stat's icon
     * @param texture resource location of a texture image, which should be 16x16 exactly
     */
    void setTexture(ResourceLocation texture);

    /**
     * Set the texture to use for the stat's icon
     * @param itemStack an item to use for the texture
     */
    void setTexture(ItemStack itemStack);

    /**
     * Set the line spacing, in pixels
     * @param spacing the line spacing
     */
    void setLineSpacing(int spacing);

    /**
     * Add a subwidget to the panel. Subwidgets are automatically rendered by the panel itself, and don't need to be
     * added to your GUI separately.
     * @param widget the subwidget
     */
    void addSubWidget(AbstractWidget widget);

    /**
     * Define X offsets for subwidget rendering. You should not normally need to call this method.
     *
     * @param left X offset when widget opens to the left
     * @param right X offset when widget opens to the right
     */
    void setSubwidgetRenderOffsets(int left, int right);
}
