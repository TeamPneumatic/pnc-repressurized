package pneumaticCraft.common.progwidgets;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetDropItem;
import pneumaticCraft.common.ai.DroneAIImExBase;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetDropItem extends ProgWidgetInventoryBase implements IItemDropper{
    private boolean dropStraight;

    @Override
    public String getWidgetString(){
        return "dropItem";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_DROP_ITEM;
    }

    @Override
    public boolean dropStraight(){
        return dropStraight;
    }

    @Override
    public void setDropStraight(boolean dropStraight){
        this.dropStraight = dropStraight;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("dropStraight", dropStraight);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        dropStraight = tag.getBoolean("dropStraight");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetDropItem(this, guiProgrammer);
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAIImExBase(drone, (ProgWidgetAreaItemBase)widget){

            private final Set<ChunkPosition> visitedPositions = new HashSet<ChunkPosition>();

            @Override
            public boolean shouldExecute(){
                boolean shouldExecute = false;
                for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                    ItemStack stack = drone.getInventory().getStackInSlot(i);
                    if(stack != null && widget.isItemValidForFilters(stack)) {
                        shouldExecute = super.shouldExecute();
                        break;
                    }
                }
                return shouldExecute;
            }

            @Override
            protected boolean moveIntoBlock(){
                return true;
            }

            @Override
            protected boolean isValidPosition(ChunkPosition pos){
                return !visitedPositions.contains(pos);//another requirement is that the drone can navigate to this exact block, but that's handled by the pathfinder.
            }

            @Override
            protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
                visitedPositions.add(pos);
                for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                    ItemStack stack = drone.getInventory().getStackInSlot(i);
                    if(stack != null && widget.isItemValidForFilters(stack)) {

                        if(useCount() && getRemainingCount() < stack.stackSize) {
                            stack = stack.splitStack(getRemainingCount());
                            decreaseCount(getRemainingCount());
                        } else {
                            decreaseCount(stack.stackSize);
                            drone.getInventory().setInventorySlotContents(i, null);
                        }
                        EntityItem item = new EntityItem(drone.getWorld(), pos.chunkPosX + 0.5, pos.chunkPosY + 0.5, pos.chunkPosZ + 0.5, stack);
                        if(((IItemDropper)widget).dropStraight()) {
                            item.motionX = 0;
                            item.motionY = 0;
                            item.motionZ = 0;
                        }
                        drone.getWorld().spawnEntityInWorld(item);
                        if(useCount() && getRemainingCount() == 0) break;
                    }
                }
                return false;
            }

        };
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.POTION_PLANT_DAMAGE;
    }

    @Override
    protected boolean isUsingSides(){
        return false;
    }

    @Override
    public String getExtraStringInfo(){
        return dropStraight() ? "Straight" : "Random";
    }
}
