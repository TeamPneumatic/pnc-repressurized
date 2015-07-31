package pneumaticCraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPneumaticDoorBase extends BlockPneumaticCraftModeled{

    public BlockPneumaticDoorBase(Material par2Material){
        super(par2Material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType(){
        return 0;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPneumaticDoorBase.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.PNEUMATIC_DOOR;
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
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        if(player.isSneaking()) {
            return super.rotateBlock(world, player, x, y, z, side);
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase teDb = (TileEntityPneumaticDoorBase)te;
                teDb.orientation = teDb.orientation.getRotation(ForgeDirection.UP);
                return true;
            }
            return false;
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side){
        TileEntityPneumaticDoorBase te = (TileEntityPneumaticDoorBase)world.getTileEntity(x, y, z);
        ItemStack camoStack = te.getStackInSlot(TileEntityPneumaticDoorBase.CAMO_SLOT);
        if(camoStack != null && camoStack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)camoStack.getItem()).field_150939_a;
            if(PneumaticCraftUtils.isRenderIDCamo(block.getRenderType())) {
                return block.getIcon(side, camoStack.getItemDamage());
            }
        }
        return this.getIcon(side, world.getBlockMetadata(x, y, z));
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side){
        ForgeDirection d = ForgeDirection.getOrientation(side);
        TileEntityPneumaticDoorBase te = (TileEntityPneumaticDoorBase)world.getTileEntity(x - d.offsetX, y - d.offsetY, z - d.offsetZ);
        ItemStack camoStack = te.getStackInSlot(TileEntityPneumaticDoorBase.CAMO_SLOT);
        if(camoStack != null && camoStack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)camoStack.getItem()).field_150939_a;
            if(PneumaticCraftUtils.isRenderIDCamo(block.getRenderType())) {
                return true;
            }
        }
        return false;
    }
}
