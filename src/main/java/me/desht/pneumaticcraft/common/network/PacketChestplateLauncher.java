/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
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

    private final float amount;

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

        ItemStack stack = player.getOffhandItem();
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);

        if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().chestplateLauncherHandler, false) && !stack.isEmpty()) {
            ItemStack toFire = player.isCreative() ? ItemHandlerHelper.copyStackWithSize(stack, 1) : stack.split(1);
            Entity launchedEntity = ItemLaunching.getEntityToLaunch(player.getCommandSenderWorld(), toFire, player,true, true);
            int upgrades = handler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.DISPENSER, PneumaticValues.PNEUMATIC_LAUNCHER_MAX_UPGRADES);

            if (launchedEntity instanceof AbstractArrowEntity) {
                AbstractArrowEntity arrow = (AbstractArrowEntity) launchedEntity;
                arrow.pickup = player.isCreative() ? AbstractArrowEntity.PickupStatus.CREATIVE_ONLY : AbstractArrowEntity.PickupStatus.ALLOWED;
                arrow.setBaseDamage(arrow.getBaseDamage() + 0.25 * upgrades * amount);
            }

            Vector3d velocity = player.getLookAngle().normalize().scale(amount * upgrades * SCALE_FACTOR);
            ItemLaunching.launchEntity(launchedEntity, player.getEyePosition(1f).add(0, -0.1, 0), velocity, true);

            if (!player.isCreative()) {
                int usedAir = (int) (20 * upgrades * amount);
                handler.addAir(EquipmentSlotType.CHEST, -usedAir);
            }
        }
    }
}
