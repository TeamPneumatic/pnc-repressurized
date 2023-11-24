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

import me.desht.pneumaticcraft.client.gui.widget.WidgetColorSelector;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncThermostatModuleToServer;
import me.desht.pneumaticcraft.common.network.PacketTubeModuleColor;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ThermostatModuleScreen extends AbstractTubeModuleScreen<ThermostatModule> {
    private int color;
    private int threshold;
    private WidgetTextFieldNumber thresholdField;

    public ThermostatModuleScreen(ThermostatModule module) {
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
        addRenderableWidget(new WidgetColorSelector(x, y-4, w -> color = w.getColor().getId())
                .withInitialColor(DyeColor.byId(color)));

        x = guiLeft + 10;
        y = guiTop + 22 + colorLabel.getHeight() + 10;
        WidgetLabel thresholdLabel;
        addRenderableWidget(thresholdLabel = new WidgetLabel(x, y, xlate("pneumaticcraft.gui.tubeModule.simpleConfig.threshold")));

        x = guiLeft + 10 + thresholdLabel.getWidth() + 5;
        thresholdField = new WidgetTextFieldNumber(font, x, y-1, 30, font.lineHeight + 2).setDecimals(0);
        addRenderableWidget(thresholdField);
        thresholdField.setWidth(40);
        setInitialFocus(thresholdField);
        thresholdField.setValue(threshold);

        x = guiLeft + 10 + thresholdLabel.getWidth() + thresholdField.getWidth() + 10;
        addRenderableWidget(new WidgetLabel(x, y, xlate("pneumaticcraft.gui.tubeModule.celsius")));
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
        // NetworkHandler.sendToServer(new PacketTubeModuleColor(module));
        NetworkHandler.sendToServer(new PacketSyncThermostatModuleToServer(module));
    }
}
