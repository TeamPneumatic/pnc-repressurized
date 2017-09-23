package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public abstract class HeatBehaviourTransition extends HeatBehaviourLiquid {
    private double extractedHeat;
    private double maxExchangedHeat;
    private double blockTemp = -1;
    private IHeatExchangerLogic logic;

    @Override
    public boolean isApplicable() {
        logic = HeatExchangerManager.getInstance().getLogic(getWorld(), getPos(), null);
        return logic != null;
    }

    protected abstract int getMaxExchangedHeat();

    protected abstract boolean transitionOnTooMuchExtraction();

    @Override
    public void update() {
        if (blockTemp == -1) {
            blockTemp = logic.getTemperature();
            maxExchangedHeat = getMaxExchangedHeat() * (logic.getThermalResistance() + getHeatExchanger().getThermalResistance());
        }
        extractedHeat += blockTemp - getHeatExchanger().getTemperature();
        if (transitionOnTooMuchExtraction() ? extractedHeat > maxExchangedHeat : extractedHeat < -maxExchangedHeat) {
            transformBlock();
            extractedHeat -= maxExchangedHeat;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("extractedHeat", extractedHeat);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        extractedHeat = tag.getDouble("extractedHeat");
    }

    protected abstract void transformBlock();

    protected void onTransition(BlockPos pos) {
        NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.AMBIENT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.5F, 2.6F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.8F, true), getWorld());
        for (int i = 0; i < 8; i++) {
            double randX = pos.getX() + getWorld().rand.nextDouble();
            double randZ = pos.getZ() + getWorld().rand.nextDouble();
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_LARGE, randX, pos.getY() + 1, randZ, 0, 0, 0), getWorld());
        }
    }
}
