package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.AmadronOfferCustom;
import pneumaticCraft.common.recipes.AmadronOfferManager;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketSyncAmadronOffers extends AbstractPacket<PacketSyncAmadronOffers>{
    private Collection<AmadronOffer> offers = new ArrayList<AmadronOffer>();

    public PacketSyncAmadronOffers(){}

    public PacketSyncAmadronOffers(Collection<AmadronOffer> offers){
        this.offers = offers;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        int offerCount = buf.readInt();
        for(int i = 0; i < offerCount; i++) {
            if(buf.readBoolean()) {
                offers.add(AmadronOfferCustom.loadFromBuf(buf));
            } else {
                offers.add(new AmadronOffer(getFluidOrItemStack(buf), getFluidOrItemStack(buf)));
            }
        }
    }

    public static Object getFluidOrItemStack(ByteBuf buf){
        if(buf.readByte() == 0) {
            return ByteBufUtils.readItemStack(buf);
        } else {
            return new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(offers.size());
        for(AmadronOffer offer : offers) {
            buf.writeBoolean(offer instanceof AmadronOfferCustom);
            writeFluidOrItemStack(offer.getInput(), buf);
            writeFluidOrItemStack(offer.getOutput(), buf);
            if(offer instanceof AmadronOfferCustom) {
                ((AmadronOfferCustom)offer).writeToBuf(buf);
            }
        }
    }

    public static void writeFluidOrItemStack(Object object, ByteBuf buf){
        if(object instanceof ItemStack) {
            buf.writeByte(0);
            ByteBufUtils.writeItemStack(buf, (ItemStack)object);
        } else {
            buf.writeByte(1);
            FluidStack stack = (FluidStack)object;
            ByteBufUtils.writeUTF8String(buf, stack.getFluid().getName());
            buf.writeInt(stack.amount);
            ByteBufUtils.writeTag(buf, stack.tag);
        }
    }

    @Override
    public void handleClientSide(PacketSyncAmadronOffers message, EntityPlayer player){
        AmadronOfferManager.getInstance().setOffers(message.offers);
    }

    @Override
    public void handleServerSide(PacketSyncAmadronOffers message, EntityPlayer player){

    }

}
