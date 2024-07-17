package me.desht.pneumaticcraft.api.remote;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.misc.IGlobalVariableHelper;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a template for a widget to be added to the Remote GUI. This is an immutable object, is used in itemstack
 * data components, and does not contain any client-only methods; see {@link xxx} for that.
 */
public interface IRemoteWidget {
    int TRAY_WIDGET_X = 200;

    Codec<IRemoteWidget> CODEC = PNCRegistries.REMOTE_WIDGETS_REGISTRY.byNameCodec().dispatch(
            IRemoteWidget::getType,
            RemoteWidgetType::codec
    );

    StreamCodec<RegistryFriendlyByteBuf,IRemoteWidget> STREAM_CODEC
            = ByteBufCodecs.registry(PNCRegistries.REMOTE_WIDGETS_KEY)
            .dispatch(IRemoteWidget::getType, RemoteWidgetType::streamCodec);

    /**
     * {@return Base settings, common to all remote widgets}
     */
    BaseSettings baseSettings();

    /**
     * {@return Settings for the physical Minecraft widget, common to all remote widgets}
     */
    WidgetSettings widgetSettings();

    /**
     * {@return an exact copy of this remote widget}
     */
    @ApiStatus.NonExtendable
    default IRemoteWidget copy() {
        return copyToPos(widgetSettings().x(), widgetSettings().y());
    }

    /**
     * Make a copy of this remote widget, at the new X/Y physical widget position
     * @param x X
     * @param y Y
     * @return the copied remote widget
     */
    IRemoteWidget copyToPos(int x, int y);

    /**
     * Does this widget allow configuration of its title and tooltip?
     * @return true if configurable, false if not
     */
    default boolean hasConfigurableText() {
        return true;
    }

    /**
     * {@return the widget type, which handles serialization}
     */
    RemoteWidgetType<? extends IRemoteWidget> getType();

    default String getTranslationKey() {
        return getTranslationKey(getType());
    }

    static String getTranslationKey(RemoteWidgetType<?> type) {
        String id = PNCRegistries.REMOTE_WIDGETS_REGISTRY.getResourceKey(type)
                .map(key -> key.location().toLanguageKey())
                .orElse("unknown");
        return "pneumaticcraft.gui.remote.tray." + id + ".name";
    }

    static String getTooltipTranslationKey(RemoteWidgetType<?> type) {
        String id = PNCRegistries.REMOTE_WIDGETS_REGISTRY.getResourceKey(type)
                .map(key -> key.location().toLanguageKey())
                .orElse("unknown");
        return "pneumaticcraft.gui.remote.tray." + id + ".tooltip";
    }

    default void discoverVariables(Set<String> variables, UUID playerId) {
        if (!baseSettings().enableVariable().isEmpty()) {
            variables.add(baseSettings().enableVariable());
        }
        if (this instanceof IRemoteVariableWidget v && !v.varName().isEmpty()) {
            variables.add(v.varName());
        }
        IGlobalVariableHelper helper = PneumaticRegistry.getInstance().getMiscHelpers().getGlobalVariableHelper();
        widgetSettings().title().visit(string -> {
            variables.addAll(helper.getRelevantVariables(string, playerId));
            return Optional.empty();
        });
    }

    default boolean isEnabled(Player player) {
        if (baseSettings().enableVariable().isEmpty()) {
            return true;
        }
        BlockPos pos = PneumaticRegistry.getInstance().getMiscHelpers().getGlobalVariableHelper()
                .getPos(player.getUUID(), baseSettings().enableVariable(), BlockPos.ZERO);
        return pos.equals(baseSettings().enablingValue());
    }
}
