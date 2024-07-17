package me.desht.pneumaticcraft.common.registry;

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.api.remote.RemoteWidgetType;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetButton;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetCheckbox;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetDropdown;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetLabel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRemoteWidgetTypes {
    public static final DeferredRegister<RemoteWidgetType<?>> REMOTE_WIDGETS
            = DeferredRegister.create(PNCRegistries.REMOTE_WIDGETS_REGISTRY, Names.MOD_ID);

    public static final Supplier<RemoteWidgetType<RemoteWidgetButton>> BUTTON
            = register("button", RemoteWidgetButton.CODEC, RemoteWidgetButton.STREAM_CODEC);
    public static final Supplier<RemoteWidgetType<RemoteWidgetCheckbox>> CHECKBOX
            = register("checkbox", RemoteWidgetCheckbox.CODEC, RemoteWidgetCheckbox.STREAM_CODEC);
    public static final Supplier<RemoteWidgetType<RemoteWidgetDropdown>> DROPDOWN
            = register("dropdown", RemoteWidgetDropdown.CODEC, RemoteWidgetDropdown.STREAM_CODEC);
    public static final Supplier<RemoteWidgetType<RemoteWidgetLabel>> LABEL
            = register("label", RemoteWidgetLabel.CODEC, RemoteWidgetLabel.STREAM_CODEC);


    private static <P extends IRemoteWidget, T extends RemoteWidgetType<P>> Supplier<T> register(String name, MapCodec<P> codec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
        //noinspection unchecked
        return REMOTE_WIDGETS.register(name, () -> (T) new RemoteWidgetType<>(codec, streamCodec));
    }
}
