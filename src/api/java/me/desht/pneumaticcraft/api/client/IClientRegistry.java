package me.desht.pneumaticcraft.api.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public interface IClientRegistry {
    /**
     * Returns a GuiAnimatedStat without icon.
     *
     * @param gui the owning GUI screen
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     */
    IGuiAnimatedStat getAnimatedStat(Screen gui, int backgroundColor);

    /**
     * Returns a GuiAnimatedStat which uses an itemstack as static icon.
     *
     * @param gui the owning GUI screen
     * @param iconStack an itemstack to use as the stat widget's icon
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     */
    IGuiAnimatedStat getAnimatedStat(Screen gui, ItemStack iconStack, int backgroundColor);

    /**
     * Returns a GuiAnimatedStat which uses a texture location as static icon. This texture will always be
     * namespaced with "pneumaticcraft:"
     *
     * @param gui the owning GUI screen
     * @param iconTexture name of a texture to use as the stat widget's icon
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     * @deprecated use {@link #getAnimatedStat(Screen, ResourceLocation, int)}
     */
    @Deprecated
    IGuiAnimatedStat getAnimatedStat(Screen gui, String iconTexture, int backgroundColor);

    /**
     * Returns a GuiAnimatedStat which uses a texture location as static icon.
     *
     * @param gui the owning GUI screen
     * @param iconTexture a texture to use as the stat widget's icon
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     */
    IGuiAnimatedStat getAnimatedStat(Screen gui, ResourceLocation iconTexture, int backgroundColor);

    /**
     * Draws a Pressure Gauge, the same which is also used in many PneumaticCraft applications. This should only be used
     * in GUI context, <strong>not</strong> for in-world rendering!
     *
     * @param matrixStack        the matrix stack
     * @param fontRenderer       fontrenderer used to draw the numbers of the pressure gauge.
     * @param minPressure        The minimal pressure that needs to be displayed (this is -1 in most applications).
     * @param maxPressure        The maximal pressure that needs to be rendered (this is 7 for tier one machines, and 25 for tier two).
     * @param dangerPressure     The transition pressure from green to red (this is 5 for tier one, and 20 for tier two machines).
     * @param minWorkingPressure The transition pressure from yellow to green (varies per machine).
     * @param currentPressure    The pressure that the needle should point to.
     * @param xPos               x position of the gauge.
     * @param yPos               y position of the gauge.
     */
    void drawPressureGauge(MatrixStack matrixStack, FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos);

    void registerRenderOverride(@Nonnull IForgeRegistryEntry<?> entry, @Nonnull IAssemblyRenderOverriding renderOverride);
}
