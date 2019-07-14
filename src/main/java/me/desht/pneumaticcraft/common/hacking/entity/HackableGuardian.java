package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;

import java.util.List;

public class HackableGuardian implements IHackableEntity {
    @Override
    public String getId() {
        return "guardian";
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
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
        GoalSelector tasks = ((MobEntity) entity).goalSelector;

        tasks.getRunningGoals()
                .filter(goal -> Reflections.guardian_aiGuardianAttack.isAssignableFrom(goal.getClass()))
                .forEach(tasks::removeGoal);

        if (entity instanceof ElderGuardianEntity) {
            player.removeActivePotionEffect(Effects.MINING_FATIGUE);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
