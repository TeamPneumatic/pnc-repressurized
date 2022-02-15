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

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SeismicSensorItem extends Item {
    private static final int MAX_SEARCH = 500;

    private static final Set<ResourceLocation> fluidsOfInterest = new HashSet<>();
    private static boolean needRecache = true;  // recache on first startup & when tags are reloaded

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
        if (needRecache) {
            fluidsOfInterest.clear();
            Set<ResourceLocation> tagsFromConfig = ConfigHelper.common().machines.seismicSensorFluidTags.get().stream()
                    .map(ResourceLocation::new)
                    .collect(Collectors.toSet());
            Set<ResourceLocation> fluidsFromConfig = ConfigHelper.common().machines.seismicSensorFluids.get().stream()
                    .map(ResourceLocation::new)
                    .collect(Collectors.toSet());
            for (Fluid f : ForgeRegistries.FLUIDS.getValues()) {
                if (!Sets.intersection(f.getTags(), tagsFromConfig).isEmpty()) {
                    fluidsOfInterest.add(f.getRegistryName());
                } else if (fluidsFromConfig.contains(f.getRegistryName())) {
                    fluidsOfInterest.add(f.getRegistryName());
                }
            }
            needRecache = false;
        }

        FluidState state = world.getFluidState(pos);
        return fluidsOfInterest.contains(state.getType().getRegistryName()) ? state.getType() : null;
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

    public static void clearCachedFluids() {
        needRecache = true;
    }
}
