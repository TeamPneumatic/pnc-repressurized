package me.desht.pneumaticcraft.common.remote;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.remote.*;
import me.desht.pneumaticcraft.common.registry.ModRemoteWidgetTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record RemoteWidgetButton(BaseSettings baseSettings, WidgetSettings widgetSettings, String varName, BlockPos settingPos) implements IRemoteVariableWidget {
    public static final MapCodec<RemoteWidgetButton> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BaseSettings.CODEC.optionalFieldOf("base", BaseSettings.DEFAULT).forGetter(RemoteWidgetButton::baseSettings),
            WidgetSettings.CODEC.fieldOf("widget").forGetter(RemoteWidgetButton::widgetSettings),
            Codec.STRING.optionalFieldOf("var_name", "").forGetter(RemoteWidgetButton::varName),
            BlockPos.CODEC.optionalFieldOf("set_pos", BlockPos.ZERO).forGetter(RemoteWidgetButton::settingPos)
    ).apply(builder, RemoteWidgetButton::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteWidgetButton> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, RemoteWidgetButton::baseSettings,
            WidgetSettings.STREAM_CODEC, RemoteWidgetButton::widgetSettings,
            ByteBufCodecs.STRING_UTF8, RemoteWidgetButton::varName,
            BlockPos.STREAM_CODEC, RemoteWidgetButton::settingPos,
            RemoteWidgetButton::new
    );
    public static final Supplier<RemoteWidgetButton> TRAY = Suppliers.memoize(() -> new RemoteWidgetButton(
            BaseSettings.DEFAULT,
            new WidgetSettings(TRAY_WIDGET_X, 53, 50, 20,
                    xlate(IRemoteWidget.getTranslationKey(ModRemoteWidgetTypes.BUTTON.get())),
                    xlate(IRemoteWidget.getTooltipTranslationKey(ModRemoteWidgetTypes.BUTTON.get()))
            ),
            "",
            BlockPos.ZERO
    ));

    @Override
    public RemoteWidgetButton copyToPos(int x, int y) {
        return new RemoteWidgetButton(baseSettings, widgetSettings.copyToPos(x, y), varName, settingPos);
    }

    @Override
    public RemoteWidgetType<RemoteWidgetButton> getType() {
        return ModRemoteWidgetTypes.BUTTON.get();
    }

}
