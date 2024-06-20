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
import me.desht.pneumaticcraft.client.gui.remote.RemoteVariableOptionScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public abstract class ActionWidgetVariable<W extends AbstractWidget> extends ActionWidget<W> {
    private String variableName = "";

    ActionWidgetVariable(W widget) {
        super(widget);
    }

    ActionWidgetVariable() {
    }

    @Override
    public void readFromNBT(HolderLookup.Provider provider, CompoundTag tag, int guiLeft, int guiTop) {
        super.readFromNBT(provider, tag, guiLeft, guiTop);
        variableName = tag.getString("variableName");
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider, int guiLeft, int guiTop) {
        CompoundTag tag = super.toNBT(provider, guiLeft, guiTop);
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
    public Screen getGui(RemoteEditorScreen guiRemote) {
        return new RemoteVariableOptionScreen<>(this, guiRemote);
    }

    public abstract void onActionPerformed();

    public void onKeyTyped() {
    }

    public abstract void onVariableChange();
}
