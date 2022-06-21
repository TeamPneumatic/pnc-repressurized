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

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.JackHammerItem.DigMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.tier").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(getType().tier.toString()).withStyle(ChatFormatting.GOLD)));
        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.blocks").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(Integer.toString(getType().getBestDigType().getBlocksDug())).withStyle(ChatFormatting.GOLD)));
        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.speed").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(Integer.toString(getType().baseEfficiency)).withStyle(ChatFormatting.GOLD)));
    }

    public enum DrillBitType {
        NONE("none", Tiers.WOOD, 0x00000000, 1, 0),
        IRON("iron", Tiers.IRON, 0xFFd8d8d8, 6, 1),
        COMPRESSED_IRON("compressed_iron", Tiers.IRON, 0xFF4d4846, 7, 2),
        DIAMOND("diamond", Tiers.DIAMOND, 0xFF4aedd9, 8, 3),
        NETHERITE("netherite", Tiers.NETHERITE, 0xFF31292a, 9, 4);

        private final String name;
        private final Tier tier;
        private final int tint;
        private final int baseEfficiency;
        private final int bitQuality;

        DrillBitType(String name, Tier tier, int tint, int baseEfficiency, int bitQuality) {
            this.name = name;
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
            return this == NONE ? ItemStack.EMPTY : new ItemStack(ForgeRegistries.ITEMS.getValue(RL(getRegistryName())));
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
