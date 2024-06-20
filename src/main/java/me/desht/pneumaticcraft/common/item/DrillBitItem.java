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

import me.desht.pneumaticcraft.common.item.JackHammerItem.DigMode;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class DrillBitItem extends Item {
    private final DrillBitType type;

    public DrillBitItem(DrillBitType type) {
        super(ModItems.defaultProps().stacksTo(1));

        this.type = type;
    }

    public DrillBitType getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.tier").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(getType().tier.toString()).withStyle(ChatFormatting.GOLD)));
        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.blocks").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(Integer.toString(getType().getBestDigType().getBlocksDug())).withStyle(ChatFormatting.GOLD)));
        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.speed").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(Integer.toString(getType().baseEfficiency)).withStyle(ChatFormatting.GOLD)));
    }

    public enum DrillBitType {
        // double supplier is necessary since registry objects will still be null at time of enum init
        NONE("none", null, Tiers.WOOD, 0x00000000, 1, 0),
        IRON("iron", () -> ModItems.IRON_DRILL_BIT, Tiers.IRON, 0xFFd8d8d8, 6, 1),
        COMPRESSED_IRON("compressed_iron", () -> ModItems.COMPRESSED_IRON_DRILL_BIT, Tiers.IRON, 0xFF4d4846, 7, 2),
        DIAMOND("diamond", () -> ModItems.DIAMOND_DRILL_BIT, Tiers.DIAMOND, 0xFF4aedd9, 8, 3),
        NETHERITE("netherite", () -> ModItems.NETHERITE_DRILL_BIT, Tiers.NETHERITE, 0xFF31292a, 9, 4);

        private final String name;
        private final Supplier<Supplier<? extends DrillBitItem>> itemSupplier;
        private final Tier tier;
        private final int tint;
        private final int baseEfficiency;
        private final int bitQuality;

        DrillBitType(String name, Supplier<Supplier<? extends DrillBitItem>> itemSupplier, Tier tier, int tint, int baseEfficiency, int bitQuality) {
            this.name = name;
            this.itemSupplier = itemSupplier;
            this.tier = tier;
            this.tint = tint;
            this.baseEfficiency = baseEfficiency;
            this.bitQuality = bitQuality;
        }

        public Tier getTier() {
            return tier;
        }

        public int getTint() {
            return tint;
        }

        public int getBitQuality() {
            // this controls the available dig modes for the bit type
            return bitQuality;
        }

        public String getRegistryName() {
            return "drill_bit_" + name;
        }

        public int getBaseEfficiency() {
            return baseEfficiency;
        }

        public ItemStack asItemStack() {
            return itemSupplier == null ? ItemStack.EMPTY : new ItemStack(itemSupplier.get().get());
        }

        public DigMode getBestDigType() {
            for (int i = DigMode.values().length - 1; i >= 0; i--) {
                DigMode digMode = DigMode.values()[i];
                if (digMode.getBitType().getBitQuality() <= getBitQuality()) {
                    return digMode;
                }
            }
            return DigMode.MODE_1X1;
        }
    }
}
