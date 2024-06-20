/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.config.subconfig.ProgWidgetConfig;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidget implements IProgWidget {
    public static final Codec<IProgWidget> CODEC = PNCRegistries.PROG_WIDGETS_REGISTRY.byNameCodec()
            .dispatch(IProgWidget::getType, ProgWidgetType::codec
    );
    public static final Codec<List<IProgWidget>> LIST_CODEC = CODEC.listOf();
    public static final StreamCodec<RegistryFriendlyByteBuf, IProgWidget> STREAM_CODEC = StreamCodec.of(
            (buf, widget) -> widget.writeToPacket(buf),
            ProgWidget::fromPacket
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,List<IProgWidget>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    protected static <P extends ProgWidget> Products.P1<RecordCodecBuilder.Mu<P>, PositionFields> baseParts(RecordCodecBuilder.Instance<P> pInstance) {
        return pInstance.group(
                PositionFields.CODEC.fieldOf("pos").forGetter(ProgWidget::getPosition)
        );
    }

    static final MutableComponent ALL_TEXT = xlate("pneumaticcraft.gui.misc.all");
    static final MutableComponent NONE_TEXT = xlate("pneumaticcraft.gui.misc.none");

    private PositionFields positionFields;
    private IProgWidget[] connectedParameters;
    private IProgWidget outputStepConnection;
    private IProgWidget parent;
    private Pair<Float,Float> maxUV = null;

    protected ProgWidget(PositionFields pos) {
        this.positionFields = pos;
        if (!getParameters().isEmpty())
            connectedParameters = new IProgWidget[getParameters().size() * 2]; //times two because black- and whitelist.
    }

    protected static byte encodeSides(boolean[] sides) {
        int res = 0;
        for (int i = 0; i < 6; i++) {
            res |= sides[i] ? 1 << i : 0;
        }
        return (byte) res;
    }

    protected static boolean[] decodeSides(byte val) {
        boolean[] res = new boolean[6];
        for (int i = 0; i < 6; i++) {
            res[i] = (val & (1 << i)) != 0;
        }
        return res;
    }

    @Override
    public abstract ProgWidgetType<?> getType();

    @Override
    public ResourceLocation getTypeID() {
        return PNCRegistries.PROG_WIDGETS_REGISTRY.getKey(getType());
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        curTooltip.add(xlate(getTranslationKey()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.UNDERLINE));
        if (freeToUse()) {
            curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.comment.tooltip.freeToUse"));
        }
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.emptyList();
    }

    @Override
    public void addWarnings(List<Component> curInfo, List<IProgWidget> widgets) {
        if (this instanceof IVariableWidget variableWidget) {
            for (String variable : Util.make(new HashSet<>(), variableWidget::addVariables)) {
                if (!variable.isEmpty() && !variable.startsWith("#") && !variable.startsWith("$") && !isVariableSetAnywhere(widgets, variable)) {
                    curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.warning.variableNeverSet", variable));
                }
            }
        }
    }

    private static boolean isVariableSetAnywhere(List<IProgWidget> widgets, String variable) {
        for (IProgWidget widget : widgets) {
            if (widget instanceof IVariableSetWidget vsw) {
                Set<String> variables = Util.make(new HashSet<>(), vsw::addVariables);
                if (variables.contains(variable)) return true;
            }
        }
        return false;
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        if (!hasStepInput() && hasStepOutput() && outputStepConnection == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.noPieceConnected"));
        }
    }

    @Override
    public boolean isAvailable() {
        return !ProgWidgetConfig.INSTANCE.isWidgetBlacklisted(getType());
    }

    public PositionFields getPosition() {
        return positionFields;
    }

    @Override
    public int getX() {
        return positionFields.x;
    }

    @Override
    public int getY() {
        return positionFields.y;
    }

    @Override
    public void setX(int x) {
        this.positionFields = new PositionFields(x, positionFields.y);
    }

    @Override
    public void setY(int y) {
        this.positionFields = new PositionFields(positionFields.x, y);
    }

    @Override
    public int getWidth() {
        return 30;
    }

    @Override
    public int getHeight() {
        return !getParameters().isEmpty() ? getParameters().size() * 22 : 22;
    }

    @Override
    public void setParent(IProgWidget widget) {
        parent = widget;
    }

    @Override
    public IProgWidget getParent() {
        return parent;
    }

    @Override
    public Pair<Float,Float> getMaxUV() {
        if (maxUV == null) {
            int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);
            int height = getHeight() + (hasStepOutput() ? 10 : 0);
            int maxSize = Math.max(width, height);
            int textureSize = 1;
            while (textureSize < maxSize) {
                textureSize *= 2;
            }
            float u = (float) width / textureSize;
            float v = (float) height / textureSize;
            maxUV = new ImmutablePair<>(u, v);
        }
        return maxUV;
    }

    @Override
    public boolean hasStepOutput() {
        return hasStepInput();
    }

    @Override
    public Goal getWidgetTargetAI(IDrone drone, IProgWidget widget) {
        return null;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return null;
    }

    @Override
    public void setParameter(int index, IProgWidget parm) {
        int index2 = index >= getParameters().size() ? index - getParameters().size() : index;
        if (connectedParameters != null && (parm == null || parm.getType() == getParameters().get(index2)))
            connectedParameters[index] = parm;
    }

    @Override
    public boolean canSetParameter(int index) {
        if (connectedParameters != null) {
            return hasBlacklist() || index < connectedParameters.length / 2;
        }
        return false;
    }

    protected boolean hasBlacklist() {
        return true;
    }

    @Override
    public IProgWidget[] getConnectedParameters() {
        return connectedParameters;
    }

    @Override
    public void setOutputWidget(IProgWidget widget) {
        outputStepConnection = widget;
    }

    @Override
    public IProgWidget getOutputWidget() {
        return outputStepConnection;
    }

    @Override
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        return outputStepConnection;
    }

    @Override
    public Optional<? extends IProgWidget> copy() {
        try {
            Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
            var copy = CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();

            return copy instanceof IProgWidget p ? Optional.of(p) : Optional.empty();
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        // since most or all widgets have a 'pneumaticcraft:' namespace, omitting that saves 15 bytes per widget
        buf.writeUtf(PneumaticCraftUtils.modDefaultedString(getTypeID()));
        PositionFields.STREAM_CODEC.encode(buf, positionFields);
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        // note: widget type ID is not read here (see ProgWidget.fromPacket() static method)
        positionFields = PositionFields.STREAM_CODEC.decode(buf);
    }

    static <T extends IProgWidget> List<T> getConnectedWidgetList(IProgWidget widget, int parameterIndex, ProgWidgetType<T> type) {
        validateType(widget, parameterIndex, type);

        IProgWidget connectingWidget = widget.getConnectedParameters()[parameterIndex];
        if (connectingWidget != null) {
            List<T> list = new ArrayList<>();
            while (connectingWidget != null) {
                list.add(type.cast(connectingWidget));  // should be safe; we checked the type above
                connectingWidget = connectingWidget.getConnectedParameters()[0];
            }
            return list;
        } else {
            return null;
        }
    }

    private static <T extends IProgWidget> void validateType(IProgWidget widget, int parameterIndex, ProgWidgetType<T> type) {
        int l = widget.getParameters().size();
        if (parameterIndex >= l) parameterIndex -= l;  // blacklist side
        if (type != widget.getParameters().get(parameterIndex)) {
            throw new IllegalArgumentException(String.format("invalid type %s for parameter %d (expected %s)",
                    type, parameterIndex, widget.getParameters().get(parameterIndex)));
        }
    }

    public static IProgWidget fromPacket(RegistryFriendlyByteBuf buf) {
        ResourceLocation typeID = PneumaticCraftUtils.modDefaultedRL(buf.readUtf(256));
        ProgWidgetType<?> type = PNCRegistries.PROG_WIDGETS_REGISTRY.get(typeID);
        if (type != null) {
            IProgWidget newWidget = IProgWidget.create(type);
            newWidget.readFromPacket(buf);
            return newWidget;
        } else {
            throw new IllegalStateException("can't read progwidget from packet: bad widget ID: " + typeID);
        }
    }

    public static <T extends IProgWidget> T fromPacket(RegistryFriendlyByteBuf buf, @NotNull ProgWidgetType<T> expectedType) {
        ResourceLocation typeID = PneumaticCraftUtils.modDefaultedRL(buf.readUtf(256));
        ProgWidgetType<?> type = PNCRegistries.PROG_WIDGETS_REGISTRY.get(typeID);
        if (type == expectedType) {
            IProgWidget newWidget = IProgWidget.create(type);
            newWidget.readFromPacket(buf);
            //noinspection unchecked
            return (T) newWidget;
        } else {
            throw new IllegalStateException("can't read progwidget from packet: unexpected widget type: " + type);
        }
    }

    public static IProgWidget fromNBT(CompoundTag widgetTag) {
        IProgWidget widget = CODEC.parse(NbtOps.INSTANCE, widgetTag)
                .result().orElse(null);
        return widget instanceof IProgWidget w ? w : null;
    }

    @Override
    public boolean canBeRunByComputers(IDrone drone, IProgWidget widget) {
        return getWidgetAI(drone, widget) != null;
    }

    Component varAsTextComponent(String var) {
        return var.isEmpty() ? Component.empty() : Component.literal("\"" + var + "\"");
    }

    public record PositionFields(int x, int y) {
        public static final PositionFields DEFAULT = new PositionFields(0, 0);

        public static final Codec<PositionFields> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("x").forGetter(PositionFields::x),
            Codec.INT.fieldOf("y").forGetter(PositionFields::y)
        ).apply(builder, PositionFields::new));

        public static StreamCodec<FriendlyByteBuf, PositionFields> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, PositionFields::x,
                ByteBufCodecs.INT, PositionFields::y,
                PositionFields::new
        );
    }
}
