package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.item.IPressurizable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPressurizable extends ItemPneumatic implements IPressurizable{
    private final int volume;

    public ItemPressurizable(String textureLocation, int maxAir, int volume){
        super(textureLocation);
        setMaxStackSize(1);
        setMaxDamage(maxAir);
        this.volume = volume;
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
    public float getPressure(ItemStack iStack){
        return (float)(getMaxDamage() - iStack.getItemDamage()) / (float)volume;
    }

    // the information displayed as tooltip info. (saved coordinates in this
    // case)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){
        infoList.add("Pressure: " + Math.round(getPressure(stack) * 10D) / 10D + " bar");
        super.addInformation(stack, player, infoList, par4);
    }

    @Override
    public void addAir(ItemStack iStack, int amount){
        iStack.setItemDamage(iStack.getItemDamage() - amount);
    }

    @Override
    public float maxPressure(ItemStack iStack){
        return 10F;
    }

}
