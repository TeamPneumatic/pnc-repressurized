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

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;

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

    PacketChestplateLauncher(FriendlyByteBuf buffer) {
        this.amount = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(amount);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleLaunch(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    private void handleLaunch(ServerPlayer player) {
        if (player == null) return;

        ItemStack stack = player.getOffhandItem();
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);

        // Allows launching if upgrade is active, there is an offhand item, and the item is not on cooldown
        if (handler.upgradeUsable(CommonUpgradeHandlers.chestplateLauncherHandler, false) && !stack.isEmpty()
                && !player.getCooldowns().isOnCooldown(stack.getItem())) {

            ItemStack toFire = stack;

            // Split stack only for items that are consumed when dispensed (not micromissiles)
            if (!(stack.getItem() == ModItems.MICROMISSILES.get())) {
                toFire = player.isCreative() ? ItemHandlerHelper.copyStackWithSize(stack, 1) : stack.split(1);
            }

            Entity launchedEntity = ItemLaunching.getEntityToLaunch(player.getCommandSenderWorld(), toFire, player,true, true);
            int upgrades = handler.getUpgradeCount(EquipmentSlot.CHEST, ModUpgrades.DISPENSER.get(), PneumaticValues.PNEUMATIC_LAUNCHER_MAX_UPGRADES);
            Vec3 velocity = player.getLookAngle().normalize().scale(amount * upgrades * SCALE_FACTOR);

            // Special launch case for arrows/tridents
            if (launchedEntity instanceof AbstractArrow arrow) {
                arrow.pickup = player.isCreative() ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
                arrow.setBaseDamage(arrow.getBaseDamage() + 0.25 * upgrades * amount);
            }

            // Launches item
            ItemLaunching.launchEntity(launchedEntity, player.getEyePosition(1f).add(0, -0.1, 0), velocity, true);

            // Uses air from chestplate (unless in creative)
            if (!player.isCreative()) {
                int usedAir = (int) (20 * upgrades * amount);
                handler.addAir(EquipmentSlot.CHEST, -usedAir);
            }
        }
    }
}
