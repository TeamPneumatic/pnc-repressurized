package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableEnderman implements IHackableEntity {

    @Override
    public ResourceLocation getHackableId() {
        return RL("enderman");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity instanceof EndermanEntity && canEndermanTeleport((EndermanEntity) entity);
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.stopTeleport"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.stopTeleporting"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        // enderman teleport suppression is handled in onEnderTeleport()
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return entity.isAlive();
    }

    private static boolean canEndermanTeleport(LivingEntity entity) {
        List<IHackableEntity> hacks = PneumaticRegistry.getInstance().getHelmetRegistry().getCurrentEntityHacks(entity);
        return hacks.stream().noneMatch(hack -> hack instanceof HackableEnderman);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
            LivingEntity e = event.getEntityLiving();
            if (e instanceof EndermanEntity && !canEndermanTeleport(e)) {
                event.setCanceled(true);
            }
        }
    }
}
