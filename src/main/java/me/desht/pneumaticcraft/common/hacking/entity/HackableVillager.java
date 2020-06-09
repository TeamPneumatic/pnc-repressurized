package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

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
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.result.resetTrades");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.finished.resetTrades");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 120;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        // todo 1.14 villagers
//        if (entity instanceof VillagerEntity) {
//            VillagerEntity villager = (VillagerEntity) entity;
//            CompoundNBT tag = new CompoundNBT();
//            villager.writeAdditional(tag);
//            int newLevel = tag.hasKey("CareerLevel") ? Math.max(0, tag.getInteger("CareerLevel") - 1) : 0;
//            tag.setInteger("CareerLevel", newLevel);
//            villager.readEntityFromNBT(tag);
//            villager.buyingList = null;
//
//            if (!entity.world.isRemote) {
//                int n = villager.world.rand.nextInt(25);
//                if (n == 0) {
//                    ItemStack emeralds = new ItemStack(Items.EMERALD, villager.world.rand.nextInt(3) + 1);
//                    villager.world.addEntity(new ItemEntity(villager.world, villager.posX, villager.posY, villager.posZ, emeralds));
//                } else if (n == 1) {
//                    MerchantRecipeList list = villager.getRecipes(player);
//                    if (list != null) {
//                        MerchantRecipe recipe = list.get(villager.world.rand.nextInt(list.size()));
//                        if (!recipe.getItemToSell().isEmpty()) {
//                            villager.world.spawnEntity(new ItemEntity(villager.world, villager.posX, villager.posY, villager.posZ, recipe.getItemToSell()));
//                        }
//                    }
//                }
//            }
//        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
