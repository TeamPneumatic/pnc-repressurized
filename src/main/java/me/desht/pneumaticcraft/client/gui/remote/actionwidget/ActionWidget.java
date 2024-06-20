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
import me.desht.pneumaticcraft.client.gui.remote.RemoteLayout;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.mixin.accessors.TooltipAccess;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public abstract class ActionWidget<W extends AbstractWidget> {
    private static final BlockPos ONE_ZERO_ZERO = new BlockPos(1, 0, 0);
    protected W widget;
    private String enableVariable = "";
    private BlockPos enablingValue = BlockPos.ZERO;

    ActionWidget(W widget) {
        this.widget = widget;
    }

    ActionWidget() {
    }

    public void readFromNBT(HolderLookup.Provider provider, CompoundTag tag, int guiLeft, int guiTop) {
        enableVariable = tag.getString("enableVariable");
        enablingValue = NbtUtils.readBlockPos(tag, "enablingVal").orElse(ONE_ZERO_ZERO);
    }

    public CompoundTag toNBT(HolderLookup.Provider provider, int guiLeft, int guiTop) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", getId());
        tag.putString("enableVariable", enableVariable);
        tag.put("enablingVal", NbtUtils.writeBlockPos(enablingValue));
        return tag;
    }

    public ActionWidget<?> copy(HolderLookup.Provider provider) {
        try {
            ActionWidget<?> copy = RemoteLayout.createWidget(getId())
                    .orElseThrow(() -> new IllegalArgumentException("unknown id"));
            copy.readFromNBT(provider, toNBT(provider, 0, 0), 0, 0);
            return copy;
        } catch (Exception e) {
            Log.error("Error occurred when trying to copy a {} action widget: {}", getId(), e.getMessage());
            return null;
        }
    }

    public W getWidget() {
        return widget;
    }

    public abstract void setWidgetPos(int x, int y);

    public abstract String getId();

    public Screen getGui(RemoteEditorScreen guiRemote) {
        return null;
    }

    public void setEnableVariable(String varName) {
        this.enableVariable = varName;
    }

    public String getEnableVariable() {
        return enableVariable;
    }

    public boolean isEnabled() {
        if (enableVariable.isEmpty()) return true;
        BlockPos pos = GlobalVariableHelper.getPos(ClientUtils.getClientPlayer().getUUID(), enableVariable, BlockPos.ZERO);
        return pos.equals(enablingValue);
    }

    public void setEnablingValue(int x, int y, int z) {
        enablingValue = new BlockPos(x, y, z);
    }

    public BlockPos getEnablingValue() {
        return enablingValue;
    }

    public void setTooltip(Tooltip tooltip) {
        widget.setTooltip(tooltip);
    }

    public Tooltip getTooltip() {
        return widget.getTooltip();
    }

    public Component getTooltipMessage() {
        Tooltip tooltip = getWidget().getTooltip();
        return tooltip == null ? Component.empty() : ((TooltipAccess) tooltip).getMessage();
    }

    void deserializeTooltip(String val, HolderLookup.Provider provider) {
        if (!val.isEmpty()) {
            Component c = Component.Serializer.fromJson(val, provider);
            widget.setTooltip(c == null ? null : Tooltip.create(c));
        } else {
            widget.setTooltip(null);
        }
    }
}