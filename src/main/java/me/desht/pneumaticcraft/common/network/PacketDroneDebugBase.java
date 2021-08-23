package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class PacketDroneDebugBase {
    final int entityId;
    final BlockPos pos;

    public PacketDroneDebugBase(IDroneBase drone) {
        if (drone instanceof EntityDrone) {
            entityId = ((EntityDrone) drone).getId();
            pos = null;
        } else if (drone instanceof TileEntityProgrammableController) {
            pos = ((TileEntityProgrammableController) drone).getBlockPos();
            entityId = -1;
        } else {
            throw new IllegalArgumentException("drone must be an EntityDrone or TileEntityProgrammableController!");
        }
    }

    public PacketDroneDebugBase(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            entityId = buffer.readInt();
            pos = null;
        } else {
            pos = buffer.readBlockPos();
            entityId = -1;
        }
    }

    PacketDroneDebugBase(int entityId, BlockPos pos) {
        this.entityId = entityId;
        this.pos = pos;
    }

    public void toBytes(PacketBuffer buf) {
        if (pos != null) {
            buf.writeBoolean(false);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(entityId);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ctx.get().getSender() == null ? ClientUtils.getClientWorld() : ctx.get().getSender().level;
            PlayerEntity player =  ctx.get().getSender() == null ? ClientUtils.getClientPlayer() : ctx.get().getSender();
            if (entityId >= 0) {
                Entity entity = world.getEntity(entityId);
                if (entity instanceof EntityDrone) {
                    handle(player, (IDroneBase) entity);
                }
            } else if (pos != null) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof TileEntityProgrammableController) {
                    handle(player, (IDroneBase) te);
                }
            } else {
                handle(player, null);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    abstract void handle(PlayerEntity player, IDroneBase drone);

}
