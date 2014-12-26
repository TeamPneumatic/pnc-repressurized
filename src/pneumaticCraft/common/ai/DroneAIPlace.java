package pneumaticCraft.common.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.progwidgets.ProgWidgetPlace;
import pneumaticCraft.lib.PneumaticValues;

public class DroneAIPlace extends DroneAIBlockInteraction{

    /** 
     * @param drone
     * @param speed
     * @param widget needs to implement IBlockOrdered and IDirectionalWidget.
     */
    public DroneAIPlace(EntityDrone drone, double speed, ProgWidgetAreaItemBase widget){
        super(drone, speed, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        if(drone.worldObj.isAirBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ)) {
            for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                ItemStack droneStack = drone.getInventory().getStackInSlot(i);
                if(droneStack != null && droneStack.getItem() instanceof ItemBlock && ((ItemBlock)droneStack.getItem()).field_150939_a.canPlaceBlockOnSide(drone.worldObj, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, ProgWidgetPlace.getDirForSides(((ISidedWidget)widget).getSides()).ordinal())) {
                    if(widget.isItemValidForFilters(droneStack)) {
                        if(drone.worldObj.canPlaceEntityOnSide(((ItemBlock)droneStack.getItem()).field_150939_a, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, false, 0, null, droneStack)) {
                            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                                if(drone.isBlockValidPathfindBlock(pos.chunkPosX + dir.offsetX, pos.chunkPosY + dir.offsetY, pos.chunkPosZ + dir.offsetZ)) {
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
        if(distToBlock > 0.5) {
            ForgeDirection side = ProgWidgetPlace.getDirForSides(((ISidedWidget)widget).getSides());
            for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                ItemStack droneStack = drone.getInventory().getStackInSlot(i);
                if(droneStack != null && droneStack.getItem() instanceof ItemBlock && ((ItemBlock)droneStack.getItem()).field_150939_a.canPlaceBlockOnSide(drone.worldObj, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, ProgWidgetPlace.getDirForSides(((ISidedWidget)widget).getSides()).ordinal())) {
                    if(widget.isItemValidForFilters(droneStack)) {
                        if(drone.worldObj.canPlaceEntityOnSide(((ItemBlock)droneStack.getItem()).field_150939_a, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, false, 0, null, droneStack)) {
                            Block block = Block.getBlockFromItem(droneStack.getItem());
                            int meta = droneStack.getItem().getMetadata(droneStack.getItemDamage());
                            int newMeta = block.onBlockPlaced(drone.worldObj, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, side.ordinal(), side.offsetX, side.offsetY, side.offsetZ, meta);
                            setFakePlayerAccordingToDir();
                            if(placeBlockAt(droneStack, drone.getFakePlayer(), drone.worldObj, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, side.ordinal(), 0, 0, 0, newMeta)) {
                                drone.addAir(null, -PneumaticValues.DRONE_USAGE_PLACE);
                                drone.worldObj.playSoundEffect(pos.chunkPosX + 0.5F, pos.chunkPosY + 0.5F, pos.chunkPosZ + 0.5F, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                                if(--droneStack.stackSize <= 0) {
                                    drone.getInventory().setInventorySlotContents(i, null);
                                }
                            }
                            return false;
                        }
                    }
                }
            }
            return false;
        } else {
            return !drone.getNavigator().noPath();
        }
    }

    private void setFakePlayerAccordingToDir(){
        EntityPlayer fakePlayer = drone.getFakePlayer();
        fakePlayer.posX = drone.posX;
        fakePlayer.posZ = drone.posZ;
        switch(ProgWidgetPlace.getDirForSides(((ISidedWidget)widget).getSides())){
            case UP:
                fakePlayer.rotationPitch = -90;
                fakePlayer.posY = drone.posY - 10;//do this for PistonBase.determineDirection()
                return;
            case DOWN:
                fakePlayer.rotationPitch = 90;
                fakePlayer.posY = drone.posY + 10;//do this for PistonBase.determineDirection()
                return;
            case NORTH:
                fakePlayer.rotationYaw = 180;
                fakePlayer.posY = drone.posY;//do this for PistonBase.determineDirection()
                break;
            case EAST:
                fakePlayer.rotationYaw = 270;
                fakePlayer.posY = drone.posY;//do this for PistonBase.determineDirection()
                break;
            case SOUTH:
                fakePlayer.rotationYaw = 0;
                fakePlayer.posY = drone.posY;//do this for PistonBase.determineDirection()
                break;
            case WEST:
                fakePlayer.rotationYaw = 90;
                fakePlayer.posY = drone.posY;//do this for PistonBase.determineDirection()
                break;
        }
    }

    /**
     * Called to actually place the block, after the location is determined
     * and all permission checks have been made.
     *
     * @param stack The item stack that was used to place the block. This can be changed inside the method.
     * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
     * @param side The side the player (or machine) right-clicked on.
     */
    private boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata){
        Block block = Block.getBlockFromItem(stack.getItem());
        if(!world.setBlock(x, y, z, block, metadata, 3)) {
            return false;
        }

        block.onBlockPlacedBy(world, x, y, z, player, stack);
        block.onPostBlockPlaced(world, x, y, z, metadata);

        return true;
    }

}
