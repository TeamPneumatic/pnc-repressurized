package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PacketSendArmorHUDMessage extends AbstractPacket<PacketSendArmorHUDMessage> {
    private String title;
    private int duration;
    private int color;
    private List<String> args;

    public PacketSendArmorHUDMessage() {
    }

    public PacketSendArmorHUDMessage(String title, int duration, String... args) {
        this(title, duration, 0x7000FF00);
    }

    public PacketSendArmorHUDMessage(String title, int duration, int color, String... args) {
        this.title = title;
        this.duration = duration;
        this.color = color;
        this.args = Arrays.asList(args);
    }

    @Override
    public void handleClientSide(PacketSendArmorHUDMessage message, EntityPlayer player) {
        String msg = I18n.format(message.title, message.args.toArray());
        HUDHandler.instance().addMessage(new ArmorMessage(msg, Collections.emptyList(), message.duration, message.color));
    }

    @Override
    public void handleServerSide(PacketSendArmorHUDMessage message, EntityPlayer player) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.title = ByteBufUtils.readUTF8String(buf);
        this.duration = buf.readInt();
        this.color = buf.readInt();
        this.args = new ArrayList<>();
        int n = buf.readByte();
        for (int i = 0; i < n; i++) {
            this.args.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.title);
        buf.writeInt(this.duration);
        buf.writeInt(this.color);
        buf.writeByte(this.args.size());
        this.args.forEach(s -> ByteBufUtils.writeUTF8String(buf, s));
    }
}
