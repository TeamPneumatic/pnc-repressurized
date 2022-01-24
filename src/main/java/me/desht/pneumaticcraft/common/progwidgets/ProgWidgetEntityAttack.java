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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIAttackEntity;
import me.desht.pneumaticcraft.common.ai.DroneAINearestAttackableTarget;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaTypeBox;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEntityAttack extends ProgWidget implements IAreaProvider, IEntityProvider, IMaxActions {
    private EntityFilterPair<ProgWidgetEntityAttack> entityFilters;
    private int maxActions;
    private boolean useMaxActions;

    public ProgWidgetEntityAttack() {
        super(ModProgWidgets.ENTITY_ATTACK.get());
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
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIAttackEntity((EntityDrone) drone, 1.0D, false);
    }

    @Override
    public Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAINearestAttackableTarget((EntityDrone) drone, false, (ProgWidget) widget);
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ATTACK;
    }

    @Override
    public List<Entity> getValidEntities(Level world) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.getValidEntities(world);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.isEntityValid(entity);
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
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (useMaxActions) tag.putBoolean("useMaxActions", true);
        tag.putInt("maxActions", maxActions);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        useMaxActions = tag.getBoolean("useMaxActions");
        maxActions = tag.getInt("maxActions");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(useMaxActions);
        buf.writeVarInt(maxActions);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        useMaxActions = buf.readBoolean();
        maxActions = buf.readVarInt();
    }
}
