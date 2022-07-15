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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.capabilities.CapabilityHacking;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncEntityHacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public enum HackEventListener {
    INSTANCE;

    public static HackEventListener getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void worldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HackTickTracker.getInstance(event.level).tick(event.level);
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(CapabilityHacking.ID, new CapabilityHacking.Provider());
    }

    @SubscribeEvent
    public void onEntityJoining(EntityJoinLevelEvent event) {
        // re-add any entity with hacks on it (i.e. before the last server restart) to the tracker
        event.getEntity().getCapability(PNCCapabilities.HACKING_CAPABILITY)
                .ifPresent(hacking -> hacking.getCurrentHacks()
                        .forEach(hack -> HackTickTracker.getInstance(event.getLevel()).trackEntity(event.getEntity(), hack)));
    }

    @SubscribeEvent
    public void onEntityTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            event.getTarget().getCapability(PNCCapabilities.HACKING_CAPABILITY).ifPresent(hacking -> {
                List<ResourceLocation> ids = hacking.getCurrentHacks().stream().map(IHackableEntity::getHackableId).toList();
                if (!ids.isEmpty()) {
                    NetworkHandler.sendToPlayer(new PacketSyncEntityHacks(event.getTarget(), ids), sp);
                }
            });
        }
    }
}