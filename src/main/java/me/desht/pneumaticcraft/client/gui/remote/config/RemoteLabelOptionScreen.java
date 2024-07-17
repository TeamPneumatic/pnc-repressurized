package me.desht.pneumaticcraft.client.gui.remote.config;

import me.desht.pneumaticcraft.client.gui.remote.AbstractRemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteClientRegistry;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetLabel;
import net.minecraft.client.gui.screens.Screen;

public class RemoteLabelOptionScreen extends AbstractRemoteConfigScreen<RemoteWidgetLabel> {
    public RemoteLabelOptionScreen(RemoteWidgetLabel widget, RemoteEditorScreen guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    protected RemoteWidgetLabel makeUpdatedRemoteWidget() {
        return new RemoteWidgetLabel(
                makeBaseSettings(),
                makeWidgetSettings()
        );
    }

    public enum Factory implements RemoteClientRegistry.Factory<RemoteWidgetLabel,WidgetLabel> {
        INSTANCE;

        @Override
        public WidgetLabel createMinecraftWidget(RemoteWidgetLabel remoteWidget, AbstractRemoteScreen screen) {
            return new WidgetLabel(
                    remoteWidget.widgetSettings().x() + screen.getGuiLeft(),
                    remoteWidget.widgetSettings().y() + screen.getGuiTop(),
                    remoteWidget.widgetSettings().title()
            ).setTooltipText(remoteWidget.widgetSettings().tooltip());
        }

        @Override
        public Screen createConfigurationScreen(RemoteWidgetLabel remoteWidget, RemoteEditorScreen screen) {
            return new RemoteLabelOptionScreen(remoteWidget, screen);
        }
    }
}
