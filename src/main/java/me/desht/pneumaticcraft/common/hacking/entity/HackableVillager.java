package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableVillager implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("villager");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.resetTrades"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.resetTrades"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 120;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (entity instanceof VillagerEntity && !player.level.isClientSide) {
            VillagerEntity villager = (VillagerEntity) entity;
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
