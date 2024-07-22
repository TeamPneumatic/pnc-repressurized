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

package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.AbstractPersistentEntityHack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackableBlaze extends AbstractPersistentEntityHack<Blaze> {
    private static final ResourceLocation ID = RL("blaze");

    public HackableBlaze() {
        super(StockHackTypes.DISARM);
    }

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public Class<Blaze> getHackableClass() {
        return Blaze.class;
    }

    @Override
    public void onHackFinished(Blaze entity, Player player) {
        super.onHackFinished(entity, player);

        entity.setSilent(true);
    }

    @EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onFireball(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof SmallFireball f && f.getOwner() instanceof Blaze blaze
                    && hasPersistentHack(blaze, HackableBlaze.class)) {
                event.setCanceled(true);
            }
        }
    }
}
