package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem.FilterCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumSet;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by Processing Filter GUI to update the filter settings on a held Processing Filter item
 */
public record PacketSyncClassifyFilter(boolean matchAll, EnumSet<FilterCondition> conditions, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<PacketSyncClassifyFilter> TYPE = new Type<>(RL("sync_classify_filter"));

    private static final StreamCodec<FriendlyByteBuf, EnumSet<FilterCondition>> FILTER_SET = StreamCodec.of(
            (buf, filterSet) -> buf.writeEnumSet(filterSet, FilterCondition.class),
            buf -> buf.readEnumSet(FilterCondition.class)
    );

    public static final StreamCodec<FriendlyByteBuf, PacketSyncClassifyFilter> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketSyncClassifyFilter::matchAll,
            FILTER_SET, PacketSyncClassifyFilter::conditions,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketSyncClassifyFilter::hand,
            PacketSyncClassifyFilter::new
    );

    @Override
    public Type<PacketSyncClassifyFilter> type() {
        return TYPE;
    }

    public static void handle(PacketSyncClassifyFilter message, IPayloadContext ctx) {
        ItemStack stack = ctx.player().getItemInHand(message.hand());
        if (stack.getItem() instanceof ClassifyFilterItem) {
            new ClassifyFilterItem.FilterSettings(message.matchAll(), message.conditions().stream().toList()).save(stack);
        }
    }
}
