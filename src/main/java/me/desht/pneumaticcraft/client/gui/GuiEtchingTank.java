package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerEtchingTank;
import me.desht.pneumaticcraft.common.tileentity.TileEntityEtchingTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiEtchingTank extends GuiPneumaticContainerBase<ContainerEtchingTank, TileEntityEtchingTank> {
    private WidgetTemperature tempWidget;

    public GuiEtchingTank(ContainerEtchingTank container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 206;
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 149, guiTop + 18, te.getAcidTank()));

        addButton(tempWidget = new WidgetTemperature(guiLeft + 134, guiTop + 18, TemperatureRange.of(273, 773), 323, 50) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
                super.addTooltip(mouseX, mouseY, curTip, shift);

                int interval = te.getTickInterval();
                int processTimeSecs = interval * 5;
                curTip.add(TextFormatting.GREEN + I18n.format("pneumaticcraft.gui.tooltip.etching_tank.process_time", processTimeSecs));
                if (getTemperature() > 323) {
                    float usage = (30 - interval) / (5f * interval);
                    curTip.add(TextFormatting.YELLOW + I18n.format("pneumaticcraft.gui.tooltip.etching_tank.acid_usage", PneumaticCraftUtils.roundNumberTo(usage, 2)));
                }
            }
        });
    }

    @Override
    public void tick() {
        super.tick();

        te.getHeatCap(null).ifPresent(l -> tempWidget.setTemperature(l.getTemperatureAsInt()));
        tempWidget.autoScaleForTemperature();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ETCHING_TANK;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (te.isOutputFull()) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.etching_tank.output_full"));
        }
        if (te.isFailedOutputFull()) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.etching_tank.failed_full"));
        }
        if (te.getAcidTank().getFluid().isEmpty()) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.etching_tank.no_acid"));
        }
    }
}
