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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerCreativeCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileEntityCreativeCompressor extends TileEntityPneumaticBase implements INamedContainerProvider {
    @GuiSynced
    private float pressureSetpoint;

    public TileEntityCreativeCompressor() {
        super(ModTileEntities.CREATIVE_COMPRESSOR.get(), 30, 30, 50000, 0);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        pressureSetpoint = tag.getFloat("setpoint");
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putFloat("setpoint", pressureSetpoint);
        return nbt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            airHandler.setPressure(pressureSetpoint);
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        try {
            pressureSetpoint += Float.parseFloat(tag);
            if (pressureSetpoint > 30) pressureSetpoint = 30;
            if (pressureSetpoint < -1) pressureSetpoint = -1;
            setChanged();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerCreativeCompressor(i, playerInventory, getBlockPos());
    }

}
