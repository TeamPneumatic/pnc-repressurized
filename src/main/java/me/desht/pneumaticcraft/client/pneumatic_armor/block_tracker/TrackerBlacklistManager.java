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

package me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class TrackerBlacklistManager {
    private static final Set<BlockEntityType<?>> ENERGY_BLACKLIST = Sets.newHashSet();
    private static final Set<BlockEntityType<?>> INVENTORY_BLACKLIST = Sets.newHashSet();
    private static final Set<BlockEntityType<?>> FLUID_BLACKLIST = Sets.newHashSet();

    public static void addEnergyTEToBlacklist(BlockEntity te, Throwable e) {
        addEntry(ENERGY_BLACKLIST, te, e);
    }

    public static void addInventoryTEToBlacklist(BlockEntity te, Throwable e) {
        addEntry(INVENTORY_BLACKLIST, te, e);
    }

    public static void addFluidTEToBlacklist(BlockEntity te, Throwable e) {
        addEntry(FLUID_BLACKLIST, te, e);
    }

    private static void addEntry(Set<BlockEntityType<?>> blacklist, BlockEntity te, Throwable e) {
        if (!blacklist.contains(te.getType())) {
            e.printStackTrace();
            String title = PneumaticCraftUtils.getRegistryName(te.getBlockState().getBlock()).orElseThrow().toString();
            HUDHandler.getInstance().addMessage(
                    Component.literal("Block tracking failed for " + title + "!"),
                    ImmutableList.of(Component.literal("A stacktrace can be found in the log.")),
                    80, 0xFFFF0000);
            blacklist.add(te.getType());
        }
    }

    public static boolean isEnergyBlacklisted(BlockEntity te) {
        return ENERGY_BLACKLIST.contains(te.getType());
    }

    public static boolean isInventoryBlacklisted(BlockEntity te) {
        return INVENTORY_BLACKLIST.contains(te.getType());
    }

    public static boolean isFluidBlacklisted(BlockEntity te) {
        return FLUID_BLACKLIST.contains(te.getType());
    }
}
