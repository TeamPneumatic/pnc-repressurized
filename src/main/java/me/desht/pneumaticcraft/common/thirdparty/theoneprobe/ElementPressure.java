package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.theoneprobe.api.IElement;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.tileentity.IMinWorkingPressure;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class ElementPressure implements IElement {
    private final float min;
    private final float pressure;
    private final float danger;
    private final float crit;

    private static final float SCALE = 0.7f;

    ElementPressure(TileEntity te, IAirHandlerMachine airHandler) {
        min = te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : 0;
        pressure = airHandler.getPressure();
        danger = airHandler.getDangerPressure();
        crit = airHandler.getCriticalPressure();
    }

    ElementPressure(PacketBuffer buf) {
        min = buf.readFloat();
        pressure = buf.readFloat();
        danger = buf.readFloat();
        crit = buf.readFloat();
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        matrixStack.pushPose();
        matrixStack.scale(SCALE, SCALE, SCALE);
        int x1 = (int)((x + getWidth() / 2) / SCALE);
        int y1 = (int)((y + getHeight() / 2) / SCALE);
        PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().font, -1, crit, danger, min, pressure, x1, y1,0xFFC0C0C0);
        matrixStack.popPose();
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
    public void toBytes(PacketBuffer buf) {
        buf.writeFloat(min);
        buf.writeFloat(pressure);
        buf.writeFloat(danger);
        buf.writeFloat(crit);
    }

    @Override
    public int getID() {
        return TOPInit.elementPressure;
    }
}
