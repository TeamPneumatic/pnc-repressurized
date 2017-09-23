package igwmod.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import igwmod.IGWMod;
import igwmod.api.WikiRegistry;
import igwmod.gui.tabs.ServerWikiTab;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageSendServerTab extends AbstractPacket<MessageSendServerTab>{
    private File serverFolder;

    public MessageSendServerTab(){

    }

    public MessageSendServerTab(File serverFolder){
        this.serverFolder = serverFolder;
    }

    @Override
    public void toBytes(ByteBuf buf){
        File[] files = serverFolder.listFiles();
        buf.writeInt(files.length);
        for(File file : files) {
            try {
                ByteBufUtils.writeUTF8String(buf, file.getName());
                FileInputStream stream = new FileInputStream(file);
                byte[] byteArray = IOUtils.toByteArray(stream);
                buf.writeInt(byteArray.length);
                buf.writeBytes(byteArray);
                stream.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf){
        File folder = new File(IGWMod.proxy.getSaveLocation() + "\\igwmod\\");

        folder.mkdirs();
        try {
            FileUtils.deleteDirectory(folder);//clear the folder
        } catch(IOException e1) {
            e1.printStackTrace();
        }
        folder.mkdirs();

        int fileAmount = buf.readInt();
        for(int i = 0; i < fileAmount; i++) {
            try {
                File file = new File(folder.getAbsolutePath() + "\\" + ByteBufUtils.readUTF8String(buf));
                byte[] fileBytes = new byte[buf.readInt()];
                buf.readBytes(fileBytes);
                FileOutputStream stream = new FileOutputStream(file);
                IOUtils.write(fileBytes, stream);
                stream.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleClientSide(MessageSendServerTab message, EntityPlayer player){
        WikiRegistry.registerWikiTab(new ServerWikiTab());
    }

    @Override
    public void handleServerSide(MessageSendServerTab message, EntityPlayer player){
        // TODO Auto-generated method stub

    }

}
