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

package me.desht.pneumaticcraft.common.hacking;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncEntityHacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

public enum HackEventListener {
    INSTANCE;

    public static HackEventListener getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void worldTick(LevelTickEvent.Post event) {
        HackTickTracker.getInstance(event.getLevel()).tick(event.getLevel());
    }

    @SubscribeEvent
    public void onEntityJoining(EntityJoinLevelEvent event) {
        // re-add any entity with hacks on it (i.e. before the last server restart) to the tracker
        HackManager.getActiveHacks(event.getEntity()).ifPresent(hacking ->
                hacking.getCurrentHacks().forEach(hack ->
                        HackTickTracker.getInstance(event.getLevel()).trackEntity(event.getEntity(), hack)));
    }

    @SubscribeEvent
    public void onEntityTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            HackManager.getActiveHacks(event.getEntity()).ifPresent(hacking -> {
                List<ResourceLocation> ids = hacking.getCurrentHacks().stream().map(IHackableEntity::getHackableId).toList();
                if (!ids.isEmpty()) {
                    NetworkHandler.sendToPlayer(PacketSyncEntityHacks.create(event.getTarget(), ids), sp);
                }
            });
        }
    }
}