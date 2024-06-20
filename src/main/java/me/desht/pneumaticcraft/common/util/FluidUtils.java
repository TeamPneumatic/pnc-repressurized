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

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

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
     * Have the player attempt to insert liquid into a block entity, which must support FLUID_HANDLER_CAPABILITY.
     * The player's held item will be updated if any fluid was inserted.
     *
     * @param te the block entity to insert into
     * @param face the face of the block entity's block to insert to
     * @param player the player
     * @param hand the hand being used
     * @return true if any fluid was inserted, false otherwise
     */
    public static boolean tryFluidInsertion(BlockEntity te, Direction face, Player player, InteractionHand hand) {
        return doFluidInteraction(te, face, player, hand, true);
    }

    /**
     * Have the player attempt to extract liquid from a block entity, which must support FLUID_HANDLER_CAPABILITY.
     * The player's held item will be updated if any fluid was extracted.
     *
     * @param te the block entity to extract from
     * @param face the face of the block entity's block to extract from
     * @param player the player
     * @param hand the hand being used
     * @return true if any fluid was extracted, false otherwise
     */
    public static boolean tryFluidExtraction(BlockEntity te, Direction face, Player player, InteractionHand hand) {
        return doFluidInteraction(te, face, player, hand, false);
    }

    private static boolean doFluidInteraction(BlockEntity te, Direction face, Player player, InteractionHand hand, boolean isInserting) {
        ItemStack stack = player.getItemInHand(hand);
        return FluidUtil.getFluidHandler(stack).map(stackHandler -> {
            if (IOHelper.getFluidHandlerForBlock(te, face).isPresent()) {
                if (stackHandler.getTanks() == 0) return false;
                int capacity = stackHandler.getTankCapacity(0);
                return IOHelper.getFluidHandlerForBlock(te, face).map(handler -> {
                    PlayerInvWrapper invWrapper = new PlayerInvWrapper(player.getInventory());
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
    public static boolean isSourceFluidBlock(Level world, BlockPos pos) {
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
    public static boolean isSourceFluidBlock(Level world, BlockPos pos, @Nullable Fluid fluid) {
        FluidState state = world.getFluidState(pos);
        return state.isSource() && fluid == null || state.getType() == fluid;
    }

    /**
     * Check if the given blockpos contains a flowing fluid block of any fluid.
     * @param world the world
     * @param pos the blockpos
     * @return true if there is a fluid block at the given blockpos, which is not a source block
     */
    public static boolean isFlowingFluidBlock(Level world, BlockPos pos) {
        return isFlowingFluidBlock(world, pos, null);
    }

    /**
     * Check if the given blockpos contains a flowing fluid block of the given fluid type.
     * @param world the world
     * @param pos the blockpos
     * @param fluid the fluid (null to match any fluid)
     * @return true if there is a matching fluid block at the given blockpos, which is not a source block
     */
    public static boolean isFlowingFluidBlock(Level world, BlockPos pos, @Nullable Fluid fluid) {
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
    public static FluidStack tryPickupFluid(IFluidHandler fluidCap, Level world, BlockPos pos, boolean playSound, IFluidHandler.FluidAction action) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return FluidStack.EMPTY;
        }

        FluidState fluidState = state.getFluidState();
        Fluid fluid = fluidState.getType();
        if (fluid == Fluids.EMPTY || !fluid.isSource(fluidState)) {
            return FluidStack.EMPTY;
        }
        FluidTank tank = new FluidTank(BUCKET_VOLUME);
        tank.setFluid(new FluidStack(fluid, BUCKET_VOLUME));
        FluidStack maybeSent = FluidUtil.tryFluidTransfer(fluidCap, tank, BUCKET_VOLUME, false);
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
            pickup.pickupBlock(null, world, pos, state);
        }
        FluidStack transferred = FluidUtil.tryFluidTransfer(fluidCap, tank, BUCKET_VOLUME, action.execute());

        if (!transferred.isEmpty() && action.execute() && playSound) {
            playFillSound(world, pos, fluid);
        }

        return transferred;
    }

    /**
     * Try to pour 1000mB (one bucket) of fluid into the world at the given position.
     *
     * @param handler the fluid handler to extract from
     * @param world the world
     * @param pos block to pour fluid at
     * @param playSound true to play a bucket-empty sound
     * @param force if true, fluid will be poured out even if the block space is not completely empty
     * @return true if fluid was poured, false otherwise
     */
    public static boolean tryPourOutFluid(IFluidHandler handler, Level world, BlockPos pos, boolean playSound, boolean force, IFluidHandler.FluidAction action) {
        // code partially lifted from BucketItem

        BlockState blockstate = world.getBlockState(pos);
        boolean isReplaceable = blockstate.canBeReplaced();
        boolean isNotSolid = !blockstate.isSolid(); //!material.isSolid();

        // if not force-placing then block must be:
        // - a waterloggable block (which is NOT currently waterlogged), or
        // - a replaceable block, NOT including fluid source blocks
        if (!force && (isSourceFluidBlock(world, pos) || !isReplaceable && !(blockstate.getBlock() instanceof LiquidBlockContainer)))
            return false;

        boolean didWork = false;
        FluidStack toPlace = handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (toPlace.getAmount() >= BUCKET_VOLUME) {
            // must be a full bucket's worth to place in the world
            Fluid fluid = toPlace.getFluid();
            Block block = blockstate.getBlock();
            if (world.isEmptyBlock(pos) || isNotSolid || isReplaceable
                    || block instanceof LiquidBlockContainer lbc && lbc.canPlaceLiquid(null, world, pos, blockstate, toPlace.getFluid())) {
                if (action.execute()) {
                    if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
                        // no pouring water in the nether!
                        playEvaporationEffects(world, pos);
                    } else if (block instanceof LiquidBlockContainer) {
                        // a block which can take fluid, e.g. waterloggable block like a slab
                        FluidState still = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getSource(false) : fluid.defaultFluidState();
                        if (((LiquidBlockContainer) block).placeLiquid(world, pos, blockstate, still) && playSound) {
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
                        world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL);
                    }
                }
                didWork = true;
            }
        }

        if (didWork && action.execute()) {
            handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        }

        return didWork;
    }

    private static void playEmptySound(Level world, BlockPos pos, Fluid fluid) {
        SoundEvent soundevent = fluid.getFluidType().getSound(SoundActions.BUCKET_EMPTY);
        if(soundevent == null) soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        world.playSound(null, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void playFillSound(Level world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
        if (soundEvent == null) soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void playEvaporationEffects(Level world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
        for(int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    public static ItemStack createFluidContainingItem(ItemLike item, FluidStack fluid) {
        ItemStack tank = new ItemStack(item);
        IFluidHandlerItem handler = tank.getCapability(Capabilities.FluidHandler.ITEM, null);
        if (handler == null) {
            return ItemStack.EMPTY;
        } else {
            handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            return handler.getContainer();
        }
    }

    //------------------------------- temp methods copied from Forge FluidUtil to work around a bug there ------------------
    // TODO remove when Forge is updated for PR 6797 and we're building against it

    @Nonnull
    private static FluidActionResult tryEmptyContainerAndStow(@Nonnull ItemStack container, IFluidHandler fluidDestination, IItemHandler inventory, int maxAmount, @Nullable Player player, boolean doDrain)
    {
        if (container.isEmpty())
        {
            return FluidActionResult.FAILURE;
        }

        if (player != null && player.getAbilities().instabuild)
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
    private static FluidActionResult tryEmptyContainer(@Nonnull ItemStack container, IFluidHandler fluidDestination, int maxAmount, @Nullable Player player, boolean doDrain)
    {
        ItemStack containerCopy = container.copyWithCount(1); // do not modify the input
        return FluidUtil.getFluidHandler(containerCopy)
                .map(containerFluidHandler -> {

                    // We are acting on a COPY of the stack, so performing changes is acceptable even if we are simulating.
                    FluidStack transfer = FluidUtil.tryFluidTransfer(fluidDestination, containerFluidHandler, maxAmount, doDrain);
                    if (transfer.isEmpty())
                        return FluidActionResult.FAILURE;

                    if (doDrain && player != null)
                    {
                        SoundEvent soundevent = transfer.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                        if (soundevent != null) {
                            player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }

                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new FluidActionResult(resultContainer);
                })
                .orElse(FluidActionResult.FAILURE);
    }
}
