package me.desht.pneumaticcraft.common.thirdparty;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.Map;

/**
 * Maintains a cache of mod id's to "friendly" mod names.
 */
public class ModNameCache {
    private static final Map<String, String> id2name = Maps.newHashMap();

    public static void init() {
        for (ModContainer mod : Loader.instance().getModList()) {
            id2name.put(mod.getModId(), mod.getName());
            id2name.put(mod.getModId().toLowerCase(), mod.getName());
        }
        id2name.put("minecraft", "Minecraft");
    }

    public static String getModName(ItemStack stack) {
        return getModName(stack.getItem().getRegistryName().getNamespace());
    }

    public static String getModName(String modId) {
        return id2name.getOrDefault(modId, modId);
    }
}
