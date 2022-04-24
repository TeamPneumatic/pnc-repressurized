/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemPressurizable extends Item implements IPressurizableItem, IVanishable {
    private final int volume;
    private final float maxPressure;

    public ItemPressurizable(int maxAir, int volume) {
        this(ModItems.defaultProps(), maxAir, volume);
    }

    public ItemPressurizable(Item.Properties props, int maxAir, int volume) {
        super(props);

        this.volume = volume;
        this.maxPressure = (float) maxAir / volume;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return shouldShowPressureDurability(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return getPressureDurabilityColor(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(h -> 1 - (h.getPressure() / h.maxPressure()))
                .orElse(1f);
    }

    static int getPressureDurabilityColor(ItemStack stack) {
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(airHandler -> {
            float f = airHandler.getPressure() / airHandler.maxPressure();
            int c = (int) (64 + 191 * f);
            return 0x40 << 16 | c << 8 | 0xFF;
        }).orElse(0xC0C0C0);
    }

    static boolean shouldShowPressureDurability(ItemStack stack) {
                return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(airHandler -> airHandler.getPressure() < airHandler.maxPressure())
                .orElse(false);
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        // too early to use a capability here :(
        if (this.allowdedIn(group)) {
            items.add(new ItemStack(this));

            ItemStack stack = new ItemStack(this);
            new AirHandlerItemStack(stack, maxPressure).addAir((int) (volume * maxPressure));
            items.add(stack);
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return stack.getItem() instanceof ItemPressurizable ?
                new AirHandlerItemStack(stack, maxPressure) :
                super.initCapabilities(stack, nbt);
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        return ConfigHelper.common().advanced.nbtToClientModification.get() ? roundedPressure(stack) : super.getShareTag(stack);
    }

    @Override
    public int getBaseVolume() {
        return volume;
    }

    @Override
    public int getVolumeUpgrades(ItemStack stack) {
        return UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.VOLUME);
    }

    @Override
    public int getAir(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null ? tag.getInt(AirHandlerItemStack.AIR_NBT_KEY) : 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 9;  // same as iron or compressed iron
    }

    /**
     * Get an ItemStack's NBT, rounding its air level for sync to client.
     * Default precision of volume/10 is enough precision to display 1 decimal place of pressure,
     * and will greatly reduce server->client chatter
     * @param stack the itemstack being sync'd
     * @return the item's NBT, but with the air level rounded
     */
    public static CompoundNBT roundedPressure(ItemStack stack) {
        CompoundNBT tag = stack.getTag();

        if (stack.getItem() instanceof IPressurizableItem && tag != null && tag.contains(AirHandlerItemStack.AIR_NBT_KEY)) {
            // Using a capability here *should* work but it seems to fail under some odd circumstances which I haven't been
            // able to reproduce. Hence the direct-access code above via the internal-use IPressurizableItem interface.
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/650
            int air = tag.getInt(AirHandlerItemStack.AIR_NBT_KEY);
            CompoundNBT tag2 = PneumaticCraftUtils.copyNBTWithout(tag, AirHandlerItemStack.AIR_NBT_KEY);
            int volume = ((IPressurizableItem) stack.getItem()).getEffectiveVolume(stack);
            tag2.putInt(AirHandlerItemStack.AIR_NBT_KEY, air - air % (volume / ConfigHelper.common().advanced.pressureSyncPrecision.get()));
            return tag2;
        } else {
            return tag;
        }
    }
}
