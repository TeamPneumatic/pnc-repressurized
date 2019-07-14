package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.pressure.AirHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.AIR_HANDLER_CAPABILITY;

public class Pressure {
    public static void register() {
        CapabilityManager.INSTANCE.register(IAirHandler.class, new Capability.IStorage<IAirHandler>() {
            @Nullable
            @Override
            public CompoundNBT writeNBT(Capability<IAirHandler> capability, IAirHandler instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IAirHandler> capability, IAirHandler instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) instance.deserializeNBT((CompoundNBT) nbt);
            }
        }, AirHandler::new);
    }

    public static class Provider implements ICapabilitySerializable<INBT> {
        private final IAirHandler impl = new AirHandler();
        private final LazyOptional<IAirHandler> l = LazyOptional.of(() -> impl);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
            return cap == AIR_HANDLER_CAPABILITY ? l.cast() : LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return AIR_HANDLER_CAPABILITY.getStorage().writeNBT(AIR_HANDLER_CAPABILITY, this.impl, null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            AIR_HANDLER_CAPABILITY.getStorage().readNBT(AIR_HANDLER_CAPABILITY, this.impl, null, nbt);
        }
    }
}
