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
import me.desht.pneumaticcraft.client.gui.remote.BasicRemoteOptionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class ActionWidgetLabel extends ActionWidget<WidgetLabelVariable> implements IActionWidgetLabeled {

    public ActionWidgetLabel(WidgetLabelVariable widget) {
        super(widget);
    }

    public ActionWidgetLabel() {
    }

    @Override
    public CompoundTag toNBT(int guiLeft, int guiTop) {
        CompoundTag tag = super.toNBT(guiLeft, guiTop);
        tag.putString("text", Component.Serializer.toJson(widget.getMessage()));
        tag.putInt("x", widget.getX() - guiLeft);
        tag.putInt("y", widget.getY() - guiTop);
        tag.putString("tooltip", Component.Serializer.toJson(getTooltipMessage()));
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetLabelVariable(tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop, deserializeTextComponent(tag.getString("text")));
        deserializeTooltip(tag.getString("tooltip"));
    }

    @Override
    public String getId() {
        return "label";
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
    public Screen getGui(RemoteEditorScreen guiRemote) {
        return new BasicRemoteOptionScreen<>(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.setPosition(x, y);
    }

}
