package me.desht.pneumaticcraft.common.sensor.pollSensors.entity;

import me.desht.pneumaticcraft.api.universalSensor.EntityPollSensor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class EntityInRangeSensor extends EntityPollSensor {

    @Override
    public String getSensorPath() {
        return "Within Range";
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public int getRedstoneValue(List<Entity> entities, String textboxText) {
        int entitiesFound = 0;
        if (textboxText.equals("")) {
            return Math.min(15, entities.size());
        } else {
            for (Entity entity : entities) {
                if (PneumaticCraftUtils.isEntityValidForFilter(textboxText, entity)) entitiesFound++;
            }
        }
        return Math.min(15, entitiesFound);
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.BLACK + "Emits a redstone level for every entity within range. You can select a specific entity by filling in its name in the textbox. For instance for Creepers type 'Creeper', or for Player1 type 'Player1'. You can also select an entity type. If you want to detect mobs, you can type '@mob'. All selectable entity types are @mob, @animal, @living, @player, @item, @minecart.");
        return text;
    }

    @Override
    public Class getEntityTracked() {
        return Entity.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
        fontRenderer.drawString("Entity filter", 195, 48, 4210752);
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }
}
