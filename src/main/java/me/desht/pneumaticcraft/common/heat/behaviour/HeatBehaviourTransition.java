package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatExtractionTracker;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public abstract class HeatBehaviourTransition extends HeatBehaviourLiquid {
    private double maxExchangedHeat;
    private double blockTemp = -1;  // -1 = not yet init'd (init on first tick)
    private LazyOptional<IHeatExchangerLogic> logic;
    private HeatExtractionTracker tracker;

    @Override
    public boolean isApplicable() {
        logic = HeatExchangerManager.getInstance().getLogic(getWorld(), getPos(), null);
        return logic.isPresent();
    }

    @Override
    public HeatBehaviour initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
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
            logic.ifPresent(exchanger -> {
                blockTemp = exchanger.getTemperature();
                maxExchangedHeat = getMaxExchangedHeat() * (exchanger.getThermalResistance() + getHeatExchanger().getThermalResistance());
            });
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
        NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.AMBIENT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.5F, 2.6F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.8F, true), getWorld());
        NetworkHandler.sendToAllAround(new PacketSpawnParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 1, pos.getZ(),
                        0, 0, 0, 8, 1, 0, 1), getWorld());
    }
}
