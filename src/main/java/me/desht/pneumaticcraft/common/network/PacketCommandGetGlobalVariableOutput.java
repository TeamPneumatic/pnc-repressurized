package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Received on: CLIENT
 */
public class PacketCommandGetGlobalVariableOutput extends LocationIntPacket {
    private String varName;
    private ItemStack stack;

    public PacketCommandGetGlobalVariableOutput() {
    }

    public PacketCommandGetGlobalVariableOutput(String varName, BlockPos pos, ItemStack stack) {
        super(pos);
        this.varName = varName;
        this.stack = stack;
    }

    PacketCommandGetGlobalVariableOutput(PacketBuffer buffer) {
        super(buffer);
        varName = PacketUtil.readUTF8String(buffer);
        stack = buffer.readItemStack();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        PacketUtil.writeUTF8String(buf, varName);
        new PacketBuffer(buf).writeItemStack(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(xlate("command.getGlobalVariable.output",
                            varName,
                            pos.getX(), pos.getY(), pos.getZ(),
                            stack.isEmpty() ? "-" : stack.getDisplayName().getFormattedText()),
                    false);
        });
        ctx.get().setPacketHandled(true);
    }
}
