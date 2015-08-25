package pneumaticCraft.client.render.pneumaticArmor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.gui.pneumaticHelmet.GuiHelmetMainOptions;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.config.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MainHelmetHandler implements IUpgradeRenderHandler{
    private GuiAnimatedStat powerStat;
    public GuiAnimatedStat testMessageStat;

    private int powerStatX;
    private int powerStatY;
    private boolean powerStatLeftSided;
    public int messagesStatX;
    public int messagesStatY;
    public boolean messagesStatLeftSided;

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName(){
        return "coreComponents";
    }

    @Override
    public void initConfig(Configuration config){
        powerStatX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat X", -1).getInt();
        powerStatY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat Y", 2).getInt();
        powerStatLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat leftsided", true).getBoolean(true);
        messagesStatX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat X", 2).getInt();
        messagesStatY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat Y", 2).getInt();
        messagesStatLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat leftsided", false).getBoolean(true);
    }

    @Override
    public void saveToConfig(){
        Configuration config = Config.config;
        config.load();
        if(powerStat != null) {
            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat X", -1).set(powerStat.getBaseX());
            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat Y", 2).set(powerStat.getBaseY());
            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat leftsided", true).set(powerStat.isLeftSided());
            powerStatX = powerStat.getBaseX();
            powerStatY = powerStat.getBaseY();
            powerStatLeftSided = powerStat.isLeftSided();
        }
        if(testMessageStat != null) {
            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat X", 2).set(testMessageStat.getBaseX());
            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat Y", 2).set(testMessageStat.getBaseY());
            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat leftsided", false).set(testMessageStat.isLeftSided());
            messagesStatX = testMessageStat.getBaseX();
            messagesStatY = testMessageStat.getBaseY();
            messagesStatLeftSided = testMessageStat.isLeftSided();
            messagesStatX = testMessageStat.getBaseX();
            messagesStatY = testMessageStat.getBaseY();
            messagesStatLeftSided = testMessageStat.isLeftSided();
        }
        config.save();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades){
        powerStat.setTitle((CommonHUDHandler.getHandlerForPlayer(player).helmetPressure < 0.5F ? EnumChatFormatting.RED : "") + "Helmet Pressure: " + Math.round(CommonHUDHandler.getHandlerForPlayer(player).helmetPressure * 10F) / 10F + " bar");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks){}

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled){}

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiAnimatedStat getAnimatedStat(){
        if(powerStat == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
            powerStat = new GuiAnimatedStat(null, "Helmet Pressure: ", "", powerStatX != -1 ? powerStatX : sr.getScaledWidth() - 2, powerStatY, 0x3000AA00, null, powerStatLeftSided);
            powerStat.setMinDimensionsAndReset(0, 0);
            powerStat.openWindow();
        }
        return powerStat;
    }

    @Override
    public boolean isEnabled(ItemStack[] upgradeStacks){
        return true;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player){
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset(){
        powerStat = null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage(){
        return new GuiHelmetMainOptions(this);
    }

}
