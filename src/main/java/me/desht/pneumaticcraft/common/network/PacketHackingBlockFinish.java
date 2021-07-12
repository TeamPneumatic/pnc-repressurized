package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.WorldAndCoord;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when a block hack has completed.
 */
public class PacketHackingBlockFinish extends LocationIntPacket {
    public PacketHackingBlockFinish(WorldAndCoord gPos) {
        super(gPos.pos);
    }

    public PacketHackingBlockFinish(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtils.getClientPlayer();
            IHackableBlock hackableBlock = HackManager.getHackableForBlock(player.world, pos, player);
            if (hackableBlock != null) {
                hackableBlock.onHackComplete(player.world, pos, player);
                HackTickHandler.instance().trackBlock(player.world, pos, hackableBlock);
                CommonArmorHandler.getHandlerForPlayer(player).getExtensionData(ArmorUpgradeRegistry.getInstance().hackHandler).setHackedBlockPos(null);
                player.playSound(ModSounds.HELMET_HACK_FINISH.get(), 1.0F, 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
