package pneumaticCraft.common.item;

import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEmptyPCB extends ItemNonDespawning{
    private static Random rand = new Random();

    public ItemEmptyPCB(){
        setMaxStackSize(1);
        setMaxDamage(100);
        setNoRepair();
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, getMaxDamage()));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){
        super.addInformation(stack, player, infoList, par4);
        if(stack.getItemDamage() < 100) {
            infoList.add("Etch success chance: " + (100 - stack.getItemDamage()) + "%");
        } else {
            infoList.add("Put in a UV Light Box to progress...");
        }
        if(stack.hasTagCompound()) {
            infoList.add("Etching progress: " + stack.getTagCompound().getInteger("etchProgress") + "%");
        } else if(stack.getItemDamage() < 100) {
            infoList.add("Throw in Etching Acid to develop...");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister){
        itemIcon = par1IconRegister.registerIcon(Textures.ICON_LOCATION + Textures.ITEM_EMPTY_PCB);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem){
        super.onEntityItemUpdate(entityItem);
        ItemStack stack = entityItem.getEntityItem();
        if(Fluids.areFluidsEqual(FluidRegistry.lookupFluidForBlock(entityItem.worldObj.getBlock((int)Math.floor(entityItem.posX), (int)Math.floor(entityItem.posY), (int)Math.floor(entityItem.posZ))), Fluids.etchingAcid)) {
            if(!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            int etchProgress = stack.getTagCompound().getInteger("etchProgress");
            if(etchProgress < 100) {
                if(entityItem.ticksExisted % (TileEntityConstants.PCB_ETCH_TIME / 5) == 0) stack.getTagCompound().setInteger("etchProgress", etchProgress + 1);
            } else {
                entityItem.setEntityItemStack(new ItemStack(rand.nextInt(100) >= stack.getItemDamage() ? Itemss.unassembledPCB : Itemss.failedPCB));
            }
        }
        return false;
    }
}
