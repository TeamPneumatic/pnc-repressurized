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
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ProgWidgetDroneConditionEntity extends ProgWidgetDroneCondition implements IEntityProvider {
    public static final MapCodec<ProgWidgetDroneConditionEntity> CODEC = RecordCodecBuilder.mapCodec(builder ->
            droneConditionParts(builder).apply(builder, ProgWidgetDroneConditionEntity::new));

    private EntityFilterPair<ProgWidgetDroneConditionEntity> entityFilters;

    public ProgWidgetDroneConditionEntity() {
    }

    public ProgWidgetDroneConditionEntity(PositionFields pos, DroneConditionFields cond) {
        super(pos, cond);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.TEXT.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.DRONE_CONDITION_ENTITY.get();
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        EntityFilterPair.addErrors(this, curInfo);
    }

    @Override
    public int getEntityFilterPosition() {
        return 0;
    }

    @Override
    protected int getCount(IDrone d, IProgWidget widget) {
        int count = 0;
        for (Entity e : IDroneBase.asDrone(d).getPassengers()) {
            if (((IEntityProvider) widget).isEntityValid(e)) count++;
        }
        maybeRecordMeasuredVal(d, count);
        return count;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_ENTITY;
    }

    @Override
    public List<Entity> getValidEntities(Level world) {
        return new ArrayList<>();
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.isEntityValid(entity);
    }

}
