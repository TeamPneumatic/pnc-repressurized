package me.desht.pneumaticcraft.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemNonDespawning extends ItemPneumatic {
    public ItemNonDespawning(Item.Properties props,  String registryName) {
        super(props, registryName);
    }

    public ItemNonDespawning(String registryName) {
        super(registryName);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        if (!entityItem.world.isRemote) entityItem.setNoDespawn();
        return false;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag moreInfo) {
        super.addInformation(stack, worldIn, curInfo, moreInfo);
        curInfo.add(xlate("gui.tooltip.doesNotDespawn"));
    }
}
