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

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableEnderman implements IHackableEntity {

    @Override
    public ResourceLocation getHackableId() {
        return RL("enderman");
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return entity instanceof EnderMan && canEndermanTeleport((EnderMan) entity);
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.stopTeleport"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.stopTeleporting"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        // enderman teleport suppression is handled in onEnderTeleport()
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return entity.isAlive();
    }

    private static boolean canEndermanTeleport(LivingEntity entity) {
        List<IHackableEntity> hacks = CommonArmorRegistry.getInstance().getCurrentEntityHacks(entity);
        return hacks.stream().noneMatch(hack -> hack instanceof HackableEnderman);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
            LivingEntity e = event.getEntityLiving();
            if (e instanceof EnderMan && !canEndermanTeleport(e)) {
                event.setCanceled(true);
            }
        }
    }
}
