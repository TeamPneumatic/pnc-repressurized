package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketSyncAmadronOffers extends AbstractPacket<PacketSyncAmadronOffers> {
    private Collection<AmadronOffer> staticOffers = new ArrayList<>();
    private Collection<AmadronOffer> selectedPeriodicOffers = new ArrayList<>();
    private boolean mayAddPeriodic;
    private boolean mayAddStatic;

    @SuppressWarnings("unused")
    public PacketSyncAmadronOffers() {
    }

    public PacketSyncAmadronOffers(EntityPlayer playerIn) {
        this.staticOffers = AmadronOfferManager.getInstance().getStaticOffers();
        this.selectedPeriodicOffers = AmadronOfferManager.getInstance().getSelectedPeriodicOffers();
        this.mayAddPeriodic = PermissionAPI.hasPermission(playerIn, Names.AMADRON_ADD_PERIODIC_TRADE);
        this.mayAddStatic = PermissionAPI.hasPermission(playerIn, Names.AMADRON_ADD_STATIC_TRADE);
    }

    public static Object readFluidOrItemStack(ByteBuf buf) {
        if (buf.readByte() == 0) {
            return ByteBufUtils.readItemStack(buf);
        } else {
            return new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf));
        }
    }

    public static void writeFluidOrItemStack(Object object, ByteBuf buf) {
        if (object instanceof ItemStack) {
            buf.writeByte(0);
            ByteBufUtils.writeItemStack(buf, (ItemStack) object);
        } else {
            buf.writeByte(1);
            FluidStack stack = (FluidStack) object;
            ByteBufUtils.writeUTF8String(buf, stack.getFluid().getName());
            buf.writeInt(stack.amount);
            ByteBufUtils.writeTag(buf, stack.tag);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        staticOffers = readOffers(buf);
        selectedPeriodicOffers = readOffers(buf);
        mayAddPeriodic = buf.readBoolean();
        mayAddStatic = buf.readBoolean();
    }

    private Collection<AmadronOffer> readOffers(ByteBuf buf) {
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

    @Override
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

    @Override
    public void handleClientSide(PacketSyncAmadronOffers message, EntityPlayer player) {
        AmadronOfferManager.getInstance().syncOffers(message.staticOffers, message.selectedPeriodicOffers);
        ContainerAmadron.mayAddPeriodicOffers = message.mayAddPeriodic;
        ContainerAmadron.mayAddStaticOffers = message.mayAddStatic;
    }

    @Override
    public void handleServerSide(PacketSyncAmadronOffers message, EntityPlayer player) {

    }

}
