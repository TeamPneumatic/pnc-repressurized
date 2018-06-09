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
    }

    @Override
    public void saveToConfig() {
        if (powerStat != null) {
            ConfigHandler.helmetOptions.powerX = powerStatX = powerStat.getBaseX();
            ConfigHandler.helmetOptions.powerY = powerStatY = powerStat.getBaseY();
            ConfigHandler.helmetOptions.powerLeft = powerStatLeftSided = powerStat.isLeftSided();
        }
        if (testMessageStat != null) {
            ConfigHandler.helmetOptions.messageX = messagesStatX = testMessageStat.getBaseX();
            ConfigHandler.helmetOptions.messageY = messagesStatY = testMessageStat.getBaseY();
            ConfigHandler.helmetOptions.messageLeft = messagesStatLeftSided = testMessageStat.isLeftSided();
        }

        ConfigHandler.sync();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        TextFormatting colour;
        float pressure = CommonHUDHandler.getHandlerForPlayer(player).helmetPressure;
        if (pressure < 0.5F) {
            colour = TextFormatting.RED;
        } else if (pressure < 2.0F) {
            colour = TextFormatting.GOLD;
        } else if (pressure < 4.0F) {
            colour = TextFormatting.YELLOW;
        } else {
            colour = TextFormatting.GREEN;
        }
        powerStat.setTitle(TextFormatting.WHITE + "Helmet Pressure: " + colour + Math.round(CommonHUDHandler.getHandlerForPlayer(player).helmetPressure * 10F) / 10F + " bar");
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

    @Override
    public float getMinimumPressure() {
        // pressure display always shows, even when empty
        return -1.0f;
    }
}
