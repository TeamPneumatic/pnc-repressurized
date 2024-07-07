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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.RemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteButtonOptionScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ActionWidgetButton extends ActionWidgetVariable<WidgetButtonExtended> implements IActionWidgetLabeled {
    public static final MapCodec<ActionWidgetButton> CODEC = RecordCodecBuilder.mapCodec(builder ->
            varParts(builder).and(
                BlockPos.CODEC.fieldOf("set_pos").forGetter(a -> a.settingPos)
            ).apply(builder, ActionWidgetButton::new));
    public static final String ID = "button";

    public BlockPos settingPos = BlockPos.ZERO; // The coordinate the variable is set to when the button is pressed.

    public ActionWidgetButton(RemoteEditorScreen screen, WidgetButtonExtended widget) {
        super(BaseSettings.DEFAULT, WidgetSettings.forWidget(screen, widget), "");
    }

    public ActionWidgetButton(BaseSettings baseSettings, WidgetSettings widgetSettings, String s, BlockPos settingPos) {
        super(baseSettings, widgetSettings, s);

        this.settingPos = settingPos;
    }

    @Override
    public MapCodec<? extends ActionWidget<WidgetButtonExtended>> codec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void setText(Component text) {
        widgetSettings.setTitle(text);
        widget.setMessage(text);
    }

    @Override
    public Component getText() {
        return widget.getMessage();
    }

    @Override
    public void onActionPerformed() {
        if (!getVariableName().isEmpty()) {
            NetworkHandler.sendToServer(PacketSetGlobalVariable.forPos(getVariableName(), settingPos));
        }
    }

    @Override
    public void onVariableChange() {
        // no action needed
    }

    @Override
    public Screen createConfigurationGui(RemoteEditorScreen guiRemote) {
        return new RemoteButtonOptionScreen(this, guiRemote);
    }

    @Override
    protected WidgetButtonExtended createMinecraftWidget(RemoteScreen screen) {
        return new WidgetButtonExtended(
                widgetSettings.getX() + screen.getGuiLeft(),
                widgetSettings.getY() + screen.getGuiTop(),
                widgetSettings.getWidth(),
                widgetSettings.getHeight(),
                widgetSettings.getTitle(),
                b -> onActionPerformed()
        );
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
