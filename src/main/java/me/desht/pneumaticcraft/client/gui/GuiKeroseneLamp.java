package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.inventory.ContainerKeroseneLamp;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiKeroseneLamp extends GuiPneumaticContainerBase<ContainerKeroseneLamp,TileEntityKeroseneLamp> {
    private WidgetLabel rangeLabel;
    private WidgetTextFieldNumber rangeWidget;
    private int sendDelay = -1;

    public GuiKeroseneLamp(ContainerKeroseneLamp container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(guiLeft + 152, guiTop + 15, te.getTank()));
        addButton(rangeLabel = new WidgetLabel(guiLeft + 5, guiTop + 38, ""));
        //  addWidget(timeLeftWidget = new WidgetLabel(guiLeft + 5, guiTop + 26, ""));
        String maxRange = I18n.format("gui.keroseneLamp.maxRange");
        int maxRangeLength = font.getStringWidth(maxRange);
        addLabel(maxRange, guiLeft + 5, guiTop + 50);
        addLabel(I18n.format("gui.keroseneLamp.blocks"), guiLeft + maxRangeLength + 40, guiTop + 50);
        addButton(rangeWidget = new WidgetTextFieldNumber(font, guiLeft + 7 + maxRangeLength, guiTop + 50, 30, font.FONT_HEIGHT));
        rangeWidget.minValue = 1;
        rangeWidget.maxValue = TileEntityKeroseneLamp.MAX_RANGE;
        rangeWidget.setValue(te.getTargetRange());
        rangeWidget.func_212954_a(s -> sendDelay = 5);
    }

    @Override
    public void tick() {
        super.tick();
        if (sendDelay > 0 && --sendDelay == 0) {
            sendPacketToServer(rangeWidget.getText());
            sendDelay = -1;
        }
        rangeLabel.setMessage(I18n.format("gui.keroseneLamp.currentRange", te.getRange()));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_KEROSENE_LAMP;
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
}
