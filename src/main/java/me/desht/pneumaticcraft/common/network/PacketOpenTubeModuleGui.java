package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.gui.tubemodule.GuiTubeModule;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when it needs the client to open a (containerless) module GUI
 */
public class PacketOpenTubeModuleGui extends LocationIntPacket {
    private final ResourceLocation moduleType;

    public PacketOpenTubeModuleGui(ResourceLocation type, BlockPos pos) {
        super(pos);
        this.moduleType = type;
    }

    PacketOpenTubeModuleGui(PacketBuffer buffer) {
        super(buffer);
        moduleType = buffer.readResourceLocation();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeResourceLocation(moduleType);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtils.getClientPlayer();
            if (BlockPressureTube.getFocusedModule(player.world, pos, player) != null) {
                GuiTubeModule.openGuiForType(moduleType, pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
