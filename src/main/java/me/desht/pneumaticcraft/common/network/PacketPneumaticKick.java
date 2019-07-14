package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.core.Sounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class PacketPneumaticKick {
    public PacketPneumaticKick() {
        // empty
    }

    PacketPneumaticKick(PacketBuffer buffer) {
        // empty
    }

    public void toBytes(ByteBuf buf) {
        // empty
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.FEET)) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.isArmorEnabled() && handler.isArmorReady(EquipmentSlotType.FEET) && handler.getArmorPressure(EquipmentSlotType.FEET) > 0.1f) {
                    int upgrades = handler.getUpgradeCount(EquipmentSlotType.FEET, IItemRegistry.EnumUpgrade.DISPENSER);
                    if (upgrades > 0) {
                        handleKick(player, Math.min(PneumaticValues.PNEUMATIC_KICK_MAX_UPGRADES, upgrades));
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleKick(PlayerEntity player, int upgrades) {
        Vec3d lookVec = player.getLookVec().normalize();

        double playerFootY = player.posY - player.getHeight() / 4;
        AxisAlignedBB box = new AxisAlignedBB(player.posX, playerFootY, player.posZ, player.posX, playerFootY, player.posZ)
                .grow(1.0, 1.0, 1.0).offset(lookVec);
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, box);
        if (entities.isEmpty()) return;
        entities.sort(Comparator.comparingDouble(o -> o.getDistanceSq(player)));

        Entity target = entities.get(0);
        if (target instanceof MobEntity) {
            target.attackEntityFrom(DamageSource.causePlayerDamage(player), 3.0f + upgrades * 0.5f);
        }
        target.setMotion(target.getMotion().add(lookVec.scale(1.0 + upgrades * 0.5f)));

        NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PUNCH, SoundCategory.PLAYERS, target.posX, target.posY, target.posZ, 1.0f, 1.0f, false), player.world);
        NetworkHandler.sendToAllAround(new PacketSetEntityMotion(target, target.getMotion()), player.world);
        NetworkHandler.sendToAllAround(new PacketSpawnParticle(ParticleTypes.EXPLOSION, target.posX, target.posY, target.posZ, 1.0D, 0.0D, 0.0D), player.world);
        CommonArmorHandler.getHandlerForPlayer(player).addAir(EquipmentSlotType.FEET, -PneumaticValues.PNEUMATIC_KICK_AIR_USAGE * (2 << upgrades));
    }
}
