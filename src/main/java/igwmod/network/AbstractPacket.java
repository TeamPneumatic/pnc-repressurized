package igwmod.network;

import igwmod.IGWMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * @author MineMaarten
 */
public abstract class AbstractPacket<REQ extends IMessage> implements IMessage, IMessageHandler<REQ, REQ>{

    @Override
    public REQ onMessage(REQ message, MessageContext ctx){

        if(ctx.side == Side.SERVER) {
            handleServerSide(message, ctx.getServerHandler().player);
        } else {
            handleClientSide(message, IGWMod.proxy.getPlayer());
        }
        return null;
    }

    /**
     * Handle a packet on the client side.
     * 
     * @param message
     *            TODO
     * @param player
     *            the player reference
     */
    public abstract void handleClientSide(REQ message, EntityPlayer player);

    /**
     * Handle a packet on the server side.
     * 
     * @param message
     *            TODO
     * @param player
     *            the player reference
     */
    public abstract void handleServerSide(REQ message, EntityPlayer player);
}
