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

package me.desht.pneumaticcraft.common.progwidgets;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for widgets which have side filtering and count limits.
 */
public abstract class ProgWidgetInventoryBase extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget {
    private boolean[] accessingSides = new boolean[]{false, true, false, false, false, false};
    private boolean useCount;
    private int count = 1;

    public ProgWidgetInventoryBase(ProgWidgetType<?> type) {
        super(type);
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for (boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if (!sideActive) curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.noSideActive"));
    }

    @Override
    public void setSides(boolean[] sides) {
        accessingSides = sides;
    }

    @Override
    public boolean[] getSides() {
        return accessingSides;
    }

    @Override
    public boolean useCount() {
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount) {
        this.useCount = useCount;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (isUsingSides()) curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.accessingSides"));
        curTooltip.add(Component.literal(Symbols.TRIANGLE_RIGHT + " ").append(getExtraStringInfo().get(0)));
        if (useCount) curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.usingCount", count));
    }

    protected boolean isUsingSides() {
        return true;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        boolean allSides = true;
        boolean noSides = true;
        for (boolean bool : accessingSides) {
            if (bool) {
                noSides = false;
            } else {
                allSides = false;
            }
        }
        if (allSides) {
            return Collections.singletonList(ALL_TEXT);
        } else if (noSides) {
            return Collections.singletonList(NONE_TEXT);
        } else {
            List<String> l = Arrays.stream(DirectionUtil.VALUES)
                    .filter(side -> accessingSides[side.get3DDataValue()])
                    .map(ClientUtils::translateDirection)
                    .collect(Collectors.toList());
            return Collections.singletonList(Component.literal(Strings.join(l, ", ")));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            if (accessingSides[i]) tag.putBoolean(Direction.from3DDataValue(i).name(), true);
        }
        if (useCount) tag.putBoolean("useCount", true);
        tag.putInt("count", count);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(Direction.from3DDataValue(i).name());
        }
        useCount = tag.getBoolean("useCount");
        count = tag.getInt("count");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        for (int i = 0; i < 6; i++) {
            buf.writeBoolean(accessingSides[i]);
        }
        buf.writeBoolean(useCount);
        buf.writeVarInt(count);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = buf.readBoolean();
        }
        useCount = buf.readBoolean();
        count = buf.readVarInt();
    }
}
