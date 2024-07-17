package me.desht.pneumaticcraft.common.remote;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.remote.*;
import me.desht.pneumaticcraft.common.registry.ModRemoteWidgetTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record RemoteWidgetCheckbox(BaseSettings baseSettings, WidgetSettings widgetSettings, String varName) implements IRemoteVariableWidget {
    public static final MapCodec<RemoteWidgetCheckbox> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BaseSettings.CODEC.optionalFieldOf("base", BaseSettings.DEFAULT).forGetter(RemoteWidgetCheckbox::baseSettings),
            WidgetSettings.CODEC.fieldOf("widget").forGetter(RemoteWidgetCheckbox::widgetSettings),
            Codec.STRING.optionalFieldOf("var_name", "").forGetter(RemoteWidgetCheckbox::varName)
    ).apply(builder, RemoteWidgetCheckbox::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteWidgetCheckbox> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, RemoteWidgetCheckbox::baseSettings,
            WidgetSettings.STREAM_CODEC, RemoteWidgetCheckbox::widgetSettings,
            ByteBufCodecs.STRING_UTF8, RemoteWidgetCheckbox::varName,
            RemoteWidgetCheckbox::new
    );
    public static final Supplier<RemoteWidgetCheckbox> TRAY = Suppliers.memoize(() -> new RemoteWidgetCheckbox(
            BaseSettings.DEFAULT,
            new WidgetSettings(TRAY_WIDGET_X, 23, 50, 20,
                    xlate(IRemoteWidget.getTranslationKey(ModRemoteWidgetTypes.CHECKBOX.get())),
                    xlate(IRemoteWidget.getTooltipTranslationKey(ModRemoteWidgetTypes.CHECKBOX.get()))
            ),
            ""
    ));

    @Override
    public RemoteWidgetCheckbox copyToPos(int x, int y) {
        return new RemoteWidgetCheckbox(baseSettings, widgetSettings.copyToPos(x, y), varName);
    }

    @Override
    public RemoteWidgetType<RemoteWidgetCheckbox> getType() {
        return ModRemoteWidgetTypes.CHECKBOX.get();
    }

}
