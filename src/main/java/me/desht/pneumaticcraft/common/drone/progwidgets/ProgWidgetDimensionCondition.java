package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.WildcardedRLMatcher;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetDimensionCondition extends ProgWidgetConditionBase {
    public static final MapCodec<ProgWidgetDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).apply(builder, ProgWidgetDimensionCondition::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ProgWidgetDimensionCondition::new
    );

    public ProgWidgetDimensionCondition() {
        super(PositionFields.DEFAULT);
    }

    ProgWidgetDimensionCondition(PositionFields pos) {
        super(pos);
    }

    @Override
    public boolean evaluate(IDrone drone, IProgWidget widget) {
        ResourceLocation dimId = drone.getDroneLevel().dimension().location();
        return !checkDimensions(false, dimId) && checkDimensions(true, dimId);
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        for (String s : getDimList(false)) {
            if (!WildcardedRLMatcher.isValidRL(s)) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.condition.error.badDimensionId"));
                return;
            }
        }
        for (String s : getDimList(true)) {
            if (!WildcardedRLMatcher.isValidRL(s)) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.condition.error.badDimensionId"));
                return;
            }
        }
    }

    private boolean checkDimensions(boolean whitelist, ResourceLocation dimId) {
        List<String> list = getDimList(whitelist);
        return list.isEmpty() ? whitelist : new WildcardedRLMatcher(list).test(dimId);
    }

    private List<String> getDimList(boolean whitelist) {
        List<String> list = new ArrayList<>();
        var widget = getConnectedParameters()[whitelist ? 0 : 2];
        while (widget instanceof ProgWidgetText textWidget) {
            list.add(textWidget.getString());
            widget = widget.getConnectedParameters()[0];
        }
        return list;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DIMENSION;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_DIMENSION.get();
    }

    @Override
    public @NotNull List<ProgWidgetType<?>> getParameters() {
        return List.of(ModProgWidgetTypes.TEXT.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetDimensionCondition(positionFields);
    }
}
