package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackablePufferfish implements IHackableEntity {
    private static final ResourceLocation ID = RL("pufferfish");

    @Nullable
    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return entity instanceof Pufferfish && Reflections.pufferfish_aiPuff != null;
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.trigger"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.triggered"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (entity instanceof Pufferfish pufferfish) {
            pufferfish.goalSelector.getAvailableGoals().stream()
                    .filter(w -> Reflections.pufferfish_aiPuff.isAssignableFrom(w.getGoal().getClass()))
                    .findFirst()
                    .ifPresent(w -> w.getGoal().start());
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
