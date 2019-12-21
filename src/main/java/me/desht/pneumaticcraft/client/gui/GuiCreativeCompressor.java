package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerCreativeCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiCreativeCompressor extends GuiPneumaticContainerBase<ContainerCreativeCompressor,TileEntityCreativeCompressor> {

    public GuiCreativeCompressor(ContainerCreativeCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        int y = height / 2 - 5;
        int x = width / 2;
        addButton(new WidgetButtonExtended(x - 90, y, 30, 20, "-1").withTag("-1"));
        addButton(new WidgetButtonExtended(x - 58, y, 30, 20, "-0.1").withTag("-0.1"));
        addButton(new WidgetButtonExtended(x + 28, y, 30, 20, "+0.1").withTag("+0.1"));
        addButton(new WidgetButtonExtended(x + 60, y, 30, 20, "+1").withTag("+1"));
    }

    @Override
    protected PointXY getGaugeLocation() {
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
    protected boolean shouldAddInfoTab(){
        return false;
    }
    
    @Override
    protected boolean shouldAddUpgradeTab(){
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        drawCenteredString(font, PneumaticCraftUtils.roundNumberTo(te.getPressure(), 1) + " bar", width / 2 - guiLeft, height / 2 - guiTop, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j){
        renderBackground();
        super.drawGuiContainerBackgroundLayer(partialTicks, i, j);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return null;  // no texture!
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
    protected PointXY getInvTextOffset() {
        return null;
    }
}
