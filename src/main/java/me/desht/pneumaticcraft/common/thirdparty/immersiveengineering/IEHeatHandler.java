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

package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class IEHeatHandler {
    public static class Impl implements ExternalHeaterHandler.IExternalHeatable {
        private final BlockEntity blockEntity;

        public Impl(BlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public int doHeatTick(int energyAvailable, boolean redstone) {
            return blockEntity.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).map(handler -> {
                int rfPerTick = ConfigHelper.common().integration.ieExternalHeaterFEperTick.get();
                double heatPerRF = ConfigHelper.common().integration.ieExternalHeaterHeatPerFE.get();
                if (energyAvailable >= rfPerTick) {
                    handler.addHeat(rfPerTick * heatPerRF);
                    return rfPerTick;
                }
                return 0;
            }).orElse(0);
        }
    }

    public static class Provider implements net.minecraftforge.common.capabilities.ICapabilityProvider {
        private final Impl impl;
        private final LazyOptional<ExternalHeaterHandler.IExternalHeatable> lazy;

        public Provider(BlockEntity blockEntity) {
            this.impl = new Impl(blockEntity);
            this.lazy = LazyOptional.of(() -> impl);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return ExternalHeaterHandler.CAPABILITY.orEmpty(cap, lazy);
        }

        public void invalidate() {
            lazy.invalidate();
        }
    }
}
