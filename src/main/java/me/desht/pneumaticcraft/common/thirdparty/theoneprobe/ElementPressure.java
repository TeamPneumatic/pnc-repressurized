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

package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.block.entity.IMinWorkingPressure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ElementPressure implements IElement {
    private final float min;
    private final float pressure;
    private final float danger;
    private final float crit;

    private static final float SCALE = 0.7f;

    ElementPressure(BlockEntity te, IAirHandlerMachine airHandler) {
        min = te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : 0;
        pressure = airHandler.getPressure();
        danger = airHandler.getDangerPressure();
        crit = airHandler.getCriticalPressure();
    }

    ElementPressure(FriendlyByteBuf buf) {
        min = buf.readFloat();
        pressure = buf.readFloat();
        danger = buf.readFloat();
        crit = buf.readFloat();
    }

    @Override
    public void render(GuiGraphics matrixStack, int x, int y) {
        matrixStack.pose().pushPose();
        matrixStack.pose().scale(SCALE, SCALE, SCALE);
        int x1 = (int)((x + getWidth() / 2f) / SCALE);
        int y1 = (int)((y + getHeight() / 2f) / SCALE);
        PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().font, -1, crit, danger, min, pressure, x1, y1,0xFFC0C0C0);
        matrixStack.pose().popPose();
    }

    @Override
    public int getWidth() {
        return 40;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(min);
        buf.writeFloat(pressure);
        buf.writeFloat(danger);
        buf.writeFloat(crit);
    }

    @Override
    public ResourceLocation getID() {
        return TOPInit.ELEMENT_PRESSURE;
    }

    public static class Factory implements IElementFactory {
        @Override
        public IElement createElement(RegistryFriendlyByteBuf friendlyByteBuf) {
            return new ElementPressure(friendlyByteBuf);
        }

        @Override
        public ResourceLocation getId() {
            return TOPInit.ELEMENT_PRESSURE;
        }
    }
}
