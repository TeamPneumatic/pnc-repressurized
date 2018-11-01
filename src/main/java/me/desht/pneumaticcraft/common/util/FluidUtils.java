package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

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
    public static boolean tryFluidInsertion(TileEntity te, EnumFacing face, EntityPlayer player, EnumHand hand) {
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
    public static boolean tryFluidExtraction(TileEntity te, EnumFacing face, EntityPlayer player, EnumHand hand) {
        return doFluidInteraction(te, face, player, hand, false);
    }

    private static boolean doFluidInteraction(TileEntity te, EnumFacing face, EntityPlayer player, EnumHand hand, boolean isInserting) {
        ItemStack stack = player.getHeldItem(hand);
        IFluidHandlerItem stackHandler = FluidUtil.getFluidHandler(stack);
        if (stackHandler != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)) {
            IFluidTankProperties[] properties = stackHandler.getTankProperties();
            if (properties.length == 0) return false;
            int capacity = properties[0].getCapacity();
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);
            PlayerInvWrapper invWrapper = new PlayerInvWrapper(player.inventory);
            FluidActionResult result = isInserting ?
                    FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(hand), handler, invWrapper, capacity, player, true) :
                    FluidUtil.tryFillContainerAndStow(player.getHeldItem(hand), handler, invWrapper, capacity, player, true);
            if (result.isSuccess()) {
                player.setHeldItem(hand, result.getResult());
                return true;
            }
        }
        return false;
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
     * Check if the given blockpos contains a fluid source block of a certain fluid.
     *
     * @param world the world
     * @param pos the blockpos
     * @param fluid the fluid
     * @return true if there is a fluid source block of the right fluid at the given blockpos, false otherwise
     */
    public static boolean isSourceBlock(World world, BlockPos pos, Fluid fluid) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, EnumFacing.UP);
        if (handler != null) {
            FluidStack fluidStack = handler.drain(Fluid.BUCKET_VOLUME, false);
            return fluidStack != null && fluidStack.amount == Fluid.BUCKET_VOLUME && (fluid == null || fluidStack.getFluid().getName().equals(fluid.getName()));
        }
        return false;
    }

    /**
     * Get a fluidstack for the fluid at the given position, possibly also draining it.
     *
     * @param world the world
     * @param pos the blockpos
     * @param doDrain true if the fluid at the position should be drained
     * @return a fluidstack of the fluid, or null if no fluid could be drained
     */
    public static FluidStack getFluidAt(World world, BlockPos pos, boolean doDrain) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, EnumFacing.UP);
        return handler != null ? handler.drain(Fluid.BUCKET_VOLUME, doDrain) : null;
    }
}
