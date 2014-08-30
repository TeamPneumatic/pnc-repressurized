package pneumaticCraft.common.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;
import pneumaticCraft.lib.BBConstants;

public class BlockAssemblyPlatform extends BlockPneumaticCraftModeled{

    public BlockAssemblyPlatform(Material par2Material){
        super(par2Material);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
        setBlockBounds(BBConstants.ASSEMBLY_PLATFORM_MIN_POS, 0F, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS_TOP, BBConstants.ASSEMBLY_PLATFORM_MAX_POS);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS_TOP, BBConstants.ASSEMBLY_PLATFORM_MAX_POS);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAssemblyPlatform.class;
    }

    //overriden here because the Assembly Platform isn't implementing IInventory (intentionally).
    @Override
    protected void dropInventory(World world, int x, int y, int z){
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(!(tileEntity instanceof TileEntityAssemblyPlatform)) return;
        TileEntityAssemblyPlatform inventory = (TileEntityAssemblyPlatform)tileEntity;
        Random rand = new Random();
        ItemStack itemStack = inventory.getHeldStack();
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
