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
import me.desht.pneumaticcraft.mixin.accessors.ShulkerAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackableShulker extends AbstractPersistentEntityHack<Shulker> {

    private static final ResourceLocation ID = RL("shulker");

    public HackableShulker() {
        super(StockHackTypes.NEUTRALIZE);
    }

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Shulker> getHackableClass() {
        return Shulker.class;
    }

    @Override
    public boolean afterHackTick(Shulker entity) {
        if (entity.level().random.nextInt(5) < 4) {
            ((ShulkerAccess)entity).callSetRawPeekAmount(100);
        }
        return true;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onBullet(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof ShulkerBullet b && b.getOwner() instanceof Shulker shulker
                    && hasPersistentHack(shulker, HackableShulker.class)) {
                event.setCanceled(true);
            }
        }
    }
}
