package me.desht.pneumaticcraft.common.remote;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.remote.BaseSettings;
import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.api.remote.RemoteWidgetType;
import me.desht.pneumaticcraft.api.remote.WidgetSettings;
import me.desht.pneumaticcraft.common.registry.ModRemoteWidgetTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record RemoteWidgetLabel(BaseSettings baseSettings, WidgetSettings widgetSettings) implements IRemoteWidget {
    public static final MapCodec<RemoteWidgetLabel> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BaseSettings.CODEC.optionalFieldOf("base", BaseSettings.DEFAULT).forGetter(RemoteWidgetLabel::baseSettings),
            WidgetSettings.CODEC.fieldOf("widget").forGetter(RemoteWidgetLabel::widgetSettings)
    ).apply(builder, RemoteWidgetLabel::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteWidgetLabel> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, RemoteWidgetLabel::baseSettings,
            WidgetSettings.STREAM_CODEC, RemoteWidgetLabel::widgetSettings,
            RemoteWidgetLabel::new
    );
    public static final Supplier<RemoteWidgetLabel> TRAY = Suppliers.memoize(() -> new RemoteWidgetLabel(
            BaseSettings.DEFAULT,
            new WidgetSettings(TRAY_WIDGET_X, 38, 50, 20,
                    xlate(IRemoteWidget.getTranslationKey(ModRemoteWidgetTypes.LABEL.get())),
                    xlate(IRemoteWidget.getTooltipTranslationKey(ModRemoteWidgetTypes.LABEL.get()))
            )
    ));

    @Override
    public RemoteWidgetLabel copyToPos(int x, int y) {
        return new RemoteWidgetLabel(baseSettings, widgetSettings.copyToPos(x, y));
    }

    @Override
    public RemoteWidgetType<RemoteWidgetLabel> getType() {
        return ModRemoteWidgetTypes.LABEL.get();
    }

}
