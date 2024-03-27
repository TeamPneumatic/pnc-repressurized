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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.*;

public class SeismicSensorItem extends Item {
    private static final int MAX_SEARCH = 500;

    public SeismicSensorItem() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (level.isClientSide && player != null) {
            BlockPos.MutableBlockPos searchPos = ctx.getClickedPos().mutable();
            findFluid(level, searchPos).ifPresentOrElse(result -> {
                int topOff = ctx.getClickedPos().getY() - result.top().getY();
                int bottomOff = ctx.getClickedPos().getY() - result.bottom().getY();
                String depthStr = topOff == bottomOff ? Integer.toString(topOff) : topOff + "-" + bottomOff;
                player.displayClientMessage(Component.translatable(
                                "pneumaticcraft.message.seismicSensor.foundOilDetails",
                                Component.translatable(result.fluid().getFluidType().getDescriptionId()),
                                ChatFormatting.GREEN + depthStr,
                                ChatFormatting.GREEN.toString() + result.lakeSize()),
                        false);
                player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1f, 1f);
            }, () -> {
                player.displayClientMessage(Component.translatable("pneumaticcraft.message.seismicSensor.noOilFound"), false);
            });
        }
        return InteractionResult.SUCCESS; // we don't want to use the item.
    }

    private Optional<FluidSearchResult> findFluid(Level level, BlockPos origin) {
        BlockPos.MutableBlockPos searchPos = origin.mutable();
        while (searchPos.getY() > level.getMinBuildHeight()) {
            searchPos.move(Direction.DOWN);
            Fluid fluid = getFluidOfInterest(level, searchPos);
            if (fluid != null) {
                BlockPos top = searchPos.immutable();
                do {
                    searchPos.move(Direction.DOWN);
                } while (searchPos.getY() > level.getMinBuildHeight() && getFluidOfInterest(level, searchPos) == fluid);
                BlockPos bottom = searchPos.above().immutable();
                Set<BlockPos> fluidPositions = findLake(level, top, fluid);
                int lakeSize = Math.max(1, fluidPositions.size() / 10 * 10);
                return Optional.of(new FluidSearchResult(fluid, top, bottom, lakeSize));
            }
        }
        return Optional.empty();
    }

    private Fluid getFluidOfInterest(Level world, BlockPos pos) {
        FluidState state = world.getFluidState(pos);
        return state.getType().is(PneumaticCraftTags.Fluids.SEISMIC) ? state.getType() : null;
    }

    private Set<BlockPos> findLake(Level world, BlockPos searchPos, Fluid fluid) {
        Set<BlockPos> fluidPositions = new HashSet<>();
        Deque<BlockPos> pendingPositions = new ArrayDeque<>();
        pendingPositions.add(searchPos);
        while (!pendingPositions.isEmpty() && fluidPositions.size() < MAX_SEARCH) {
            BlockPos checkingPos = pendingPositions.pop();
            for (Direction d : Direction.values()) {
                if (d != Direction.UP) {
                    BlockPos newPos = checkingPos.relative(d);
                    FluidState state = world.getFluidState(newPos);
                    if (state.getType() == fluid && state.isSource() && fluidPositions.add(newPos)) {
                        pendingPositions.add(newPos);
                    }
                }
            }
        }
        return fluidPositions;
    }

    record FluidSearchResult(Fluid fluid, BlockPos top, BlockPos bottom, int lakeSize) {
    }
}
