package me.desht.pneumaticcraft.common.thirdparty;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.common.config.aux.ThirdPartyConfig;
import me.desht.pneumaticcraft.common.thirdparty.botania.Botania;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.thirdparty.patchouli.Patchouli;
import me.desht.pneumaticcraft.common.thirdparty.theoneprobe.TheOneProbe;
import me.desht.pneumaticcraft.common.thirdparty.waila.Waila;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.ModList;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ThirdPartyManager {

    private static final ThirdPartyManager INSTANCE = new ThirdPartyManager();
    private final List<IThirdParty> thirdPartyMods = new ArrayList<>();
    public static boolean computerCraftLoaded;
    private IDocsProvider docsProvider = new IDocsProvider.NoDocsProvider();
    private GenericIntegrationHandler generic = new GenericIntegrationHandler();

    public static ThirdPartyManager instance() {
        return INSTANCE;
    }

    public IDocsProvider getDocsProvider() {
        return docsProvider;
    }

    public void index() {
        Map<String, Supplier<? extends IThirdParty>> thirdPartyClasses = new HashMap<>();
        try {
//            thirdPartyClasses.put(ModIds.BUILDCRAFT, BuildCraft.class);
//            thirdPartyClasses.put(ModIds.IGWMOD, IGWMod.class);
//            thirdPartyClasses.put(ModIds.COMPUTERCRAFT, ComputerCraft.class);
//            if (!ModList.get().isLoaded(ModIds.COMPUTERCRAFT)) {
//                thirdPartyClasses.put(ModIds.OPEN_COMPUTERS, OpenComputers.class);
//            }
//            thirdPartyClasses.put(ModIds.AE2, AE2.class);
//            thirdPartyClasses.put(ModIds.FORESTRY, Forestry.class);
//            thirdPartyClasses.put(ModIds.EIO, EnderIO.class);
//            thirdPartyClasses.put(ModIds.COFH_CORE, CoFHCore.class);
            thirdPartyClasses.put(ModIds.WAILA, Waila::new);
            thirdPartyClasses.put(ModIds.TOP, TheOneProbe::new);
            thirdPartyClasses.put(ModIds.CURIOS, Curios::new);
//            thirdPartyClasses.put(ModIds.CRAFTTWEAKER, CraftTweaker.class);
//            thirdPartyClasses.put(ModIds.INDUSTRIALCRAFT, IC2.class);
//            thirdPartyClasses.put(ModIds.IMMERSIVEENGINEERING, ImmersiveEngineering.class);
//            thirdPartyClasses.put(ModIds.THAUMCRAFT, Thaumcraft.class);
            thirdPartyClasses.put(ModIds.BOTANIA, Botania::new);
//            thirdPartyClasses.put(ModIds.IMMERSIVE_PETROLEUM, ImmersivePetroleum.class);
            thirdPartyClasses.put(ModIds.PATCHOULI, Patchouli::new);
//            thirdPartyClasses.put(ModIds.MEKANISM, Mekanism.class);
//            thirdPartyClasses.put(ModIds.BAUBLES, Baubles.class);
//            thirdPartyClasses.put(ModIds.TOUGH_AS_NAILS, ToughAsNails.class);
        } catch (Throwable e) {
            Log.error("A class loader loaded a class where we didn't expect it to do so! Please report, as third party content is broken.");
            e.printStackTrace();
        }

        ThirdPartyConfig.setupDefaults(thirdPartyClasses.keySet());

        Set<String> enabledThirdParty = thirdPartyClasses.keySet().stream().filter(ThirdPartyConfig::isEnabled).collect(Collectors.toSet());

        Log.info("Thirdparty integration activated for [" + Strings.join(enabledThirdParty, ", ") + "]");

        for (Map.Entry<String, Supplier<? extends IThirdParty>> entry : thirdPartyClasses.entrySet()) {
            if (enabledThirdParty.contains(entry.getKey()) && ModList.get().isLoaded(entry.getKey())) {
                thirdPartyMods.add(entry.getValue().get());
//                try {
//                } catch (Throwable e) {
//                    Log.error("Failed to instantiate third party handler!");
//                    e.printStackTrace();
//                }
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
        generic.preInit();
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
        generic.init();
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
        generic.postInit();
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.postInit();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the PostInit phase!");
                e.printStackTrace();
            }
        }

    }

    public void clientPreInit() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientPreInit();
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
                if (thirdParty instanceof IDocsProvider) {
                    // TODO: priority system or selectable in config?  right now, last docs provider found wins
                    docsProvider = (IDocsProvider) thirdParty;
                }
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " client side on the init!");
                e.printStackTrace();
            }
        }
    }
}
