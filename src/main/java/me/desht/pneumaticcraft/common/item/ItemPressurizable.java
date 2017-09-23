package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPressurizable extends ItemPneumatic implements IPressurizable {
    private final int volume;

    public ItemPressurizable(String registryName, int maxAir, int volume) {
        super(registryName);
        setMaxStackSize(1);
        setMaxDamage(maxAir);
        this.volume = volume;
        setNoRepair();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> par3List) {
        if (isInCreativeTab(tab)) {
            par3List.add(new ItemStack(this, 1, 0));
            par3List.add(new ItemStack(this, 1, getMaxDamage()));
        }
    }

    @Override
    public float getPressure(ItemStack iStack) {
        return (float) (iStack.getMaxDamage() - iStack.getItemDamage()) / (float) volume;
    }

    // the information displayed as tooltip info. (saved coordinates in this
    // case)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag par4) {
        infoList.add("Pressure: " + Math.round(getPressure(stack) * 10D) / 10D + " bar");
        super.addInformation(stack, worldIn, infoList, par4);
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        iStack.setItemDamage(iStack.getItemDamage() - amount);
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return 10F;
    }

}
