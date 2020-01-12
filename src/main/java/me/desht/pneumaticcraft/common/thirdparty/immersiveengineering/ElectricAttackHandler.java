package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElectricAttackHandler {
    private static final Map<UUID, Long> sounds = new HashMap<>();

    @SubscribeEvent
    public static void onElectricalAttack(LivingHurtEvent event) {
        // TODO 1.14
//        if (!(event.getSource() instanceof IEDamageSources.ElectricDamageSource)) return;
//
//        if (event.getEntityLiving() instanceof EntityDrone) {
//            EntityDrone drone = (EntityDrone) event.getEntityLiving();
//            float dmg = event.getAmount();
//            int sec = drone.getUpgrades(IItemRegistry.EnumUpgrade.SECURITY);
//            if (sec > 0) {
//                drone.addAir(ItemStack.EMPTY, (int)(-50 * dmg));
//                event.setAmount(0f);
//                double dy = Math.min(dmg / 4, 0.5);
//                NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, drone.posX, drone.posY, drone.posZ,
//                            0, -dy, 0, (int) (dmg), 0, 0, 0), drone.world);
//                playLeakSound(drone);
//            }
//        } else if (event.getEntityLiving() instanceof PlayerEntity) {
//            PlayerEntity player = (PlayerEntity)event.getEntityLiving();
//            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
//            if (handler.getUpgradeCount(EquipmentSlotType.CHEST, IItemRegistry.EnumUpgrade.SECURITY) > 0
//                    && handler.getArmorPressure(EquipmentSlotType.CHEST) > 0.1
//                    && handler.isArmorReady(EquipmentSlotType.CHEST)) {
//                handler.addAir(EquipmentSlotType.CHEST, (int)(-150 * event.getAmount()));
//                float sx = player.getRNG().nextFloat() * 1.5F - 0.75F;
//                float sz = player.getRNG().nextFloat() * 1.5F - 0.75F;
//                double dy = Math.min(event.getAmount() / 4, 0.5);
//                NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, player.posX + sx, player.posY + 1, player.posZ + sz, sx / 4, -dy, sz / 4), player.world);
//                event.setAmount(0f);
//                playLeakSound(player);
//            }
//        }
    }

    private static void playLeakSound(Entity e) {
        if (e.world.getGameTime() - sounds.getOrDefault(e.getUniqueID(), 0L) > 16) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.LEAKING_GAS.get(), SoundCategory.PLAYERS, e.posX, e.posY, e.posZ, 0.5f, 0.7f, true), e.world);
            sounds.put(e.getUniqueID(), e.world.getGameTime());
        }
    }
}
