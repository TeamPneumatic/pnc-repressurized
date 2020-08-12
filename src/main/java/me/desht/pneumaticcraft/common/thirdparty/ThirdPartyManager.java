package me.desht.pneumaticcraft.common.thirdparty;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.common.config.subconfig.ThirdPartyConfig;
import me.desht.pneumaticcraft.common.thirdparty.botania.Botania;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.ComputerCraft;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.thirdparty.immersiveengineering.ImmersiveEngineering;
import me.desht.pneumaticcraft.common.thirdparty.mekanism.Mekanism;
import me.desht.pneumaticcraft.common.thirdparty.patchouli.Patchouli;
import me.desht.pneumaticcraft.common.thirdparty.theoneprobe.TheOneProbe;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraftforge.fml.ModList;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ThirdPartyManager {

    private static final ThirdPartyManager INSTANCE = new ThirdPartyManager();

    private static final GenericIntegrationHandler GENERIC = new GenericIntegrationHandler();

    // for those mods which initialise implicitly (generally annotation-based)
    private static final IThirdParty IMPLICIT_INIT = new IThirdParty() {};

    private final List<IThirdParty> thirdPartyMods = new ArrayList<>();
    private IDocsProvider docsProvider = new IDocsProvider.NoDocsProvider();
    private final Set<ModType> loadedModTypes = EnumSet.noneOf(ModType.class);

    public static ThirdPartyManager instance() {
        return INSTANCE;
    }

    public IDocsProvider getDocsProvider() {
        return docsProvider;
    }

    @SuppressWarnings("Convert2MethodRef")
    public void index() {
        Map<String, Supplier<? extends IThirdParty>> thirdPartyClasses = new HashMap<>();
        try {
            // Not using method refs here, because that can cause early class loading and we don't want that
            thirdPartyClasses.put(ModIds.COMPUTERCRAFT, () -> new ComputerCraft());
            thirdPartyClasses.put(ModIds.WAILA, () -> IMPLICIT_INIT);
            thirdPartyClasses.put(ModIds.TOP, () -> new TheOneProbe());
            thirdPartyClasses.put(ModIds.CURIOS, () -> new Curios());
            thirdPartyClasses.put(ModIds.BOTANIA, () -> new Botania());
            thirdPartyClasses.put(ModIds.PATCHOULI, () -> new Patchouli());
            thirdPartyClasses.put(ModIds.JEI, () -> IMPLICIT_INIT);
            thirdPartyClasses.put(ModIds.IMMERSIVE_ENGINEERING, () -> new ImmersiveEngineering());
            thirdPartyClasses.put(ModIds.MEKANISM, () -> new Mekanism());

            // these were supported 1.12.2 and may or may not come back...

//            thirdPartyClasses.put(ModIds.BUILDCRAFT, BuildCraft.class);
//            thirdPartyClasses.put(ModIds.IGWMOD, IGWMod.class);
//            if (!ModList.get().isLoaded(ModIds.COMPUTERCRAFT)) {
//                thirdPartyClasses.put(ModIds.OPEN_COMPUTERS, OpenComputers.class);
//            }
//            thirdPartyClasses.put(ModIds.AE2, AE2.class);
//            thirdPartyClasses.put(ModIds.FORESTRY, Forestry.class);
//            thirdPartyClasses.put(ModIds.EIO, EnderIO.class);
//            thirdPartyClasses.put(ModIds.COFH_CORE, CoFHCore.class);
//            thirdPartyClasses.put(ModIds.CRAFTTWEAKER, CraftTweaker.class);
//            thirdPartyClasses.put(ModIds.INDUSTRIALCRAFT, IC2.class);
//            thirdPartyClasses.put(ModIds.THAUMCRAFT, Thaumcraft.class);
//            thirdPartyClasses.put(ModIds.IMMERSIVE_PETROLEUM, ImmersivePetroleum.class);
//            thirdPartyClasses.put(ModIds.BAUBLES, Baubles.class);
//            thirdPartyClasses.put(ModIds.TOUGH_AS_NAILS, ToughAsNails.class);
        } catch (Throwable e) {
            Log.error("A class loader loaded a class where we didn't expect it to do so! Please report, as third party content is broken.");
            e.printStackTrace();
        }

        ThirdPartyConfig.setupDefaults(thirdPartyClasses.keySet());

        Set<String> enabledThirdParty = thirdPartyClasses.keySet().stream().filter(ThirdPartyConfig::isEnabled).collect(Collectors.toSet());

        List<String> modNames = new ArrayList<>();
        for (Map.Entry<String, Supplier<? extends IThirdParty>> entry : thirdPartyClasses.entrySet()) {
            if (enabledThirdParty.contains(entry.getKey()) && ModList.get().isLoaded(entry.getKey())) {
                IThirdParty mod = entry.getValue().get();
                thirdPartyMods.add(mod);
                if (mod.modType() != null) loadedModTypes.add(mod.modType());
                modNames.add(entry.getKey());
            }
        }

        Log.info("Thirdparty integration activated for [" + Strings.join(modNames, ", ") + "]");
    }

    public void init() {
        GENERIC.init();
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
        GENERIC.postInit();
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.postInit();
            } catch (Throwable e) {
                Log.error("PneumaticCraft wasn't able to load third party content from the third party class " + thirdParty.getClass() + " in the PostInit phase!");
                e.printStackTrace();
            }
        }

    }

    public void clientInit() {
        GENERIC.clientInit();
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

    public boolean isModTypeLoaded(ModType modType) {
        return loadedModTypes.contains(modType);
    }

    /**
     * Collection of mod types; categories of mod which can be used to control whether blocks/items etc. should be
     * made visible to players (blocks/items are *always* registered, but might need to be hidden if their functionality
     * doesn't make sense without certain other mods present - e.g. the Drone Interface needs a computer mod loaded)
     */
    public enum ModType {
        COMPUTER,
        DOCUMENTATION
    }
}
