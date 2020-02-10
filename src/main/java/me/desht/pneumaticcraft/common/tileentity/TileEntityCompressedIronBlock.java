package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class TileEntityCompressedIronBlock extends TileEntityTickableBase implements IComparatorSupport, IHeatTinted {

    protected final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);
    private int comparatorOutput = 0;
    private SyncedTemperature syncedTemperature;

    public TileEntityCompressedIronBlock() {
        this(ModTileEntities.COMPRESSED_IRON_BLOCK.get());
    }

    TileEntityCompressedIronBlock(TileEntityType type) {
        super(type);
        heatExchanger.setThermalResistance(0.01);
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();

        syncedTemperature = new SyncedTemperature(this, null);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            syncedTemperature.setCurrentTemp(heatExchanger.getTemperature());

            int newComparatorOutput = HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
            if (comparatorOutput != newComparatorOutput) {
                comparatorOutput = newComparatorOutput;
                updateNeighbours();
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public int getComparatorValue() {
        return comparatorOutput;
    }

    @Override
    public TintColor getColorForTintIndex(int tintIndex) {
        return HeatUtil.getColourForTemperature(heatExchanger.getTemperatureAsInt());
    }
}
