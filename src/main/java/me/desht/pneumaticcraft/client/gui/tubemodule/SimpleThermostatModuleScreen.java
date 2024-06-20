/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetColorSelector;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncThermostatModuleToServer;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SimpleThermostatModuleScreen extends AbstractTubeModuleScreen<ThermostatModule> {
    private int color;
    private int threshold;
    private WidgetTextFieldNumber thresholdField;

    public SimpleThermostatModuleScreen(ThermostatModule module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        color = module.getColorChannel();
        threshold = module.getThreshold();

        addLabel(getTitle(), guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);

        int x = guiLeft + 10;
        int y = guiTop + 24;
        WidgetLabel colorLabel;
        addRenderableWidget(colorLabel = new WidgetLabel(x, y, xlate("pneumaticcraft.gui.tubeModule.channel")));

        x = guiLeft + 10 + colorLabel.getWidth() + 10;
        WidgetColorSelector colorSelector = new WidgetColorSelector(x, y-4, w -> color = w.getColor().getId())
            .withInitialColor(DyeColor.byId(color));
        addRenderableWidget(colorSelector);

        if (module.isUpgraded()) {
            x = guiLeft + 10 + colorLabel.getWidth() + 10 + colorSelector.getWidth() + 10;
            WidgetCheckBox advancedMode = new WidgetCheckBox(x, y, 0xFF404040, Component.literal("Advanced"), b -> {
                    module.advancedConfig = b.checked;
                    NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
            }).setChecked(false);
            advancedMode.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip")));
            addRenderableWidget(advancedMode);
        }

        x = guiLeft + 10;
        y = guiTop + 22 + colorLabel.getHeight() + 10;
        WidgetLabel thresholdLabel;
        addRenderableWidget(thresholdLabel = new WidgetLabel(x, y, xlate("pneumaticcraft.gui.tubeModule.simpleConfig.threshold")));

        x = guiLeft + 10 + thresholdLabel.getWidth() + 5;
        thresholdField = new WidgetTextFieldNumber(font, x, y-2, 30, font.lineHeight + 3).setDecimals(0);
        addRenderableWidget(thresholdField);
        thresholdField.setWidth(40);
        setInitialFocus(thresholdField);
        thresholdField.setValue(threshold);

        x = guiLeft + 10 + thresholdLabel.getWidth() + thresholdField.getWidth() + 10;
        addRenderableWidget(new WidgetLabel(x, y, xlate("pneumaticcraft.gui.tubeModule.celsius")));
    }

    @Override
    public void tick() {
        super.tick();
        if (module.advancedConfig) {
            minecraft.setScreen(new ThermostatModuleScreen(module));
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MODULE_SIMPLE;
    }

    @Override
    public void removed() {
        super.removed();

        module.setColorChannel(color);
        module.setThreshold(thresholdField.getIntValue());
        NetworkHandler.sendToServer(PacketSyncThermostatModuleToServer.create(module));
    }
}
