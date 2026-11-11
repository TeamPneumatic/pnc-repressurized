package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Common base for all tamable entities. And horses, which Mojang have, for some reason, decided are special -_-
 * @param <T>
 */
public abstract class AbstractTameableHack<T extends LivingEntity> implements IHackableEntity<T> {
    @Override
    public boolean canHack(Entity entity, Player player) {
        return entity instanceof OwnableEntity o && (o.getOwnerReference() == null || !player.getUUID().equals(o.getOwnerReference().getUUID()));
    }

    @Override
    public void addHackInfo(T entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.tame"));
    }

    @Override
    public void addPostHackInfo(T entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.tamed"));
    }

    @Override
    public int getHackTime(T entity, Player player) {
        return 60;
    }
}
