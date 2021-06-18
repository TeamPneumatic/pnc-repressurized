package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.actions.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pneumaticcraft.XPFluid")
@ZenRegister
public class XPFluid {
    @ZenCodeType.Method
    public void registerXPFluid(CTFluidIngredient fluid, int ratio) {
        CraftTweakerAPI.apply(new XPFluidActionAdd(fluid, ratio));
    }

    @ZenCodeType.Method
    public void unregisterXPFluid(CTFluidIngredient fluid) {
        CraftTweakerAPI.apply(new XPFluidActionAdd(fluid, 0));
    }

    public static class XPFluidActionAdd implements IRuntimeAction {
        private final CTFluidIngredient fluidIngredient;
        private final int ratio;

        public XPFluidActionAdd(CTFluidIngredient fluidIngredient, int ratio) {
            this.fluidIngredient = fluidIngredient;
            this.ratio = ratio;
        }

        @Override
        public void apply() {
            XPFluidManager.getInstance().registerXPFluid(CTUtils.toFluidIngredient(fluidIngredient), ratio);
        }

        @Override
        public String describe() {
            if (ratio <= 0) {
                return "Unregistering XP fluid " + fluidIngredient.getCommandString();
            } else {
                return "Registering XP fluid " + fluidIngredient.getCommandString() + " with mB->XP ratio: " + ratio;
            }
        }
    }
}
