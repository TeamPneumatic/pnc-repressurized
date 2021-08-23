package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityHeatSink extends TileEntityCompressedIronBlock {
    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private double ambientTemp;

    public TileEntityHeatSink() {
        super(ModTileEntities.HEAT_SINK.get());

        heatExchanger.setThermalCapacity(5);
        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(TileEntityConstants.HEAT_SINK_THERMAL_RESISTANCE);
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        ambientTemp = HeatExchangerLogicAmbient.getAmbientTemperature(getLevel(), getBlockPos());
        airExchanger.setTemperature(ambientTemp);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.put("airExchanger", airExchanger.serializeNBT());
        return super.save(tag);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        airExchanger.deserializeNBT(tag.getCompound("airExchanger"));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            airExchanger.tick();
            airExchanger.setTemperature(ambientTemp);
        }
    }

    public void onFannedByAirGrate() {
        // called server-side
        heatExchanger.tick();
        airExchanger.setTemperature(ambientTemp);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                getBlockPos().getX() + 1, getBlockPos().getY() + 1, getBlockPos().getZ() + 1
        );
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return side == null || side == getRotation() ? super.getHeatCap(side) : LazyOptional.empty();
    }
}
