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

package me.desht.pneumaticcraft.common.block.entity.spawning;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IRedstoneControl;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.inventory.VacuumPumpMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class VacuumPumpBlockEntity extends AbstractAirHandlingBlockEntity implements
        IRedstoneControl<VacuumPumpBlockEntity>, IManoMeasurable, MenuProvider {
    @GuiSynced
    private final IAirHandlerMachine vacuumHandler;
    public int rotation;
    public int oldRotation;
    private int turnTimer = -1;
    @DescSynced
    public boolean turning = false;
    private int rotationSpeed;
    @GuiSynced
    public final RedstoneController<VacuumPumpBlockEntity> rsController = new RedstoneController<>(this);

    public VacuumPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.VACUUM_PUMP.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_VACUUM_PUMP, 4);

        this.vacuumHandler  = new MachineAirHandler(PressureTier.TIER_ONE, PneumaticValues.VOLUME_VACUUM_PUMP);
    }

    @Override
    public IAirHandlerMachine getAirHandler(Direction side) {
        if (side == getVacuumSide()) {
            return vacuumHandler;
        } else if (side == getInputSide() || side == null) {
            return super.getAirHandler(side);
        } else {
            return null;
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getInputSide() || side == getVacuumSide();
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    public Direction getInputSide() {
        return getVacuumSide().getOpposite();
    }

    public Direction getVacuumSide() {
        return getRotation();
    }

    @Override
    public void tickClient() {
        super.tickClient();

        oldRotation = rotation;
        if (turning) {
            rotationSpeed = Math.min(rotationSpeed + 1, 20);
        } else {
            rotationSpeed = Math.max(rotationSpeed - 1, 0);
        }
        rotation += rotationSpeed;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (turnTimer >= 0) turnTimer--;

        if (airHandler.getPressure() > PneumaticValues.MIN_PRESSURE_VACUUM_PUMP && vacuumHandler.getPressure() > -0.99F && rsController.shouldRun()) {
            if (turnTimer == -1) {
                turning = true;
            }
            airHandler.addAir((int) (-PneumaticValues.USAGE_VACUUM_PUMP * getSpeedUsageMultiplierFromUpgrades()));
            // negative because it's creating a vacuum.
            vacuumHandler.addAir((int) (-PneumaticValues.PRODUCTION_VACUUM_PUMP * getSpeedMultiplierFromUpgrades()));
            turnTimer = 40;
        }
        if (turnTimer == 0) {
            turning = false;
        }
        airHandler.setSideLeaking(airHandler.getConnectedAirHandlers(this).isEmpty() ? getInputSide() : null);
        vacuumHandler.setSideLeaking(vacuumHandler.getConnectedAirHandlers(this).isEmpty() ? getVacuumSide() : null);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("vacuum", vacuumHandler.serializeNBT());
        tag.putBoolean("turning", turning);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        vacuumHandler.deserializeNBT(tag.getCompound("vacuum"));
        turning = tag.getBoolean("turning");
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public void printManometerMessage(Player player, List<Component> curInfo) {
        String input = PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1);
        String vac = PneumaticCraftUtils.roundNumberTo(vacuumHandler.getPressure(), 1);
        curInfo.add(xlate("pneumaticcraft.message.vacuum_pump.manometer", input, vac).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public RedstoneController<VacuumPumpBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new VacuumPumpMenu(i, playerInventory, getBlockPos());
    }
}
