package pneumaticCraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.tileentity.TileEntityElevatorCaller;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class BlockElevatorCaller extends BlockPneumaticCraft{

    protected BlockElevatorCaller(Material par2Material){
        super(par2Material);
        setBlockTextureName(Textures.BLOCK_ELEVATOR_CALLER);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityElevatorCaller.class;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ){
        TileEntityElevatorCaller te = (TileEntityElevatorCaller)world.getTileEntity(x, y, z);
        if(!world.isRemote) {
            MovingObjectPosition mop = PneumaticCraftUtils.getEntityLookedObject(player);
            if(mop != null && mop.subHit >= 0) {
                setSurroundingElevators(world, x, y, z, mop.subHit);
            } else if(player.isSneaking()) {
                te.camoStack = player.getCurrentEquippedItem();
                return te.camoStack != null && te.camoStack.getItem() instanceof ItemBlock;
            }
        }
        return te.getRotation().getOpposite().ordinal() == side;
    }

    @Override
    public void setBlockBoundsForItemRender(){
        setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z){
        return true;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction){
        setBlockBounds(0, 0, 0, 1, 1, 1);
        MovingObjectPosition rayTrace = super.collisionRayTrace(world, x, y, z, origin, direction);
        ForgeDirection orientation = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z) & 7).getOpposite();
        if(rayTrace != null && rayTrace.sideHit == orientation.ordinal()) {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityElevatorCaller) {
                TileEntityElevatorCaller caller = (TileEntityElevatorCaller)te;
                for(TileEntityElevatorCaller.ElevatorButton button : caller.getFloors()) {
                    float startX = 0, startZ = 0, endX = 0, endZ = 0;
                    switch(orientation){
                        case NORTH:
                            startZ = 0F;
                            endZ = 0.01F;
                            endX = 1 - (float)button.posX;
                            startX = 1 - ((float)button.posX + (float)button.width);
                            break;
                        case SOUTH:
                            startZ = 0.99F;
                            endZ = 1F;
                            startX = (float)button.posX;
                            endX = (float)button.posX + (float)button.width;
                            break;
                        case WEST:
                            startX = 0F;
                            endX = 0.01F;
                            startZ = (float)button.posX;
                            endZ = (float)button.posX + (float)button.width;
                            break;
                        case EAST:
                            startX = 0.99F;
                            endX = 1F;
                            endZ = 1 - (float)button.posX;
                            startZ = 1 - ((float)button.posX + (float)button.width);
                            break;
                    }

                    setBlockBounds(startX, 1 - (float)(button.posY + button.height), startZ, endX, 1 - (float)button.posY, endZ);
                    MovingObjectPosition buttonTrace = super.collisionRayTrace(world, x, y, z, origin, direction);
                    if(buttonTrace != null) {
                        if(startX > 0.01F && startX < 0.98F) startX += 0.01F;
                        if(startZ > 0.01F && startZ < 0.98F) startZ += 0.01F;
                        if(endX > 0.02F && endX < 0.99F) endX -= 0.01F;
                        if(endZ > 0.02F && endZ < 0.99F) endZ -= 0.01F;
                        setBlockBounds(startX, 1.01F - (float)(button.posY + button.height), startZ, endX, 0.99F - (float)button.posY, endZ);
                        buttonTrace.subHit = button.floorNumber;
                        return buttonTrace;
                    }
                }
            }
        }

        setBlockBounds(0, 0, 0, 1, 1, 1);
        return rayTrace;
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        int oldMeta = world.getBlockMetadata(x, y, z);
        boolean wasPowered = oldMeta / 8 > 0;
        boolean isPowered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if(!wasPowered && isPowered) {
            world.setBlockMetadataWithNotify(x, y, z, oldMeta + 8, 3);
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityElevatorCaller) {
                setSurroundingElevators(world, x, y, z, ((TileEntityElevatorCaller)te).thisFloor);
            }
        } else if(wasPowered && !isPowered) {
            world.setBlockMetadataWithNotify(x, y, z, oldMeta - 8, 3);
        }
    }

    private void setSurroundingElevators(World world, int x, int y, int z, int floor){
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                TileEntityElevatorBase elevator = getElevatorBase(world, x + dir.offsetX, y - 2, z + dir.offsetZ);
                if(elevator != null) {
                    elevator.goToFloor(floor);
                }
            }
        }
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta){
        updateElevatorButtons(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        updateElevatorButtons(world, x, y, z);
        super.breakBlock(world, x, y, z, block, meta);
    }

    private void updateElevatorButtons(World world, int x, int y, int z){
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                TileEntityElevatorBase elevator = getElevatorBase(world, x + dir.offsetX, y - 2, z + dir.offsetZ);
                if(elevator != null) {
                    elevator.updateFloors();
                }
            }
        }
    }

    private TileEntityElevatorBase getElevatorBase(World world, int x, int y, int z){
        Block block = world.getBlock(x, y, z);
        TileEntityElevatorBase elevator = null;
        if(block == Blockss.elevatorFrame) {
            elevator = BlockElevatorFrame.getElevatorTE(world, x, y, z);
        }
        if(block == Blockss.elevatorBase) {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityElevatorBase && ((TileEntityElevatorBase)te).isCoreElevator()) {
                elevator = (TileEntityElevatorBase)te;
            }
        }
        return elevator;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    public boolean isOpaqueCube(){
        return false;//this should return false, because otherwise I can't give color to the rendered elevator buttons for some reason...
    }

    @Override
    public int getRenderType(){
        setBlockBoundsForItemRender();
        return super.getRenderType();
    }

    @Override
    public boolean canProvidePower(){
        return true;
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side){
        TileEntityElevatorCaller te = (TileEntityElevatorCaller)world.getTileEntity(x, y, z);
        if(te.camoBlock != null && PneumaticCraftUtils.isRenderIDCamo(te.camoBlock.getRenderType())) {
            return te.camoBlock.getIcon(side, te.camoStack.getItemDamage());
        }
        return this.getIcon(side, world.getBlockMetadata(x, y, z));
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){

        TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityElevatorCaller) {
            TileEntityElevatorCaller teEc = (TileEntityElevatorCaller)te;
            return teEc.getEmittingRedstone() ? 15 : 0;
        }

        return 0;
    }
}
