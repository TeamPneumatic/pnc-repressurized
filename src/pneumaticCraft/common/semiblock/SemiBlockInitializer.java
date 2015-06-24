package pneumaticCraft.common.semiblock;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.common.FMLCommonHandler;

public class SemiBlockInitializer{
    public static void init(){
        MinecraftForge.EVENT_BUS.register(SemiBlockManager.getInstance());
        FMLCommonHandler.instance().bus().register(SemiBlockManager.getInstance());

        registerSemiBlock(SemiBlockActiveProvider.ID, SemiBlockActiveProvider.class, false);
        registerSemiBlock(SemiBlockPassiveProvider.ID, SemiBlockPassiveProvider.class, false);
        registerSemiBlock(SemiBlockStorage.ID, SemiBlockStorage.class, false);
        registerSemiBlock(SemiBlockRequester.ID, SemiBlockRequester.class, false);

        PneumaticCraft.proxy.registerSemiBlockRenderer((ItemSemiBlockBase)Itemss.logisticsFrameRequester);
        SemiBlockManager.registerSemiBlockToItemMapping(SemiBlockRequester.class, Itemss.logisticsFrameRequester);

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
