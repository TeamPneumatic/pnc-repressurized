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

package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum HackTickHandler {
    INSTANCE;

    private final Map<ResourceLocation, Map<BlockPos, IHackableBlock>> hackedBlocks = new HashMap<>();
    private final Map<ResourceLocation, Set<Entity>> hackedEntities = new HashMap<>();

    public static HackTickHandler instance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ResourceLocation worldKey = getKey(event.world);

            if (hackedBlocks.containsKey(worldKey)) {
                hackedBlocks.get(worldKey).entrySet().removeIf(entry -> !entry.getValue().afterHackTick(event.world, entry.getKey()));
            }

            if (hackedEntities.containsKey(worldKey)) {
                Set<Entity> entities = hackedEntities.get(worldKey);
                // IHacking#tick() will remove any no-longer-applicable hacks from the entity
                entities.forEach(entity -> entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).ifPresent(hacking -> {
                    if (entity.isAlive() && !hacking.getCurrentHacks().isEmpty()) hacking.tick(entity);
                }));
                // Remove the entity from the tracker if it has no more applicable hacks
                entities.removeIf(e -> !e.isAlive() ||
                        e.getCapability(PNCCapabilities.HACKING_CAPABILITY).map(hacking -> hacking.getCurrentHacks().isEmpty()).orElse(true)
                );
            }
        }
    }

    public void trackBlock(Level world, BlockPos pos, IHackableBlock iHackable) {
        hackedBlocks.computeIfAbsent(getKey(world), k1 -> new HashMap<>()).put(pos, iHackable);
    }

    public void trackEntity(Entity entity, IHackableEntity iHackable) {
        if (iHackable.getHackableId() != null) {
            entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).ifPresent(hacking -> {
                hacking.addHackable(iHackable);
                hackedEntities.computeIfAbsent(getKey(entity.level), k -> new HashSet<>()).add(entity);
            });
        }
    }

    private ResourceLocation getKey(Level w) {
        return w.dimension().location();
    }
}
