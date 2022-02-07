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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableVillager implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("villager");
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.resetTrades"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.resetTrades"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 120;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (entity instanceof Villager villager && !player.level.isClientSide) {
            if (villager.shouldRestock()) {
                villager.restock();
            }
            int n = villager.level.random.nextInt(25);
            if (n == 0) {
                ItemStack emeralds = new ItemStack(Items.EMERALD, villager.level.random.nextInt(3) + 1);
                villager.level.addFreshEntity(new ItemEntity(villager.level, villager.getX(), villager.getY(), villager.getZ(), emeralds));
            } else if (n == 1 ) {
                MerchantOffers offers = villager.getOffers();
                MerchantOffer offer = offers.get(villager.level.random.nextInt(offers.size()));
                if (!offer.getResult().isEmpty() && !offer.isOutOfStock()) {
                    villager.level.addFreshEntity(new ItemEntity(villager.level, villager.getX(), villager.getY(), villager.getZ(), offer.getResult()));
                    offer.increaseUses();
                }
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
