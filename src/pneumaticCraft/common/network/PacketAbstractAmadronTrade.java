package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import pneumaticCraft.common.recipes.AmadronOfferCustom;

public abstract class PacketAbstractAmadronTrade<REQ extends PacketAbstractAmadronTrade> extends AbstractPacket<REQ>{
    private AmadronOfferCustom offer;

    public PacketAbstractAmadronTrade(){}

    public PacketAbstractAmadronTrade(AmadronOfferCustom offer){
        this.offer = offer;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        offer = AmadronOfferCustom.loadFromBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf){
        PacketSyncAmadronOffers.writeFluidOrItemStack(offer.getInput(), buf);
        PacketSyncAmadronOffers.writeFluidOrItemStack(offer.getOutput(), buf);
        offer.writeToBuf(buf);
    }

    public AmadronOfferCustom getOffer(){
        return offer;
    }

}
