package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.Collection;

public class PacketSyncAmadronOffers extends AbstractPacket<PacketSyncAmadronOffers> {
    private Collection<AmadronOffer> offers = new ArrayList<>();
    boolean mayAddPeriodic;

    @SuppressWarnings("unused")
    public PacketSyncAmadronOffers() {
    }

    public PacketSyncAmadronOffers(Collection<AmadronOffer> offers, boolean mayAddPeriodic) {
        this.offers = offers;
        this.mayAddPeriodic = mayAddPeriodic;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int offerCount = buf.readInt();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                offers.add(AmadronOfferCustom.loadFromBuf(buf));
            } else {
                offers.add(new AmadronOffer(getFluidOrItemStack(buf), getFluidOrItemStack(buf)));
            }
        }
        mayAddPeriodic = buf.readBoolean();
    }

    public static Object getFluidOrItemStack(ByteBuf buf) {
        if (buf.readByte() == 0) {
            return ByteBufUtils.readItemStack(buf);
        } else {
            return new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(offers.size());
        for (AmadronOffer offer : offers) {
            buf.writeBoolean(offer instanceof AmadronOfferCustom);
            writeFluidOrItemStack(offer.getInput(), buf);
            writeFluidOrItemStack(offer.getOutput(), buf);
            if (offer instanceof AmadronOfferCustom) {
                ((AmadronOfferCustom) offer).writeToBuf(buf);
            }
        }
        buf.writeBoolean(mayAddPeriodic);
    }

    static void writeFluidOrItemStack(Object object, ByteBuf buf) {
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
    public void handleClientSide(PacketSyncAmadronOffers message, EntityPlayer player) {
        AmadronOfferManager.getInstance().setOffers(message.offers);
        ContainerAmadron.mayAddPeriodicOffers = mayAddPeriodic;
    }

    @Override
    public void handleServerSide(PacketSyncAmadronOffers message, EntityPlayer player) {

    }

}
