/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.client.IChargingStationRenderOverride;
import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistryImpl implements IClientRegistry {

    private static final ClientRegistryImpl INSTANCE = new ClientRegistryImpl();
    private static final Map<Item, IAssemblyRenderOverriding> renderOverrides = new ConcurrentHashMap<>();
    private static final Map<Item, IChargingStationRenderOverride> chargingRenderOverrides = new ConcurrentHashMap<>();
    private static final IChargingStationRenderOverride DEFAULT_CHARGING_RENDERER = (poseStack, renderedStack, partialTicks, bufferIn, combinedLightIn, combinedOverlayIn) -> true;

    private ClientRegistryImpl() {}

    public static ClientRegistryImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(Screen gui, int backgroundColor) {
        return new WidgetAnimatedStat(gui, backgroundColor);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(Screen gui, ItemStack iconStack, int backgroundColor) {
        return new WidgetAnimatedStat(gui, backgroundColor, iconStack);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(Screen gui, ResourceLocation iconTexture, int backgroundColor) {
        return new WidgetAnimatedStat(gui, backgroundColor, iconTexture);
    }

    @Override
    public void drawPressureGauge(GuiGraphics graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        PressureGaugeRenderer2D.drawPressureGauge(graphics, fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0x00000000);
    }

    @Override
    public void registerRenderOverride(@Nonnull ItemLike entry, @Nonnull IAssemblyRenderOverriding renderOverride) {
        renderOverrides.put(entry.asItem(), renderOverride);
    }

    public IAssemblyRenderOverriding getAssemblyRenderOverride(ItemLike entry) {
        return renderOverrides.get(entry.asItem());
    }

    @Override
    public void registerRenderOverride(@NotNull ItemLike entry, @NotNull IChargingStationRenderOverride renderOverride) {
        chargingRenderOverrides.put(entry.asItem(), renderOverride);
    }

    @Nonnull
    public IChargingStationRenderOverride getChargingRenderOverride(ItemLike item) {
        return chargingRenderOverrides.getOrDefault(item.asItem(), DEFAULT_CHARGING_RENDERER);
    }
}
