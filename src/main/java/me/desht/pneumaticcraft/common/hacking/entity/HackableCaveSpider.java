package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableCaveSpider implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("cave_spider");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.neutralize"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.neutralized"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 50;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.level.isClientSide) {
            entity.remove();
            SpiderEntity spider = new SpiderEntity(EntityType.SPIDER, entity.level);
            spider.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
            spider.setHealth(((SpiderEntity) entity).getHealth());
            spider.yBodyRot = ((SpiderEntity) entity).yBodyRot;
            entity.level.addFreshEntity(spider);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
