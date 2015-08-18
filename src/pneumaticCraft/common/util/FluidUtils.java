package pneumaticCraft.common.util;

import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidUtils{
    public static boolean tryInsertingLiquid(TileEntity te, ItemStack liquidContainer, boolean creative, List<ItemStack> returnedItems){
        if(te instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler)te;

            if(liquidContainer != null) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(liquidContainer);
                if(fluid != null) {
                    fluid.amount = 1000;
                    if(fluidHandler.canFill(ForgeDirection.UNKNOWN, fluid.getFluid()) && fluidHandler.fill(ForgeDirection.UNKNOWN, fluid, false) == 1000) {
                        fluidHandler.fill(ForgeDirection.UNKNOWN, fluid, true);
                        if(!creative) {
                            liquidContainer.stackSize--;

                            ItemStack returnedItem = null;
                            FluidContainerData[] allFluidData = FluidContainerRegistry.getRegisteredFluidContainerData();
                            for(FluidContainerData fluidData : allFluidData) {
                                if(fluidData.filledContainer.isItemEqual(liquidContainer)) {
                                    returnedItem = fluidData.emptyContainer;
                                    break;
                                }
                            }
                            if(returnedItem != null) {
                                returnedItems.add(returnedItem.copy());
                            }
                        }
                        return true;
                    }
                } else if(liquidContainer.getItem() instanceof IFluidContainerItem) {
                    IFluidContainerItem container = (IFluidContainerItem)liquidContainer.getItem();

                    fluid = container.getFluid(liquidContainer);
                    if(fluid != null) {
                        fluid = fluid.copy();
                        if(fluidHandler.canFill(ForgeDirection.UNKNOWN, fluid.getFluid()) && fluidHandler.fill(ForgeDirection.UNKNOWN, fluid, false) == fluid.amount) {
                            ItemStack returnedItem = liquidContainer.copy();
                            returnedItem.stackSize = 1;
                            container.drain(returnedItem, fluid.amount, true);
                            fluidHandler.fill(ForgeDirection.UNKNOWN, fluid, true);

                            if(!creative) {
                                liquidContainer.stackSize--;
                                returnedItems.add(returnedItem.copy());
                            }
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    public static boolean tryExtractingLiquid(TileEntity te, ItemStack liquidContainer, List<ItemStack> returnedItems){
        if(te instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler)te;

            if(liquidContainer != null) {
                int containerCapacity = FluidContainerRegistry.getContainerCapacity(liquidContainer);
                if(containerCapacity > 0 || liquidContainer.getItem() == Items.bucket) {
                    if(containerCapacity == 0) containerCapacity = 1000;
                    FluidStack extractedLiquid = fluidHandler.drain(ForgeDirection.UNKNOWN, containerCapacity, false);
                    if(extractedLiquid != null && extractedLiquid.amount == containerCapacity) {
                        ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(extractedLiquid, liquidContainer);
                        if(filledContainer != null) {
                            fluidHandler.drain(ForgeDirection.UNKNOWN, containerCapacity, true);
                            liquidContainer.stackSize--;
                            returnedItems.add(filledContainer.copy());
                            return true;
                        }
                    }

                } else if(liquidContainer.getItem() instanceof IFluidContainerItem) {
                    IFluidContainerItem container = (IFluidContainerItem)liquidContainer.getItem();

                    ItemStack singleItem = liquidContainer.copy();
                    singleItem.stackSize = 1;
                    FluidStack extractedLiquid = fluidHandler.drain(ForgeDirection.UNKNOWN, container.getCapacity(singleItem), false);
                    if(extractedLiquid != null) {
                        int filledAmount = container.fill(singleItem, extractedLiquid, true);
                        if(filledAmount > 0) {
                            liquidContainer.stackSize--;
                            returnedItems.add(singleItem);

                            FluidStack fluid = extractedLiquid.copy();
                            fluid.amount = filledAmount;
                            fluidHandler.drain(ForgeDirection.UNKNOWN, fluid, true);
                            return true;
                        }

                    }
                }
            }
        }
        return false;
    }

    public static boolean isSourceBlock(World world, int x, int y, int z){
        return world.getBlockMetadata(x, y, z) == 0;
    }
}
