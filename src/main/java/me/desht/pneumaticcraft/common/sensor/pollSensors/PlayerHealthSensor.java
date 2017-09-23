package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.universalSensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerHealthSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "Player/Player Health";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(Itemss.upgrades.get(EnumUpgrade.ENTITY_TRACKER));
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public void drawAdditionalInfo(FontRenderer fontRenderer) {

    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<String>();
        text.add("gui.universalSensor.desc.playerHealth");
        return text;
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 10;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(textBoxText);
        if (player != null) {
            return (int) (15 * player.getHealth() / player.getMaxHealth());
        } else {
            return 0;
        }
    }

}
