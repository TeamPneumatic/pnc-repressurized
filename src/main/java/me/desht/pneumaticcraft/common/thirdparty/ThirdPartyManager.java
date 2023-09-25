/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.config.subconfig.ThirdPartyConfig;
import me.desht.pneumaticcraft.common.thirdparty.botania.Botania;
import me.desht.pneumaticcraft.common.thirdparty.cofhcore.CoFHCore;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.ComputerCraft;
import me.desht.pneumaticcraft.common.thirdparty.create.Create;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.thirdparty.gamestages.Gamestages;
import me.desht.pneumaticcraft.common.thirdparty.immersiveengineering.ImmersiveEngineering;
import me.desht.pneumaticcraft.common.thirdparty.mekanism.Mekanism;
import me.desht.pneumaticcraft.common.thirdparty.patchouli.Patchouli;
import me.desht.pneumaticcraft.common.thirdparty.theoneprobe.TheOneProbe;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraftforge.fml.ModList;

import java.util.*;
import java.util.function.Supplier;

public enum ThirdPartyManager {
    INSTANCE;

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

    private void discoverMods() {
        Map<String, Supplier<? extends IThirdParty>> thirdPartyClasses = new HashMap<>();
        try {
            thirdPartyClasses.put(ModIds.COMPUTERCRAFT, ComputerCraft::new);
            thirdPartyClasses.put(ModIds.WAILA, () -> IMPLICIT_INIT);
            thirdPartyClasses.put(ModIds.TOP, TheOneProbe::new);
            thirdPartyClasses.put(ModIds.CURIOS, Curios::new);
            thirdPartyClasses.put(ModIds.BOTANIA, Botania::new);
            thirdPartyClasses.put(ModIds.PATCHOULI, Patchouli::new);
            thirdPartyClasses.put(ModIds.JEI, () -> IMPLICIT_INIT);
            thirdPartyClasses.put(ModIds.IMMERSIVE_ENGINEERING, ImmersiveEngineering::new);
            thirdPartyClasses.put(ModIds.MEKANISM, Mekanism::new);
            thirdPartyClasses.put(ModIds.AE2, () -> IMPLICIT_INIT);
            thirdPartyClasses.put(ModIds.COFH_CORE, CoFHCore::new);
            thirdPartyClasses.put(ModIds.CRAFTTWEAKER, () -> IMPLICIT_INIT);
            thirdPartyClasses.put(ModIds.GAMESTAGES, Gamestages::new);
            thirdPartyClasses.put(ModIds.CREATE, Create::new);

            // these were supported in 1.12.2 and may or may not come back...

//            thirdPartyClasses.put(ModIds.BUILDCRAFT, BuildCraft.class);
//            thirdPartyClasses.put(ModIds.IGWMOD, IGWMod.class);
//            if (!ModList.get().isLoaded(ModIds.COMPUTERCRAFT)) {
//                thirdPartyClasses.put(ModIds.OPEN_COMPUTERS, OpenComputers.class);
//            }
//            thirdPartyClasses.put(ModIds.FORESTRY, Forestry.class);
//            thirdPartyClasses.put(ModIds.EIO, EnderIO.class);
//            thirdPartyClasses.put(ModIds.COFH_CORE, CoFHCore.class);
//            thirdPartyClasses.put(ModIds.INDUSTRIALCRAFT, IC2.class);
//            thirdPartyClasses.put(ModIds.THAUMCRAFT, Thaumcraft.class);
//            thirdPartyClasses.put(ModIds.BAUBLES, Baubles.class);
//            thirdPartyClasses.put(ModIds.TOUGH_AS_NAILS, ToughAsNails.class);
        } catch (Throwable e) {
            Log.error("A class loader loaded a class where we didn't expect it to do so! Please report, as third party content is broken.");
            e.printStackTrace();
        }

        ThirdPartyConfig.setupDefaults(thirdPartyClasses.keySet());

        List<String> modNames = new ArrayList<>();
        thirdPartyMods.add(GENERIC);
        for (Map.Entry<String, Supplier<? extends IThirdParty>> entry : thirdPartyClasses.entrySet()) {
            if (ThirdPartyConfig.isEnabled(entry.getKey()) && ModList.get().isLoaded(entry.getKey())) {
                IThirdParty mod = entry.getValue().get();
                thirdPartyMods.add(mod);
                if (mod.modType() != null) loadedModTypes.add(mod.modType());
                modNames.add(entry.getKey());
            }
        }

        Log.info("Thirdparty integration activated for [" + String.join(",", modNames) + "]");
    }

    public void preInit() {
        discoverMods();

        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.preInit();
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "PreInit");
            }
        }
    }

    public void init() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.init();
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "Init");
            }
        }
    }

    public void postInit() {
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.postInit();
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "PostInit");
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
                logError(e, thirdParty.getClass(), "Client Init");
            }
        }
    }

    private void logError(Throwable e, Class<?> cls, String when) {
        Log.error("Third party integration error: class: %s, phase: %s", cls.getName(), when);
        e.printStackTrace();
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
