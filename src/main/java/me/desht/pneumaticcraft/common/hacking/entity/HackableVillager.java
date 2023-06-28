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

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableVillager implements IHackableEntity<Villager> {
    private static final ResourceLocation ID = RL("villager");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Villager> getHackableClass() {
        return Villager.class;
    }

    @Override
    public void addHackInfo(Villager entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.resetTrades"));
    }

    @Override
    public void addPostHackInfo(Villager entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.resetTrades"));
    }

    @Override
    public int getHackTime(Villager entity, Player player) {
        return 120;
    }

    @Override
    public void onHackFinished(Villager entity, Player player) {
        Level level = player.level();
        if (!level.isClientSide) {
            if (entity.shouldRestock()) {
                entity.restock();
            }
            int n = level.random.nextInt(25);
            if (n == 0) {
                ItemStack emeralds = new ItemStack(Items.EMERALD, level.random.nextInt(3) + 1);
                level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), emeralds));
            } else if (n == 1 ) {
                MerchantOffers offers = entity.getOffers();
                MerchantOffer offer = offers.get(level.random.nextInt(offers.size()));
                if (!offer.getResult().isEmpty() && !offer.isOutOfStock()) {
                    level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), offer.getResult()));
                    offer.increaseUses();
                }
            }
        }
    }
}
