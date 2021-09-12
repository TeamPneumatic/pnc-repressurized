package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerEtchingTank;
import me.desht.pneumaticcraft.common.tileentity.TileEntityEtchingTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiEtchingTank extends GuiPneumaticContainerBase<ContainerEtchingTank, TileEntityEtchingTank> {
    private WidgetTemperature tempWidget;

    public GuiEtchingTank(ContainerEtchingTank container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        imageHeight = 206;
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(leftPos + 149, topPos + 18, te.getAcidTank()));

        addButton(tempWidget = new WidgetTemperature(leftPos + 134, topPos + 18, TemperatureRange.of(273, 773), 323, 50) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
                super.addTooltip(mouseX, mouseY, curTip, shift);

                int interval = te.getTickInterval();
                int processTimeSecs = interval * 5;
                curTip.add(xlate("pneumaticcraft.gui.tooltip.etching_tank.process_time", processTimeSecs).withStyle(TextFormatting.GREEN));
                if (getTemperature() > 323) {
                    float usage = (30 - interval) / (5f * interval);
                    curTip.add(xlate("pneumaticcraft.gui.tooltip.etching_tank.acid_usage", PneumaticCraftUtils.roundNumberTo(usage, 2)).withStyle(TextFormatting.YELLOW));
                }
            }
        });
    }

    @Override
    public void tick() {
        super.tick();

        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ETCHING_TANK;
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.isOutputFull()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.etching_tank.output_full"));
        }
        if (te.isFailedOutputFull()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.etching_tank.failed_full"));
        }
        if (te.getAcidTank().getFluid().isEmpty()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.etching_tank.no_acid"));
        }
    }
}
