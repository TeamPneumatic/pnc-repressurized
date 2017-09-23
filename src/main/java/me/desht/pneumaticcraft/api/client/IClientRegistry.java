package me.desht.pneumaticcraft.api.client;

import me.desht.pneumaticcraft.api.client.assemblymachine.IAssemblyRenderOverriding;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IClientRegistry {
    /**
     * Returns a GuiAnimatedStat without icon.
     *
     * @param gui
     * @param backgroundColor
     * @return
     */
    IGuiAnimatedStat getAnimatedStat(GuiScreen gui, int backgroundColor);

    /**
     * Returns a GuiAnimatedStat which uses an itemstack as static icon.
     *
     * @param gui
     * @param iconStack
     * @param backgroundColor
     * @return
     */
    IGuiAnimatedStat getAnimatedStat(GuiScreen gui, ItemStack iconStack, int backgroundColor);

    /**
     * Returns a GuiAnimatedStat which uses a texture location as static icon.
     *
     * @param gui
     * @param iconTexture     / text
     * @param backgroundColor
     * @return
     */
    IGuiAnimatedStat getAnimatedStat(GuiScreen gui, String iconTexture, int backgroundColor);

    /**
     * Draws a Pressure Gauge, the same which is also used in many PneumaticCraft applications.
     *
     * @param fontRenderer       fontrenderer used to draw the numbers of the pressure gauge.
     * @param minPressure        The minimal pressure that needs to be displayed (this is -1 in most applications).
     * @param maxPressure        The maximal pressure that needs to be rendererd (this is 7 for tier one machines, and 25 for tier two).
     * @param dangerPressure     The transition pressure from green to red (this is 5 for tier one, and 29 for tier two machines).
     * @param minWorkingPressure The transition pressure from yellow to green (variates per machine).
     * @param currentPressure    The pressure that the needle should point to.
     * @param xPos               x position of the gauge.
     * @param yPos               y position of the gauge.
     * @param zLevel             z position of the gauge (Gui#zLevel, -90, for in normal GUI's).
     */
    void drawPressureGauge(FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, float zLevel);

    void registerRenderOverride(Block block, IAssemblyRenderOverriding renderOverride);

    void registerRenderOverride(Item item, IAssemblyRenderOverriding renderOverride);
}
