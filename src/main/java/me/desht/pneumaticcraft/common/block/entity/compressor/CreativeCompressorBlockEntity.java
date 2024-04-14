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

package me.desht.pneumaticcraft.common.block.entity.compressor;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.inventory.CreativeCompressorMenu;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class CreativeCompressorBlockEntity extends AbstractAirHandlingBlockEntity implements MenuProvider {
    @GuiSynced
    private float pressureSetpoint;

    public CreativeCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.CREATIVE_COMPRESSOR.get(), pos, state, PressureTier.TIER_TWO, 50000, 0);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        pressureSetpoint = tag.getFloat("setpoint");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putFloat("setpoint", pressureSetpoint);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        airHandler.setPressure(pressureSetpoint);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        try {
            pressureSetpoint += Float.parseFloat(tag);
            if (pressureSetpoint > 30) pressureSetpoint = 30;
            if (pressureSetpoint < -1) pressureSetpoint = -1;
            setChanged();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new CreativeCompressorMenu(i, playerInventory, getBlockPos());
    }

}
