package me.desht.pneumaticcraft.common.remote;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.remote.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.registry.ModRemoteWidgetTypes;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record RemoteWidgetDropdown(BaseSettings baseSettings, WidgetSettings widgetSettings, String varName, List<String> elements, boolean sorted) implements IRemoteVariableWidget {
    public static final MapCodec<RemoteWidgetDropdown> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BaseSettings.CODEC.optionalFieldOf("base", BaseSettings.DEFAULT).forGetter(RemoteWidgetDropdown::baseSettings),
            WidgetSettings.CODEC.fieldOf("widget").forGetter(RemoteWidgetDropdown::widgetSettings),
            Codec.STRING.optionalFieldOf("var_name", "").forGetter(RemoteWidgetDropdown::varName),
            Codec.STRING.listOf().fieldOf("elements").forGetter(RemoteWidgetDropdown::elements),
            Codec.BOOL.optionalFieldOf("sorted", false).forGetter(RemoteWidgetDropdown::sorted)
    ).apply(builder, RemoteWidgetDropdown::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteWidgetDropdown> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, RemoteWidgetDropdown::baseSettings,
            WidgetSettings.STREAM_CODEC, RemoteWidgetDropdown::widgetSettings,
            ByteBufCodecs.STRING_UTF8, RemoteWidgetDropdown::varName,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), RemoteWidgetDropdown::elements,
            ByteBufCodecs.BOOL, RemoteWidgetDropdown::sorted,
            RemoteWidgetDropdown::new
    );
    public static final Supplier<RemoteWidgetDropdown> TRAY = Suppliers.memoize(() -> new RemoteWidgetDropdown(
            BaseSettings.DEFAULT,
            new WidgetSettings(TRAY_WIDGET_X, 79, 70, 20,
                    xlate(IRemoteWidget.getTranslationKey(ModRemoteWidgetTypes.CHECKBOX.get())),
                    xlate(IRemoteWidget.getTooltipTranslationKey(ModRemoteWidgetTypes.DROPDOWN.get()))
            ),
            "", List.of(), false
    ));

    @Override
    public boolean hasConfigurableText() {
        return false;
    }

    @Override
    public RemoteWidgetDropdown copyToPos(int x, int y) {
        return new RemoteWidgetDropdown(baseSettings, widgetSettings.copyToPos(x, y), varName, List.copyOf(elements), sorted);
    }

    @Override
    public RemoteWidgetType<RemoteWidgetDropdown> getType() {
        return ModRemoteWidgetTypes.DROPDOWN.get();
    }

    public String getSelectedElement() {
        if (elements.isEmpty()) {
            return "";
        } else {
            int idx = GlobalVariableHelper.INSTANCE.getInt(ClientUtils.getClientPlayer().getUUID(), varName);
            return elements.get(Mth.clamp(idx, 0, elements.size() - 1));
        }
    }
}
