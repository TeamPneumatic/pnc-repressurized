package me.desht.pneumaticcraft.common.util;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidUtils {
//    public static boolean tryExtractingLiquid(TileEntity te, ItemStack liquidContainer, List<ItemStack> returnedItems) {
//        if (te instanceof IFluidHandler) {
//            IFluidHandler fluidHandler = (IFluidHandler) te;
//
//            if (liquidContainer != null) {
//                int containerCapacity = FluidContainerRegistry.getContainerCapacity(liquidContainer);
//                if (containerCapacity > 0 || liquidContainer.getItem() == Items.bucket) {
//                    if (containerCapacity == 0) containerCapacity = 1000;
//                    FluidStack extractedLiquid = fluidHandler.drain(null, containerCapacity, false);
//                    if (extractedLiquid != null && extractedLiquid.amount == containerCapacity) {
//                        ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(extractedLiquid, liquidContainer);
//                        if (filledContainer != null) {
//                            fluidHandler.drain(null, containerCapacity, true);
//                            liquidContainer.stackSize--;
//                            returnedItems.add(filledContainer.copy());
//                            return true;
//                        }
//                    }
//
//                } else if (liquidContainer.getItem() instanceof IFluidContainerItem) {
//                    IFluidContainerItem container = (IFluidContainerItem) liquidContainer.getItem();
//
//                    ItemStack singleItem = liquidContainer.copy();
//                    singleItem.stackSize = 1;
//                    FluidStack extractedLiquid = fluidHandler.drain(null, container.getCapacity(singleItem), false);
//                    if (extractedLiquid != null) {
//                        int filledAmount = container.fill(singleItem, extractedLiquid, true);
//                        if (filledAmount > 0) {
//                            liquidContainer.stackSize--;
//                            returnedItems.add(singleItem);
//
//                            FluidStack fluid = extractedLiquid.copy();
//                            fluid.amount = filledAmount;
//                            fluidHandler.drain(null, fluid, true);
//                            return true;
//                        }
//
//                    }
//                }
//            }
//        }
//        return false;
//    }

    public static boolean tryExtractingLiquid(IFluidHandler destHandler, ItemStack item, NonNullList<ItemStack> returnedItems) {
        if (item.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler fluidHandler = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            int amount = fluidHandler.getTankProperties()[0].getCapacity();
            FluidStack transferred = FluidUtil.tryFluidTransfer(destHandler, fluidHandler, amount, true);
            if (transferred != null && transferred.amount > 0) {
                if (fluidHandler instanceof IFluidHandlerItem) returnedItems.add(((IFluidHandlerItem) fluidHandler).getContainer());
                return true;
            }
        }
        return false;
    }

    public static boolean tryInsertingLiquid(TileEntity te, ItemStack stack, NonNullList<ItemStack> returnedItems) {
        if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            FluidActionResult result = FluidUtil.tryEmptyContainer(stack, handler, 1000, null, true);
            if (result.isSuccess()) {
                returnedItems.add(result.getResult());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Have the player attempt to insert liquid into a tile entity, which must support the Forge fluid handler capability.
     * The player's held item will be updated if fluid was inserted.
     *
     * @param te the tile entity to insert into
     * @param player the player
     * @param hand the hand being used
     * @param returnedItems a list of returned items
     * @return true if liquid was inserted, false if not
     */
    public static boolean tryInsertingLiquid(TileEntity te, EnumFacing face, EntityPlayer player, EnumHand hand, NonNullList<ItemStack> returnedItems) {
        if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);
            FluidActionResult result = FluidUtil.tryEmptyContainer(player.getHeldItem(hand), handler, 1000, player, true);
            if (result.isSuccess()) {
                player.setHeldItem(hand, result.getResult());
                returnedItems.add(result.getResult());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean isSourceBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getProperties().containsKey(BlockLiquid.LEVEL)) {
            return state.getValue(BlockLiquid.LEVEL) == 0;
        } else if (state.getProperties().containsKey(BlockFluidBase.LEVEL)) {
            return state.getValue(BlockFluidBase.LEVEL) == 0;
        } else {
            return false;
        }
    }
}
