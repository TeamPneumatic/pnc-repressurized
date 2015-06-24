package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ILiquidFiltered;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.ISpecificProvider;
import pneumaticCraft.common.semiblock.ISpecificRequester;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockLogistics.FluidStackWrapper;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.common.util.IOHelper;

public class DroneAILogistics extends DroneAIBlockInteraction{
    private final List<SemiBlockLogistics>[] logistics = new List[3];
    private EntityAIBase curAI;
    private ItemStack transportingStack;
    private FluidStackWrapper transportingFluid;
    private SemiBlockLogistics requestingBlock;

    public DroneAILogistics(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
        for(int i = 0; i < logistics.length; i++) {
            logistics[i] = new ArrayList<SemiBlockLogistics>();
        }
    }

    @Override
    protected boolean moveToPositions(){
        return false;
    }

    @Override
    protected int lookupsPerSearch(){
        return 200;
    }

    @Override
    protected void indicateToListeningPlayers(ChunkPosition pos){
        if(drone.getWorld().rand.nextInt(20) == 0) super.indicateToListeningPlayers(pos);
    }

    @Override
    public boolean shouldExecute(){

        boolean shouldExecute = super.shouldExecute();
        if(shouldExecute) {
            for(List<SemiBlockLogistics> list : logistics) {
                list.clear();
            }
        } else {
            if(doLogistics()) {
                searching = false;
                curPos = new ChunkPosition(0, 0, 0);
                return true;
            }
        }
        return shouldExecute;
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        ISemiBlock semiBlock = SemiBlockManager.getInstance().getSemiBlock(drone.getWorld(), pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(semiBlock instanceof SemiBlockLogistics) {
            SemiBlockLogistics logisticsBlock = (SemiBlockLogistics)semiBlock;
            logistics[logisticsBlock.getPriority()].add(logisticsBlock);
        }
        return false;
    }

    private boolean doLogistics(){
        transportingFluid = null;
        transportingStack = null;
        for(int priority = logistics.length - 1; priority >= 0; priority--) {
            for(SemiBlockLogistics requester : logistics[priority]) {
                for(int i = 0; i < priority; i++) {
                    for(SemiBlockLogistics provider : logistics[i]) {
                        if(provider.shouldProvideTo(priority)) {
                            if(drone.getInventory().getStackInSlot(0) != null) {
                                int requestedAmount = getRequestedAmount(requester, drone.getInventory().getStackInSlot(0));
                                if(requestedAmount > 0) {
                                    transportingStack = drone.getInventory().getStackInSlot(0).copy();
                                    transportingStack.stackSize = requestedAmount;
                                    curAI = new DroneEntityAIInventoryExport(drone, new FakeWidgetLogistics(requester.getPos(), transportingStack));
                                }
                            } else if(drone.getTank().getFluidAmount() > 0) {
                                int requestedAmount = getRequestedAmount(requester, drone.getTank().getFluid());
                                if(requestedAmount > 0) {
                                    FluidStack fluid = drone.getTank().getFluid().copy();
                                    fluid.amount = requestedAmount;
                                    transportingFluid = new FluidStackWrapper(fluid);
                                    curAI = new DroneAILiquidExport(drone, new FakeWidgetLogistics(requester.getPos(), fluid));
                                }
                            } else {
                                tryProvide(provider, requester);
                            }
                            if(curAI != null) {
                                if(curAI.shouldExecute()) {
                                    if(transportingStack != null) {
                                        requester.informIncomingStack(transportingStack);
                                    } else {
                                        requester.informIncomingStack(transportingFluid);
                                    }
                                    requestingBlock = requester;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        if(!curAI.continueExecuting()) {
            if(curAI instanceof DroneEntityAIInventoryImport) {
                requestingBlock.clearIncomingStack(transportingStack);
                return clearAIAndProvideAgain();
            } else if(curAI instanceof DroneAILiquidImport) {
                requestingBlock.clearIncomingStack(transportingFluid);
                return clearAIAndProvideAgain();
            } else {
                curAI = null;
                return false;
            }
        } else {
            if(transportingStack != null) requestingBlock.informIncomingStack(transportingStack);
            if(transportingFluid != null) requestingBlock.informIncomingStack(transportingFluid);
            return true;
        }
    }

    private boolean clearAIAndProvideAgain(){
        curAI = null;

        SemiBlockSorter sorter = new SemiBlockSorter(drone);
        for(List<SemiBlockLogistics> list : logistics) {
            Collections.sort(list, sorter);
        }
        return doLogistics();
    }

    private static class SemiBlockSorter implements Comparator<SemiBlockLogistics>{
        private final ChunkPositionSorter sorter;

        public SemiBlockSorter(IDroneBase drone){
            sorter = new ChunkPositionSorter(drone);
        }

        @Override
        public int compare(SemiBlockLogistics o1, SemiBlockLogistics o2){
            return sorter.compare(o1.getPos(), o2.getPos());
        }

    }

    private void tryProvide(SemiBlockLogistics provider, SemiBlockLogistics requester){
        IInventory providingInventory = IOHelper.getInventoryForTE(provider.getTileEntity());
        if(providingInventory != null) {
            for(int i = 0; i < providingInventory.getSizeInventory(); i++) {
                ItemStack providingStack = providingInventory.getStackInSlot(i);
                if(providingStack != null && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider)provider).canProvide(providingStack)) && IOHelper.canExtractItemFromInventory(providingInventory, providingStack, i, 0)) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if(requestedAmount > 0) {
                        transportingStack = providingStack.copy();
                        transportingStack.stackSize = requestedAmount;
                        curAI = new DroneEntityAIInventoryImport(drone, new FakeWidgetLogistics(provider.getPos(), transportingStack));
                        return;
                    }
                }
            }
        }
        if(provider.getTileEntity() instanceof IFluidHandler) {
            IFluidHandler providingTank = (IFluidHandler)provider.getTileEntity();
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                FluidStack providingStack = providingTank.drain(d, 16000, false);
                if(providingStack != null && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider)provider).canProvide(providingStack)) && providingTank.canDrain(d, providingStack.getFluid())) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if(requestedAmount > 0) {
                        providingStack = providingStack.copy();
                        providingStack.amount = requestedAmount;
                        curAI = new DroneAILiquidImport(drone, new FakeWidgetLogistics(provider.getPos(), providingStack));
                        transportingFluid = new FluidStackWrapper(providingStack);
                        return;
                    }
                }
            }
        }
    }

    private int getRequestedAmount(SemiBlockLogistics requester, ItemStack providingStack){
        TileEntity te = requester.getTileEntity();
        if(!(te instanceof IInventory)) return 0;
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester)requester).amountRequested(providingStack) : providingStack.stackSize;
        if(requestedAmount == 0) return 0;
        providingStack = providingStack.copy();
        providingStack.stackSize = requestedAmount;
        ItemStack remainder = providingStack.copy();
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            remainder = IOHelper.insert(te, remainder, d, true);
            if(remainder == null) break;
        }
        if(remainder != null) providingStack.stackSize -= remainder.stackSize;
        return providingStack.stackSize;
    }

    private int getRequestedAmount(SemiBlockLogistics requester, FluidStack providingStack){
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester)requester).amountRequested(providingStack) : providingStack.amount;
        if(requestedAmount == 0) return 0;
        providingStack = providingStack.copy();
        providingStack.amount = requestedAmount;
        FluidStack remainder = providingStack.copy();
        TileEntity te = requester.getTileEntity();
        if(te instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler)te;
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                remainder.amount -= fluidHandler.fill(d, remainder, false);
                if(remainder.amount <= 0) break;
            }
        }
        providingStack.amount -= remainder.amount;
        return providingStack.amount;
    }

    private static class FakeWidgetLogistics extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget,
            ILiquidFiltered{
        private ItemStack stack;
        private FluidStack fluid;
        private final Set<ChunkPosition> area;

        public FakeWidgetLogistics(ChunkPosition pos, ItemStack stack){
            this.stack = stack;
            area = new HashSet<ChunkPosition>();
            area.add(pos);
        }

        public FakeWidgetLogistics(ChunkPosition pos, FluidStack fluid){
            this.fluid = fluid;
            area = new HashSet<ChunkPosition>();
            area.add(pos);
        }

        @Override
        public String getWidgetString(){
            return null;
        }

        @Override
        public int getCraftingColorIndex(){
            return 0;
        }

        @Override
        public Set<ChunkPosition> getArea(){
            return area;
        }

        @Override
        public void setSides(boolean[] sides){}

        @Override
        public boolean[] getSides(){
            return new boolean[]{true, true, true, true, true, true};
        }

        @Override
        public boolean isItemValidForFilters(ItemStack item){
            return item != null && item.isItemEqual(stack);
        }

        @Override
        protected ResourceLocation getTexture(){
            return null;
        }

        @Override
        public boolean useCount(){
            return true;
        }

        @Override
        public void setUseCount(boolean useCount){}

        @Override
        public int getCount(){
            return stack != null ? stack.stackSize : fluid.amount;
        }

        @Override
        public void setCount(int count){}

        @Override
        public boolean isFluidValid(Fluid fluid){
            return fluid == this.fluid.getFluid();
        }

    }

}
