package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.inventory.ContainerKeroseneLamp;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class GuiKeroseneLamp extends GuiPneumaticContainerBase<TileEntityKeroseneLamp> {
    private WidgetLabel rangeTextWidget;
    //   private WidgetLabel timeLeftWidget;
    private WidgetTextFieldNumber rangeWidget;

    public GuiKeroseneLamp(InventoryPlayer player, TileEntityKeroseneLamp te) {
        super(new ContainerKeroseneLamp(player, te), te, Textures.GUI_KEROSENE_LAMP);
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTank(-1, guiLeft + 152, guiTop + 15, te.getTank()));
        addWidget(rangeTextWidget = new WidgetLabel(guiLeft + 5, guiTop + 38, ""));
        //  addWidget(timeLeftWidget = new WidgetLabel(guiLeft + 5, guiTop + 26, ""));
        String maxRange = I18n.format("gui.keroseneLamp.maxRange");
        int maxRangeLength = fontRenderer.getStringWidth(maxRange);
        addLabel(maxRange, guiLeft + 5, guiTop + 50);
        addLabel(I18n.format("gui.keroseneLamp.blocks"), guiLeft + maxRangeLength + 40, guiTop + 50);
        addWidget(rangeWidget = new WidgetTextFieldNumber(fontRenderer, guiLeft + 7 + maxRangeLength, guiTop + 50, 30, fontRenderer.FONT_HEIGHT));
        rangeWidget.minValue = 1;
        rangeWidget.maxValue = TileEntityKeroseneLamp.MAX_RANGE;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!rangeWidget.isFocused()) {
            rangeWidget.setValue(te.getTargetRange());
        }
        rangeTextWidget.text = I18n.format("gui.keroseneLamp.currentRange", te.getRange());

        /* if(te.getRange() > 0) {
             int ticksLeft = (int)(te.getTank().getFluidAmount() * TileEntityKeroseneLamp.FUEL_PER_MB * 5 / Math.pow(te.getRange(), 3));
             timeLeftWidget.text = I18n.format("gui.keroseneLamp.timeLeft", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(ticksLeft, false));
         } else {
             timeLeftWidget.text = "";
         }*/
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!rangeWidget.isFocused()) {
            sendPacketToServer(rangeWidget.getValue());
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getTank().getFluidAmount() == 0) {
            curInfo.add("gui.tab.problems.keroseneLamp.noFuel");
        } else if (te.getFuelQuality() == 0) {
            curInfo.add("gui.tab.problems.keroseneLamp.badFuel");
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (te.getTank().getFluidAmount() < 30 && te.getTank().getFluidAmount() > 0) {
            curInfo.add("gui.tab.problems.keroseneLamp.lowFuel");
        }
    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        if ((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_TAB) && rangeWidget.isFocused()) {
            sendPacketToServer(rangeWidget.getValue());
        } else {
            super.keyTyped(key, keyCode);
        }
    }
}
