package pneumaticCraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.proxy.CommonProxy;

public class BlockPneumaticDoorBase extends BlockPneumaticCraftModeled{

    public BlockPneumaticDoorBase(Material par2Material){
        super(par2Material);
    }

    @Override
    public int getRenderType(){
        return PneumaticCraft.proxy.CAMO_RENDER_ID;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPneumaticDoorBase.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_PNEUMATIC_DOOR;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase)world.getTileEntity(x, y, z);
        doorBase.orientation = PneumaticCraftUtils.getDirectionFacing(par5EntityLiving, false);
        updateDoorSide(doorBase);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityPneumaticDoorBase) {
            updateDoorSide((TileEntityPneumaticDoorBase)te);
            ForgeDirection dir = ((TileEntityPneumaticDoorBase)te).orientation;
            if(world.getBlock(x + dir.offsetX, y, z + dir.offsetZ) == Blockss.pneumaticDoor) {
                Blockss.pneumaticDoor.onNeighborBlockChange(world, x + dir.offsetX, y, z + dir.offsetZ, block);
            }
        }
    }

    private void updateDoorSide(TileEntityPneumaticDoorBase doorBase){
        TileEntity teDoor = doorBase.getWorldObj().getTileEntity(doorBase.xCoord + doorBase.orientation.offsetX, doorBase.yCoord, doorBase.zCoord + doorBase.orientation.offsetZ);
        if(teDoor instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor door = (TileEntityPneumaticDoor)teDoor;
            if(doorBase.orientation.getRotation(ForgeDirection.UP) == ForgeDirection.getOrientation(door.getBlockMetadata() % 6) && door.rightGoing || doorBase.orientation.getRotation(ForgeDirection.DOWN) == ForgeDirection.getOrientation(door.getBlockMetadata() % 6) && !door.rightGoing) {
                door.rightGoing = !door.rightGoing;
                door.setRotation(0);
            }
        }
    }

    @Override
    protected boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityPneumaticDoorBase) {
            TileEntityPneumaticDoorBase teDb = (TileEntityPneumaticDoorBase)te;
            int newMeta = (teDb.orientation.ordinal() + 1) % 6;
            if(newMeta == 0) newMeta = 2;
            teDb.orientation = ForgeDirection.getOrientation(newMeta);
            teDb.sendDescriptionPacket();
            return true;
        }
        return false;
    }
}
