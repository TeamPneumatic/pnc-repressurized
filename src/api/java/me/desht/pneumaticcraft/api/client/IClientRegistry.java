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

import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;

public interface IClientRegistry {
    /**
     * Creates a GuiAnimatedStat without icon.
     *
     * @param gui the owning GUI screen
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     */
    IGuiAnimatedStat getAnimatedStat(Screen gui, int backgroundColor);

    /**
     * Creates a GuiAnimatedStat which uses an itemstack as static icon.
     *
     * @param gui the owning GUI screen
     * @param iconStack an itemstack to use as the stat widget's icon
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     */
    IGuiAnimatedStat getAnimatedStat(Screen gui, ItemStack iconStack, int backgroundColor);

    /**
     * Creates a GuiAnimatedStat which uses a texture location as static icon.
     *
     * @param gui the owning GUI screen
     * @param iconTexture a texture to use as the stat widget's icon
     * @param backgroundColor background color for the stat in ARGB format
     * @return a new stat widget
     */
    IGuiAnimatedStat getAnimatedStat(Screen gui, ResourceLocation iconTexture, int backgroundColor);

    /**
     * Draw a Pressure Gauge in GUI context, the same as in many PneumaticCraft machine GUI's.
     *
     * @param graphics           the GUI draw context
     * @param fontRenderer       fontrenderer used to draw the numbers of the pressure gauge.
     * @param minPressure        The minimal pressure that needs to be displayed (this is -1 in most applications).
     * @param maxPressure        The maximal pressure that needs to be rendered (see {@link me.desht.pneumaticcraft.api.pressure.PressureTier} for standard pressure thresholds)
     * @param dangerPressure     The transition pressure from green to red (see {@link me.desht.pneumaticcraft.api.pressure.PressureTier} for standard pressure thresholds)
     * @param minWorkingPressure The transition pressure from yellow to green (varies per machine).
     * @param currentPressure    The pressure that the needle should point to.
     * @param xPos               x position of the gauge.
     * @param yPos               y position of the gauge.
     */
    void drawPressureGauge(GuiGraphics graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos);

    /**
     * Register some custom item rendering behaviour for an item when held in an Assembly machine.
     *
     * @param entry an item or block
     * @param renderOverride customized rendering behaviour for that item
     */
    void registerRenderOverride(@Nonnull ItemLike entry, @Nonnull IAssemblyRenderOverriding renderOverride);

    /**
     * Register some custom item rendering behaviour for an item when held in a Charging Station.
     *
     * @param entry an item or block
     * @param renderOverride customized rendering behaviour for that item
     */
    void registerRenderOverride(@Nonnull ItemLike entry, @Nonnull IChargingStationRenderOverride renderOverride);
}
