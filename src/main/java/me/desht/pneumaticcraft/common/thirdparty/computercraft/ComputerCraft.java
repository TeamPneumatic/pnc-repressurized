package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import me.desht.pneumaticcraft.common.tileentity.ILuaMethodProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber
public class ComputerCraft implements IThirdParty {
    @CapabilityInject(IPeripheral.class)
    public static final Capability<IPeripheral> PERIPHERAL_CAPABILITY = null;

    @Override
    public void init() {
        ComputerEventManager.getInstance().registerSender((te, name, params) -> te.getCapability(PERIPHERAL_CAPABILITY).ifPresent(handler -> {
            if (handler instanceof ComputerEventManager.IComputerEventSender) {
                ((ComputerEventManager.IComputerEventSender) handler).sendEvent(te, name, params);
            }
        }));
    }

    @SubscribeEvent
    public static void attachPeripheralCap(AttachCapabilitiesEvent<TileEntity> event) {
        if (PERIPHERAL_CAPABILITY != null && event.getObject() instanceof ILuaMethodProvider) {
            event.addCapability(RL("computercraft"), new PneumaticPeripheralProvider((ILuaMethodProvider) event.getObject()));
        }
    }

    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.COMPUTER;
    }
}
