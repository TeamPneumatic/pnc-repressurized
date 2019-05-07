package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fml.relauncher.Side;
import toughasnails.api.temperature.TemperatureHelper;

public class ToughAsNails implements IThirdParty {
    @Override
    public void init() {
        NetworkHandler.registerMessage(PacketPlayerTemperatureDelta.class, PacketPlayerTemperatureDelta.class, Side.CLIENT);
    }

    @Override
    public void postInit() {
        TemperatureHelper.registerTemperatureModifier(new TANModifierPNCBlock());
        TemperatureHelper.registerTemperatureModifier(new TANModifierAirConditioning());
    }
}
