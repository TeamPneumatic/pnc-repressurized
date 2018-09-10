package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;

public class SemiBlockInitializer {
    public static void preInit() {
        SemiBlockManager.registerSemiBlock(SemiBlockActiveProvider.ID, SemiBlockActiveProvider.class, ItemLogisticsFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockPassiveProvider.ID, SemiBlockPassiveProvider.class, ItemLogisticsFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockStorage.ID, SemiBlockStorage.class, ItemLogisticsFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockDefaultStorage.ID, SemiBlockDefaultStorage.class, ItemLogisticsFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockRequester.ID, SemiBlockRequester.class, ItemLogisticsFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockHeatFrame.ID, SemiBlockHeatFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockSpawnerAgitator.ID, SemiBlockSpawnerAgitator.class);
        SemiBlockManager.registerSemiBlock(SemiBlockCropSupport.ID, SemiBlockCropSupport.class);
        SemiBlockManager.registerSemiBlock(SemiBlockTransferGadget.ID, SemiBlockTransferGadget.class);
    }
}
