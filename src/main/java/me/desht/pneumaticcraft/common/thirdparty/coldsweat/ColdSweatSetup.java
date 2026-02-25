package me.desht.pneumaticcraft.common.thirdparty.coldsweat;

import com.momosoftworks.coldsweat.api.event.core.registry.BlockTempRegisterEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ColdSweatSetup {
    static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(ColdSweatSetup::registerModifiers);
        NeoForge.EVENT_BUS.addListener(ColdSweatSetup::registerBlockTemp);
    }

    private static void registerBlockTemp(BlockTempRegisterEvent event) {
        event.register(new PNCHeatBlockTemp());
    }

    private static void registerModifiers(TempModifierRegisterEvent event) {
        event.register(PneumaticArmorTempModifier.ID, PneumaticArmorTempModifier::new);
    }
}
