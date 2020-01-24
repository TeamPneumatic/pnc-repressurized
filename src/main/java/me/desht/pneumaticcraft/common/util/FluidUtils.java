package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

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
        ItemStack stack = player.getHeldItem(hand);
        return FluidUtil.getFluidHandler(stack).map(stackHandler -> {
            if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).isPresent()) {
                if (stackHandler.getTanks() == 0) return false;
                int capacity = stackHandler.getTankCapacity(0);
                return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).map(handler -> {
                    PlayerInvWrapper invWrapper = new PlayerInvWrapper(player.inventory);
                    FluidActionResult result = isInserting ?
                            FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(hand), handler, invWrapper, capacity, player, true) :
                            FluidUtil.tryFillContainerAndStow(player.getHeldItem(hand), handler, invWrapper, capacity, player, true);
                    if (result.isSuccess()) {
                        player.setHeldItem(hand, result.getResult());
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
    public static boolean isSourceBlock(World world, BlockPos pos) {
        return isSourceBlock(world, pos, null);
    }

    /**
     * Check if the given blockpos contains a fluid source block of a certain fluid (or possibly any fluid)
     *
     * @param world the world
     * @param pos the blockpos
     * @param fluid the fluid, may be null to match any fluid
     * @return true if there is a fluid source block of the right fluid at the given blockpos, false otherwise
     */
    public static boolean isSourceBlock(World world, BlockPos pos, Fluid fluid) {
        IFluidState state = world.getFluidState(pos);
        return state.isSource() && fluid == null || state.getFluid() == fluid;
    }

//    /**
//     * Get a fluidstack for the fluid at the given position, possibly also draining it.
//     *
//     * @param world the world
//     * @param pos the blockpos
//     * @param doDrain true if the fluid at the position should be drained
//     * @return a fluidstack of the fluid, or null if no fluid could be drained
//     */
//    public static FluidStack getFluidAt(World world, BlockPos pos, boolean doDrain) {
//        return FluidUtil.getFluidHandler(world, pos, Direction.UP)
//                .map(handler -> handler.drain(FluidAttributes.BUCKET_VOLUME, doDrain)).orElse(null);
//    }

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

        IFluidState fluidState = state.getFluidState();
        Fluid fluid = fluidState.getFluid();
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
        ((IBucketPickupHandler) state.getBlock()).pickupFluid(world, pos, state);
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
        if (!force && !(world.isAirBlock(pos) || world.getBlockState(pos).getBlock() instanceof ILiquidContainer)) {
            return false;
        }

        // code partially lifted from BucketItem
        BlockState blockstate = world.getBlockState(pos);
        Material material = blockstate.getMaterial();
        boolean isNotSolid = !material.isSolid();
        boolean isReplaceable = material.isReplaceable();

        boolean didWork = fluidCap.map(handler -> {
            FluidStack toPlace = handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
            if (toPlace.getAmount() < BUCKET_VOLUME) {
                return false;  // must be a full bucket's worth to place in the world
            }
            Fluid fluid = toPlace.getFluid();
            Block block = blockstate.getBlock();
            if (world.isAirBlock(pos) || isNotSolid || isReplaceable
                    || block instanceof ILiquidContainer && ((ILiquidContainer)block).canContainFluid(world, pos, blockstate, toPlace.getFluid())) {
                if (action.execute()) {
                    if (world.dimension.doesWaterVaporize() && fluid.isIn(FluidTags.WATER)) {
                        // no pouring water in the nether!
                        playEvaporationEffects(world, pos);
                    } else if (block instanceof ILiquidContainer) {
                        // a block which can take fluid, e.g. waterloggable block like a slab
                        IFluidState still = fluid instanceof FlowingFluid ? ((FlowingFluid) fluid).getStillFluidState(false) : fluid.getDefaultState();
                        if (((ILiquidContainer) block).receiveFluid(world, pos, blockstate, still) && playSound) {
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
                        world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 3);
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
        if(soundevent == null) soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static void playFillSound(World world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.getAttributes().getFillSound();
        if (soundEvent == null) soundEvent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static void playEvaporationEffects(World world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
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

//    public static boolean matchFluid(FluidStack fluid, FluidStack fluid2, boolean matchTags) {
//        return matchFluid(fluid.getFluid(), fluid2.getFluid(), matchTags);
//    }
//
//    public static boolean matchFluid(Fluid fluid, Fluid fluid2, boolean matchTags) {
//        return fluid == fluid2 || matchTags && !Sets.intersection(fluid.getTags(), fluid2.getTags()).isEmpty();
//    }
//
//    public static boolean matchFluid(Fluid fluid, Fluid fluid2, boolean matchTags) {
//        return fluid == fluid2 || matchTags && !Sets.intersection(fluid.getTags(), fluid2.getTags()).isEmpty();
//    }

    public static FluidTank copyTank(IFluidTank tank) {
        FluidTank res = new FluidTank(tank.getCapacity());
        res.fill(tank.getFluid(), IFluidHandler.FluidAction.EXECUTE);
        return res;
    }
}
