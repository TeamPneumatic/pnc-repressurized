package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;

public class IEIntegration {
    static void registerFuels() {
        // equivalent to IE biodiesel
        DieselHandler.registerFuel(PneumaticCraftTags.Fluids.DIESEL, 125);
        DieselHandler.registerFuel(PneumaticCraftTags.Fluids.BIODIESEL, 125);
    }
}
