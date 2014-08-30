package pneumaticCraft.common.entity.item;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;

public class EntityItemSpecial extends EntityItem{
    public EntityItemSpecial(World par1World, double par2, double par4, double par6, ItemStack par8ItemStack){
        super(par1World, par2, par4, par6, par8ItemStack);
    }

    public EntityItemSpecial(World world, ItemStack entityStack){
        super(world);
        setEntityItemStack(entityStack);
    }

    public EntityItemSpecial(World par1World, double par2, double par4, double par6){
        super(par1World, par2, par4, par6);
    }

    public EntityItemSpecial(World par1World){
        super(par1World);

    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource dmgSource, float HP){
        super.attackEntityFrom(dmgSource, HP);
        if(dmgSource.isExplosion() && (getEntityItem().getItem() == net.minecraft.init.Items.iron_ingot || getEntityItem().getItem() == Item.getItemFromBlock(net.minecraft.init.Blocks.iron_block)) && getEntityItem().stackSize > 0 && isDead && !worldObj.isRemote) {
            if(getEntityItem().stackSize < 3 && rand.nextDouble() <= Config.configCompressedIngotLossRate) return false;
            EntityItem compressedIngot = new EntityItem(worldObj);
            Item newItem = getEntityItem().getItem() == net.minecraft.init.Items.iron_ingot ? Itemss.ingotIronCompressed : Item.getItemFromBlock(Blockss.compressedIron);
            ItemStack newStack = new ItemStack(newItem, getEntityItem().stackSize, getEntityItem().getItemDamage());
            if(getEntityItem().stackSize >= 3) {
                newStack.stackSize = (int)(getEntityItem().stackSize * (rand.nextDouble() * Math.min(Config.configCompressedIngotLossRate * 0.02D, 0.2D) + (Math.max(0.9D, 1D - Config.configCompressedIngotLossRate * 0.01D) - Config.configCompressedIngotLossRate * 0.01D)));
            }
            compressedIngot.copyDataFrom(this, true);
            compressedIngot.setEntityItemStack(newStack);
            compressedIngot.delayBeforeCanPickup = delayBeforeCanPickup;
            worldObj.spawnEntityInWorld(compressedIngot);
            getEntityItem().stackSize = 0;
        }
        return false;
    }

}
