package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiAdvancedAirCompressor extends GuiAirCompressor {
    private WidgetTemperature tempWidget;

    public GuiAdvancedAirCompressor(ContainerAirCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(tempWidget = new WidgetTemperature(guiLeft + 97, guiTop + 20, TemperatureRange.of(273, 673), 273, 50)
                .setOperatingRange(TemperatureRange.of(323, 625)).setShowOperatingRange(false));
    }

    @Override
    public void tick() {
        super.tick();

        te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).ifPresent(h -> tempWidget.setTemperature(h.getTemperatureAsInt()));
        tempWidget.autoScaleForTemperature();
    }

    @Override
    protected int getFuelSlotXOffset() {
        return 69;
    }

    @Override
    public void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }

    @Override
    protected PointXY getGaugeLocation() {
        return super.getGaugeLocation().add(10, 0);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ADVANCED_AIR_COMPRESSOR;
    }
}
