package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableSquid implements IHackableEntity {
    private static final ResourceLocation ID = RL("squid");

    @Nullable
    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        return entity instanceof Squid;
    }

    @Override
    public void addHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.changeColor"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.changeColor"));
    }

    @Override
    public int getHackTime(Entity entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, Player player) {
        if (!entity.level.isClientSide && entity instanceof Squid squid) {
            entity.discard();
            GlowSquid glowSquid = new GlowSquid(EntityType.GLOW_SQUID, squid.level);
            glowSquid.moveTo(squid.getX(), squid.getY(), squid.getZ(), squid.getYRot(), squid.getXRot());
            glowSquid.setHealth(squid.getHealth());
            glowSquid.yBodyRot = squid.yBodyRot;
            entity.level.addFreshEntity(glowSquid);
            entity.level.addParticle(ParticleTypes.EXPLOSION, squid.getX(), squid.getY() + squid.getBbHeight() / 2.0F, squid.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
