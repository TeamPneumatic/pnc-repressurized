package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackablePufferfish implements IHackableEntity<Pufferfish> {
    private static final ResourceLocation ID = RL("pufferfish");

    @Nullable
    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Pufferfish> getHackableClass() {
        return Pufferfish.class;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return IHackableEntity.super.canHack(entity, player) && Reflections.pufferfish_aiPuff != null;
    }

    @Override
    public void addHackInfo(Pufferfish entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.trigger"));
    }

    @Override
    public void addPostHackInfo(Pufferfish entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.triggered"));
    }

    @Override
    public int getHackTime(Pufferfish entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Pufferfish entity, Player player) {
        entity.goalSelector.getAvailableGoals().stream()
                .filter(w -> Reflections.pufferfish_aiPuff.isAssignableFrom(w.getGoal().getClass()))
                .findFirst()
                .ifPresent(w -> w.getGoal().start());
    }

}
