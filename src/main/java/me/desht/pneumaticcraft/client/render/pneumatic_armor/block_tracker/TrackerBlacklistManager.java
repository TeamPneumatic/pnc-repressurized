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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;
import java.util.Set;

public class TrackerBlacklistManager {
    private static final Set<ResourceLocation> ENERGY_BLACKLIST = Sets.newHashSet();
    private static final Set<ResourceLocation> INVENTORY_BLACKLIST = Sets.newHashSet();
    private static final Set<ResourceLocation> FLUID_BLACKLIST = Sets.newHashSet();

    public static void addEnergyTEToBlacklist(BlockEntity te, Throwable e) {
        addEntry(ENERGY_BLACKLIST, te, e);
    }

    public static void addInventoryTEToBlacklist(BlockEntity te, Throwable e) {
        addEntry(INVENTORY_BLACKLIST, te, e);
    }

    public static void addFluidTEToBlacklist(BlockEntity te, Throwable e) {
        addEntry(FLUID_BLACKLIST, te, e);
    }

    private static void addEntry(Set<ResourceLocation> blacklist, BlockEntity te, Throwable e) {
        if (!blacklist.contains(keyFor(te))) {
            e.printStackTrace();
            String title = Objects.requireNonNull(Objects.requireNonNull(te.getLevel()).getBlockState(te.getBlockPos()).getBlock().getRegistryName()).toString();
            HUDHandler.getInstance().addMessage(
                    new TextComponent("Block tracking failed for " + title + "!"),
                    ImmutableList.of(new TextComponent("A stacktrace can be found in the log.")),
                    80, 0xFFFF0000);
            blacklist.add(keyFor(te));
        }
    }

    static boolean isEnergyBlacklisted(BlockEntity te) {
        return ENERGY_BLACKLIST.contains(keyFor(te));
    }

    static boolean isInventoryBlacklisted(BlockEntity te) {
        return INVENTORY_BLACKLIST.contains(keyFor(te));
    }

    static boolean isFluidBlacklisted(BlockEntity te) {
        return FLUID_BLACKLIST.contains(keyFor(te));
    }

    private static ResourceLocation keyFor(BlockEntity te) {
        return te.getType().getRegistryName();
    }
}
