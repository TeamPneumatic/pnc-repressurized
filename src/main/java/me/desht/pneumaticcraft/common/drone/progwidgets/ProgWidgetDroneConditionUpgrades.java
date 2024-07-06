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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ProgWidgetDroneConditionUpgrades extends ProgWidgetDroneCondition implements IItemFiltering {
    public static final MapCodec<ProgWidgetDroneConditionUpgrades> CODEC = RecordCodecBuilder.mapCodec(builder ->
            droneConditionParts(builder).apply(builder, ProgWidgetDroneConditionUpgrades::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetDroneConditionUpgrades> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            DroneConditionFields.STREAM_CODEC, ProgWidgetDroneCondition::droneConditionFields,
            ProgWidgetDroneConditionUpgrades::new
    );

    public ProgWidgetDroneConditionUpgrades() {
    }

    public ProgWidgetDroneConditionUpgrades(PositionFields pos, DroneConditionFields cond) {
        super(pos, cond);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetDroneConditionUpgrades(getPosition(), droneConditionFields());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.ITEM_FILTER.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected int getCount(IDrone drone, IProgWidget widget) {
        int count = PneumaticRegistry.getInstance().getUpgradeRegistry().getKnownUpgrades().stream()
                .filter(upgrade -> drone.getUpgrades(upgrade) > 0)
                .filter(upgrade -> widget instanceof IItemFiltering f && f.isItemValidForFilters(upgrade.getItemStack()))
                .mapToInt(drone::getUpgrades)
                .sum();

        maybeRecordMeasuredVal(drone, count);
        return count;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_UPGRADES;
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item) {
        return ProgWidgetItemFilter.isItemValidForFilters(item,
                getConnectedWidgetList(this, 0, ModProgWidgetTypes.ITEM_FILTER.get()),
                getConnectedWidgetList(this, getParameters().size(), ModProgWidgetTypes.ITEM_FILTER.get()),
                null);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.DRONE_CONDITION_UPGRADES.get();
    }
}
