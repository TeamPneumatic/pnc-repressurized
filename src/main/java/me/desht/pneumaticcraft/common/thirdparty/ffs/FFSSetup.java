package me.desht.pneumaticcraft.common.thirdparty.ffs;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class FFSSetup {
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        Item ftbFilterItem = FTBFilterSystemAPI.api().filterItem();
        if (ftbFilterItem != Items.AIR) {
            event.registerItem(PNCCapabilities.ITEM_FILTERING, (stack, ctx) ->
                    (filterStack, item) -> FTBFilterSystemAPI.api().doesFilterMatch(filterStack, item, registryAccess()),
                    ftbFilterItem);
        }
    }

    private static HolderLookup.Provider registryAccess() {
        // ugly kludge, but we have no other context to work with...
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (FMLLoader.getDist() == Dist.DEDICATED_SERVER && server != null) {
            return server.registryAccess();
        } else if (FMLLoader.getDist() == Dist.CLIENT) {
            // could be logical client or logical integrated server...
            String thread = Thread.currentThread().getName();
            if (thread.equals("Render thread")) {
                return ClientUtils.getOptionalClientLevel()
                        .map(Level::registryAccess).orElse(RegistryAccess.EMPTY);
            } else if (server != null && thread.equals("Server thread")) {
                return server.registryAccess();
            }
        }
        return RegistryAccess.EMPTY;
    }
}
