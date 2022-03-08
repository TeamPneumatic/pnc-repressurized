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

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class SeismicSensorItem extends Item {
    private static final int MAX_SEARCH = 500;

    public SeismicSensorItem() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (!world.isClientSide && player != null) {
            BlockPos.MutableBlockPos searchPos = ctx.getClickedPos().mutable();
            while (searchPos.getY() > world.getMinBuildHeight()) {
                searchPos.move(Direction.DOWN);
                Fluid fluid = findFluid(world, searchPos);
                if (fluid != null) {
                    Set<BlockPos> fluidPositions = findLake(world, searchPos.immutable(), fluid);
                    int count = Math.max(1, fluidPositions.size() / 10 * 10);
                    player.displayClientMessage(new TranslatableComponent(
                            "pneumaticcraft.message.seismicSensor.foundOilDetails",
                            new TranslatableComponent(fluid.getAttributes().getTranslationKey()),
                            ChatFormatting.GREEN.toString() + (ctx.getClickedPos().getY() - searchPos.getY()),
                            ChatFormatting.GREEN.toString() + count),
                            false);
                    world.playSound(null, ctx.getClickedPos(), SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1f);
                    return InteractionResult.SUCCESS;
                }
            }
            player.displayClientMessage(new TranslatableComponent("pneumaticcraft.message.seismicSensor.noOilFound"), false);
        }
        return InteractionResult.SUCCESS; // we don't want to use the item.
    }

    private Fluid findFluid(Level world, BlockPos pos) {
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
}
