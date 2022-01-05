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

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

public class FluidUtils {
    /**
     * Attempt to extract fluid from the given fluid handler into the given fluid-containing item.
     *
     * @param srcHandler fluid handler into which to place the fluid
     * @param destStack the fluid container item to extract from
     * @param returnedItems the modified fluid container after extraction
     * @return true if any fluid was moved, false otherwise
     */
    public static boolean tryFluidExtraction(IFluidHandler srcHandler, ItemStack destStack, NonNullList<ItemStack> returnedItems) {
        FluidActionResult result = FluidUtil.tryFillContainer(destStack, srcHandler, 1000, null, true);
        if (result.isSuccess()) {
            returnedItems.add(result.getResult());
            destStack.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to insert fluid into the given fluid handler from the given fluid container item.
     *
     * @param handler the handler to extract from
     * @param srcStack the fluid container item to insert to
     * @param returnedItems the modified fluid container after insertion
     * @return true if any fluid was moved, false otherwise
     */
    public static boolean tryFluidInsertion(IFluidHandler handler, ItemStack srcStack, NonNullList<ItemStack> returnedItems) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(srcStack, handler, 1000, null, true);
        if (result.isSuccess()) {
            returnedItems.add(result.getResult());
            srcStack.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Have the player attempt to insert liquid into a tile entity, which must support FLUID_HANDLER_CAPABILITY.
     * The player's held item will be updated if any fluid was inserted.
     *
     * @param te the tile entity to insert into
     * @param face the face of the tile entity's block to insert to
     * @param player the player
     * @param hand the hand being used
     * @return true if any fluid was inserted, false otherwise
     */
    public static boolean tryFluidInsertion(TileEntity te, Direction face, PlayerEntity player, Hand hand) {
        return doFluidInteraction(te, face, player, hand, true);
    }

    /**
     * Have the player attempt to extract liquid from a tile entity, which must support FLUID_HANDLER_CAPABILITY.
     * The player's held item will be updated if any fluid was extracted.
     *
     * @param te the tile entity to extract from
     * @param face the face of the tile entity's block to extract from
     * @param player the player
     * @param hand the hand being used
     * @return true if any fluid was extracted, false otherwise
     */
    public static boolean tryFluidExtraction(TileEntity te, Direction face, PlayerEntity player, Hand hand) {
        return doFluidInteraction(te, face, player, hand, false);
    }

    private static boolean doFluidInteraction(TileEntity te, Direction face, PlayerEntity player, Hand hand, boolean isInserting) {
        ItemStack stack = player.getItemInHand(hand);
        return FluidUtil.getFluidHandler(stack).map(stackHandler -> {
            if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).isPresent()) {
                if (stackHandler.getTanks() == 0) return false;
                int capacity = stackHandler.getTankCapacity(0);
                return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).map(handler -> {
                    PlayerInvWrapper invWrapper = new PlayerInvWrapper(player.inventory);
                    FluidActionResult result = isInserting ?
                            FluidUtils.tryEmptyContainerAndStow(player.getItemInHand(hand), handler, invWrapper, capacity, player, true) :
                            FluidUtil.tryFillContainerAndStow(player.getItemInHand(hand), handler, invWrapper, capacity, player, true);
                    if (result.isSuccess()) {
                        player.setItemInHand(hand, result.getResult());
                        return true;
                    }
                    return false;
                }).orElse(false);
            }
            return false;
        }).orElse(false);
    }

    /**
     * Check if the given blockpos contains a fluid source block.
     *
     * @param world the world
     * @param pos the blockpos
     * @return true if there is a fluid source block at the given blockpos, false otherwise
     */
    public static boolean isSourceFluidBlock(World world, BlockPos pos) {
        return isSourceFluidBlock(world, pos, null);
    }

    /**
     * Check if the given blockpos contains a fluid source block of a certain fluid (or possibly any fluid)
     *
     * @param world the world
     * @param pos the blockpos
     * @param fluid the fluid, may be null to match any fluid
     * @return true if there is a fluid source block of the right fluid at the given blockpos, false otherwise
     */
    public static boolean isSourceFluidBlock(World world, BlockPos pos, @Nullable Fluid fluid) {
        FluidState state = world.getFluidState(pos);
        return state.isSource() && fluid == null || state.getType() == fluid;
    }

    /**
     * Check if the given blockpos contains a flowing fluid block of any fluid.
     * @param world the world
     * @param pos the blockpos
     * @return true if there is a fluid block at the given blockpos, which is not a source block
     */
    public static boolean isFlowingFluidBlock(World world, BlockPos pos) {
        return isFlowingFluidBlock(world, pos, null);
    }

