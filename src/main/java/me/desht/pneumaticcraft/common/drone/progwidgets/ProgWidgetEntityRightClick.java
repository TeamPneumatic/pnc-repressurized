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
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneEntityBase;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEntityRightClick extends ProgWidget implements IAreaProvider, IEntityProvider {

    private EntityFilterPair<ProgWidgetEntityRightClick> entityFilters;

    public ProgWidgetEntityRightClick() {
        super(ModProgWidgets.ENTITY_RIGHT_CLICK.get());
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
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ENTITY_RIGHT_CLICK;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget progWidget) {
        return new DroneEntityBase<IEntityProvider, LivingEntity>(drone, (IEntityProvider) progWidget) {
            private final Set<Entity> visitedEntities = new HashSet<>();

            @Override
            protected boolean isEntityValid(Entity entity) {
                return entity instanceof LivingEntity && !visitedEntities.contains(entity);
            }

            @Override
            protected boolean doAction() {
                visitedEntities.add(targetedEntity);
                ItemStack stack = drone.getInv().getStackInSlot(0);
                Player fakePlayer = drone.getFakePlayer();
                if (stack.getItem().interactLivingEntity(stack, fakePlayer, targetedEntity, InteractionHand.MAIN_HAND).consumesAction()
                        || targetedEntity.interact(fakePlayer, InteractionHand.MAIN_HAND).consumesAction()) {
                    // fake player's inventory has probably been modified in some way
                    // copy items back to drone inventory, dropping on the ground any items that don't fit
                    for (int i = 0; i < fakePlayer.getInventory().items.size(); i++) {
                        ItemStack fakePlayerStack = fakePlayer.getInventory().items.get(i);
                        if (i < drone.getInv().getSlots()) {
                            drone.getInv().setStackInSlot(i, fakePlayerStack);
                        } else if (!fakePlayerStack.isEmpty()) {
                            drone.dropItem(fakePlayerStack);
                            fakePlayer.getInventory().items.set(i, ItemStack.EMPTY);
                        }
                    }
                }
                return false; // always returning false, to indicate we're done trying
            }
        };
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
        ProgWidgetEntityAttack.getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[2]);
    }
}
