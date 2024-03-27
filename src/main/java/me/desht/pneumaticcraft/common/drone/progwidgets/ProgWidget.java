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

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.config.subconfig.ProgWidgetConfig;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidget implements IProgWidget {
    static final MutableComponent ALL_TEXT = xlate("pneumaticcraft.gui.misc.all");
    static final MutableComponent NONE_TEXT = xlate("pneumaticcraft.gui.misc.none");

    private final ProgWidgetType<?> type;
    private int x, y;
    private IProgWidget[] connectedParameters;
    private IProgWidget outputStepConnection;
    private IProgWidget parent;
    private Pair<Float,Float> maxUV = null;

    public ProgWidget(ProgWidgetType<?> type) {
        this.type = type;
        if (!getParameters().isEmpty())
            connectedParameters = new IProgWidget[getParameters().size() * 2]; //times two because black- and whitelist.
    }

    @Override
    public ProgWidgetType<?> getType() {
        return type;
    }

    @Override
    public ResourceLocation getTypeID() {
        return ModProgWidgets.PROG_WIDGETS_REGISTRY.getKey(getType());
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
        if (this instanceof IVariableWidget) {
            Set<String> variables = new HashSet<>();
            ((IVariableWidget) this).addVariables(variables);
            for (String variable : variables) {
                if (!variable.isEmpty() && !variable.startsWith("#") && !variable.startsWith("$") && !isVariableSetAnywhere(widgets, variable)) {
                    curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.warning.variableNeverSet", variable));
                }
            }
        }
    }

    private boolean isVariableSetAnywhere(List<IProgWidget> widgets, String variable) {
        if (variable.isEmpty()) return true;
        for (IProgWidget widget : widgets) {
            if (widget instanceof IVariableSetWidget) {
                Set<String> variables = new HashSet<>();
                ((IVariableSetWidget) widget).addVariables(variables);
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

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
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
    public Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
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
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        return outputStepConnection;
    }

    @Override
    public IProgWidget copy() {
        IProgWidget copy = IProgWidget.create(getType());
        CompoundTag tag = new CompoundTag();
        writeToNBT(tag);
        copy.readFromNBT(tag);
        return copy;
    }

    public final CompoundTag toNbt() {
        return Util.make(new CompoundTag(), this::writeToNBT);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putString("name", PneumaticCraftUtils.modDefaultedString(getTypeID()));
        tag.putInt("x", x);
        tag.putInt("y", y);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        // note: widget type ID is not read here (see ProgWidget.fromNBT() static method)
        x = tag.getInt("x");
        y = tag.getInt("y");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        // since most or all widgets have a 'pneumaticcraft:' namespace, omitting that saves 15 bytes per widget
        buf.writeUtf(PneumaticCraftUtils.modDefaultedString(getTypeID()));
        buf.writeInt(x);
        buf.writeInt(y);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        // note: widget type ID is not read here (see ProgWidget.fromPacket() static method)
        x = buf.readInt();
        y = buf.readInt();
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

    public static IProgWidget fromPacket(FriendlyByteBuf buf) {
        ResourceLocation typeID = PneumaticCraftUtils.modDefaultedRL(buf.readUtf(256));
        ProgWidgetType<?> type = ModProgWidgets.PROG_WIDGETS_REGISTRY.get(typeID);
        if (type != null) {
            IProgWidget newWidget = IProgWidget.create(type);
            newWidget.readFromPacket(buf);
            return newWidget;
        } else {
            throw new IllegalStateException("can't read progwidget from packet: bad widget ID: " + typeID);
        }
    }

    public static IProgWidget fromNBT(CompoundTag widgetTag) {
        ResourceLocation typeID = PneumaticCraftUtils.modDefaultedRL(widgetTag.getString("name"));
        ProgWidgetType<?> type = ModProgWidgets.PROG_WIDGETS_REGISTRY.get(typeID);
        if (type == null) {
            Log.warning("can't read progwidget from NBT: bad widget ID: " + typeID);
            return null;
        }
        IProgWidget widget = IProgWidget.create(type);
        widget.readFromNBT(widgetTag);
        return widget;
    }

    @Override
    public boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget) {
        return getWidgetAI(drone, widget) != null;
    }

    Component varAsTextComponent(String var) {
        return var.isEmpty() ? Component.empty() : Component.literal("\"" + var + "\"");
    }
}
