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
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.Objects;

public class PressurizableItem extends Item implements IPressurizableItem, Vanishable {
    private final int volume;
    private final float maxPressure;

    public PressurizableItem(Item.Properties props, int maxAir, int volume) {
        super(props);

        this.volume = volume;
        this.maxPressure = (float) maxAir / volume;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return shouldShowPressureDurability(pStack);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return pStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(h -> Math.round(h.getPressure() / h.maxPressure() * 13F))
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return getPressureDurabilityColor(pStack);
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

//    @Override
//    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
//        // too early to use a capability here :(
//        if (this.allowedIn(group)) {
//            items.add(new ItemStack(this));
//
//            ItemStack stack = new ItemStack(this);
//            new AirHandlerItemStack(stack, maxPressure).addAir((int) (volume * maxPressure));
//            items.add(stack);
//        }
//    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return stack.getItem() instanceof PressurizableItem ?
                new AirHandlerItemStack(stack) :
                super.initCapabilities(stack, nbt);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        return ConfigHelper.common().advanced.nbtToClientModification.get() ? roundedPressure(stack) : super.getShareTag(stack);
    }

    @Override
    public int getBaseVolume() {
        return volume;
    }

    @Override
    public int getVolumeUpgrades(ItemStack stack) {
        return UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.VOLUME.get());
    }

    @Override
    public int getAir(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(AirHandlerItemStack.AIR_NBT_KEY) : 0;
    }

    @Override
    public float getMaxPressure() {
        return maxPressure;
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
    public static CompoundTag roundedPressure(ItemStack stack) {
        if (stack.getItem() instanceof IPressurizableItem p && stack.getTag() != null && stack.getTag().contains(AirHandlerItemStack.AIR_NBT_KEY)) {
            ItemStack stackCopy = stack.copy();
            CompoundTag tag = Objects.requireNonNull(stackCopy.getTag());
            // Using a capability here *should* work but it seems to fail under some odd circumstances which I haven't been
            // able to reproduce. Hence the direct-access code above via the internal-use IPressurizableItem interface.
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/650
            int air = tag.getInt(AirHandlerItemStack.AIR_NBT_KEY);
            int volume = p.getEffectiveVolume(stackCopy);
            // ok to modify tag directly here because we're working on a copy of the itemstack
            tag.putInt(AirHandlerItemStack.AIR_NBT_KEY, air - air % (volume / ConfigHelper.common().advanced.pressureSyncPrecision.get()));
            return tag;
        } else {
            return stack.getTag();
        }
    }
}
