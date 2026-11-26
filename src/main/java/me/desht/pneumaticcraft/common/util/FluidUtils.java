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
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

public class FluidUtils {
    /**
     * Insert fluid into a fluid handler.
     *
     * @param handler the fluid handler
     * @param stack the fluid to insert; not modified
     * @param simulate true to only simulate the insertion
     * @return the amount of fluid inserted (or which would be inserted)
     */
    public static int fill(@Nullable ResourceHandler<FluidResource> handler, FluidStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int filled = handler.insert(FluidResource.of(stack), stack.getAmount(), tx);
            if (!simulate) tx.commit();
            return filled;
        }
    }

    /**
     * Extract a specific fluid from a fluid handler.
     *
     * @param handler the fluid handler
     * @param stack the fluid and the maximum amount to extract; not modified
     * @param simulate true to only simulate the extraction
     * @return the fluid extracted (or which would be extracted)
     */
    public static FluidStack drain(@Nullable ResourceHandler<FluidResource> handler, FluidStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) return FluidStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            int drained = handler.extract(FluidResource.of(stack), stack.getAmount(), tx);
            if (!simulate) tx.commit();
            return stack.copyWithAmount(drained);
        }
    }

    /**
     * Extract the first available fluid from a fluid handler.
     *
     * @param handler the fluid handler
     * @param maxAmount the maximum amount to extract
     * @param simulate true to only simulate the extraction
     * @return the fluid extracted (or which would be extracted)
     */
    public static FluidStack drain(@Nullable ResourceHandler<FluidResource> handler, int maxAmount, boolean simulate) {
        if (handler == null || maxAmount <= 0) return FluidStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            ResourceStack<FluidResource> drained = ResourceHandlerUtil.extractFirst(handler, r -> true, maxAmount, tx);
            if (drained == null) return FluidStack.EMPTY;
            if (!simulate) tx.commit();
            return drained.resource().toStack(drained.amount());
        }
    }

    /**
     * Move fluid from one handler to another.
     *
     * @param from the source handler
     * @param to the destination handler
     * @param maxAmount the maximum amount to move
     * @param simulate true to only simulate the move
     * @return the fluid moved (or which would be moved)
     */
    public static FluidStack transfer(@Nullable ResourceHandler<FluidResource> from, @Nullable ResourceHandler<FluidResource> to, int maxAmount, boolean simulate) {
        if (from == null || to == null || maxAmount <= 0) return FluidStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            ResourceStack<FluidResource> moved = ResourceHandlerUtil.moveFirst(from, to, r -> true, maxAmount, tx);
            if (moved == null) return FluidStack.EMPTY;
            if (!simulate) tx.commit();
            return moved.resource().toStack(moved.amount());
        }
    }

    /**
     * Move fluid between a fluid handler and the fluid container item(s) in an item entity, one container at a time.
     * Containers which are changed and don't stack with the remaining containers are dropped as a new item entity.
     *
     * @param entity the item entity
     * @param handler the fluid handler
     * @param maxAmount the maximum amount of fluid to move
     * @param fillItem true to move fluid from the handler into the item, false to move fluid from the item into the handler
     * @return the amount of fluid moved
     */
    public static int transferWithItemEntity(ItemEntity entity, ResourceHandler<FluidResource> handler, int maxAmount, boolean fillItem) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || maxAmount <= 0) return 0;
        // slot 0 holds the entity's items, slot 1 receives any changed container which doesn't stack with them
        ItemStacksResourceHandler work = new ItemStacksResourceHandler(2);
        work.set(0, ItemResource.of(stack), stack.getCount());
        ResourceHandler<FluidResource> itemHandler = ItemAccess.forHandlerIndex(work, 0).oneByOne().getCapability(Capabilities.Fluid.ITEM);
        if (itemHandler == null) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            ResourceStack<FluidResource> moved = fillItem ?
                    ResourceHandlerUtil.moveFirst(handler, itemHandler, r -> true, maxAmount, tx) :
                    ResourceHandlerUtil.moveFirst(itemHandler, handler, r -> true, maxAmount, tx);
            if (moved == null) return 0;
            tx.commit();
            ItemStack remaining = IOHelper.getStack(work, 0);
            ItemStack changed = IOHelper.getStack(work, 1);
            if (remaining.isEmpty()) {
                entity.setItem(changed);
            } else {
                entity.setItem(remaining);
                if (!changed.isEmpty()) {
                    entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), changed));
                }
            }
            return moved.amount();
        }
    }

    /**
     * Get the crafting remainder for a fluid container item used in a recipe: the container, with the given amount
     * of fluid drained (or not drained, if the item has a creative upgrade).
     *
     * @param instance the item
     * @param amount the amount of fluid to drain
     * @param requireFluid if true, there's no remainder if the item contains no fluid
     * @return the remainder, or null if there's none
     */
    @Nullable
    public static ItemStackTemplate getDrainedCraftingRemainder(ItemInstance instance, int amount, boolean requireFluid) {
        ItemStack stack = switch (instance) {
            case ItemStack s -> s.copyWithCount(1);
            case ItemStackTemplate t -> t.create().copyWithCount(1);
            default -> ItemStack.EMPTY;
        };
        if (stack.isEmpty()) return null;
        ItemStacksResourceHandler work = new ItemStacksResourceHandler(1);
        work.set(0, ItemResource.of(stack), 1);
        ResourceHandler<FluidResource> handler = ItemAccess.forHandlerIndexStrict(work, 0).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) return null;
        if (requireFluid && drain(handler, Integer.MAX_VALUE, true).isEmpty()) return null;
        if (!UpgradableItemUtils.hasCreativeUpgrade(stack)) {
            drain(handler, amount, false);
        }
        ItemStack result = IOHelper.getStack(work, 0);
        return result.isEmpty() ? null : ItemStackTemplate.fromNonEmptyStack(result);
    }

    /**
     * {@return the first fluid contained in an item, or FluidStack.EMPTY if it has none or isn't a fluid container}
     */
    public static FluidStack getFirstFluidContained(ItemStack stack) {
        return FluidUtil.getFirstStackContained(stack);
    }

    /**
     * {@return the fluid in the given tank of a fluid handler, as a new stack}
     */
    public static FluidStack getFluid(@Nullable ResourceHandler<FluidResource> handler, int tank) {
        return handler == null ? FluidStack.EMPTY : FluidUtil.getStack(handler, tank);
    }

    /**
     * Have the player attempt to insert liquid into a block entity, which must expose a fluid handler.
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
     * Have the player attempt to extract liquid from a block entity, which must expose a fluid handler.
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
        ResourceHandler<FluidResource> handler = IOHelper.getFluidHandlerForBlock(te, face).orElse(null);
        if (handler == null) return false;
        ResourceHandler<FluidResource> itemHandler = ItemAccess.forPlayerInteraction(player, hand).oneByOne().getCapability(Capabilities.Fluid.ITEM);
        if (itemHandler == null) return false;
        try (Transaction tx = Transaction.openRoot()) {
            ResourceStack<FluidResource> moved = isInserting ?
                    ResourceHandlerUtil.moveFirst(itemHandler, handler, r -> true, Integer.MAX_VALUE, tx) :
                    ResourceHandlerUtil.moveFirst(handler, itemHandler, r -> true, Integer.MAX_VALUE, tx);
            if (moved == null) return false;
            tx.commit();
            FluidUtil.triggerSoundAndGameEvent(moved.resource(), player.level(), Vec3.atCenterOf(te.getBlockPos()), player, !isInserting);
            return true;
        }
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
     * @param handler the fluid handler to insert fluid to
     * @param world the world
     * @param pos the block pos to pull fluid from
     * @param playSound true to play a bucket-fill sound
     * @param simulate true to only check if the fluid could be picked up
     * @return the fluidstack which was picked up, or FluidStack.EMPTY
     */
    public static FluidStack tryPickupFluid(ResourceHandler<FluidResource> handler, Level world, BlockPos pos, boolean playSound, boolean simulate) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return FluidStack.EMPTY;
        }

        FluidState fluidState = state.getFluidState();
        Fluid fluid = fluidState.getType();
        if (fluid == Fluids.EMPTY || !fluid.isSource(fluidState)) {
            return FluidStack.EMPTY;
        }
        FluidStack toPickup = new FluidStack(fluid, BUCKET_VOLUME);
        if (fill(handler, toPickup, true) != BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }
        if (simulate) {
            return toPickup;
        }

        // actually do the pickup & transfer now
        boolean removeBlock = true;
        if (fluid == Fluids.WATER && ConfigHelper.common().advanced.dontUpdateInfiniteWaterSources.get()) {
            int n = 0;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (world.getFluidState(pos.relative(d)).getType() == Fluids.WATER && ++n >= 2) {
                    removeBlock = false;
                    break;
                }
            }
        }
        if (removeBlock) {
            pickup.pickupBlock(null, world, pos, state);
        }
        int filled = fill(handler, toPickup, false);

        if (filled > 0 && playSound) {
            playFillSound(world, pos, fluid);
        }

        return toPickup.copyWithAmount(filled);
    }

    /**
     * Try to pour 1000mB (one bucket) of fluid into the world at the given position.
     *
     * @param handler the fluid handler to extract from
     * @param world the world
     * @param pos block to pour fluid at
     * @param playSound true to play a bucket-empty sound
     * @param force if true, fluid will be poured out even if the block space is not completely empty
     * @param simulate true to only check if fluid could be poured
     * @return true if fluid was poured, false otherwise
     */
    public static boolean tryPourOutFluid(ResourceHandler<FluidResource> handler, Level world, BlockPos pos, boolean playSound, boolean force, boolean simulate) {
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
        FluidStack toPlace = drain(handler, BUCKET_VOLUME, true);
        if (toPlace.getAmount() >= BUCKET_VOLUME) {
            // must be a full bucket's worth to place in the world
            Fluid fluid = toPlace.getFluid();
            Block block = blockstate.getBlock();
            if (world.isEmptyBlock(pos) || isNotSolid || isReplaceable
                    || block instanceof LiquidBlockContainer lbc && lbc.canPlaceLiquid(null, world, pos, blockstate, toPlace.getFluid())) {
                if (!simulate) {
                    if (world.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos) && fluid.is(FluidTags.WATER)) {
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

        if (didWork && !simulate) {
            drain(handler, toPlace.copyWithAmount(BUCKET_VOLUME), false);
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
        world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.8F);
        for(int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    public static ItemStack createFluidContainingItem(ItemLike item, FluidStack fluid) {
        ItemStacksResourceHandler container = new ItemStacksResourceHandler(1);
        container.set(0, ItemResource.of(item), 1);
        ResourceHandler<FluidResource> handler = ItemAccess.forHandlerIndex(container, 0).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        fill(handler, fluid, false);
        return container.getResource(0).toStack(container.getAmountAsInt(0));
    }
}
