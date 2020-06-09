package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableGhast implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("ghast");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.finished.disarmed");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        GoalSelector tasks = ((GhastEntity) entity).goalSelector;

        tasks.getRunningGoals()
                .filter(goal -> Reflections.ghast_aiFireballAttack.isAssignableFrom(goal.getClass()))
                .forEach(tasks::removeGoal);
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
