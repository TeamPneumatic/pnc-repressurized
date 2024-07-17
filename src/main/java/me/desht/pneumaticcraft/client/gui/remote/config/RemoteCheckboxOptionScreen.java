package me.desht.pneumaticcraft.client.gui.remote.config;

import me.desht.pneumaticcraft.client.gui.remote.AbstractRemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteClientRegistry;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetCheckbox;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.gui.screens.Screen;

public class RemoteCheckboxOptionScreen extends AbstractRemoteVariableConfigScreen<RemoteWidgetCheckbox> {
    public RemoteCheckboxOptionScreen(RemoteWidgetCheckbox widget, RemoteEditorScreen guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    protected RemoteWidgetCheckbox makeUpdatedRemoteWidget() {
        return new RemoteWidgetCheckbox(
                makeBaseSettings(),
                makeWidgetSettings(),
                makeVarName()
        );
    }

    public enum Factory implements RemoteClientRegistry.Factory<RemoteWidgetCheckbox, WidgetCheckBox> {
        INSTANCE;

        @Override
        public WidgetCheckBox createMinecraftWidget(RemoteWidgetCheckbox remoteWidget, AbstractRemoteScreen screen) {
            return new WidgetCheckBox(
                    remoteWidget.widgetSettings().x() + screen.getGuiLeft(),
                    remoteWidget.widgetSettings().y() + screen.getGuiTop(),
                    0xFF404040, remoteWidget.widgetSettings().title(),
                    btn -> {
                        if (!remoteWidget.varName().isEmpty()) {
                            NetworkHandler.sendToServer(PacketSetGlobalVariable.forBool(remoteWidget.varName(), btn.checked));
                        }
                    }
            ).setTooltipText(remoteWidget.widgetSettings().tooltip());
        }

        @Override
        public Screen createConfigurationScreen(RemoteWidgetCheckbox remoteWidget, RemoteEditorScreen screen) {
            return new RemoteCheckboxOptionScreen(remoteWidget, screen);
        }

        @Override
        public void handleGlobalVariableChange(RemoteWidgetCheckbox remoteWidget, WidgetCheckBox mcWidget, String varName) {
            mcWidget.setChecked(GlobalVariableHelper.getInstance().getBool(ClientUtils.getClientPlayer().getUUID(), varName));
        }
    }
}
