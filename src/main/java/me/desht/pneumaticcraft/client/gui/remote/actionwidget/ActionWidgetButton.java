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

package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteButtonOptionScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;

public class ActionWidgetButton extends ActionWidgetVariable<WidgetButtonExtended> implements IActionWidgetLabeled {
    public BlockPos settingPos = BlockPos.ZERO; // The coordinate the variable is set to when the button is pressed.

    public ActionWidgetButton() {
    }

    public ActionWidgetButton(WidgetButtonExtended widget) {
        super(widget);
    }

    @Override
    public void readFromNBT(HolderLookup.Provider provider, CompoundTag tag, int guiLeft, int guiTop) {
        super.readFromNBT(provider, tag, guiLeft, guiTop);

        widget = new WidgetButtonExtended(
                tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop,
                tag.getInt("width"), tag.getInt("height"),
                deserializeTextComponent(tag.getString("text"), provider),
                b -> onActionPerformed()
        );
        settingPos = NbtUtils.readBlockPos(tag, "settingPos").orElse(BlockPos.ZERO);
        deserializeTooltip(tag.getString("tooltip"), provider);
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider, int guiLeft, int guiTop) {
        CompoundTag tag = super.toNBT(provider, guiLeft, guiTop);
        tag.putInt("x", widget.getX() - guiLeft);
        tag.putInt("y", widget.getY() - guiTop);
        tag.putInt("width", widget.getWidth());
        tag.putInt("height", widget.getHeight());
        tag.putString("text", Component.Serializer.toJson(widget.getMessage(), provider));
        tag.put("settingPos", NbtUtils.writeBlockPos(settingPos));
        tag.putString("tooltip", Component.Serializer.toJson(getTooltipMessage(), provider));
        return tag;
    }

    @Override
    public String getId() {
        return "button";
    }

    @Override
    public void setText(Component text) {
        widget.setMessage(text);
    }

    @Override
    public Component getText() {
        return widget.getMessage();
    }

    @Override
    public void onActionPerformed() {
        if (!getVariableName().isEmpty()) NetworkHandler.sendToServer(PacketSetGlobalVariable.forPos(getVariableName(), settingPos));
    }

    @Override
    public void onVariableChange() {
        // no action needed
    }

    @Override
    public Screen getGui(RemoteEditorScreen guiRemote) {
        return new RemoteButtonOptionScreen(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.setPosition(x, y);
    }

    public void setWidth(int width) {
        widget.setWidth(width);
    }

    public int getWidth() {
        return widget.getWidth();
    }

    public void setHeight(int height) {
        widget.setHeight(height);
    }

    public int getHeight() {
        return widget.getHeight();
    }
}
