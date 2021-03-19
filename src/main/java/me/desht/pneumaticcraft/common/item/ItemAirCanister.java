package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;

public abstract class ItemAirCanister extends ItemPressurizable {
    private ItemAirCanister(int maxAir, int volume) {
        super(ModItems.defaultProps(), maxAir, volume);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        // only completely empty (freshly crafted) canisters may stack
        // this makes it easier for players when needed in a crafting recipe
        return stack.hasTag() ? 1 : super.getItemStackLimit(stack);
    }

    public static class Basic extends ItemAirCanister {
        public Basic() {
            super(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
        }
    }

    public static class Reinforced extends ItemAirCanister {
        public Reinforced() {
            super(PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR, PneumaticValues.REINFORCED_AIR_CANISTER_VOLUME);
        }
    }
}
