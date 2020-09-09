package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

class IEHeatHandler {
    static void registerHeatHandler() {
//        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter<TileEntity>() {
//            @Override
//            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
//                return tileEntity.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).map(handler -> {
//                    if (energyAvailable >= Integration.ieExternalHeaterRFperTick) {
//                        handler.addHeat(Integration.ieExternalHeaterRFperTick * Integration.ieExternalHeaterHeatPerRF);
//                        return Integration.ieExternalHeaterRFperTick;
//                    }
//                    return 0;
//                }).orElse(0);
//            }
//        });
    }
}
