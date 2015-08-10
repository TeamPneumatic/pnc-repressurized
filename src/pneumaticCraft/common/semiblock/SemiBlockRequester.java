package pneumaticCraft.common.semiblock;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester{

    public static final String ID = "logisticFrameRequester";

    @Override
    public int getColor(){
        return 0xFF0000FF;
    }

    @Override
    public int amountRequested(ItemStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            IInventory inv = IOHelper.getInventoryForTE(getTileEntity());
            int count = 0;
            if(inv != null) {
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if(s != null && isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                for(ItemStack s : incomingStacks.keySet()) {
                    if(isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                int requested = Math.max(0, Math.min(stack.stackSize, totalRequestingAmount - count));
                return requested;
            }
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack){
        int requesting = 0;
        for(int i = 0; i < getFilters().getSizeInventory(); i++) {
            ItemStack requestingStack = getFilters().getStackInSlot(i);
            if(requestingStack != null && isItemEqual(stack, requestingStack)) {
                requesting += requestingStack.stackSize;
            }
        }
        return requesting;
    }

    @Override
    public int amountRequested(FluidStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            TileEntity te = getTileEntity();
            if(te instanceof IFluidHandler) {

                int count = 0;

                for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    FluidTankInfo[] infos = ((IFluidHandler)te).getTankInfo(d);
                    if(infos != null) {
                        for(FluidTankInfo info : infos) {
                            if(info.fluid != null && info.fluid.getFluid() == stack.getFluid()) {
                                count += info.fluid.amount;
                            }
                        }
                        if(count > 0) break;
                    }
                }

                for(FluidStackWrapper s : incomingFluid.keySet()) {
                    if(s.stack.getFluid() == stack.getFluid()) {
                        count += s.stack.amount;
                    }
                }
                int requested = Math.max(0, Math.min(stack.amount, totalRequestingAmount - count));
                return requested;
            }

        }
        return 0;
    }

    private int getTotalRequestedAmount(FluidStack stack){
        int requesting = 0;
        for(int i = 0; i < 9; i++) {
            FluidStack requestingStack = getTankFilter(i).getFluid();
            if(requestingStack != null && requestingStack.getFluid() == stack.getFluid()) {
                requesting += requestingStack.amount;
            }
        }
        return requesting;
    }

    @Override
    public int getPriority(){
        return 2;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.LOGISTICS_REQUESTER;
    }

    @Override
    public boolean canFilterStack(){
        return true;
    }

}
