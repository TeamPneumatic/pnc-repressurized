package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerGasLift;
import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift.PumpMode;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiGasLift extends GuiPneumaticContainerBase<ContainerGasLift,TileEntityGasLift> {
    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[PumpMode.values().length];

    public GuiGasLift(ContainerGasLift container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(guiLeft + 80, guiTop + 15, te.getTank()));
        statusStat = addAnimatedStat("gui.tab.status", new ItemStack(ModBlocks.GAS_LIFT), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat("gui.tab.gasLift.mode", new ItemStack(ModBlocks.PRESSURE_TUBE), 0xFFFFCC00, false);
        optionStat.addPadding(4, 17);

        WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, "").withTag(PumpMode.PUMP_EMPTY.toString());
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(I18n.format("gui.tab.gasLift.mode.pumpEmpty"));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(30, 20, 20, 20, "").withTag(PumpMode.PUMP_LEAVE_FLUID.toString());
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(I18n.format("gui.tab.gasLift.mode.pumpLeave"));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;

        button = new WidgetButtonExtended(55, 20, 20, 20, "").withTag(PumpMode.RETRACT.toString());
        button.setRenderStacks(new ItemStack(ModBlocks.PRESSURE_TUBE));
        button.setTooltipText(I18n.format("gui.tab.gasLift.mode.drawIn"));
        optionStat.addSubWidget(button);
        modeButtons[2] = button;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {

        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 17, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_GAS_LIFT;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -1);
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatus());
        for (int i = 0; i < modeButtons.length; i++) {
            modeButtons[i].active = te.pumpMode != PumpMode.values()[i];
        }
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<>();
        textList.add(I18n.format("gui.tab.status.gasLift.action"));
        String status = "gui.tab.status.gasLift.action." + te.status.desc;
        textList.add(I18n.format(status, te.getTank().getFluid() != null ? te.getTank().getFluid().getDisplayName().getFormattedText() : ""));
        textList.add(I18n.format("gui.tab.status.gasLift.currentDepth", te.currentDepth));
        return textList;
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.pumpMode == PumpMode.PUMP_EMPTY || te.pumpMode == PumpMode.PUMP_LEAVE_FLUID) {
            if (te.getTank().getCapacity() - te.getTank().getFluidAmount() < 1000) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.noLiquidSpace"));
            }
            if (te.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.noTubes"));
            }
            if (te.status == TileEntityGasLift.Status.STUCK) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.stuck"));
            }
        } else {
            if (te.getPrimaryInventory().getStackInSlot(0).getCount() == 64) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.noTubeSpace"));
            }
        }
    }
}
