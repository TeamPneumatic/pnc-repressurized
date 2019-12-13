package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Some helper methods to manage items which can store upgrades (Pneumatic Armor, Drones...)
 */
public class UpgradableItemUtils {
    public static final String NBT_CREATIVE = "CreativeUpgrade";

    /**
     * Add a standardized tooltip listing the installed upgrades in the given item.
     *
     * @param iStack the item
     * @param world the world
     * @param textList list of text to append tooltip too
     * @param flag tooltip flag
     * @return number of (unique) upgrades in the item
     */
    public static int addUpgradeInformation(ItemStack iStack, IBlockReader world, List<ITextComponent> textList, ITooltipFlag flag) {
        ItemStack[] inventoryStacks = getUpgradeStacks(iStack);
        boolean isItemEmpty = true;
        for (ItemStack stack : inventoryStacks) {
            if (!stack.isEmpty()) {
                isItemEmpty = false;
                break;
            }
        }
        if (isItemEmpty) {
            textList.add(xlate("gui.tooltip.upgrades.empty").applyTextStyle(TextFormatting.DARK_GREEN));
        } else {
            textList.add(xlate("gui.tooltip.upgrades.not_empty").applyTextStyle(TextFormatting.GREEN));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(textList, inventoryStacks);
        }
        return inventoryStacks.length;
    }

    /**
     * Retrieves the upgrades currently installed on the given itemstack.
     */
    public static ItemStack[] getUpgradeStacks(ItemStack iStack) {
        CompoundNBT tag;
        if (iStack.getItem() instanceof BlockItem) {
            // tag will be in the serialized BlockEntityTag
            tag = iStack.getChildTag("BlockEntityTag");
            if (tag == null) return new ItemStack[0];
            tag = tag.getCompound(ChargeableItemHandler.NBT_UPGRADE_TAG);
        } else {
            // TODO implement this as a capability
            tag = NBTUtil.getCompoundTag(iStack, ChargeableItemHandler.NBT_UPGRADE_TAG);
        }
        ItemStack[] inventoryStacks = new ItemStack[9];
        Arrays.fill(inventoryStacks, ItemStack.EMPTY);
        ListNBT itemList = tag.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            CompoundNBT slotEntry = itemList.getCompound(i);
            int j = slotEntry.getByte("Slot");
            if (j >= 0 && j < 9) {
                inventoryStacks[j] = ItemStack.read(slotEntry);
            }
        }
        return inventoryStacks;
    }

    public static int getUpgrades(EnumUpgrade upgrade, ItemStack stack) {
        return getUpgrades(upgrade.getItem(), stack);
    }

    public static int getUpgrades(Item upgrade, ItemStack iStack) {
        int upgrades = 0;
        ItemStack[] stacks = getUpgradeStacks(iStack);
        for (ItemStack stack : stacks) {
            if (stack.getItem() == upgrade) {
                upgrades += stack.getCount();
            }
        }
        return upgrades;
    }
}
