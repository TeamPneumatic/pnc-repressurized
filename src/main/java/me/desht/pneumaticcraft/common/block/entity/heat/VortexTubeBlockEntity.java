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

package me.desht.pneumaticcraft.common.block.entity.heat;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.block.entity.IHeatTinted;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class VortexTubeBlockEntity extends AbstractAirHandlingBlockEntity implements IHeatTinted, IHeatExchangingTE {
    // hot side heat exchanger is also the default
    private final IHeatExchangerLogic hotHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final IHeatExchangerLogic coldHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final IHeatExchangerLogic connectingExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    @DescSynced
    private final SyncedTemperature syncHot = new SyncedTemperature(hotHeatExchanger);
    @DescSynced
    private final SyncedTemperature syncCold = new SyncedTemperature(coldHeatExchanger);

    public VortexTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.VORTEX_TUBE.get(), pos, state, PressureTier.TIER_TWO, 2000, 0);
        coldHeatExchanger.setThermalResistance(0.01);
        hotHeatExchanger.setThermalResistance(0.01);
        connectingExchanger.setThermalResistance(100);
        connectingExchanger.addConnectedExchanger(coldHeatExchanger);
        connectingExchanger.addConnectedExchanger(hotHeatExchanger);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != getRotation() && side != getRotation().getOpposite();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // hot side heat exchanger is the default, and handled in TileEntityPneumaticBase
        tag.put("coldHeat", coldHeatExchanger.serializeNBT());
        tag.put("connector", connectingExchanger.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // hot side heat exchanger is the default, and handled in TileEntityPneumaticBase
        coldHeatExchanger.deserializeNBT(tag.getCompound("coldHeat"));
        connectingExchanger.deserializeNBT(tag.getCompound("connector"));
    }

    @Override
    public void tickServer() {
        super.tickServer();

        // Only update the cold and connecting side; the hot side is handled in TileEntityPneumaticBase
        connectingExchanger.tick();
        coldHeatExchanger.tick();
        int usedAir = (int) (getPressure() * 10);
        if (usedAir > 0) {
            addAir(-usedAir);
            double generatedHeat = usedAir / 10D;
            coldHeatExchanger.addHeat(-generatedHeat);
            hotHeatExchanger.addHeat(generatedHeat);
        }
        syncHot.tick();
        syncCold.tick();
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public TintColor getColorForTintIndex(int tintIndex) {
        return switch (tintIndex) {
            case 1 -> HeatUtil.getColourForTemperature(syncHot.getSyncedTemp());
            case 2 -> HeatUtil.getColourForTemperature(syncCold.getSyncedTemp());
            default -> HeatUtil.getColourForTemperature(300);
        };
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        if (dir == null || dir == getRotation().getOpposite()) {
            return hotHeatExchanger;
        } else if (dir == getRotation()) {
            return coldHeatExchanger;
        } else {
            return null;
        }
    }

    @Override
    public void initHeatExchangersOnPlacement(Level world, BlockPos pos) {
        double temp = HeatExchangerLogicAmbient.getAmbientTemperature(world, pos);
        hotHeatExchanger.setTemperature(temp);
        coldHeatExchanger.setTemperature(temp);
    }
}
