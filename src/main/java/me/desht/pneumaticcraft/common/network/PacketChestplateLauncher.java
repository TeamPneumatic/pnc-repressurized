package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to launch an item from the chestplate launcher
 */
public class PacketChestplateLauncher {
    private static final float SCALE_FACTOR = 0.7f;

    private float amount;

    public PacketChestplateLauncher() {
        // empty
    }

    public PacketChestplateLauncher(float amount) {
        this.amount = amount;
    }

    PacketChestplateLauncher(PacketBuffer buffer) {
        this.amount = buffer.readFloat();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeFloat(amount);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleLaunch(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    private void handleLaunch(ServerPlayerEntity player) {
        if (player == null) return;

        ItemStack stack = player.getHeldItemOffhand();
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
        int upgrades = handler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.DISPENSER, PneumaticValues.PNEUMATIC_LAUNCHER_MAX_UPGRADES);

        if (handler.getArmorPressure(EquipmentSlotType.CHEST) > 0.1f && handler.isArmorReady(EquipmentSlotType.CHEST) && upgrades > 0 && !stack.isEmpty()) {
            ItemStack toFire = player.isCreative() ? ItemHandlerHelper.copyStackWithSize(stack, 1) : stack.split(1);
            Entity launchedEntity = ItemLaunching.getEntityToLaunch(player.getEntityWorld(), toFire, player,true, true);

            if (launchedEntity instanceof AbstractArrowEntity) {
                AbstractArrowEntity arrow = (AbstractArrowEntity) launchedEntity;
                arrow.pickupStatus = player.isCreative() ? AbstractArrowEntity.PickupStatus.CREATIVE_ONLY : AbstractArrowEntity.PickupStatus.ALLOWED;
                arrow.setDamage(arrow.getDamage() + 0.25 * upgrades * amount);
            }

            Vector3d velocity = player.getLookVec().normalize().scale(amount * upgrades * SCALE_FACTOR);
            ItemLaunching.launchEntity(launchedEntity, player.getEyePosition(1f).add(0, -0.1, 0), velocity, true);

            if (!player.isCreative()) {
                int usedAir = (int) (20 * upgrades * amount);
                handler.addAir(EquipmentSlotType.CHEST, -usedAir);
            }
        }
    }
}
