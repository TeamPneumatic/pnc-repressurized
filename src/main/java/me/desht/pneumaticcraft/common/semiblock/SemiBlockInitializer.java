package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SemiBlockInitializer {

    public static void preInit() {
        SemiBlockManager.registerSemiBlock(SemiBlockActiveProvider.ID, SemiBlockActiveProvider.class);
        SemiBlockManager.registerSemiBlock(SemiBlockPassiveProvider.ID, SemiBlockPassiveProvider.class);
        SemiBlockManager.registerSemiBlock(SemiBlockStorage.ID, SemiBlockStorage.class);
        SemiBlockManager.registerSemiBlock(SemiBlockDefaultStorage.ID, SemiBlockDefaultStorage.class);
        SemiBlockManager.registerSemiBlock(SemiBlockRequester.ID, SemiBlockRequester.class);
        SemiBlockManager.registerSemiBlock(SemiBlockHeatFrame.ID, SemiBlockHeatFrame.class);
        SemiBlockManager.registerSemiBlock(SemiBlockSpawnerAgitator.ID, SemiBlockSpawnerAgitator.class);
    }

    public static void init() {
        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.LOGISTICS_FRAME_REQUESTER);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockRequester.class, Itemss.LOGISTICS_FRAME_REQUESTER);

        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.LOGISTICS_FRAME_DEFAULT_STORAGE);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockDefaultStorage.class, Itemss.LOGISTICS_FRAME_DEFAULT_STORAGE);

        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.LOGISTICS_FRAME_STORAGE);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockStorage.class, Itemss.LOGISTICS_FRAME_STORAGE);

        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.LOGISTICS_FRAME_PASSIVE_PROVIDER);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockPassiveProvider.class, Itemss.LOGISTICS_FRAME_PASSIVE_PROVIDER);

        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.LOGISTICS_FRAME_ACTIVE_PROVIDER);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockActiveProvider.class, Itemss.LOGISTICS_FRAME_ACTIVE_PROVIDER);

        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.HEAT_FRAME);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockHeatFrame.class, Itemss.HEAT_FRAME);
        
        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.SPAWNER_AGITATOR);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockSpawnerAgitator.class, Itemss.SPAWNER_AGITATOR);
    }
}
