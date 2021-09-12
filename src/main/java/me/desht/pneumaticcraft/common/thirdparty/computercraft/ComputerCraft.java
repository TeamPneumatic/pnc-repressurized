package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import me.desht.pneumaticcraft.common.tileentity.ILuaMethodProvider;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class ComputerCraft implements IThirdParty {
    private static boolean available;

    @Override
    public void preInit() {
        available = true;
    }

    @Override
    public void init() {
        ComputerEventManager.getInstance().registerSender((te, name, params) -> te.getCapability(PneumaticTilePeripheral.PERIPHERAL_CAPABILITY).ifPresent(handler -> {
            if (handler instanceof ComputerEventManager.IComputerEventSender) {
                ((ComputerEventManager.IComputerEventSender) handler).sendEvent(te, name, params);
            }
        }));
    }

    @SubscribeEvent
    public static void attachPeripheralCap(AttachCapabilitiesEvent<TileEntity> event) {
        if (available && event.getObject() instanceof ILuaMethodProvider) {
            event.addCapability(RL(ModIds.COMPUTERCRAFT), new PneumaticPeripheralProvider((ILuaMethodProvider) event.getObject()));
        }
    }

    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.COMPUTER;
    }
}
