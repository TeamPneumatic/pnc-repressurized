package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityAirHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IAirHandler.class, new DefaultAirHandlerStorage<>(),
                () -> new BasicAirHandler(1000));
        CapabilityManager.INSTANCE.register(IAirHandlerItem.class, new DefaultAirHandlerStorage<>(),
                () -> new AirHandlerItemStack(new ItemStack(ModItems.AIR_CANISTER.get()), PneumaticValues.AIR_CANISTER_VOLUME, 10f));
        CapabilityManager.INSTANCE.register(IAirHandlerMachine.class, new DefaultAirHandlerStorage<>(),
                () -> new MachineAirHandler(PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, 3000));
    }

    public static class DefaultAirHandlerStorage<T extends IAirHandler> implements Capability.IStorage<T> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            return new IntNBT(instance.getAir());
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
            instance.addAir(((IntNBT) nbt).getInt());
        }
    }
}
