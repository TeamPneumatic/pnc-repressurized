package me.desht.pneumaticcraft.common.thirdparty.coldsweat;

import me.desht.pneumaticcraft.common.thirdparty.ITemperatureProvider;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;

public class ColdSweat implements IThirdParty, ITemperatureProvider {
    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.TEMPERATURE;
    }

    @Override
    public void preInit(IEventBus modBus) {
        ColdSweatSetup.registerEvents();
    }

    @Override
    public void tickAirConditioning(Player player) {
        PneumaticArmorTempModifier.addModifier(player);
    }
}
