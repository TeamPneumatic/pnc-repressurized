package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerCreativeCompressedIronBlock;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressedIronBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiCreativeCompressedIronBlock extends GuiPneumaticContainerBase<ContainerCreativeCompressedIronBlock, TileEntityCreativeCompressedIronBlock> {
    private WidgetButtonExtended down2, down1, up1, up2;

    public GuiCreativeCompressedIronBlock(ContainerCreativeCompressedIronBlock container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        int y = height / 2 - 5;
        int x = width / 2;
        down2 = addButton(new WidgetButtonExtended(x - 90, y, 30, 20, "-10").withTag("-10"));
        down1 = addButton(new WidgetButtonExtended(x - 58, y, 30, 20, "-1").withTag("-1"));
        up1 = addButton(new WidgetButtonExtended(x + 28, y, 30, 20, "+1").withTag("+1"));
        up2 = addButton(new WidgetButtonExtended(x + 60, y, 30, 20, "+10").withTag("+10"));
    }

    @Override
    public void tick() {
        super.tick();

        setText(down2, "-100", "-10");
        setText(down1, "-10", "-1");
        setText(up1, "+10", "+1");
        setText(up2, "+100", "+10");
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return null;
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
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
    protected int getTitleColor() {
        return 0xff00ff;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        super.renderLabels(matrixStack, x, y);
        ITextComponent txt = HeatUtil.formatHeatString(te.targetTemperature);
        drawCenteredString(matrixStack, font, txt, width / 2 - leftPos, height / 2 - topPos - 20, 0xFFFFFF);
        drawCenteredString(matrixStack, font, xlate("pneumaticcraft.gui.misc.holdShiftFastAdjust"), width / 2 - leftPos, height / 2 - topPos + 20, 0x808080);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int i, int j){
        renderBackground(matrixStack);
        super.renderBg(matrixStack, partialTicks, i, j);
    }

    private void setText(WidgetButtonExtended b, String txt1, String txt2) {
        b.setMessage(ClientUtils.hasShiftDown() ? new StringTextComponent(txt1) : new StringTextComponent(txt2));
    }
}
