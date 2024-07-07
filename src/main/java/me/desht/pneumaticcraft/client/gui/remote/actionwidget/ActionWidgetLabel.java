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
import me.desht.pneumaticcraft.client.gui.remote.BasicRemoteOptionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ActionWidgetLabel extends ActionWidget<WidgetLabelVariable> implements IActionWidgetLabeled {
    public static final MapCodec<ActionWidgetLabel> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).apply(builder, ActionWidgetLabel::new)
    );
    public static final String ID = "label";

    public ActionWidgetLabel(RemoteEditorScreen remoteEditorScreen, WidgetLabelVariable widget) {
        super(WidgetSettings.forWidget(remoteEditorScreen, widget));
    }

    public ActionWidgetLabel(BaseSettings baseSettings, WidgetSettings widgetSettings) {
        super(baseSettings, widgetSettings);
    }

    @Override
    public MapCodec<? extends ActionWidget<WidgetLabelVariable>> codec() {
        return CODEC;
    }

    @Override
    protected WidgetLabelVariable createMinecraftWidget(RemoteScreen screen) {
        return  new WidgetLabelVariable(
                widgetSettings.getX() + screen.getGuiLeft(),
                widgetSettings.getY() + screen.getGuiTop(),
                widgetSettings.getTitle()
        );
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
    public Screen createConfigurationGui(RemoteEditorScreen guiRemote) {
        return new BasicRemoteOptionScreen<>(this, guiRemote);
    }
}
