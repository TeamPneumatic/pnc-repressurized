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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.remote.RemoteLayout;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetVariable;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.inventory.RemoteMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RemoteScreen extends AbstractPneumaticCraftContainerScreen<RemoteMenu, AbstractPneumaticCraftBlockEntity> {

    RemoteLayout remoteLayout;
    protected final ItemStack remote;

    public RemoteScreen(RemoteMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageWidth = 183;
        imageHeight = 202;
        this.remote = inv.player.getItemInHand(container.getHand());
    }

    /**
     * Client has received a PacketSetGlobalVariable message; update the remote GUI, if it's open.
     * @param varName variable that changed
     */
    public static void handleVariableChangeIfOpen(String varName) {
        if (Minecraft.getInstance().screen instanceof RemoteScreen r) {
            r.onGlobalVariableChange(varName);
        }
    }

    @Override
    public void init() {
        remoteLayout = null;

        super.init();

        if (remoteLayout == null) {
            remoteLayout = new RemoteLayout(registryAccess(), remote, leftPos, topPos);
        }
        remoteLayout.getWidgets(!(this instanceof RemoteEditorScreen)).forEach(this::addRenderableWidget);
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    public void onGlobalVariableChange(String variable) {
        clearWidgets();
        init();

        remoteLayout.getActionWidgets().stream()
                .filter(actionWidget -> actionWidget instanceof ActionWidgetVariable)
                .forEach(actionWidget -> ((ActionWidgetVariable<?>) actionWidget).onVariableChange());
    }

    @Override
    protected boolean shouldParseVariablesInTooltips() {
        return true;
    }
}
