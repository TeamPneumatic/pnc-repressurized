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

package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.api.remote.IRemoteVariableWidget;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.RemoteMenu;
import me.desht.pneumaticcraft.common.remote.SavedRemoteLayout;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RemoteScreen extends AbstractRemoteScreen {
    public RemoteScreen(RemoteMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageWidth = 183;
        imageHeight = 202;
    }

    @Override
    public void init() {
        super.init();

        SavedRemoteLayout remoteLayout = SavedRemoteLayout.fromItem(remoteItem);

        buildMinecraftWidgetList(remoteLayout.getWidgets(), this, true)
                .forEach(this::addRemoteWidget);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        GuiUtils.drawScreenWithTitleArea(graphics, leftPos, topPos, imageWidth, imageHeight, 0xFFDDD7BA);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return null;
    }

    public void onGlobalVariableChanged(String varName) {
        widgetMap.forEach((mcWidget, remoteWidget) -> {
            if (remoteWidget instanceof IRemoteVariableWidget rv && varName.equals(rv.varName())) {
                    RemoteClientRegistry.INSTANCE.handleGlobalVariableChange(rv, mcWidget, varName);
            }
            mcWidget.setMessage(TextVariableParser.parseComponent(remoteWidget.widgetSettings().title(), ClientUtils.getClientPlayer().getUUID()));
            mcWidget.visible = remoteWidget.isEnabled(Minecraft.getInstance().player);
        });
    }
}
