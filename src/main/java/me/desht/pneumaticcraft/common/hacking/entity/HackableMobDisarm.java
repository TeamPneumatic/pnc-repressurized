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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableMobDisarm implements IHackableEntity {

    private static final ResourceLocation ID = RL("mob_disarm");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return entity instanceof Mob mob
                && Arrays.stream(EquipmentSlot.values()).anyMatch(slot -> !mob.getItemBySlot(slot).isEmpty());
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.disarm"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.disarmed"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (!entity.level.isClientSide) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (doDisarm((Mob) entity, slot, player.getRandom())) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

    private boolean doDisarm(Mob entity, EquipmentSlot slot, Random rand) {
        if (entity.getItemBySlot(slot).isEmpty()) return false;

        float[] dropChances = slot.getType() == EquipmentSlot.Type.ARMOR ? entity.armorDropChances : entity.handDropChances;
        int slotIdx = slot.getIndex();
        boolean noDamage = dropChances[slotIdx] > 1f;
        ItemStack stack = entity.getItemBySlot(slot);
        if (!stack.isEmpty() && rand.nextFloat() < dropChances[slotIdx]) {
            if (!noDamage && stack.isDamageableItem()) {
                int k = Math.max(stack.getMaxDamage() - 25, 1);
                int l = stack.getMaxDamage() - rand.nextInt(rand.nextInt(k) + 1);
                stack.setDamageValue(Mth.clamp(l, 1, k));
            }
            entity.spawnAtLocation(stack, 0f);
        }
        entity.setItemSlot(slot, ItemStack.EMPTY);
        return true;
    }

}
