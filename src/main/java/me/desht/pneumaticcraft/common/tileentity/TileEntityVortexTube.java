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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class TileEntityVortexTube extends TileEntityPneumaticBase implements IHeatTinted, IHeatExchangingTE {
    // hot side heat exchanger is also the default
    private final IHeatExchangerLogic hotHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> hotHeatCap = LazyOptional.of(() -> hotHeatExchanger);
    private final IHeatExchangerLogic coldHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> coldHeatCap = LazyOptional.of(() -> coldHeatExchanger);
    private final IHeatExchangerLogic connectingExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    @DescSynced
    private final SyncedTemperature syncHot = new SyncedTemperature(hotHeatExchanger);
    @DescSynced
    private final SyncedTemperature syncCold = new SyncedTemperature(coldHeatExchanger);

    public TileEntityVortexTube() {
        super(ModTileEntities.VORTEX_TUBE.get(), 20, 25, 2000, 0);
        coldHeatExchanger.setThermalResistance(0.01);
        hotHeatExchanger.setThermalResistance(0.01);
        connectingExchanger.setThermalResistance(100);
        connectingExchanger.addConnectedExchanger(coldHeatExchanger);
        connectingExchanger.addConnectedExchanger(hotHeatExchanger);
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        if (side == null || side == getRotation().getOpposite()) {
            return hotHeatCap;
        } else if (side == getRotation()) {
            return coldHeatCap;
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != getRotation() && side != getRotation().getOpposite();
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        // hot side heat exchanger is the default, and handled in TileEntityPneumaticBase
        tag.put("coldHeat", coldHeatExchanger.serializeNBT());
        tag.put("connector", connectingExchanger.serializeNBT());
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        // hot side heat exchanger is the default, and handled in TileEntityPneumaticBase
        coldHeatExchanger.deserializeNBT(tag.getCompound("coldHeat"));
        connectingExchanger.deserializeNBT(tag.getCompound("connector"));
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide) {
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
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public TintColor getColorForTintIndex(int tintIndex) {
        switch (tintIndex) {
            case 1: return HeatUtil.getColourForTemperature(syncHot.getSyncedTemp());
            case 2: return HeatUtil.getColourForTemperature(syncCold.getSyncedTemp());
            default: return HeatUtil.getColourForTemperature(300);
        }
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
    public void initHeatExchangersOnPlacement(World world, BlockPos pos) {
        double temp = HeatExchangerLogicAmbient.getAmbientTemperature(world, pos);
        hotHeatExchanger.setTemperature(temp);
        coldHeatExchanger.setTemperature(temp);
    }
}
