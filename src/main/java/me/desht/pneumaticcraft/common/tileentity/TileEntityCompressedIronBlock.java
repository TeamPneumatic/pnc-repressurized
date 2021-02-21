package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class TileEntityCompressedIronBlock extends TileEntityTickableBase implements IComparatorSupport, IHeatTinted, IHeatExchangingTE {

    protected final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);
    private int comparatorOutput = 0;
    @DescSynced
    private final SyncedTemperature syncedTemperature = new SyncedTemperature(heatExchanger);

    public TileEntityCompressedIronBlock() {
        this(ModTileEntities.COMPRESSED_IRON_BLOCK.get());
    }

    TileEntityCompressedIronBlock(TileEntityType type) {
        super(type);

        heatExchanger.setThermalCapacity(10);
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            syncedTemperature.tick();

            int newComparatorOutput = HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
            if (comparatorOutput != newComparatorOutput) {
                comparatorOutput = newComparatorOutput;
                world.updateComparatorOutputLevel(getPos(), getBlockState().getBlock());
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
        return HeatUtil.getColourForTemperature(syncedTemperature.getSyncedTemp());
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
