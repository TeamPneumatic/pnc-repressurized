package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class GuiCreativeCompressor extends GuiPneumaticContainerBase<TileEntityCreativeCompressor> {

    public GuiCreativeCompressor(TileEntityCreativeCompressor te) {
        super(new ContainerPneumaticBase(te), te, null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = height / 2 - 5;
        int x = width / 2;
        buttonList.add(new GuiButton(0, x - 90, y, 30, 20, "-1"));
        buttonList.add(new GuiButton(1, x - 58, y, 30, 20, "-0.1"));
        buttonList.add(new GuiButton(2, x + 28, y, 30, 20, "+0.1"));
        buttonList.add(new GuiButton(3, x + 60, y, 30, 20, "+1"));
    }

    @Override
    protected Point getGaugeLocation() {
        return null;
    }

    @Override
    protected boolean shouldAddPressureTab() {
        return false;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        drawCenteredString(fontRenderer, PneumaticCraftUtils.roundNumberTo(te.getPressure(), 1) + " bar", width / 2, height / 2, 0xFFFFFF);
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected int getTitleColor() {
        return 0xff00ff;
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }
}
