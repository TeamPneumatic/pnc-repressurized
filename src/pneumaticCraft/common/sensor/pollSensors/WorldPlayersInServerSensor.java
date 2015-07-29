package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldPlayersInServerSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "dispenser/World/Players in server";
    }

    @Override
    public boolean needsTextBox(){
        return true;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "Emits a redstone level for every player logged into the server.");
        text.add(EnumChatFormatting.BLACK + "When you fill in a specific player name, the Universal Sensor will emit a redstone signal of 15 if the player is online and 0 otherwise.");
        return text;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
        ServerConfigurationManager serverManager = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager();
        if(textBoxText.equals("")) {
            return Math.min(15, serverManager.playerEntityList.size());
        } else {
            String[] userNames = serverManager.getAllUsernames();
            for(String userName : userNames) {
                if(userName.equalsIgnoreCase(textBoxText)) return 15;
            }
            return 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }
}
