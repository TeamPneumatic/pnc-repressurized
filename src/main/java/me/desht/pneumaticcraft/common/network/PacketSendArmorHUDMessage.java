package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to get a message displayed on the Pneumatic Armor HUD
 */
public class PacketSendArmorHUDMessage {
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

    PacketSendArmorHUDMessage(PacketBuffer buffer) {
        this.title = buffer.readString();
        this.duration = buffer.readInt();
        this.color = buffer.readInt();
        this.args = new ArrayList<>();
        int n = buffer.readByte();
        for (int i = 0; i < n; i++) {
            this.args.add(buffer.readString());
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.title);
        buf.writeInt(this.duration);
        buf.writeInt(this.color);
        buf.writeByte(this.args.size());
        this.args.forEach(buf::writeString);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            String msg = I18n.format(title, args.toArray());
            HUDHandler.instance().addMessage(new ArmorMessage(msg, Collections.emptyList(), duration, color));
        });
        ctx.get().setPacketHandled(true);
    }
}
