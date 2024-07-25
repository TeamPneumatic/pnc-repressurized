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
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidget implements IProgWidget {
    public static final Codec<IProgWidget> CODEC = PNCRegistries.PROG_WIDGETS_REGISTRY.byNameCodec()
            .dispatch(IProgWidget::getType, ProgWidgetType::codec);
    public static final Codec<List<IProgWidget>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf, IProgWidget> STREAM_CODEC
            = ByteBufCodecs.registry(PNCRegistries.PROG_WIDGETS_KEY).dispatch(IProgWidget::getType, ProgWidgetType::streamCodec);

    // the current version when exporting to clipboard/pastebin
    public static final int JSON_VERSION = 3;

    public static final int PROGWIDGET_WIDTH = 30;
    public static final int PROGWIDGET_HEIGHT = 22;  // per parameter

    protected static <P extends ProgWidget> Products.P1<RecordCodecBuilder.Mu<P>, PositionFields> baseParts(RecordCodecBuilder.Instance<P> pInstance) {
        return pInstance.group(
                PositionFields.CODEC.fieldOf("pos").forGetter(ProgWidget::getPosition)
        );
    }

    static final MutableComponent ALL_TEXT = xlate("pneumaticcraft.gui.misc.all");
    static final MutableComponent NONE_TEXT = xlate("pneumaticcraft.gui.misc.none");

    protected PositionFields positionFields;
    private IProgWidget[] connectedParameters;
    private IProgWidget outputStepConnection;
    private IProgWidget parent;
    private Pair<Float,Float> maxUV = null;

    protected ProgWidget(PositionFields pos) {
        this.positionFields = pos;
        if (!getParameters().isEmpty()) {
            // twice the parameter size: one for the whitelist (right) and one for the blacklist (left)
            connectedParameters = new IProgWidget[getParameters().size() * 2];
        }
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
    final public ResourceLocation getTypeID() {
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
    final public int getX() {
        return positionFields.x;
    }

    @Override
    final public int getY() {
        return positionFields.y;
    }

    @Override
    final public void setX(int x) {
        this.positionFields = new PositionFields(x, positionFields.y);
    }

    @Override
    final public void setY(int y) {
        this.positionFields = new PositionFields(positionFields.x, y);
    }

    @Override
    public int getWidth() {
        return PROGWIDGET_WIDTH;
    }

    @Override
    public int getHeight() {
        return PROGWIDGET_HEIGHT * Math.max(1, getParameters().size());
    }

    @Override
    final public void setParent(IProgWidget widget) {
        parent = widget;
    }

    @Override
    final public IProgWidget getParent() {
        return parent;
    }

    @Override
    final public Pair<Float,Float> getMaxUV() {
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
    final public void setParameter(int index, IProgWidget paramWidget) {
        int index2 = index >= getParameters().size() ? index - getParameters().size() : index;
        if (connectedParameters != null && (paramWidget == null || paramWidget.getType() == getParameters().get(index2)))
            connectedParameters[index] = paramWidget;
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

    @Override
    public boolean canBeRunByComputers(IDrone drone, IProgWidget widget) {
        return getWidgetAI(drone, widget) != null;
    }

    Component varAsTextComponent(String var) {
        return var.isEmpty() ? Component.empty() : Component.literal("\"" + var + "\"");
    }

    protected boolean baseEquals(ProgWidget other) {
        return positionFields.equals(other.positionFields);
    }

    protected int baseHashCode() {
        return positionFields.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidget that = (ProgWidget) o;
        return baseEquals(that);
    }

    @Override
    public int hashCode() {
        return baseHashCode();
    }

    public record PositionFields(int x, int y) {
        public static final PositionFields DEFAULT = new PositionFields(0, 0);

        public static final Codec<PositionFields> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("x").forGetter(PositionFields::x),
                Codec.INT.fieldOf("y").forGetter(PositionFields::y)
        ).apply(builder, PositionFields::new));

        public static final StreamCodec<FriendlyByteBuf, PositionFields> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, PositionFields::x,
                ByteBufCodecs.INT, PositionFields::y,
                PositionFields::new
        );

        public static PositionFields rightParam(IProgWidget widget, int paramIdx) {
            return new ProgWidget.PositionFields(
                    widget.getX() + widget.getWidth() / 2,
                    widget.getY() + paramIdx * ProgWidget.PROGWIDGET_HEIGHT / 2
            );
        }

        public static PositionFields leftParam(IProgWidget widget, int paramIdx) {
            int width = widget.getParameters().get(paramIdx).create().getWidth();
            return new ProgWidget.PositionFields(
                    widget.getX() - width / 2,
                    widget.getY() + paramIdx * 11
            );
        }

        public static PositionFields below(IProgWidget widget) {
            return new ProgWidget.PositionFields(widget.getX(), widget.getY() + widget.getHeight() / 2);
        }
    }

    /**
     * Used for JSON & clipboard import/export
     * @param version numeric version; always {@code ProgWidget.JSON_VERSION} when exporting
     * @param widgets the exported widgets
     */
    public record Versioned(int version, List<IProgWidget> widgets) {
        public static final Codec<Versioned> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("version").forGetter(Versioned::version),
                LIST_CODEC.fieldOf("widgets").forGetter(Versioned::widgets)
        ).apply(builder, Versioned::new));
    }
}
