package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.common.thirdparty.IHeatDisperser;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import net.minecraft.tileentity.TileEntity;

public class Mekanism implements IThirdParty, IHeatDisperser {
    // TODO 1.14
//    @CapabilityInject(IHeatTransfer.class)
//    public static Capability<IHeatTransfer> CAPABILITY_HEAT_TRANSFER = null;
//
//    @CapabilityInject(IGridTransmitter.class)
//    public static Capability<IGridTransmitter> CAPABILITY_GRID_TRANSMITTER = null;

    private static MekanismHeatAdapter adapter = null;

    public static boolean available = false;

    @Override
    public void init() {
        available = true;

        TileEntityBase.registerHeatDisperser(this);
    }

    @Override
    public void disperseHeat(TileEntity te, TileEntityCache[] tileCache) {
//        IHeatTransfer source = te.getCapability(CAPABILITY_HEAT_TRANSFER, null);
//        if (source != null) {
//            for (Direction side : Direction.VALUES) {
//                // don't push heat to PneumaticCraft TE's even though they provide the Mek IHeatTransfer capability
//                // - heat dispersal to native TE's is handled via IHeatExchangerLogic
//                if (tileCache[side.getIndex()].getTileEntity() instanceof TileEntityBase) continue;
//
//                IHeatTransfer sink = source.getAdjacent(side);
//                if (sink != null && source.getTemp() > sink.getTemp() + 300) {
//                    double invConduction = sink.getInverseConductionCoefficient() + source.getInverseConductionCoefficient();
//                    double heatToTransfer = (source.getTemp() - (sink.getTemp() + 300)) / invConduction;
//                    source.transferHeatTo(-heatToTransfer);
//                    sink.transferHeatTo(heatToTransfer * ConfigHandler.integration.mekHeatEfficiency);
//                }
//            }
//        }
    }

    /**
     * Get a Mekanism->PneumaticCraft heat adapter, to allow Mekanism TE's to disperse heat to us.  This adapter is
     * provided via a capability.  Don't cache this adapter! It's only valid in the method from which it's obtained.
     *
     * @param te the PneumaticCraft tile entity
     * @param side side on which the capability is requested
     * @return a Mekanism IHeatTransfer object
     */
//    public static LazyOptional<IHeatTransfer> getHeatAdapter(TileEntityBase te, Direction side) {
//        if (adapter == null) {
//            adapter = new MekanismHeatAdapter();
//        }
//        if (te instanceof IHeatExchanger) {
//            return adapter.setup(te, side);
//        } else {
//            return null;
//        }
//    }
}
