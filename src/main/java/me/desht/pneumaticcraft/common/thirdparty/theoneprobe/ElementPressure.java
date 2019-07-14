package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.tileentity.IMinWorkingPressure;
import net.minecraft.client.Minecraft;

public class ElementPressure implements IElement {
    private final float min;
    private final float pressure;
    private final float danger;
    private final float crit;

    private static final float SCALE = 0.7f;

    ElementPressure(IPneumaticMachine te) {
        min = te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : 0;
        IAirHandler airHandler = te.getAirHandler(null);
        pressure = airHandler.getPressure();
        danger = airHandler.getDangerPressure();
        crit = airHandler.getCriticalPressure();
    }

    ElementPressure(ByteBuf byteBuf) {
        min = byteBuf.readFloat();
        pressure = byteBuf.readFloat();
        danger = byteBuf.readFloat();
        crit = byteBuf.readFloat();
    }

    @Override
    public void render(int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.scaled(SCALE, SCALE, SCALE);
        int x1 = (int)((x + getWidth() / 2) / SCALE);
        int y1 = (int)((y + getHeight() / 2) / SCALE);
        GuiUtils.drawPressureGauge(Minecraft.getInstance().fontRenderer, -1, crit, danger, min, pressure, x1, y1,0xFFC0C0C0);
        GlStateManager.popMatrix();
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
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(min);
        buf.writeFloat(pressure);
        buf.writeFloat(danger);
        buf.writeFloat(crit);
    }

    @Override
    public int getID() {
        return TOPCallback.elementPressure;
    }
}
