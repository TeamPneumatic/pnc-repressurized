package pneumaticCraft.common.ai;

import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.common.ai.LogisticsManager.LogisticsTask;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ILiquidFiltered;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockManager;

public class DroneAILogistics extends EntityAIBase{
    private EntityAIBase curAI;
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase widget;
    private final LogisticsManager manager = new LogisticsManager();
    private LogisticsTask curTask;

    public DroneAILogistics(IDroneBase drone, ProgWidgetAreaItemBase widget){
        this.drone = drone;
        this.widget = widget;
    }

    @Override
    public boolean shouldExecute(){
        manager.clearLogistics();
        Set<ChunkPosition> area = widget.getCachedAreaSet();
        if(area.size() == 0) return false;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for(ChunkPosition pos : area) {
            minX = Math.min(minX, pos.chunkPosX);
            maxX = Math.max(maxX, pos.chunkPosX);
            minZ = Math.min(minZ, pos.chunkPosZ);
            maxZ = Math.max(maxZ, pos.chunkPosZ);
        }

        for(int x = minX; x < maxX + 16; x += 16) {
            for(int z = minZ; z < maxZ + 16; z += 16) {
                Chunk chunk = drone.getWorld().getChunkFromBlockCoords(x, z);
                Map<ChunkPosition, ISemiBlock> map = SemiBlockManager.getInstance(drone.getWorld()).getSemiBlocks().get(chunk);
                if(map != null) {
                    for(Map.Entry<ChunkPosition, ISemiBlock> entry : map.entrySet()) {
                        if(entry.getValue() instanceof SemiBlockLogistics && area.contains(entry.getKey())) {
                            SemiBlockLogistics logisticsBlock = (SemiBlockLogistics)entry.getValue();
                            manager.addLogisticFrame(logisticsBlock);
                        }
                    }
                }
            }
        }
        curTask = null;
        return doLogistics();
    }

    private boolean doLogistics(){
        ItemStack item = drone.getInventory().getStackInSlot(0);
        FluidStack fluid = drone.getTank().getFluid();
        PriorityQueue<LogisticsTask> tasks = manager.getTasks(item != null ? item : fluid);
        if(tasks.size() > 0) {
            curTask = tasks.poll();
            return execute(curTask);
        }
        return false;
    }

    @Override
    public boolean continueExecuting(){
        if(curTask == null) return false;
        if(!curAI.continueExecuting()) {
            if(curAI instanceof DroneEntityAIInventoryImport) {
                curTask.requester.clearIncomingStack(curTask.transportingItem);
                return clearAIAndProvideAgain();
            } else if(curAI instanceof DroneAILiquidImport) {
                curTask.requester.clearIncomingStack(curTask.transportingFluid);
                return clearAIAndProvideAgain();
            } else {
                curAI = null;
                return false;
            }
        } else {
            curTask.informRequester();
            return true;
        }
    }

    private boolean clearAIAndProvideAgain(){
        curAI = null;
        if(curTask.isStillValid(drone.getInventory().getStackInSlot(0) != null ? drone.getInventory().getStackInSlot(0) : drone.getTank().getFluid()) && execute(curTask)) {
            return true;
        } else {
            curTask = null;
            return doLogistics();
        }
    }

    public boolean execute(LogisticsTask task){
        if(drone.getInventory().getStackInSlot(0) != null) {
            if(!isPosPathfindable(task.requester.getPos())) return false;
            curAI = new DroneEntityAIInventoryExport(drone, new FakeWidgetLogistics(task.requester.getPos(), task.transportingItem));
        } else if(drone.getTank().getFluidAmount() > 0) {
            if(!isPosPathfindable(task.requester.getPos())) return false;
            curAI = new DroneAILiquidExport(drone, new FakeWidgetLogistics(task.requester.getPos(), task.transportingFluid.stack));
        } else if(task.transportingItem != null) {
            if(!isPosPathfindable(task.provider.getPos())) return false;
            curAI = new DroneEntityAIInventoryImport(drone, new FakeWidgetLogistics(task.provider.getPos(), task.transportingItem));
        } else {
            if(!isPosPathfindable(task.provider.getPos())) return false;
            curAI = new DroneAILiquidImport(drone, new FakeWidgetLogistics(task.provider.getPos(), task.transportingFluid.stack));
        }
        if(curAI.shouldExecute()) {
            task.informRequester();
            return true;
        } else {
            return false;
        }
    }

    private boolean isPosPathfindable(ChunkPosition pos){
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if(drone.isBlockValidPathfindBlock(pos.chunkPosX + d.offsetX, pos.chunkPosY + d.offsetY, pos.chunkPosZ + d.offsetZ)) return true;
        }
        return false;
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
        public void getArea(Set<ChunkPosition> area){
            area.addAll(this.area);
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
