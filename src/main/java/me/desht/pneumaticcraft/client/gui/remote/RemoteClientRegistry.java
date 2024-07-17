package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.api.remote.IRemoteVariableWidget;
import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.api.remote.RemoteWidgetType;
import me.desht.pneumaticcraft.client.gui.remote.config.RemoteButtonOptionScreen;
import me.desht.pneumaticcraft.client.gui.remote.config.RemoteCheckboxOptionScreen;
import me.desht.pneumaticcraft.client.gui.remote.config.RemoteDropdownOptionScreen;
import me.desht.pneumaticcraft.client.gui.remote.config.RemoteLabelOptionScreen;
import me.desht.pneumaticcraft.common.registry.ModRemoteWidgetTypes;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum RemoteClientRegistry {
    INSTANCE;

    private final Map<ResourceLocation, Factory<? extends IRemoteWidget,? extends AbstractWidget>> factoryMap = new ConcurrentHashMap<>();

    public void registerClientFactories() {
        register(ModRemoteWidgetTypes.BUTTON.get(), RemoteButtonOptionScreen.Factory.INSTANCE);
        register(ModRemoteWidgetTypes.CHECKBOX.get(), RemoteCheckboxOptionScreen.Factory.INSTANCE);
        register(ModRemoteWidgetTypes.DROPDOWN.get(), RemoteDropdownOptionScreen.Factory.INSTANCE);
        register(ModRemoteWidgetTypes.LABEL.get(), RemoteLabelOptionScreen.Factory.INSTANCE);
    }

    public <RW extends IRemoteWidget> void register(RemoteWidgetType<RW> type, Factory<RW,? extends AbstractWidget> factory) {
        factoryMap.put(PNCRegistries.REMOTE_WIDGETS_REGISTRY.getKey(type), factory);
    }

    public <RW extends IRemoteWidget, MCW extends AbstractWidget> MCW createMinecraftWidget(RW remoteWidget, AbstractRemoteScreen screen) {
        @SuppressWarnings("unchecked") Factory<RW, MCW> factory = (Factory<RW, MCW>) factoryMap.get(getKey(remoteWidget));
        return factory.createMinecraftWidget(remoteWidget, screen);
    }

    public <RW extends IRemoteWidget, MCW extends AbstractWidget> Screen createConfigurationScreen(RW remoteWidget, RemoteEditorScreen screen) {
        @SuppressWarnings("unchecked") Factory<RW, MCW> factory = (Factory<RW, MCW>) factoryMap.get(getKey(remoteWidget));
        return factory.createConfigurationScreen(remoteWidget, screen);
    }

    public <RW extends IRemoteVariableWidget, MCW extends AbstractWidget> void handleGlobalVariableChange(RW remoteWidget, MCW mcWidget, String varName) {
        @SuppressWarnings("unchecked") Factory<RW, MCW> factory = (Factory<RW, MCW>) factoryMap.get(getKey(remoteWidget));
        factory.handleGlobalVariableChange(remoteWidget, mcWidget, varName);
    }

    private static @NotNull ResourceLocation getKey(IRemoteWidget remoteWidget) {
        return Objects.requireNonNull(PNCRegistries.REMOTE_WIDGETS_REGISTRY.getKey(remoteWidget.getType()));
    }

    public interface Factory<RW extends IRemoteWidget, MCW extends AbstractWidget> {
        MCW createMinecraftWidget(RW remoteWidget, AbstractRemoteScreen screen);

        Screen createConfigurationScreen(RW remoteWidget, RemoteEditorScreen screen);

        default void handleGlobalVariableChange(RW remoteWidget, MCW mcWidget, String varName) {
        }
    }
}
