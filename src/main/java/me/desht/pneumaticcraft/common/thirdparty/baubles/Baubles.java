package me.desht.pneumaticcraft.common.thirdparty.baubles;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.tileentity.PneumaticEnergyStorage;
import net.minecraft.entity.player.PlayerEntity;

public class Baubles implements IThirdParty {
//    @CapabilityInject(IBaublesItemHandler.class)
//    public static Capability<IBaublesItemHandler> CAPABILITY_BAUBLES = null;

    public static boolean available = false;

    @Override
    public void preInit() {
        available = true;
    }

    public static void chargeBaubles(PlayerEntity player, PneumaticEnergyStorage energyStorage, int rfPerTick) {
        // TODO 1.14
//        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
//
//        if (handler != null) {
//            for (int i = 0; i < handler.getSlots(); i++) {
//                ItemStack stack = handler.getStackInSlot(i);
//                if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
//                    IEnergyStorage receivingStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
//                    int energyLeft = energyStorage.getEnergyStored();
//                    if (energyLeft > 0) {
//                        energyStorage.extractEnergy(receivingStorage.receiveEnergy(Math.min(energyLeft, rfPerTick), false), false);
//                    }
//                    if (energyStorage.getEnergyStored() == 0) {
//                        break;
//                    }
//                }
//            }
//        }
    }
}
