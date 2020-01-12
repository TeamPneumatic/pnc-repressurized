package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileEntityCompressedIronBlock extends TileEntityTickableBase implements IHeatExchanger, IComparatorSupport, IHeatTinted {

    protected final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @DescSynced
    private int heatLevel = 10;
    private int oldComparatorOutput = 0;

    public TileEntityCompressedIronBlock() {
        this(ModTileEntities.COMPRESSED_IRON_BLOCK.get());
    }

    TileEntityCompressedIronBlock(TileEntityType type) {
        super(type);
        heatExchanger.setThermalResistance(0.01);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return heatExchanger;
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            heatLevel = HeatUtil.getHeatLevelForTemperature(heatExchanger.getTemperature());

            int comparatorOutput = HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
            if (oldComparatorOutput != comparatorOutput) {
                oldComparatorOutput = comparatorOutput;
                updateNeighbours();
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public int getComparatorValue() {
        return HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
    }

    @Override
    public int getHeatLevelForTintIndex(int tintIndex) {
        return heatLevel;
    }
}
