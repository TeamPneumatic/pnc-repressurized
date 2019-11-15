package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.item.*;

public class SemiBlockInitializer {
    public static void preInit() {
        SemiBlockManager.registerSemiBlock(SemiBlockActiveProvider.ID, SemiBlockActiveProvider::new, ItemLogisticsFrameActiveProvider::new);
        SemiBlockManager.registerSemiBlock(SemiBlockPassiveProvider.ID, SemiBlockPassiveProvider::new, ItemLogisticsFramePassiveProvider::new);
        SemiBlockManager.registerSemiBlock(SemiBlockStorage.ID, SemiBlockStorage::new, ItemLogisticsFrameStorage::new);
        SemiBlockManager.registerSemiBlock(SemiBlockDefaultStorage.ID, SemiBlockDefaultStorage::new, ItemLogisticsFrameDefaultStorage::new);
        SemiBlockManager.registerSemiBlock(SemiBlockRequester.ID, SemiBlockRequester::new, ItemLogisticsFrameRequester::new);
        SemiBlockManager.registerSemiBlock(SemiBlockHeatFrame.ID, SemiBlockHeatFrame::new);
        SemiBlockManager.registerSemiBlock(SemiBlockSpawnerAgitator.ID, SemiBlockSpawnerAgitator::new);
        SemiBlockManager.registerSemiBlock(SemiBlockCropSupport.ID, SemiBlockCropSupport::new);
        SemiBlockManager.registerSemiBlock(SemiBlockTransferGadget.ID, SemiBlockTransferGadget::new);
    }
}
