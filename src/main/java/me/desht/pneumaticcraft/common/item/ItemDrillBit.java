package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemJackHammer.DigMode;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemDrillBit extends Item implements ColorHandlers.ITintableItem {
    private final DrillBitType type;

    public ItemDrillBit(DrillBitType type) {
        super(ModItems.defaultProps().stacksTo(1));

        this.type = type;
    }

    public DrillBitType getType() {
        return type;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return type.getTint();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.tier").withStyle(TextFormatting.YELLOW)
                .append(new StringTextComponent(Integer.toString(getType().tier)).withStyle(TextFormatting.GOLD)));
        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.blocks").withStyle(TextFormatting.YELLOW)
                .append(new StringTextComponent(Integer.toString(getType().getBestDigType().getBlocksDug())).withStyle(TextFormatting.GOLD)));
        tooltip.add(xlate("pneumaticcraft.gui.tooltip.item.drillBit.speed").withStyle(TextFormatting.YELLOW)
                .append(new StringTextComponent(Integer.toString(getType().baseEfficiency)).withStyle(TextFormatting.GOLD)));
    }

    public enum DrillBitType {
        NONE("none", 0, 0x00000000, 1, -1),
        IRON("iron", 1, 0xFFD8D8D8, 6, 2),
        COMPRESSED_IRON("compressed_iron", 2, 0xFF888888, 7, 2),
        DIAMOND("diamond", 3, 0xFF2BC7AC, 8, 3),
        NETHERITE("netherite", 4, 0xFF4A2940, 9, 4);

        private final String name;
        private final int tier;
        private final int tint;
        private final int baseEfficiency;
        private final int harvestLevel;

        DrillBitType(String name, int tier, int tint, int baseEfficiency, int harvestLevel) {
            this.name = name;
            this.tier = tier;
            this.tint = tint;
            this.baseEfficiency = baseEfficiency;
            this.harvestLevel = harvestLevel;
        }

        public int getTier() {
            return tier;
        }

        public int getTint() {
            return tint;
        }

        public int getHarvestLevel() {
            return harvestLevel;
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
            DigMode best = DigMode.MODE_1X1;
            for (DigMode dt : DigMode.values()) {
                if (dt.getBitType().getTier() > this.getTier()) {
                    return best;
                }
                best = dt;
            }
            return DigMode.MODE_VEIN_PLUS;
        }
    }
}
