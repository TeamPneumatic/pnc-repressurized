package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerBase;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityAirHandler {
    @CapabilityInject(IAirHandlerBase.class)
    public static Capability<IAirHandlerBase> AIR_HANDLER_CAPABILITY = null;
    @CapabilityInject(IAirHandlerItem.class)
    public static Capability<IAirHandlerItem> AIR_HANDLER_ITEM_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IAirHandlerBase.class, new DefaultAirHandlerStorage<>(),
                () -> new BasicAirHandler(1000));
        CapabilityManager.INSTANCE.register(IAirHandlerItem.class, new DefaultAirHandlerStorage<>(),
                () -> new AirHandlerItemStack(new ItemStack(ModItems.AIR_CANISTER), PneumaticValues.AIR_CANISTER_VOLUME, 10f));
    }

    public static class DefaultAirHandlerStorage<T extends IAirHandlerBase> implements Capability.IStorage<T> {
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
