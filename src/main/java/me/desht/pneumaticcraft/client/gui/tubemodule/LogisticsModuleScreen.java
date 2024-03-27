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
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketTubeModuleColor;
import me.desht.pneumaticcraft.common.tubemodules.LogisticsModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class LogisticsModuleScreen extends AbstractTubeModuleScreen<LogisticsModule> {
    private int ourColor;

    public LogisticsModuleScreen(LogisticsModule module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        ourColor = module.getColorChannel();

        WidgetLabel ourColorLabel;
        addRenderableWidget(ourColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 26, xlate("pneumaticcraft.gui.tubeModule.channel")));

        addLabel(getTitle(), guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);

        addRenderableWidget(new WidgetColorSelector(guiLeft + 10 + ourColorLabel.getWidth() + 5, guiTop + 22, w -> ourColor = w.getColor().getId())
                .withInitialColor(DyeColor.byId(ourColor)));
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MODULE_SIMPLE;
    }

    @Override
    public void removed() {
        super.removed();

        module.setColorChannel(ourColor);
        NetworkHandler.sendToServer(PacketTubeModuleColor.create(module));
    }
}
