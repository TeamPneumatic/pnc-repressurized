/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package igwmod.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import igwmod.lib.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * @author MineMaarten
 */

public class NetworkHandler{

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);
    private static int discriminant;

    /*
     * The integer is the ID of the message, the Side is the side this message will be handled (received) on!
     */
    public static void init(){

        INSTANCE.registerMessage(MessageSendServerTab.class, MessageSendServerTab.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(MessageMultiHeader.class, MessageMultiHeader.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(MessageMultiPart.class, MessageMultiPart.class, discriminant++, Side.CLIENT);
    }

    /*
     * public static void INSTANCE.registerMessage(Class<? extends AbstractPacket<? extends IMessage>> clazz){ INSTANCE.registerMessage(clazz, clazz,
     * discriminant++, Side.SERVER, discriminant++, Side.SERVER); }
     */

    public static void sendToAll(IMessage message){

        INSTANCE.sendToAll(message);
    }

    public static void sendTo(IMessage message, EntityPlayerMP player){
        List<IMessage> messages = getSplitMessages(message);
        for(IMessage m : messages) {
            INSTANCE.sendTo(m, player);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void sendToAllAround(LocationIntPacket message, World world, double distance){

        sendToAllAround(message, message.getTargetPoint(world, distance));
    }

    @SuppressWarnings("rawtypes")
    public static void sendToAllAround(LocationIntPacket message, World world){

        sendToAllAround(message, message.getTargetPoint(world));
    }

    @SuppressWarnings("rawtypes")
    public static void sendToAllAround(LocationDoublePacket message, World world){

        sendToAllAround(message, message.getTargetPoint(world));
    }

    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point){

        INSTANCE.sendToAllAround(message, point);
    }

    public static void sendToDimension(IMessage message, int dimensionId){

        INSTANCE.sendToDimension(message, dimensionId);
    }

    public static void sendToServer(IMessage message){

        INSTANCE.sendToServer(message);
    }

    public static final int MAX_SIZE = 65530;

    private static List<IMessage> getSplitMessages(IMessage message){
        List<IMessage> messages = new ArrayList<IMessage>();
        ByteBuf buf = Unpooled.buffer();
        message.toBytes(buf);
        byte[] bytes = buf.array();
        if(bytes.length < MAX_SIZE) {
            messages.add(message);
        } else {
            messages.add(new MessageMultiHeader(bytes.length));
            int offset = 0;
            while(offset < bytes.length) {
                messages.add(new MessageMultiPart(Arrays.copyOfRange(bytes, offset, Math.min(offset + MAX_SIZE, bytes.length))));
                offset += MAX_SIZE;
            }
        }
        return messages;
    }
}
