package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableItemFrame implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("item_frame");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity instanceof ItemFrameEntity && !((ItemFrameEntity) entity).getItem().isEmpty();
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("Hack to detach item"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("Item detached!"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.level.isClientSide) {
            entity.hurt(DamageSource.playerAttack(player), 0.1f);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
