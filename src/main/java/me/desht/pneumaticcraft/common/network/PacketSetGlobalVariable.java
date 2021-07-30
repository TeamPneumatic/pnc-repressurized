package me.desht.pneumaticcraft.common.network;

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.client.gui.GuiRemote;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sync's global variable data between server and client. Used by the Remote and GPS Tool.
 * Note: variables are always sync'd with leading % or # for global or player-global, respectively
 */
public class PacketSetGlobalVariable {
    private final String varName;
    private final Either<BlockPos, ItemStack> value;

    public PacketSetGlobalVariable(String varName, BlockPos value) {
        Validate.isTrue(varName.startsWith("#") || varName.startsWith("%"));
        this.value = Either.left(value);
        this.varName = varName;
    }

    public PacketSetGlobalVariable(String varName, @Nonnull ItemStack stack) {
        Validate.isTrue(varName.startsWith("#") || varName.startsWith("%"));
        this.value = Either.right(stack);
        this.varName = varName;
    }

    public PacketSetGlobalVariable(String varName, int value) {
        this(varName, new BlockPos(value, 0, 0));
    }

    public PacketSetGlobalVariable(String varName, boolean value) {
        this(varName, value ? 1 : 0);
    }

    public PacketSetGlobalVariable(PacketBuffer buf) {
        if (buf.readBoolean()) {
            this.value = Either.left(buf.readBoolean() ? buf.readBlockPos() : null);
        } else {
            this.value = Either.right(buf.readItemStack());
        }
        this.varName = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    public void toBytes(PacketBuffer buf) {
        value.ifLeft(pos -> {
            buf.writeBoolean(true);
            buf.writeBoolean(pos != null);
            if (pos != null) buf.writeBlockPos(pos);
        }).ifRight(stack -> {
            buf.writeBoolean(false);
            buf.writeItemStack(stack);
        });
        buf.writeString(varName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity p = ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ? ClientUtils.getClientPlayer() : ctx.get().getSender();
            value.ifLeft(pos -> GlobalVariableHelper.setPos(p.getUniqueID(), varName, pos))
                    .ifRight(stack -> GlobalVariableHelper.setStack(p.getUniqueID(), varName, stack));
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                GuiRemote.maybeHandleVariableChange(varName);
                AreaRenderManager.getInstance().clearPosProviderCache();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
