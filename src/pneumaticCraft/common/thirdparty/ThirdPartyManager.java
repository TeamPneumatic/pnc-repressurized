package pneumaticCraft.common.thirdparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.thirdparty.ae2.AE2;
import pneumaticCraft.common.thirdparty.bloodmagic.BloodMagic;
import pneumaticCraft.common.thirdparty.buildcraft.BuildCraft;
import pneumaticCraft.common.thirdparty.cofh.CoFHCore;
import pneumaticCraft.common.thirdparty.computercraft.ComputerCraft;
import pneumaticCraft.common.thirdparty.computercraft.OpenComputers;
import pneumaticCraft.common.thirdparty.ee3.EE3;
import pneumaticCraft.common.thirdparty.enderio.EnderIO;
import pneumaticCraft.common.thirdparty.fmp.FMPLoader;
import pneumaticCraft.common.thirdparty.forestry.Forestry;
import pneumaticCraft.common.thirdparty.hydraulicraft.Hydraulicraft;
import pneumaticCraft.common.thirdparty.ic2.IC2;
import pneumaticCraft.common.thirdparty.igwmod.IGWMod;
import pneumaticCraft.common.thirdparty.mfr.MFR;
import pneumaticCraft.common.thirdparty.openblocks.OpenBlocks;
import pneumaticCraft.common.thirdparty.thaumcraft.Thaumcraft;
import pneumaticCraft.common.thirdparty.waila.Waila;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.IGuiHandler;

public class ThirdPartyManager implements IGuiHandler{

    private static ThirdPartyManager INSTANCE = new ThirdPartyManager();
    private final List<IThirdParty> thirdPartyMods = new ArrayList<IThirdParty>();
    public static boolean computerCraftLoaded;

    public static ThirdPartyManager instance(){
        return INSTANCE;
    }

    public void index(){
        Map<String, Class<? extends IThirdParty>> thirdPartyClasses = new HashMap<String, Class<? extends IThirdParty>>();
        try {
            thirdPartyClasses.put(ModIds.INDUSTRIALCRAFT, IC2.class);
            thirdPartyClasses.put(ModIds.BUILDCRAFT, BuildCraft.class);
            thirdPartyClasses.put(ModIds.IGWMOD, IGWMod.class);
            thirdPartyClasses.put(ModIds.COMPUTERCRAFT, ComputerCraft.class);
            if(!Loader.isModLoaded(ModIds.COMPUTERCRAFT)) thirdPartyClasses.put(ModIds.OPEN_COMPUTERS, OpenComputers.class);
            thirdPartyClasses.put(ModIds.FMP, FMPLoader.class);
            thirdPartyClasses.put(ModIds.WAILA, Waila.class);
            thirdPartyClasses.put(ModIds.HC, Hydraulicraft.class);
            thirdPartyClasses.put(ModIds.THAUMCRAFT, Thaumcraft.class);
            thirdPartyClasses.put(ModIds.BLOOD_MAGIC, BloodMagic.class);
            thirdPartyClasses.put(ModIds.AE2, AE2.class);
            thirdPartyClasses.put(ModIds.CHISEL, Chisel.class);
            thirdPartyClasses.put(ModIds.FORESTRY, Forestry.class);
            thirdPartyClasses.put(ModIds.MFR, MFR.class);
            thirdPartyClasses.put(ModIds.OPEN_BLOCKS, OpenBlocks.class);
            thirdPartyClasses.put(ModIds.COFH_CORE, CoFHCore.class);
            thirdPartyClasses.put(ModIds.NOT_ENOUGH_KEYS, NotEnoughKeys.class);
            thirdPartyClasses.put(ModIds.EE3, EE3.class);
            thirdPartyClasses.put(ModIds.EIO, EnderIO.class);
            DramaSplash.newDrama();
        } catch(Throwable e) {
            Log.error("A class loader loaded a class where MineMaarten didn't expect it to do so! Please report, as third party content is broken.");
            e.printStackTrace();
        }

        List<String> enabledThirdParty = new ArrayList<String>();
        Config.config.addCustomCategoryComment("third_party_enabling", "With these options you can disable third party content by mod. Useful if something in the mod changes and causes crashes.");
        for(String modid : thirdPartyClasses.keySet()) {
            if(Config.config.get("Third_Party_Enabling", modid, true).getBoolean()) {
                enabledThirdParty.add(modid);
            }
        }
        Config.config.save();

        for(Map.Entry<String, Class<? extends IThirdParty>> entry : thirdPartyClasses.entrySet()) {
            if(enabledThirdParty.contains(entry.getKey()) && Loader.isModLoaded(entry.getKey())) {
                try {
                    thirdPartyMods.add(entry.getValue().newInstance());
                } catch(Throwable e) {
                    Log.error("Failed to instantiate third party handler!");
                    e.printStackTrace();
                }
            }
        }
    }

    public void onItemRegistry(Item item){
        for(IThirdParty thirdParty : thirdPartyMods) {
            if(thirdParty instanceof IRegistryListener) ((IRegistryListener)thirdParty).onItemRegistry(item);
        }
    }

    public void onBlockRegistry(Block block){
        for(IThirdParty thirdParty : thirdPartyMods) {
            if(thirdParty instanceof IRegistryListener) ((IRegistryListener)thirdParty).onBlockRegistry(block);
        }
    }

    public void preInit(){
        for(IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.preInit();
            } catch(Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the PreInit phase!");
                e.printStackTrace();
            }
        }
    }

    public void init(){
        for(IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.init();
            } catch(Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the Init phase!");
                e.printStackTrace();
            }
        }
    }

    public void postInit(){
        for(IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.postInit();
            } catch(Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the PostInit phase!");
                e.printStackTrace();
            }
        }
    }

    public void clientSide(){
        for(IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientSide();
            } catch(Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " client side!");
                e.printStackTrace();
            }
        }
    }

    public void clientInit(){
        for(IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientInit();
            } catch(Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " client side on the init!");
                e.printStackTrace();
            }
        }
    }

    @Optional.Method(modid = ModIds.FMP)
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
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        for(IThirdParty thirdParty : thirdPartyMods) {
            if(thirdParty instanceof IGuiHandler) {
                Object obj = ((IGuiHandler)thirdParty).getServerGuiElement(ID, player, world, x, y, z);
                if(obj != null) return obj;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        for(IThirdParty thirdParty : thirdPartyMods) {
            if(thirdParty instanceof IGuiHandler) {
                Object obj = ((IGuiHandler)thirdParty).getClientGuiElement(ID, player, world, x, y, z);
                if(obj != null) return obj;
            }
        }
        return null;
    }

}
