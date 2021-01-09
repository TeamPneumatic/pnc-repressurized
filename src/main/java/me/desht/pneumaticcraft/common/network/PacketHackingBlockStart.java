package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.WorldAndCoord;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sent by client when player initiates a hack, and from server back to client to confirm initiation
 */
public class PacketHackingBlockStart extends LocationIntPacket {
    public PacketHackingBlockStart(BlockPos pos) {
        super(pos);
    }

    public PacketHackingBlockStart(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) {
                // client
                PlayerEntity cPlayer = ClientUtils.getClientPlayer();
                CommonArmorHandler.getHandlerForPlayer(cPlayer).setHackedBlockPos(new WorldAndCoord(cPlayer.world, pos));
                RenderBlockTarget target = HUDHandler.getInstance().getSpecificRenderer(BlockTrackerClientHandler.class).getTargetForCoord(pos);
                if (target != null) target.onHackConfirmServer();
            } else {
                // server
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().blockTrackerHandler, true)) {
                    handler.setHackedBlockPos(new WorldAndCoord(player.world, pos));
                    NetworkHandler.sendToAllTracking(this, player.world, player.getPosition());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
