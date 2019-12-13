package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.List;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;

public class GuiPressureChamberInterface extends GuiPneumaticContainerBase<ContainerPressureChamberInterface,TileEntityPressureChamberInterface> {
    private WidgetAnimatedStat statusStat;
    private WidgetButtonExtended exportAnyButton;
    private WidgetLabel exportTypeLabel;

    private boolean hasEnoughPressure = true;

    public GuiPressureChamberInterface(ContainerPressureChamberInterface container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat("gui.pressureChamberInterface.status", new ItemStack(ModBlocks.PRESSURE_CHAMBER_INTERFACE), 0xFFFFAA00, false);

        exportAnyButton = addButton(new WidgetButtonExtended(guiLeft + 111, guiTop + 32, 60, 20, "")
                .withTag("export_mode"));
        exportTypeLabel = addButton(new WidgetLabel(guiLeft + 111, guiTop + 20, I18n.format("gui.pressureChamberInterface.exportLabel")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString("Upgr.", 24, 16, 4210752);

        int inputShift = (int) ((1F - (float) Math.cos(te.inputProgress / (float) MAX_PROGRESS * Math.PI)) * 11);
        int outputShift = (int) ((1F - (float) Math.cos(te.outputProgress / (float) MAX_PROGRESS * Math.PI)) * 11);
        fill(63 + inputShift, 30, 87 + inputShift, 32, 0xFF5A62FF);
        fill(63 + outputShift, 54, 87 + outputShift, 56, 0xFFFFA800);

    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -2);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PRESSURE_CHAMBER_INTERFACE_LOCATION;
    }

    @Override
    public void tick() {
        super.tick();

        boolean exporting = te.interfaceMode == TileEntityPressureChamberInterface.InterfaceDirection.EXPORT;
        exportAnyButton.setVisible(exporting);
        exportAnyButton.visible = exporting;
        exportTypeLabel.visible = exporting;
        if (exportAnyButton.visible) {
            String textKey = "gui.pressureChamberInterface.export." + (te.exportAny ? "any" : "valid");
            exportAnyButton.setMessage(I18n.format(textKey));
            exportAnyButton.setTooltipText(I18n.format(textKey + ".tooltip"));
        }

        statusStat.setText(ImmutableList.of(
                TextFormatting.WHITE + I18n.format("gui.pressureChamberInterface.mode"),
                TextFormatting.BLACK + I18n.format(te.interfaceMode.getTranslationKey())
        ));

        if (hasEnoughPressure && !te.hasEnoughPressure()) {
            hasEnoughPressure = false;
            problemTab.openWindow();
        } else if (te.hasEnoughPressure()) {
            hasEnoughPressure = true;
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        curInfo.addAll(te.getProblemStat());
    }
}
