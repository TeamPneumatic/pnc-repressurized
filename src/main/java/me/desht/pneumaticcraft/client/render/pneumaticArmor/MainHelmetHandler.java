package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.GuiHelmetMainOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MainHelmetHandler implements IUpgradeRenderHandler {
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
    public String getUpgradeName() {
        return "coreComponents";
    }

    @Override
    public void initConfig() {
        powerStatX = ConfigHandler.helmetOptions.powerX;
        powerStatY = ConfigHandler.helmetOptions.powerY;
        powerStatLeftSided = ConfigHandler.helmetOptions.powerLeft;
        messagesStatX = ConfigHandler.helmetOptions.messageX;
        messagesStatY = ConfigHandler.helmetOptions.messageY;
        messagesStatLeftSided = ConfigHandler.helmetOptions.messageLeft;

//        powerStatX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat X", -1).getInt();
//        powerStatY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat Y", 2).getInt();
//        powerStatLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat leftsided", true).getBoolean(true);
//        messagesStatX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat X", 2).getInt();
//        messagesStatY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat Y", 2).getInt();
//        messagesStatLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat leftsided", false).getBoolean(true);
    }

    @Override
    public void saveToConfig() {
//        Configuration config = ConfigHandler.config;
//        config.load();
        if (powerStat != null) {
            ConfigHandler.helmetOptions.powerX = powerStatX = powerStat.getBaseX();
            ConfigHandler.helmetOptions.powerY = powerStatY = powerStat.getBaseY();
            ConfigHandler.helmetOptions.powerLeft = powerStatLeftSided = powerStat.isLeftSided();
//            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat X", -1).set(powerStat.getBaseX());
//            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat Y", 2).set(powerStat.getBaseY());
//            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Power_Stat", "stat leftsided", true).set(powerStat.isLeftSided());
//            powerStatX = powerStat.getBaseX();
//            powerStatY = powerStat.getBaseY();
//            powerStatLeftSided = powerStat.isLeftSided();
        }
        if (testMessageStat != null) {
            ConfigHandler.helmetOptions.messageX = messagesStatX = testMessageStat.getBaseX();
            ConfigHandler.helmetOptions.messageY = messagesStatY = testMessageStat.getBaseY();
            ConfigHandler.helmetOptions.messageLeft = messagesStatLeftSided = testMessageStat.isLeftSided();
//            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat X", 2).set(testMessageStat.getBaseX());
//            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat Y", 2).set(testMessageStat.getBaseY());
//            config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Message_Stat", "stat leftsided", false).set(testMessageStat.isLeftSided());
//            messagesStatX = testMessageStat.getBaseX();
//            messagesStatY = testMessageStat.getBaseY();
//            messagesStatLeftSided = testMessageStat.isLeftSided();
        }

        ConfigHandler.sync();
//        config.save();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        powerStat.setTitle((CommonHUDHandler.getHandlerForPlayer(player).helmetPressure < 0.5F ? TextFormatting.RED : "") + "Helmet Pressure: " + Math.round(CommonHUDHandler.getHandlerForPlayer(player).helmetPressure * 10F) / 10F + " bar");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiAnimatedStat getAnimatedStat() {
        if (powerStat == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft);
            powerStat = new GuiAnimatedStat(null, "Helmet Pressure: ", "", powerStatX != -1 ? powerStatX : sr.getScaledWidth() - 2, powerStatY, 0x3000AA00, null, powerStatLeftSided);
            powerStat.setMinDimensionsAndReset(0, 0);
            powerStat.openWindow();
        }
        return powerStat;
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{};
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset() {
        powerStat = null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiHelmetMainOptions(this);
    }

}
