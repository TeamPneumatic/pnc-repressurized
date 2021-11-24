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

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class ActionWidgetButton extends ActionWidgetVariable<WidgetButtonExtended> implements IActionWidgetLabeled {
    public BlockPos settingCoordinate = BlockPos.ZERO; // The coordinate the variable is set to when the button is pressed.

    public ActionWidgetButton() {
    }

    public ActionWidgetButton(WidgetButtonExtended widget) {
        super(widget);
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetButtonExtended(tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop, tag.getInt("width"), tag.getInt("height"), deserializeTextComponent(tag.getString("text")), b -> onActionPerformed());
        settingCoordinate = new BlockPos(tag.getInt("settingX"), tag.getInt("settingY"), tag.getInt("settingZ"));
        widget.setTooltipText(NBTUtils.deserializeTextComponents(tag.getList("tooltip", Constants.NBT.TAG_STRING)));
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putInt("x", widget.x - guiLeft);
        tag.putInt("y", widget.y - guiTop);
        tag.putInt("width", widget.getWidth());
        tag.putInt("height", widget.getHeight());
        tag.putString("text", ITextComponent.Serializer.toJson(widget.getMessage()));
        tag.putInt("settingX", settingCoordinate.getX());
        tag.putInt("settingY", settingCoordinate.getY());
        tag.putInt("settingZ", settingCoordinate.getZ());
        tag.put("tooltip", NBTUtils.serializeTextComponents(widget.getTooltip()));
        return tag;
    }

    @Override
    public String getId() {
        return "button";
    }

    @Override
    public void setText(ITextComponent text) {
        widget.setMessage(text);
    }

    @Override
    public ITextComponent getText() {
        return widget.getMessage();
    }

    @Override
    public void onActionPerformed() {
        NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), settingCoordinate));
    }

    @Override
    public void onVariableChange() {
    }

    @Override
    public Screen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteButton(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.x = x;
        widget.y = y;
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

    @Override
    public void setTooltip(List<ITextComponent> text) {
        widget.setTooltipText(text);
    }

    @Override
    public List<ITextComponent> getTooltip() {
        return widget.getTooltip();
    }
}
