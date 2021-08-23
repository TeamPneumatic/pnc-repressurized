package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeItem;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.NBTUtils;
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
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag par4) {
        if (ClientUtils.hasShiftDown()) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.upgrade.usedIn"));
            PneumaticRegistry.getInstance().getItemRegistry().addTooltip(upgrade, infoList);
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.upgrade.shiftMessage").withStyle(TextFormatting.AQUA));
        }
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            Direction dir = stack.hasTag() ? Direction.byName(NBTUtils.getString(stack, NBT_DIRECTION)) : null;
            infoList.add(xlate("pneumaticcraft.message.dispenser.direction", dir == null ? "*" : dir.getSerializedName()));
            infoList.add(xlate("pneumaticcraft.message.dispenser.clickToSet"));
        }
        super.appendHoverText(stack, world, infoList, par4);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            if (!context.getLevel().isClientSide) {
                setDirection(context.getPlayer(), context.getHand(), context.getClickedFace());
            }
            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            if (!worldIn.isClientSide) {
                setDirection(playerIn, handIn, null);
            }
            return ActionResult.success(playerIn.getItemInHand(handIn));
        }
        return super.use(worldIn, playerIn, handIn);
    }

    private void setDirection(PlayerEntity player, Hand hand, Direction facing) {
        ItemStack stack = player.getItemInHand(hand);
        if (facing == null) {
            stack.setTag(null);
            player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.message.dispenser.direction", "*"), true);
        } else {
            NBTUtils.setString(stack, NBT_DIRECTION, facing.getSerializedName());
            player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.message.dispenser.direction", facing.getSerializedName()), true);
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
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            if (upgrade.isDepLoaded()) {
                items.add(new ItemStack(this));
            }
        }
    }
}
