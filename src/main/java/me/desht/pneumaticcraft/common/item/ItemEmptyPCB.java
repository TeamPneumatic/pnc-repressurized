package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class ItemEmptyPCB extends ItemNonDespawning {
    private static Random rand = new Random();

    public ItemEmptyPCB() {
        super("empty_pcb");
        setMaxStackSize(1);
        setMaxDamage(100);
        setNoRepair();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this, 1, 0);
            items.add(stack);
            items.add(new ItemStack(this, 1, getMaxDamage(stack)));
        }
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> infoList, ITooltipFlag par4) {
        super.addInformation(stack, player, infoList, par4);
        if (stack.getItemDamage() < 100) {
            infoList.add(I18n.format("gui.tooltip.item.uvLightBox.successChance", 100 - stack.getItemDamage()));
        } else {
            infoList.add(I18n.format("gui.tooltip.item.uvLightBox.putInLightBox"));
        }
        if (stack.hasTagCompound()) {
            infoList.add(I18n.format("gui.tooltip.item.uvLightBox.etchProgress",stack.getTagCompound().getInteger("etchProgress")));
        } else if (stack.getItemDamage() < 100) {
            infoList.add(I18n.format("gui.tooltip.item.uvLightBox.putInAcid"));
        }
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        super.onEntityItemUpdate(entityItem);
        ItemStack stack = entityItem.getItem();
        if (Fluids.areFluidsEqual(FluidRegistry.lookupFluidForBlock(entityItem.world.getBlockState(new BlockPos(entityItem)).getBlock()), Fluids.ETCHING_ACID)) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            int etchProgress = stack.getTagCompound().getInteger("etchProgress");
            if (etchProgress < 100) {
                if (entityItem.ticksExisted % (TileEntityConstants.PCB_ETCH_TIME / 5) == 0)
                    stack.getTagCompound().setInteger("etchProgress", etchProgress + 1);
            } else {
                entityItem.setItem(new ItemStack(rand.nextInt(100) >= stack.getItemDamage() ? Itemss.UNASSEMBLED_PCB : Itemss.FAILED_PCB));
            }
        }
        return false;
    }
}
