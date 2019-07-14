package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up current Amadron offer list when the tablet is opened
 */
public class PacketSyncAmadronOffers {
    private Collection<AmadronOffer> staticOffers = new ArrayList<>();
    private Collection<AmadronOffer> selectedPeriodicOffers = new ArrayList<>();
    private boolean mayAddPeriodic;
    private boolean mayAddStatic;

    @SuppressWarnings("unused")
    public PacketSyncAmadronOffers() {
    }

    public PacketSyncAmadronOffers(PlayerEntity playerIn) {
        this.staticOffers = AmadronOfferManager.getInstance().getStaticOffers();
        this.selectedPeriodicOffers = AmadronOfferManager.getInstance().getSelectedPeriodicOffers();
        this.mayAddPeriodic = PermissionAPI.hasPermission(playerIn, Names.AMADRON_ADD_PERIODIC_TRADE);
        this.mayAddStatic = PermissionAPI.hasPermission(playerIn, Names.AMADRON_ADD_STATIC_TRADE);
    }

    public PacketSyncAmadronOffers(PacketBuffer buf) {
        this.staticOffers = readOffers(buf);
        this.selectedPeriodicOffers = readOffers(buf);
        this.mayAddPeriodic = buf.readBoolean();
        this.mayAddStatic = buf.readBoolean();
    }

    public static Object readFluidOrItemStack(ByteBuf buf) {
        return readFluidOrItemStack(new PacketBuffer(buf));
    }

    public static Object readFluidOrItemStack(PacketBuffer buf) {
        if (buf.readByte() == 0) {
            return buf.readItemStack();
        } else {
            return FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
        }
    }

    public static void writeFluidOrItemStack(Object object, ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        if (object instanceof ItemStack) {
            pb.writeByte(0);
            pb.writeItemStack((ItemStack) object);
        } else {
            pb.writeByte(1);
            pb.writeCompoundTag(((FluidStack) object).writeToNBT(new CompoundNBT()));
        }
    }

    private Collection<AmadronOffer> readOffers(PacketBuffer buf) {
        int offerCount = buf.readInt();
        List<AmadronOffer> offers = new ArrayList<>();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                offers.add(AmadronOfferCustom.loadFromBuf(buf));
            } else {
                offers.add(new AmadronOffer(readFluidOrItemStack(buf), readFluidOrItemStack(buf)));
            }
        }
        return offers;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(staticOffers.size());
        for (AmadronOffer offer : staticOffers) {
            buf.writeBoolean(offer instanceof AmadronOfferCustom);
            offer.writeToBuf(buf);
        }
        buf.writeInt(selectedPeriodicOffers.size());
        for (AmadronOffer offer : selectedPeriodicOffers) {
            buf.writeBoolean(offer instanceof AmadronOfferCustom);
            offer.writeToBuf(buf);
        }
        buf.writeBoolean(mayAddPeriodic);
        buf.writeBoolean(mayAddStatic);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AmadronOfferManager.getInstance().syncOffers(staticOffers, selectedPeriodicOffers);
            ContainerAmadron.mayAddPeriodicOffers = mayAddPeriodic;
            ContainerAmadron.mayAddStaticOffers = mayAddStatic;
        });
        ctx.get().setPacketHandled(true);
    }

}
