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
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIAttackEntity;
import me.desht.pneumaticcraft.common.drone.ai.DroneAINearestAttackableTarget;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypeBox;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEntityAttack extends ProgWidget
        implements IAreaProvider, IEntityProvider, IMaxActions, ICheckLineOfSight {

    public static final MapCodec<ProgWidgetEntityAttack> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                    Codec.BOOL.optionalFieldOf("use_max_actions", false).forGetter(ProgWidgetEntityAttack::useMaxActions),
                    Codec.INT.optionalFieldOf("max_actions", 1).forGetter(ProgWidgetEntityAttack::getMaxActions),
                    Codec.BOOL.optionalFieldOf("check_sight", false).forGetter(ProgWidgetEntityAttack::isCheckSight)
            )
    ).apply(builder, ProgWidgetEntityAttack::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetEntityAttack> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetEntityAttack::useMaxActions,
            ByteBufCodecs.VAR_INT, ProgWidgetEntityAttack::getMaxActions,
            ByteBufCodecs.BOOL, ProgWidgetEntityAttack::isCheckSight,
            ProgWidgetEntityAttack::new
    );

    private EntityFilterPair<ProgWidgetEntityAttack> entityFilters;
    private int maxActions;
    private boolean useMaxActions;
    private boolean checkSight;

    public ProgWidgetEntityAttack(PositionFields pos, boolean useMaxActions, int maxActions, boolean checkSight) {
        super(pos);

        this.useMaxActions = useMaxActions;
        this.maxActions = maxActions;
        this.checkSight = checkSight;
    }

    public ProgWidgetEntityAttack() {
        this(PositionFields.DEFAULT, false, 1, false);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.ENTITY_ATTACK.get();
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
        EntityFilterPair.addErrors(this, curInfo);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIAttackEntity(IDroneBase.asDrone(drone), 1.0D, false, getEntityFilters().getFilterString());
    }

    @Override
    public Goal getWidgetTargetAI(IDrone drone, IProgWidget widget) {
        return new DroneAINearestAttackableTarget(IDroneBase.asDrone(drone), checkSight, (ProgWidget) widget);
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ATTACK;
    }

    @Override
    public List<Entity> getValidEntities(Level world) {
        return getEntityFilters().getValidEntities(world);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        return getEntityFilters().isEntityValid(entity);
    }

    public EntityFilterPair<ProgWidgetEntityAttack> getEntityFilters() {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters;
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[2]);
    }

    public static void getArea(Set<BlockPos> area, ProgWidgetArea whitelistWidget, ProgWidgetArea blacklistWidget) {
        if (whitelistWidget == null) return;
        ProgWidgetArea widget = whitelistWidget;
        while (widget != null) {
            widget.getArea(area, new AreaTypeBox());
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        while (widget != null) {
            Set<BlockPos> blacklistedArea = new HashSet<>();
            widget.getArea(blacklistedArea, new AreaTypeBox());
            area.removeAll(blacklistedArea);
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetEntityAttack(getPosition(), useMaxActions, maxActions, checkSight);
    }

    @Override
    public void setMaxActions(int maxActions) {
        this.maxActions = maxActions;
    }

    @Override
    public int getMaxActions() {
        return maxActions;
    }

    @Override
    public void setUseMaxActions(boolean useMaxActions) {
        this.useMaxActions = useMaxActions;
    }

    @Override
    public boolean useMaxActions() {
        return useMaxActions;
    }

    @Override
    public void setCheckSight(boolean checkSight) {
        this.checkSight = checkSight;
    }

    @Override
    public boolean isCheckSight() {
        return checkSight;
    }

}
