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
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteVariable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;

public abstract class ActionWidgetVariable<W extends Widget> extends ActionWidget<W> {
    private String variableName = "";

    ActionWidgetVariable(W widget) {
        super(widget);
    }

    ActionWidgetVariable() {
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        variableName = tag.getString("variableName");
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putString("variableName", variableName);
        return tag;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public Screen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteVariable<>(this, guiRemote);
    }

    public abstract void onActionPerformed();

    public void onKeyTyped() {
    }

    public abstract void onVariableChange();
}
