package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicTicking;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.HEAT_EXCHANGER_CAPABILITY;

public class CapabilityHeat {
    public static void register() {
        CapabilityManager.INSTANCE.register(IHeatExchangerLogic.class, new Capability.IStorage<IHeatExchangerLogic>() {
            @Nullable
            @Override
            public CompoundNBT writeNBT(Capability<IHeatExchangerLogic> capability, IHeatExchangerLogic instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IHeatExchangerLogic> capability, IHeatExchangerLogic instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) instance.deserializeNBT((CompoundNBT) nbt);
            }
        }, HeatExchangerLogicTicking::new);
    }

    public static class Provider implements ICapabilitySerializable<INBT> {
        private final IHeatExchangerLogic impl = new HeatExchangerLogicTicking();
        private final LazyOptional<IHeatExchangerLogic> l = LazyOptional.of(() -> impl);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
            return cap == HEAT_EXCHANGER_CAPABILITY ? l.cast() : LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return HEAT_EXCHANGER_CAPABILITY.getStorage().writeNBT(HEAT_EXCHANGER_CAPABILITY, this.impl, null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            HEAT_EXCHANGER_CAPABILITY.getStorage().readNBT(HEAT_EXCHANGER_CAPABILITY, this.impl, null, nbt);
        }
    }
}
