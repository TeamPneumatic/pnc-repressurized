package pneumaticCraft.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockElevatorBase extends BlockPneumaticCraftModeled{

    public BlockElevatorBase(Material par2Material){
        super(par2Material);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z){
        super.onBlockAdded(world, x, y, z);
        TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, x, y, z);
        if(elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityElevatorBase.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.ELEVATOR;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        return super.onBlockActivated(world, x, getCoreElevatorY(world, x, y, z), z, player, par6, par7, par8, par9);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        super.onNeighborBlockChange(world, x, y, z, block);
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityElevatorBase) {
            TileEntityElevatorBase thisTe = (TileEntityElevatorBase)te;
            if(thisTe.isCoreElevator()) {
                TileEntityElevatorBase teAbove = getCoreTileEntity(world, x, y, z);
                if(teAbove != null && teAbove != thisTe) {
                    for(int i = 0; i < thisTe.getSizeInventory(); i++) {
                        ItemStack item = thisTe.getStackInSlot(i);
                        if(item != null) {
                            ItemStack leftover = TileEntityHopper.func_145889_a(teAbove, item, 0);
                            thisTe.setInventorySlotContents(i, null);
                            if(leftover != null) {
                                EntityItem entity = new EntityItem(world, teAbove.xCoord + 0.5, teAbove.yCoord + 1.5, teAbove.zCoord + 0.5, leftover);
                                world.spawnEntityInWorld(entity);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getCoreElevatorY(World world, int x, int y, int z){

        if(world.getBlock(x, y + 1, z) == Blockss.elevatorBase) {
            return getCoreElevatorY(world, x, y + 1, z);
        } else {
            return y;
        }
    }

    public static TileEntityElevatorBase getCoreTileEntity(World world, int x, int y, int z){
        return (TileEntityElevatorBase)world.getTileEntity(x, getCoreElevatorY(world, x, y, z), z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        if(world.getBlock(x, y - 1, z) == Blockss.elevatorBase) {
            TileEntity te = world.getTileEntity(x, y - 1, z);
            ((TileEntityElevatorBase)te).moveInventoryToThis();
        }
        TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, x, y, z);
        if(elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void dropInventory(World world, int x, int y, int z){

        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if(!(tileEntity instanceof TileEntityElevatorBase)) return;

        TileEntityElevatorBase inventory = (TileEntityElevatorBase)tileEntity;
        Random rand = new Random();
        for(int i = getInventoryDropStartSlot(inventory); i < getInventoryDropEndSlot(inventory); i++) {

            ItemStack itemStack = inventory.getRealStackInSlot(i);

            if(itemStack != null && itemStack.stackSize > 0) {
                float dX = rand.nextFloat() * 0.8F + 0.1F;
                float dY = rand.nextFloat() * 0.8F + 0.1F;
                float dZ = rand.nextFloat() * 0.8F + 0.1F;

                EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(itemStack.getItem(), itemStack.stackSize, itemStack.getItemDamage()));

                if(itemStack.hasTagCompound()) {
                    entityItem.getEntityItem().setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
                }

                float factor = 0.05F;
                entityItem.motionX = rand.nextGaussian() * factor;
                entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                entityItem.motionZ = rand.nextGaussian() * factor;
                world.spawnEntityInWorld(entityItem);
                itemStack.stackSize = 0;
            }
        }
    }
}
