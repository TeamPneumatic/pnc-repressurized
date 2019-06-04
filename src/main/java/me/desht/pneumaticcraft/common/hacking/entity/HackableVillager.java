package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.List;

public class HackableVillager implements IHackableEntity {
    @Override
    public String getId() {
        return "villager";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.resetTrades");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.resetTrades");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 120;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        if (entity instanceof EntityVillager) {
            EntityVillager villager = (EntityVillager) entity;
            NBTTagCompound tag = new NBTTagCompound();
            villager.writeEntityToNBT(tag);
            int newLevel = tag.hasKey("CareerLevel") ? Math.max(0, tag.getInteger("CareerLevel") - 1) : 0;
            tag.setInteger("CareerLevel", newLevel);
            villager.readEntityFromNBT(tag);
            villager.buyingList = null;

            if (!entity.world.isRemote) {
                int n = villager.world.rand.nextInt(25);
                if (n == 0) {
                    ItemStack emeralds = new ItemStack(Items.EMERALD, 1, villager.world.rand.nextInt(3) + 1);
                    villager.world.spawnEntity(new EntityItem(villager.world, villager.posX, villager.posY, villager.posZ, emeralds));
                } else if (n == 1) {
                    MerchantRecipeList list = villager.getRecipes(player);
                    if (list != null) {
                        MerchantRecipe recipe = list.get(villager.world.rand.nextInt(list.size()));
                        if (!recipe.getItemToSell().isEmpty()) {
                            villager.world.spawnEntity(new EntityItem(villager.world, villager.posX, villager.posY, villager.posZ, recipe.getItemToSell()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
