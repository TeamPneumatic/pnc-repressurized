package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Random;

public class HackableLivingDisarm implements IHackableEntity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.disarmed");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.world.isRemote && entity instanceof MobEntity) {
            MobEntity entityLiving = (MobEntity) entity;

            // access from vertical gets hands, horizontal gets armor - see EntityLivingBase#getCapability
            entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP)
                    .ifPresent(h -> doDisarm(entityLiving, player.getRNG(), h, entityLiving.inventoryHandsDropChances));

            entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH)
                    .ifPresent(h -> doDisarm(entityLiving, player.getRNG(), h, entityLiving.inventoryArmorDropChances));

            entityLiving.setCanPickUpLoot(false);
        }
    }

    private void doDisarm(MobEntity entity, Random rand, IItemHandler handler, float[] dropChances) {
        if (handler == null) return;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            boolean flag1 = dropChances[i] > 1.0F;
            if (!stack.isEmpty() && rand.nextFloat() < dropChances[i]) {
                if (!flag1 && stack.isDamageable()) {
                    int k = Math.max(stack.getMaxDamage() - 25, 1);
                    int l = stack.getMaxDamage() - rand.nextInt(rand.nextInt(k) + 1);
                    stack.setDamage(MathHelper.clamp(l, 1, k));
                }
                entity.entityDropItem(stack, 0f);
            }
            handler.extractItem(i, stack.getCount(), false);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
