package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeItem;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemMachineUpgrade extends Item implements IUpgradeItem {
    public static final String NBT_DIRECTION = "Facing";
    private final EnumUpgrade upgrade;
    private final int tier;

    public ItemMachineUpgrade(EnumUpgrade upgrade, int tier) {
        super(ModItems.defaultProps());
        this.upgrade = upgrade;
        this.tier = tier;
    }

    public EnumUpgrade getUpgradeType() {
        return upgrade;
    }

    public int getTier() {
        return tier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag par4) {
        if (ClientUtils.hasShiftDown()) {
            infoList.add(xlate("gui.tooltip.item.upgrade.usedIn"));
            PneumaticRegistry.getInstance().getItemRegistry().addTooltip(upgrade, infoList);
        } else {
            infoList.add(xlate("gui.tooltip.item.upgrade.shiftMessage").applyTextStyle(TextFormatting.AQUA));
        }
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            Direction dir = stack.hasTag() ? Direction.byName(NBTUtil.getString(stack, NBT_DIRECTION)) : null;
            infoList.add(xlate("message.dispenser.direction", dir == null ? "*" : dir.getName()));
            infoList.add(xlate("message.dispenser.clickToSet"));
        }
        super.addInformation(stack, world, infoList, par4);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            if (!context.getWorld().isRemote) {
                setDirection(context.getPlayer(), context.getHand(), context.getFace());
            }
            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            if (!worldIn.isRemote) {
                setDirection(playerIn, handIn, null);
            }
            return ActionResult.newResult(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    private void setDirection(PlayerEntity player, Hand hand, Direction facing) {
        ItemStack stack = player.getHeldItem(hand);
        if (facing == null) {
            stack.setTag(null);
            player.sendStatusMessage(new TranslationTextComponent("message.dispenser.direction", "*"), true);
        } else {
            NBTUtil.setString(stack, NBT_DIRECTION, facing.getName());
            player.sendStatusMessage(new TranslationTextComponent("message.dispenser.direction", facing.getName()), true);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return getUpgradeType() == EnumUpgrade.CREATIVE ? Rarity.EPIC : Rarity.COMMON;
    }

    public static ItemMachineUpgrade of(ItemStack stack) {
        return (ItemMachineUpgrade) stack.getItem();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            if (upgrade.isDepLoaded()) {
                items.add(new ItemStack(this));
            }
        }
    }
}
