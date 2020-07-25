package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.Set;

public class TrackerBlacklistManager {
    private static final Set<ResourceLocation> ENERGY_BLACKLIST = Sets.newHashSet();
    private static final Set<ResourceLocation> INVENTORY_BLACKLIST = Sets.newHashSet();
    private static final Set<ResourceLocation> FLUID_BLACKLIST = Sets.newHashSet();

    public static void addEnergyTEToBlacklist(TileEntity te, Throwable e) {
        addEntry(ENERGY_BLACKLIST, te, e);
    }

    public static void addInventoryTEToBlacklist(TileEntity te, Throwable e) {
        addEntry(INVENTORY_BLACKLIST, te, e);
    }

    public static void addFluidTEToBlacklist(TileEntity te, Throwable e) {
        addEntry(FLUID_BLACKLIST, te, e);
    }

    private static void addEntry(Set<ResourceLocation> blacklist, TileEntity te, Throwable e) {
        if (!blacklist.contains(keyFor(te))) {
            e.printStackTrace();
            String title = te.getWorld().getBlockState(te.getPos()).getBlock().getRegistryName().toString();
            HUDHandler.instance().addMessage(
                    new StringTextComponent("Block tracking failed for " + title + "!"),
                    Lists.newArrayList("A stacktrace can be found in the log."),
                    80, 0xFFFF0000);
            blacklist.add(keyFor(te));
        }
    }

    static boolean isEnergyBlacklisted(TileEntity te) {
        return ENERGY_BLACKLIST.contains(keyFor(te));
    }

    static boolean isInventoryBlacklisted(TileEntity te) {
        return INVENTORY_BLACKLIST.contains(keyFor(te));
    }

    static boolean isFluidBlacklisted(TileEntity te) {
        return FLUID_BLACKLIST.contains(keyFor(te));
    }

    private static ResourceLocation keyFor(TileEntity te) {
        return te.getType().getRegistryName();
    }
}
