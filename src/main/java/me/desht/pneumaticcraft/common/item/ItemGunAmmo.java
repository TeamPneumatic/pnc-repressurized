package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.NBTUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemGunAmmo extends ItemPneumatic {

    public ItemGunAmmo() {
        super("gun_ammo");
        setMaxStackSize(1);
        setMaxDamage(1000);
    }

    @Nonnull
    public static ItemStack getPotion(ItemStack ammo) {
        if (ammo.getTagCompound() != null && ammo.getTagCompound().hasKey("potion")) {
            return new ItemStack(ammo.getTagCompound().getCompoundTag("potion"));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static void setPotion(ItemStack ammo, ItemStack potion) {
        NBTTagCompound tag = new NBTTagCompound();
        potion.writeToNBT(tag);
        NBTUtil.setCompoundTag(ammo, "potion", tag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> infoList, ITooltipFlag extraInfo) {
        infoList.add(I18n.format("gui.tooltip.gunAmmo.combineWithPotion"));
        ItemStack potion = getPotion(stack);
        if (potion != null) {
            potion.getItem().addInformation(potion, world, infoList, extraInfo);
            if (infoList.size() > 2) infoList.set(2, I18n.format("gui.tooltip.gunAmmo") + " " + infoList.get(2));
        }
        super.addInformation(stack, world, infoList, extraInfo);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (this.isInCreativeTab(tab)) {
            super.getSubItems(tab, list);
            NonNullList<ItemStack> potions = NonNullList.create();
            Items.POTIONITEM.getSubItems(tab, potions);
            for (ItemStack potion : potions) {
                ItemStack ammo = new ItemStack(this);
                setPotion(ammo, potion);
                list.add(ammo);
            }
        }
    }
}
