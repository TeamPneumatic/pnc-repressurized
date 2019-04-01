package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemMachineUpgrade extends ItemPneumatic {
    public static final String NBT_DIRECTION = "Facing";
    private final int index;

    public ItemMachineUpgrade(String registryName, int index) {
        super(registryName);
        this.index = index;
    }

    public IItemRegistry.EnumUpgrade getUpgradeType() {
        return IItemRegistry.EnumUpgrade.values()[index];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> infoList, ITooltipFlag par4) {
        if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
            infoList.add(I18n.format("gui.tooltip.item.upgrade.usedIn"));
            PneumaticRegistry.getInstance().getItemRegistry().addTooltip(this, infoList);
        } else {
            infoList.add(I18n.format("gui.tooltip.item.upgrade.shiftMessage"));
        }
        if (getUpgradeType() == IItemRegistry.EnumUpgrade.DISPENSER) {
            EnumFacing dir = stack.hasTagCompound() ? EnumFacing.byName(NBTUtil.getString(stack, NBT_DIRECTION)) : null;
            infoList.add(I18n.format("message.dispenser.direction", dir == null ? "*" : dir.getName()));
            infoList.add(I18n.format("message.dispenser.clickToSet"));
        }
        super.addInformation(stack, world, infoList, par4);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (getUpgradeType() == IItemRegistry.EnumUpgrade.DISPENSER) {
            if (!world.isRemote) {
                setDirection(player, hand, side);
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (getUpgradeType() == IItemRegistry.EnumUpgrade.DISPENSER) {
            if (!worldIn.isRemote) {
                setDirection(playerIn, handIn, null);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    private void setDirection(EntityPlayer player, EnumHand hand, EnumFacing facing) {
        ItemStack stack = player.getHeldItem(hand);
        if (facing == null) {
            stack.setTagCompound(null);
            player.sendStatusMessage(new TextComponentTranslation("message.dispenser.direction", "*"), true);
        } else {
            NBTUtil.setString(stack, NBT_DIRECTION, facing.getName());
            player.sendStatusMessage(new TextComponentTranslation("message.dispenser.direction", facing.getName()), true);
        }
    }
}
