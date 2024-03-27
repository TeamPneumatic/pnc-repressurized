package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractCreativeAdjusterScreen<C extends AbstractPneumaticCraftMenu<T>,T extends AbstractPneumaticCraftBlockEntity> extends AbstractPneumaticCraftContainerScreen<C,T> {
    private WidgetButtonExtended down2, down1, up1, up2;

    public AbstractCreativeAdjusterScreen(C container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        int y = height / 2 - 5;
        int x = width / 2;

        float small = getAdjustments().small();
        float big = getAdjustments().big();

        down2 = addRenderableWidget(new WidgetButtonExtended(x - 90, y, 30, 20, "").withTag(() -> makeTag(-big)));
        down1 = addRenderableWidget(new WidgetButtonExtended(x - 58, y, 30, 20, "").withTag(() -> makeTag(-small)));
        up1 = addRenderableWidget(new WidgetButtonExtended(x + 28, y, 30, 20, "").withTag(() -> makeTag(small)));
        up2 = addRenderableWidget(new WidgetButtonExtended(x + 60, y, 30, 20, "").withTag(() -> makeTag(big)));
    }

    @Override
    public void containerTick() {
        super.containerTick();

        float small = getAdjustments().small();
        float small2 = small * getShiftMultiplier();
        float big = getAdjustments().big();
        float big2 = big * getShiftMultiplier();

        setText(down2, formatAdjustment(-big), formatAdjustment(-big2));
        setText(down1, formatAdjustment(-small), formatAdjustment(-small2));
        setText(up1, formatAdjustment(small), formatAdjustment(small2));
        setText(up2, formatAdjustment(big), formatAdjustment(big2));
    }

    protected abstract float getShiftMultiplier();

    protected abstract Component formatStringDesc();

    protected abstract Adjustments getAdjustments();

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
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);

        graphics.drawCenteredString(font, formatStringDesc(), width / 2 - leftPos, height / 2 - topPos, 0xFFFFFF);
        graphics.drawCenteredString(font, xlate("pneumaticcraft.gui.misc.holdShiftFastAdjust"), width / 2 - leftPos, height / 2 - topPos + 20, 0x808080);
    }

//    @Override
//    protected void renderBg(GuiGraphics graphics, float partialTicks, int i, int j){
//        super.renderBg(graphics, partialTicks, i, j);
//    }

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

    private void setText(WidgetButtonExtended b, String unshifted, String shifted) {
        b.setMessage(Component.literal(ClientUtils.hasShiftDown() ? shifted : unshifted));
    }

    protected String formatAdjustment(float adj) {
        return String.format("%+.1f", adj);
    }

    private String makeTag(float f) {
        return formatAdjustment(hasShiftDown() ? f * getShiftMultiplier() : f);
    }

    record Adjustments(float small, float big) {
    }
}
