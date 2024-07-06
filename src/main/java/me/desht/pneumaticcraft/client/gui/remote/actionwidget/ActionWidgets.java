package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.common.util.CodecUtil;

import java.util.List;
import java.util.function.Function;

public class ActionWidgets {
    private static final BiMap<String, MapCodec<? extends ActionWidget<?>>> registeredWidgets = ImmutableBiMap.of(
            ActionWidgetCheckBox.ID, ActionWidgetCheckBox.CODEC,
            ActionWidgetLabel.ID, ActionWidgetLabel.CODEC,
            ActionWidgetButton.ID, ActionWidgetButton.CODEC,
            ActionWidgetDropdown.ID, ActionWidgetDropdown.CODEC
    );

    private static final Codec<MapCodec<? extends ActionWidget<?>>> DISPATCH = CodecUtil.simpleDispatchCodec(Codec.STRING, registeredWidgets);

    public static final Codec<ActionWidget<?>> CODEC = DISPATCH.dispatch(ActionWidget::codec, Function.identity());

    public static final Codec<List<ActionWidget<?>>> LIST_CODEC = CODEC.listOf();
}
