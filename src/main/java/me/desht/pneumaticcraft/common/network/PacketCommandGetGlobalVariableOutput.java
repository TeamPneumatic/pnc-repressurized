package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
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
        varName = buffer.readString();
        stack = buffer.readItemStack();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeString(varName);
        buf.writeItemStack(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientUtils.getClientPlayer().sendStatusMessage(xlate("pneumaticcraft.command.getGlobalVariable.output",
                        varName,
                        pos.getX(), pos.getY(), pos.getZ(),
                        stack.isEmpty() ? new StringTextComponent("-") : stack.getDisplayName()),
                false));
        ctx.get().setPacketHandled(true);
    }
}
