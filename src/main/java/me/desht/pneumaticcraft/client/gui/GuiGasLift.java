package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerGasLift;
import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiGasLift extends GuiPneumaticContainerBase<TileEntityGasLift> {
    private GuiAnimatedStat statusStat;
    private final GuiButtonSpecial[] modeButtons = new GuiButtonSpecial[3];

    public GuiGasLift(InventoryPlayer player, TileEntityGasLift te) {
        super(new ContainerGasLift(player, te), te, Textures.GUI_GAS_LIFT);
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTank(-1, guiLeft + 80, guiTop + 15, te.getTank()));
        statusStat = addAnimatedStat("gui.tab.status", new ItemStack(Blockss.GAS_LIFT), 0xFFFFAA00, false);

        GuiAnimatedStat optionStat = addAnimatedStat("gui.tab.gasLift.mode", new ItemStack(Blockss.PRESSURE_TUBE), 0xFFFFCC00, false);
        List<String> text = new ArrayList<String>();
        for (int i = 0; i < 4; i++)
            text.add("                  ");
        optionStat.setTextWithoutCuttingString(text);

        GuiButtonSpecial button = new GuiButtonSpecial(1, 5, 20, 20, 20, "");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(I18n.format("gui.tab.gasLift.mode.pumpEmpty"));
        optionStat.addWidget(button);
        modeButtons[0] = button;

        button = new GuiButtonSpecial(2, 30, 20, 20, 20, "");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(I18n.format("gui.tab.gasLift.mode.pumpLeave"));
        optionStat.addWidget(button);
        modeButtons[1] = button;

        button = new GuiButtonSpecial(3, 55, 20, 20, 20, "");
        button.setRenderStacks(new ItemStack(Blockss.PRESSURE_TUBE));
        button.setTooltipText(I18n.format("gui.tab.gasLift.mode.drawIn"));
        optionStat.addWidget(button);
        modeButtons[2] = button;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {

        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 17, 19, 4210752);
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -1);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatus());
        for (int i = 0; i < modeButtons.length; i++) {
            modeButtons[i].enabled = te.mode != i;
        }
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<String>();
        textList.add(I18n.format("gui.tab.status.gasLift.action"));
        String status = "gui.tab.status.gasLift.action.";
        switch (te.status) {
            case IDLE:
                status += "idling";
                break;
            case PUMPING:
                status += "pumping";
                break;
            case DIGGING:
                status += "diggingDown";
                break;
            case RETRACTING:
                status += "retracting";
        }
        textList.add(I18n.format(status, te.getTank().getFluid() != null ? te.getTank().getFluid().getLocalizedName() : ""));
        textList.add(I18n.format("gui.tab.status.gasLift.currentDepth", te.currentDepth));
        return textList;
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.mode == 0 || te.mode == 1) {
            if (te.getTank().getCapacity() - te.getTank().getFluidAmount() < 1000) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.noLiquidSpace"));
            }
            if (te.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.noTubes"));
            }
        } else {
            if (te.getPrimaryInventory().getStackInSlot(0).getCount() == 64) {
                curInfo.add(I18n.format("gui.tab.problems.gasLift.noTubeSpace"));
            }
        }
    }
}
