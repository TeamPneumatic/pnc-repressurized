package pneumaticCraft.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGunAmmo extends ItemPneumatic{
    private IIcon overlay;

    public ItemGunAmmo(){
        super("gunAmmo");
        setMaxStackSize(1);
        setMaxDamage(1000);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister){
        super.registerIcons(iconRegister);
        overlay = iconRegister.registerIcon(Textures.ICON_LOCATION + "gunAmmoOverlay");
    }

    public static ItemStack getPotion(ItemStack ammo){
        if(ammo.getTagCompound() != null && ammo.getTagCompound().hasKey("potion")) {
            return ItemStack.loadItemStackFromNBT(ammo.getTagCompound().getCompoundTag("potion"));
        } else {
            return null;
        }
    }

    public static void setPotion(ItemStack ammo, ItemStack potion){
        NBTTagCompound tag = new NBTTagCompound();
        potion.writeToNBT(tag);
        NBTUtil.setCompoundTag(ammo, "potion", tag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack itemStack, int renderPass){
        ItemStack potion = getPotion(itemStack);
        return renderPass == 0 ? super.getColorFromItemStack(itemStack, renderPass) : potion != null ? Items.potionitem.getColorFromItemStack(potion, 0) : 0x00FFFF00;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses(){

        return true;
    }

    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int meta, int renderPass){

        return renderPass == 0 ? super.getIconFromDamageForRenderPass(meta, renderPass) : overlay;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean extraInfo){
        infoList.add(I18n.format("gui.tooltip.gunAmmo.combineWithPotion"));
        ItemStack potion = getPotion(stack);
        if(potion != null) {
            potion.getItem().addInformation(potion, player, infoList, extraInfo);
            if(infoList.size() > 2) infoList.set(2, I18n.format("gui.tooltip.gunAmmo") + " " + infoList.get(2));
        }
        super.addInformation(stack, player, infoList, extraInfo);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list){
        super.getSubItems(item, tab, list);
        List<ItemStack> potions = new ArrayList<ItemStack>();
        Items.potionitem.getSubItems(Items.potionitem, tab, potions);
        for(ItemStack potion : potions) {
            ItemStack ammo = new ItemStack(item);
            setPotion(ammo, potion);
            list.add(ammo);
        }
    }
}
