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

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEmitRedstone extends ProgWidget implements IRedstoneEmissionWidget, ISidedWidget {
    public static final MapCodec<ProgWidgetEmitRedstone> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.BYTE.xmap(ProgWidget::decodeSides, ProgWidget::encodeSides)
                            .optionalFieldOf("sides", ALL_SIDES).forGetter(ProgWidgetEmitRedstone::getSides)
    ).apply(builder, ProgWidgetEmitRedstone::new));

    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};

    public ProgWidgetEmitRedstone(PositionFields pos, boolean[] accessingSides) {
        super(pos);
        this.accessingSides = accessingSides;
    }

    public ProgWidgetEmitRedstone() {
        super(PositionFields.DEFAULT);
    }

    @Override
    public int getEmittingRedstone() {
        if (getConnectedParameters()[0] != null) {
            return NumberUtils.toInt(((ProgWidgetText) getConnectedParameters()[0]).string);
        } else {
            return 0;
        }
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
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for (boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if (!sideActive) curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.noSideActive"));
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.EMIT_REDSTONE.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.general.affectingSides"));
        curTooltip.addAll(getExtraStringInfo());
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
                    .toList();
            return Collections.singletonList(Component.literal(Strings.join(l, ", ")));
        }
    }

//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        for (int i = 0; i < 6; i++) {
//            if (accessingSides[i]) tag.putBoolean(Direction.from3DDataValue(i).name(), true);
//        }
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        for (int i = 0; i < 6; i++) {
//            accessingSides[i] = tag.getBoolean(Direction.from3DDataValue(i).name());
//        }
//    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);

        buf.writeByte(encodeSides(accessingSides));
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);

        accessingSides = decodeSides(buf.readByte());
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_EMIT_REDSTONE;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIEmitRedstone(drone, widget);
    }

    private static class DroneAIEmitRedstone extends Goal {
        private final IProgWidget widget;
        private final IDrone drone;

        DroneAIEmitRedstone(IDrone drone, IProgWidget widget) {
            this.widget = widget;
            this.drone = drone;
        }

        @Override
        public boolean canUse() {
            boolean[] sides = ((ISidedWidget) widget).getSides();
            for (int i = 0; i < 6; i++) {
                if (sides[i]) {
                    drone.setEmittingRedstone(Direction.from3DDataValue(i), ((IRedstoneEmissionWidget) widget).getEmittingRedstone());
                }
            }
            return false;
        }

    }

}
