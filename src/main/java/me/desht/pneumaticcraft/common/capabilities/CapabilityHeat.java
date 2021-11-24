/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicTicking;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

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

//    public static class Provider implements ICapabilitySerializable<INBT> {
//        private final IHeatExchangerLogic impl = new HeatExchangerLogicTicking();
//        private final LazyOptional<IHeatExchangerLogic> l = LazyOptional.of(() -> impl);
//
//        @Nonnull
//        @Override
//        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
//            return cap == HEAT_EXCHANGER_CAPABILITY ? l.cast() : LazyOptional.empty();
//        }
//
//        @Override
//        public INBT serializeNBT() {
//            return HEAT_EXCHANGER_CAPABILITY.getStorage().writeNBT(HEAT_EXCHANGER_CAPABILITY, this.impl, null);
//        }
//
//        @Override
//        public void deserializeNBT(INBT nbt) {
//            HEAT_EXCHANGER_CAPABILITY.getStorage().readNBT(HEAT_EXCHANGER_CAPABILITY, this.impl, null, nbt);
//        }
//    }
}
