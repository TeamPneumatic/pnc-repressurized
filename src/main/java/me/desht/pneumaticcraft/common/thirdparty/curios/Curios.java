package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.tileentity.PneumaticEnergyStorage;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import top.theillusivec4.curios.api.CuriosAPI;

public class Curios implements IThirdParty {
    public static boolean available = false;

    @Override
    public void preInit() {
        available = true;
    }

    /**
     * Supply Forge Energy from the given energy storage to all Curio items the player has.
     *
     * @param player the player
     * @param energyStorage source energy storage
     * @param maxTransfer max amount to transfer per item
     */
    public static void chargeItems(PlayerEntity player, PneumaticEnergyStorage energyStorage, int maxTransfer) {
        CuriosAPI.getCuriosHandler(player).ifPresent(handler -> handler.getCurioMap().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots() && energyStorage.getEnergyStored() > 0; i++) {
                ItemStack stack = stackHandler.getStackInSlot(i);
                stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(receivingStorage -> {
                    int energyLeft = energyStorage.getEnergyStored();
                    energyStorage.extractEnergy(
                            receivingStorage.receiveEnergy(Math.min(energyLeft, maxTransfer), false), false
                    );
                });
            }
        }));
    }

    /**
     * Called when player is connected or disconnected.  Register or unregister side configurator entries for each
     * Curios type found on the player.
     * @param configurator the side configurator
     * @param player the player - may be null
     */
    public static void setupSideConfigurator(SideConfigurator<IItemHandler> configurator, PlayerEntity player) {
        if (player != null) {
            CuriosAPI.getCuriosHandler(player).ifPresent(handler -> handler.getCurioMap().forEach((id, stackHandler) -> {
                configurator.registerHandler("curios_" + id, CuriosAPI.getIcon(id), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> stackHandler);
            }));
        } else {
            configurator.unregisterHandlers(s -> s.startsWith("curios_"));
        }
    }

    public static IItemHandler makeCombinedInvWrapper(PlayerEntity player) {
        return CuriosAPI.getCuriosHandler(player)
                .map(handler -> new CombinedInvWrapper(handler.getCurioMap().values().toArray(new IItemHandlerModifiable[0])))
                .orElse(new CombinedInvWrapper());

    }
}
