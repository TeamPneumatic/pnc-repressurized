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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when an immediate update is needed to a client-side entity's motion
 */
public class PacketSetEntityMotion extends LocationDoublePacket {
    private final int entityId;

    public PacketSetEntityMotion(Entity entity, Vec3 motion) {
        super(motion);
        entityId = entity.getId();
    }

    PacketSetEntityMotion(FriendlyByteBuf buffer) {
        super(buffer);
        entityId = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtils.getClientLevel().getEntity(entityId);
            if (entity != null) {
                entity.setDeltaMovement(x, y, z);
                entity.setOnGround(false);
//                entity.collided = false;
                entity.horizontalCollision = false;
                entity.verticalCollision = false;
                if (entity instanceof LivingEntity) ((LivingEntity) entity).setJumping(true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
