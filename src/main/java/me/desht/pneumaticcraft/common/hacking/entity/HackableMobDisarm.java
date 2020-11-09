package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableMobDisarm implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("mob_disarm");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity instanceof MobEntity && Arrays.stream(EquipmentSlotType.values())
                .anyMatch(slot -> !((MobEntity) entity).getItemStackFromSlot(slot).isEmpty());
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.disarm"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.disarmed"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.world.isRemote) {
            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                if (doDisarm((MobEntity) entity, slot, player.getRNG())) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

    private boolean doDisarm(MobEntity entity, EquipmentSlotType slot, Random rand) {
        if (entity.getItemStackFromSlot(slot).isEmpty()) return false;

        float[] dropChances = slot.getSlotType() == EquipmentSlotType.Group.ARMOR ? entity.inventoryArmorDropChances : entity.inventoryHandsDropChances;
        int slotIdx = slot.getIndex();
        boolean noDamage = dropChances[slotIdx] > 1f;
        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (!stack.isEmpty() && rand.nextFloat() < dropChances[slotIdx]) {
            if (!noDamage && stack.isDamageable()) {
                int k = Math.max(stack.getMaxDamage() - 25, 1);
                int l = stack.getMaxDamage() - rand.nextInt(rand.nextInt(k) + 1);
                stack.setDamage(MathHelper.clamp(l, 1, k));
            }
            entity.entityDropItem(stack, 0f);
        }
        entity.setItemStackToSlot(slot, ItemStack.EMPTY);
        return true;
    }

}
