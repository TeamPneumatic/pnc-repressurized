package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.config.ThirdPartyConfig;
import me.desht.pneumaticcraft.common.thirdparty.ae2.AE2;
import me.desht.pneumaticcraft.common.thirdparty.buildcraft.BuildCraft;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.ComputerCraft;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.OpenComputers;
import me.desht.pneumaticcraft.common.thirdparty.enderio.EnderIO;
import me.desht.pneumaticcraft.common.thirdparty.forestry.Forestry;
import me.desht.pneumaticcraft.common.thirdparty.igwmod.IGWMod;
import me.desht.pneumaticcraft.common.thirdparty.mcmultipart.PneumaticMultiPart;
import me.desht.pneumaticcraft.common.thirdparty.mfr.MFR;
import me.desht.pneumaticcraft.common.thirdparty.openblocks.OpenBlocks;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirdPartyManager implements IGuiHandler {

    private static ThirdPartyManager INSTANCE = new ThirdPartyManager();
    private final List<IThirdParty> thirdPartyMods = new ArrayList<>();
    public static boolean computerCraftLoaded;

    public static ThirdPartyManager instance() {
        return INSTANCE;
    }

    public void index() {
        Map<String, Class<? extends IThirdParty>> thirdPartyClasses = new HashMap<>();
        try {
            // thirdPartyClasses.put(ModIds.INDUSTRIALCRAFT, IC2.class);
            thirdPartyClasses.put(ModIds.BUILDCRAFT, BuildCraft.class);
            thirdPartyClasses.put(ModIds.IGWMOD, IGWMod.class);
            thirdPartyClasses.put(ModIds.COMPUTERCRAFT, ComputerCraft.class);
            if (!Loader.isModLoaded(ModIds.COMPUTERCRAFT))
                thirdPartyClasses.put(ModIds.OPEN_COMPUTERS, OpenComputers.class);
            // thirdPartyClasses.put(ModIds.FMP, FMPLoader.class);
            //thirdPartyClasses.put(ModIds.WAILA, Waila.class);
            // thirdPartyClasses.put(ModIds.THAUMCRAFT, Thaumcraft.class);
            thirdPartyClasses.put(ModIds.AE2, AE2.class);
            // thirdPartyClasses.put(ModIds.CHISEL, Chisel.class);
            thirdPartyClasses.put(ModIds.FORESTRY, Forestry.class);
            thirdPartyClasses.put(ModIds.MFR, MFR.class);
            thirdPartyClasses.put(ModIds.OPEN_BLOCKS, OpenBlocks.class);
//              thirdPartyClasses.put(ModIds.COFH_CORE, CoFHCore.class);
            thirdPartyClasses.put(ModIds.NOT_ENOUGH_KEYS, NotEnoughKeys.class);
            //  thirdPartyClasses.put(ModIds.EE3, EE3.class);
            thirdPartyClasses.put(ModIds.EIO, EnderIO.class);
            thirdPartyClasses.put(ModIds.MCMP, PneumaticMultiPart.class);
//            DramaSplash.newDrama();
        } catch (Throwable e) {
            Log.error("A class loader loaded a class where we didn't expect it to do so! Please report, as third party content is broken.");
            e.printStackTrace();
        }

        List<String> enabledThirdParty = new ArrayList<>();
        for (String modid : thirdPartyClasses.keySet()) {
            if (ThirdPartyConfig.isEnabled(modid)) {
                enabledThirdParty.add(modid);
            }
        }

        for (Map.Entry<String, Class<? extends IThirdParty>> entry : thirdPartyClasses.entrySet()) {
            if (enabledThirdParty.contains(entry.getKey()) && Loader.isModLoaded(entry.getKey())) {
                try {
                    thirdPartyMods.add(entry.getValue().newInstance());
                } catch (Throwable e) {
                    Log.error("Failed to instantiate third party handler!");
                    e.printStackTrace();
                }
            }
        }
    }

    public void onItemRegistry(Item item) {
        for (IThirdParty thirdParty : thirdPartyMods) {
            if (thirdParty instanceof IRegistryListener) ((IRegistryListener) thirdParty).onItemRegistry(item);
        }
    }

    public void onBlockRegistry(Block block) {
        for (IThirdParty thirdParty : thirdPartyMods) {
            if (thirdParty instanceof IRegistryListener) ((IRegistryListener) thirdParty).onBlockRegistry(block);
        }
    }

    public void preInit() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.preInit();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the PreInit phase!");
                e.printStackTrace();
            }
        }
    }

    public void init() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.init();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the Init phase!");
                e.printStackTrace();
            }
        }
    }

    public void postInit() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.postInit();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the PostInit phase!");
                e.printStackTrace();
            }
        }
    }

    public void clientSide() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientSide();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " client side!");
                e.printStackTrace();
            }
        }
    }

    public void clientInit() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientInit();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " client side on the init!");
                e.printStackTrace();
            }
        }
    }

    /*TODO FMP dep @Optional.Method(modid = ModIds.FMP)
    public TMultiPart getPart(String partName){
        for(IThirdParty thirdParty : thirdPartyMods) {
            if(thirdParty instanceof FMPLoader) {
                return ((FMPLoader)thirdParty).fmp.createPart(partName, false);
            }
        }
        return null;
    }

    @Optional.Method(modid = ModIds.FMP)
    public void registerPart(String partName, Class<? extends TMultiPart> multipart){
        for(IThirdParty thirdParty : thirdPartyMods) {
            if(thirdParty instanceof FMPLoader) {
                ((FMPLoader)thirdParty).fmp.registerPart(partName, multipart);
                return;
            }
        }
        throw new IllegalStateException("No FMP found!");
    }*/

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        for (IThirdParty thirdParty : thirdPartyMods) {
            if (thirdParty instanceof IGuiHandler) {
                Object obj = ((IGuiHandler) thirdParty).getServerGuiElement(ID, player, world, x, y, z);
                if (obj != null) return obj;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        for (IThirdParty thirdParty : thirdPartyMods) {
            if (thirdParty instanceof IGuiHandler) {
                Object obj = ((IGuiHandler) thirdParty).getClientGuiElement(ID, player, world, x, y, z);
                if (obj != null) return obj;
            }
        }
        return null;
    }

}
