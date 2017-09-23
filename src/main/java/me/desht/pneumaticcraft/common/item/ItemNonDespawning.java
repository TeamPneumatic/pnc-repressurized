package me.desht.pneumaticcraft.common.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemNonDespawning extends ItemPneumatic {
    public ItemNonDespawning(String registryName) {
        super(registryName);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (!entityItem.world.isRemote) entityItem.setNoDespawn(); //age-- TODO 1.8 test
        return false;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag moreInfo) {
        super.addInformation(stack, worldIn, curInfo, moreInfo);
        curInfo.add(I18n.format("gui.tooltip.doesNotDespawn"));
    }
}
