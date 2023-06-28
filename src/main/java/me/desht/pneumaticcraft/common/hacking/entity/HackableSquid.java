package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableSquid implements IHackableEntity<Squid> {
    private static final ResourceLocation ID = RL("squid");

    @Override
    public ResourceLocation getHackableId() {
        return ID;
    }

    @NotNull
    @Override
    public Class<Squid> getHackableClass() {
        return Squid.class;
    }

    @Override
    public void addHackInfo(Squid entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.changeColor"));
    }

    @Override
    public void addPostHackInfo(Squid entity, List<Component> curInfo, Player player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.changeColor"));
    }

    @Override
    public int getHackTime(Squid entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(Squid entity, Player player) {
        if (!entity.level().isClientSide) {
            entity.discard();
            GlowSquid glowSquid = new GlowSquid(EntityType.GLOW_SQUID, entity.level());
            glowSquid.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            glowSquid.setHealth(entity.getHealth());
            glowSquid.yBodyRot = entity.yBodyRot;
            entity.level().addFreshEntity(glowSquid);
            entity.level().addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0F, entity.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}
