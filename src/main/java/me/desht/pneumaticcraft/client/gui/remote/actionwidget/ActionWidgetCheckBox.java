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
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.network.chat.Component;

public class ActionWidgetCheckBox extends ActionWidgetVariable<WidgetCheckBox> implements IActionWidgetLabeled {
    public static final MapCodec<ActionWidgetCheckBox> CODEC = RecordCodecBuilder.mapCodec(builder ->
        varParts(builder).apply(builder, ActionWidgetCheckBox::new));
    public static final String ID = "checkbox";

    public ActionWidgetCheckBox(RemoteEditorScreen screen, WidgetCheckBox widget) {
        super(BaseSettings.DEFAULT, WidgetSettings.forWidget(screen, widget), "");
    }

    public ActionWidgetCheckBox(BaseSettings baseSettings, WidgetSettings widgetSettings, String variableName) {
        super(baseSettings, widgetSettings, variableName);
    }

    @Override
    public MapCodec<? extends ActionWidget<WidgetCheckBox>> codec() {
        return CODEC;
    }

    @Override
    protected WidgetCheckBox createMinecraftWidget(RemoteScreen screen) {
        return  new WidgetCheckBox(
                widgetSettings.getX() + screen.getGuiLeft(),
                widgetSettings.getY() + screen.getGuiTop(),
                0xFF404040, widgetSettings.getTitle(),
                b -> onActionPerformed()
        );
    }

    @Override
    public String getId() {
        return ID;
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
        if (!getVariableName().isEmpty()) {
            NetworkHandler.sendToServer(PacketSetGlobalVariable.forBool(getVariableName(), widget.checked));
        }
    }

    @Override
    public void onVariableChange() {
        widget.checked = GlobalVariableHelper.getBool(ClientUtils.getClientPlayer().getUUID(), getVariableName());
    }
}