    /**
     * Check if the given blockpos contains a flowing fluid block of the given fluid type.
     * @param world the world
     * @param pos the blockpos
     * @param fluid the fluid (null to match any fluid)
     * @return true if there is a matching fluid block at the given blockpos, which is not a source block
     */
    public static boolean isFlowingFluidBlock(World world, BlockPos pos, @Nullable Fluid fluid) {
        FluidState state = world.getFluidState(pos);
        return !state.isEmpty() && !state.isSource() && (fluid == null || fluid == state.getType());
    }

    /**
     * Attempt to pick up 1000mB (one bucket) fluid from the world and stow it in the given fluid handler.
     *
     * @param fluidCap the fluid capability to insert fluid to
     * @param world the world
     * @param pos the block pos to pull fluid from
     * @param playSound true to play a bucket-fill sound
     * @param action whether to simulate the action
     * @return the fluidstack which was picked up, or FluidStack.EMPTY
     */
    public static FluidStack tryPickupFluid(LazyOptional<IFluidHandler> fluidCap, World world, BlockPos pos, boolean playSound, IFluidHandler.FluidAction action) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof IBucketPickupHandler)) {
            return FluidStack.EMPTY;
        }

        FluidState fluidState = state.getFluidState();
        Fluid fluid = fluidState.getType();
        if (fluid == Fluids.EMPTY || !fluid.isSource(fluidState)) {
            return FluidStack.EMPTY;
        }
        FluidTank tank = new FluidTank(BUCKET_VOLUME);
        tank.setFluid(new FluidStack(fluid, BUCKET_VOLUME));
        FluidStack maybeSent = fluidCap.map(
                h -> FluidUtil.tryFluidTransfer(h, tank, BUCKET_VOLUME, false)
        ).orElse(FluidStack.EMPTY);
        if (maybeSent.getAmount() != BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }
        // actually do the pickup & transfer now
        boolean removeBlock = true;
        if (fluid == Fluids.WATER && ConfigHelper.common().advanced.dontUpdateInfiniteWaterSources.get()) {
            int n = 0;
            for (Direction d : DirectionUtil.HORIZONTALS) {
                if (world.getFluidState(pos.relative(d)).getType() == Fluids.WATER && ++n >= 2) {
                    removeBlock = false;
                    break;
                }
            }
        }
        if (removeBlock && action.execute()) {
            ((IBucketPickupHandler) state.getBlock()).takeLiquid(world, pos, state);
        }
        FluidStack transferred = fluidCap.map(h ->
                FluidUtil.tryFluidTransfer(h, tank, BUCKET_VOLUME, action.execute()))
                .orElse(FluidStack.EMPTY);
        if (!transferred.isEmpty() && playSound) {
            playFillSound(world, pos, fluid);
        }
        return transferred;
    }

    /**
     * Try to pour 1000mB (one bucket) of fluid into the world at the given position.
     *
     * @param fluidCap the fluid capability
     * @param world the world
     * @param pos block to pour fluid at
     * @param playSound true to play a bucket-empty sound
     * @param force if true, fluid will be poured out even if the block space is not completely empty
     * @return true if fluid was poured, false otherwise
     */
    public static boolean tryPourOutFluid(LazyOptional<IFluidHandler> fluidCap, World world, BlockPos pos, boolean playSound, boolean force, IFluidHandler.FluidAction action) {
        // code partially lifted from BucketItem

        BlockState blockstate = world.getBlockState(pos);
        Material material = blockstate.getMaterial();
        boolean isReplaceable = material.isReplaceable();
        boolean isNotSolid = !material.isSolid();

        // if not force-placing then block must be:
        // - a waterloggable block (which is NOT currently waterlogged), or
        // - a replaceable block, NOT including fluid source blocks
        if (!force && (isSourceFluidBlock(world, pos) || !isReplaceable && !(blockstate.getBlock() instanceof ILiquidContainer)))
            return false;

        boolean didWork = fluidCap.map(handler -> {
            FluidStack toPlace = handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
            if (toPlace.getAmount() < BUCKET_VOLUME) {
                return false;  // must be a full bucket's worth to place in the world
            }
            Fluid fluid = toPlace.getFluid();
            Block block = blockstate.getBlock();
            if (world.isEmptyBlock(pos) || isNotSolid || isReplaceable
                    || block instanceof ILiquidContainer && ((ILiquidContainer)block).canPlaceLiquid(world, pos, blockstate, toPlace.getFluid())) {
                if (action.execute()) {
                    if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
                        // no pouring water in the nether!
                        playEvaporationEffects(world, pos);
                    } else if (block instanceof ILiquidContainer) {
                        // a block which can take fluid, e.g. waterloggable block like a slab
                        FluidState still = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getSource(false) : fluid.defaultFluidState();
                        if (((ILiquidContainer) block).placeLiquid(world, pos, blockstate, still) && playSound) {
                            playEmptySound(world, pos, fluid);
                        }
                    } else {
                        // air or some non-solid/replaceable block: just overwrite with the fluid
                        if (playSound) {
                            playEmptySound(world, pos, fluid);
                        }
                        if (isNotSolid || isReplaceable) {
                            world.destroyBlock(pos, true);
                        }
                        world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Constants.BlockFlags.DEFAULT);
                    }
                }
                return true;
            }
            return false;
        }).orElse(false);

        if (didWork && action.execute()) {
            fluidCap.ifPresent(handler -> handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE));
        }
        return didWork;
    }

    private static void playEmptySound(World world, BlockPos pos, Fluid fluid) {
        SoundEvent soundevent = fluid.getAttributes().getEmptySound();
        if(soundevent == null) soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static void playFillSound(World world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getAttributes().getFillSound();
        if (soundEvent == null) soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static void playEvaporationEffects(World world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
        for(int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    public static boolean matchFluid(FluidIngredient fluidIngredient, FluidStack fluidStack, boolean matchTags) {
        return fluidIngredient.testFluid(fluidStack);
    }

    public static boolean matchFluid(FluidIngredient fluidIngredient, Fluid fluid, boolean matchTags) {
        return fluidIngredient.testFluid(fluid);
    }

    //------------------------------- temp methods copied from Forge FluidUtil to work around a bug there ------------------
    // TODO remove when Forge is updated for PR 6797 and we're building against it

    @Nonnull
    private static FluidActionResult tryEmptyContainerAndStow(@Nonnull ItemStack container, IFluidHandler fluidDestination, IItemHandler inventory, int maxAmount, @Nullable PlayerEntity player, boolean doDrain)
    {
        if (container.isEmpty())
        {
            return FluidActionResult.FAILURE;
        }

        if (player != null && player.abilities.instabuild)
        {
            FluidActionResult emptiedReal = tryEmptyContainer(container, fluidDestination, maxAmount, player, doDrain);
            if (emptiedReal.isSuccess())
            {
                return new FluidActionResult(container); // creative mode: item does not change
            }
        }
        else if (container.getCount() == 1) // don't need to stow anything, just fill and edit the container stack
        {
            FluidActionResult emptiedReal = tryEmptyContainer(container, fluidDestination, maxAmount, player, doDrain);
            if (emptiedReal.isSuccess())
            {
                return emptiedReal;
            }
        }
        else
        {
            FluidActionResult emptiedSimulated = tryEmptyContainer(container, fluidDestination, maxAmount, player, false);
            if (emptiedSimulated.isSuccess())
            {
                // check if we can give the itemStack to the inventory
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, emptiedSimulated.getResult(), true);
                if (remainder.isEmpty() || player != null)
                {
                    FluidActionResult emptiedReal = tryEmptyContainer(container, fluidDestination, maxAmount, player, doDrain);
                    remainder = ItemHandlerHelper.insertItemStacked(inventory, emptiedReal.getResult(), !doDrain);

                    // give it to the player or drop it at their feet
                    if (!remainder.isEmpty() && player != null && doDrain)
                    {
                        ItemHandlerHelper.giveItemToPlayer(player, remainder);
                    }

                    ItemStack containerCopy = container.copy();
                    containerCopy.shrink(1);
                    return new FluidActionResult(containerCopy);
                }
            }
        }

        return FluidActionResult.FAILURE;
    }

    // copied from FluidUtil.tryEmptyContainer() to work around a bug where tryFluidTransfer() is always called with doTransfer=true
    // even for simulations
    @Nonnull
    private static FluidActionResult tryEmptyContainer(@Nonnull ItemStack container, IFluidHandler fluidDestination, int maxAmount, @Nullable PlayerEntity player, boolean doDrain)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return FluidUtil.getFluidHandler(containerCopy)
                .map(containerFluidHandler -> {

                    // We are acting on a COPY of the stack, so performing changes is acceptable even if we are simulating.
                    FluidStack transfer = FluidUtil.tryFluidTransfer(fluidDestination, containerFluidHandler, maxAmount, doDrain);
                    if (transfer.isEmpty())
                        return FluidActionResult.FAILURE;

                    if (doDrain && player != null)
                    {
                        SoundEvent soundevent = transfer.getFluid().getAttributes().getEmptySound(transfer);
                        player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }

                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new FluidActionResult(resultContainer);
                })
                .orElse(FluidActionResult.FAILURE);
    }
}
