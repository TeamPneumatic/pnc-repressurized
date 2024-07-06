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
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.CodecUtil;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for widgets which have side filtering and count limits.
 */
public abstract class ProgWidgetInventoryBase extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget {
    protected static <P extends ProgWidgetInventoryBase> Products.P2<RecordCodecBuilder.Mu<P>, PositionFields, InvBaseFields> invParts(RecordCodecBuilder.Instance<P> pInstance) {
        return baseParts(pInstance).and(
                InvBaseFields.CODEC.fieldOf("inv").forGetter(p -> p.invBaseFields)
        );
    }

    protected InvBaseFields invBaseFields;

    protected ProgWidgetInventoryBase(PositionFields pos, InvBaseFields invBaseFields) {
        super(pos);

        this.invBaseFields = invBaseFields;
    }

    public BitSet getAccessingSides() {
        return invBaseFields.accessingSides;
    }

    public InvBaseFields invBaseFields() {
        return invBaseFields;
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        if (getAccessingSides().isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.noSideActive"));
        }
    }

    @Override
    public void setSides(boolean[] sides) {
        invBaseFields = invBaseFields.withSides(BitSet.valueOf(new byte[] { encodeSides(sides) }));
    }

    @Override
    public boolean[] getSides() {
        return decodeSides(getAccessingSides().toByteArray()[0]);
    }

    @Override
    public boolean useCount() {
        return invBaseFields.useCount;
    }

    @Override
    public void setUseCount(boolean useCount) {
        invBaseFields = invBaseFields.withUseCount(useCount);
    }

    @Override
    public int getCount() {
        return invBaseFields.count;
    }

    @Override
    public void setCount(int count) {
        invBaseFields = invBaseFields.withCount(count);
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (isUsingSides()) curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.accessingSides"));
        curTooltip.add(Component.literal(Symbols.TRIANGLE_RIGHT + " ").append(getExtraStringInfo().getFirst()));
        if (useCount()) curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.usingCount", getCount()));
    }

    protected boolean isUsingSides() {
        return true;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        if (getAccessingSides().cardinality() == 6) {
            return List.of(ALL_TEXT);
        } else if (getAccessingSides().isEmpty()) {
            return List.of(NONE_TEXT);
        } else {
            List<String> l = Arrays.stream(DirectionUtil.VALUES)
                    .filter(side -> getAccessingSides().get(side.get3DDataValue()))
                    .map(ClientUtils::translateDirection)
                    .toList();
            return List.of(Component.literal(Strings.join(l, ", ")));
        }
    }

    public record InvBaseFields(BitSet accessingSides, boolean useCount, int count) {
        private static final BitSet DEFAULT_SIDES = Util.make(new BitSet(6), bs -> bs.set(Direction.UP.get3DDataValue()));
        public static final InvBaseFields DEFAULT = new InvBaseFields(DEFAULT_SIDES, false, 1);

        public static final Codec<InvBaseFields> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                CodecUtil.bitSetCodec(6).optionalFieldOf("sides", DEFAULT_SIDES).forGetter(InvBaseFields::accessingSides),
                Codec.BOOL.optionalFieldOf("use_count", false).forGetter(InvBaseFields::useCount),
                Codec.INT.optionalFieldOf("count", 1).forGetter(InvBaseFields::count)
        ).apply(builder, InvBaseFields::new));

        public static final StreamCodec<FriendlyByteBuf, InvBaseFields> STREAM_CODEC = StreamCodec.composite(
                CodecUtil.bitSetStreamCodec(6), InvBaseFields::accessingSides,
                ByteBufCodecs.BOOL, InvBaseFields::useCount,
                ByteBufCodecs.VAR_INT, InvBaseFields::count,
                InvBaseFields::new
        );

        public InvBaseFields withSides(BitSet accessingSides) {
            return new InvBaseFields(accessingSides, useCount, count);
        }

        public InvBaseFields withUseCount(boolean useCount) {
            return new InvBaseFields(accessingSides, useCount, count);
        }

        public InvBaseFields withCount(int count) {
            return new InvBaseFields(accessingSides, useCount, count);
        }

        public InvBaseFields copy() {
            return new InvBaseFields(BitSet.valueOf(accessingSides.toByteArray()), useCount, count);
        }
    }

}
