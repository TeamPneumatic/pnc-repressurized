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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.IToolUser;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.registry.ModHarvestHandlers;
import me.desht.pneumaticcraft.common.registry.ModHoeHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DroneAIHarvest<W extends ProgWidgetAreaItemBase & IToolUser> extends DroneAIBlockInteraction<W> {
    public DroneAIHarvest(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    public boolean canUse() {
        if (abortIfRequiredHoeIsMissing()) return false;
        return super.canUse();
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (abortIfRequiredHoeIsMissing()) return false;
        return getApplicableHandler(pos).isPresent();
    }

    private boolean abortIfRequiredHoeIsMissing() {
        if (progWidget.requiresTool() && getDamageableHoe() == null) {
            abort();
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.harvest.debug.missingHoe");
            return true;
        } else {
            return false;
        }
    }

    private Optional<HarvestHandler> getApplicableHandler(BlockPos pos) {
        BlockState state = worldCache.getBlockState(pos);
        return ModHarvestHandlers.HARVEST_HANDLER_REGISTRY.stream()
                .filter(handler -> handler.canHarvest(drone.world(), worldCache, pos, state, drone) &&
                        hasApplicableItemFilters(handler, pos, state))
                .findFirst();
    }

    private boolean hasApplicableItemFilters(HarvestHandler harvestHandler, BlockPos pos, BlockState blockState) {
        List<ItemStack> droppedStacks = harvestHandler.addFilterItems(drone.world(), worldCache, pos, blockState, drone);
        return droppedStacks.stream().anyMatch(droppedStack -> progWidget.isItemValidForFilters(droppedStack, blockState));
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        getApplicableHandler(pos).ifPresent(applicableHandler -> {
            BlockState state = worldCache.getBlockState(pos);
            if (applicableHandler.canHarvest(drone.world(), worldCache, pos, state, drone)) {
                Consumer<Player> damageableHoe = getDamageableHoe();
                if (damageableHoe != null) {
                    if (applicableHandler.harvestAndReplant(drone.world(), worldCache, pos, state, drone)) {
                        damageableHoe.accept(drone.getFakePlayer());
                    }
                } else {
                    applicableHandler.harvest(drone.world(), worldCache, pos, state, drone);
                }

            }
        });
        return false;
    }

    private Consumer<Player> getDamageableHoe() {
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            HoeHandler handler = ModHoeHandlers.HOE_HANDLER_REGISTRY.stream()
                    .filter(hoeHandler -> hoeHandler.test(stack))
                    .findFirst().orElse(null);
            if (handler != null) return handler.getConsumer(stack);
        }
        return null;
    }
}
