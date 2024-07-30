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

package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatExtractionTracker;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class HeatBehaviourTransition extends HeatBehaviourLiquid {
    private static final Vector3f VEC3F_101 = new Vector3f(1, 0, 1);
    private double maxExchangedHeat;
    private double blockTemp = -1;  // -1 = not yet init'd (init on first tick)
    private IHeatExchangerLogic logic;
    private HeatExtractionTracker tracker;

    @Override
    public boolean isApplicable() {
        logic = HeatExchangerManager.getInstance().getLogic(getWorld(), getPos(), null).orElse(null);
        return logic != null;
    }

    @Override
    public HeatBehaviour initialize(IHeatExchangerLogic connectedHeatLogic, Level world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);

        tracker = HeatExtractionTracker.getInstance(getWorld());

        return this;
    }

    protected abstract int getMaxExchangedHeat();

    protected boolean transformBlockHot() { return false; }

    protected boolean transformBlockCold() { return false; }

    @Override
    public void tick() {
        if (blockTemp == -1) {
            if (logic != null) {
                blockTemp = logic.getTemperature();
                maxExchangedHeat = getMaxExchangedHeat() * (logic.getThermalResistance() + getHeatExchanger().getThermalResistance());
            }
        }

        double extractedHeat = tracker.getHeatExtracted(getPos());
        if (extractedHeat < Math.abs(maxExchangedHeat)) {
            double toExtract = blockTemp - getHeatExchanger().getTemperature();
            tracker.extractHeat(getPos(), toExtract);
            extractedHeat += toExtract;
        }
        if (extractedHeat >= maxExchangedHeat) {
            if (transformBlockCold()) tracker.extractHeat(getPos(), -maxExchangedHeat);
        } else if (extractedHeat <= -maxExchangedHeat) {
            if (transformBlockHot()) tracker.extractHeat(getPos(), maxExchangedHeat);
        }
    }

    void onTransition(BlockPos pos) {
        getWorld().playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5f, 2.6F + (getWorld().random.nextFloat() - getWorld().random.nextFloat()) * 0.8F);
        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.SMOKE,
                new Vector3f(pos.getX(), pos.getY() + 1, pos.getZ()),
                PneumaticCraftUtils.VEC3F_ZERO,
                8,
                Optional.of(VEC3F_101)
        ), getWorld(), pos);
    }

    public double getExtractionProgress() {
        return maxExchangedHeat == 0 ? 0 : tracker.getHeatExtracted(getPos()) / maxExchangedHeat;
    }
}
