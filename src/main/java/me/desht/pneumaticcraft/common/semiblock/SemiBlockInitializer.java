package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SemiBlockInitializer {
    private static Class<? extends SemiBlockRequester> requesterClass;

    public static void preInit() {
        requesterClass = Loader.isModLoaded(ModIds.AE2) ? SemiBlockRequesterAE.class : SemiBlockRequester.class;

        SemiBlockManager.registerSemiBlock(SemiBlockActiveProvider.ID, SemiBlockActiveProvider.class);
        SemiBlockManager.registerSemiBlock(SemiBlockPassiveProvider.ID, SemiBlockPassiveProvider.class);
        SemiBlockManager.registerSemiBlock(SemiBlockStorage.ID, SemiBlockStorage.class);
        SemiBlockManager.registerSemiBlock(SemiBlockDefaultStorage.ID, SemiBlockDefaultStorage.class);
        SemiBlockManager.registerSemiBlock(SemiBlockRequester.ID, requesterClass);
        SemiBlockManager.registerSemiBlock(SemiBlockHeatFrame.ID, SemiBlockHeatFrame.class);
    }

    public static void init() {
        PneumaticCraftRepressurized.proxy.registerSemiBlockRenderer(Itemss.LOGISTICS_FRAME_REQUESTER);
        SemiBlockManager.registerSemiBlockToItemMapping(requesterClass, Itemss.LOGISTICS_FRAME_REQUESTER);

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
    }
}
