package me.desht.pneumaticcraft.common.thirdparty;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;

/**
 * Maintains a cache of mod id's to "friendly" mod names.
 */
public class ModNameCache {
    private static final Map<String, String> id2name = Maps.newHashMap();

    public static void init() {
        ModList.get().forEachModFile(modFile -> {
            for (IModInfo info : modFile.getModInfos()) {
                id2name.put(info.getModId(), info.getDisplayName());
                id2name.put(info.getModId().toLowerCase(), info.getDisplayName());
            }
        });
        id2name.put("minecraft", "Minecraft");
    }

    public static String getModName(IForgeRegistryEntry<?> entry) {
        return getModName(entry.getRegistryName().getNamespace());
    }

    public static String getModName(String modId) {
        return id2name.getOrDefault(modId, modId);
    }
}
