package pneumaticCraft.common.semiblock;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;

public class SemiBlockInitializer{
    public static void init(){
        MinecraftForge.EVENT_BUS.register(SemiBlockManager.getServerInstance());
        FMLCommonHandler.instance().bus().register(SemiBlockManager.getServerInstance());
        Class requesterClass = Loader.isModLoaded(ModIds.AE2) ? SemiBlockRequesterAE.class : SemiBlockRequester.class;

        registerSemiBlock(SemiBlockActiveProvider.ID, SemiBlockActiveProvider.class, false);
        registerSemiBlock(SemiBlockPassiveProvider.ID, SemiBlockPassiveProvider.class, false);
        registerSemiBlock(SemiBlockStorage.ID, SemiBlockStorage.class, false);
        registerSemiBlock(SemiBlockDefaultStorage.ID, SemiBlockDefaultStorage.class, false);
        registerSemiBlock(SemiBlockRequester.ID, requesterClass, false);
        registerSemiBlock("heatFrame", SemiBlockHeatFrame.class);

        PneumaticCraft.proxy.registerSemiBlockRenderer((ItemSemiBlockBase)Itemss.logisticsFrameRequester);
        SemiBlockManager.registerSemiBlockToItemMapping(requesterClass, Itemss.logisticsFrameRequester);

        PneumaticCraft.proxy.registerSemiBlockRenderer((ItemSemiBlockBase)Itemss.logisticsFrameDefaultStorage);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockDefaultStorage.class, Itemss.logisticsFrameDefaultStorage);

        PneumaticCraft.proxy.registerSemiBlockRenderer((ItemSemiBlockBase)Itemss.logisticsFrameStorage);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockStorage.class, Itemss.logisticsFrameStorage);

        PneumaticCraft.proxy.registerSemiBlockRenderer((ItemSemiBlockBase)Itemss.logisticsFramePassiveProvider);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockPassiveProvider.class, Itemss.logisticsFramePassiveProvider);

        PneumaticCraft.proxy.registerSemiBlockRenderer((ItemSemiBlockBase)Itemss.logisticsFrameActiveProvider);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockActiveProvider.class, Itemss.logisticsFrameActiveProvider);
    }

    private static Item registerSemiBlock(String key, Class<? extends ISemiBlock> semiBlock){
        return registerSemiBlock(key, semiBlock, true);
    }

    private static Item registerSemiBlock(String key, Class<? extends ISemiBlock> semiBlock, boolean addItem){
        Item item = SemiBlockManager.registerSemiBlock(key, semiBlock, addItem);
        if(item != null) item.setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        return item;
    }
}
