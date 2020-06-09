package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableShulker implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("shulker");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        GoalSelector tasks = ((ShulkerEntity) entity).goalSelector;

        tasks.getRunningGoals()
                .filter(goal -> Reflections.shulker_aiAttack.isAssignableFrom(goal.getClass()))
                .forEach(tasks::removeGoal);
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        if (entity.getEntityWorld().rand.nextInt(5) < 4) {
            ((ShulkerEntity) entity).updateArmorModifier(100);
        }
        return false;
    }
}
