package me.desht.pneumaticcraft.common.sensor.pollSensors.entity;

import me.desht.pneumaticcraft.common.util.EntityFilter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class EntityInRangeSensor extends EntityPollSensor {

    private EntityFilter filter;

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
        if (filter == null) {
            filter = new EntityFilter(textboxText);
        }

        int entitiesFound = 0;
        if (textboxText.equals("")) {
            return Math.min(15, entities.size());
        } else {
            for (Entity entity : entities) {
                if (filter.test(entity)) entitiesFound++;
            }
        }
        return Math.min(15, entitiesFound);
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.BLACK + "Emits a redstone level for every entity within range. You can select a specific entity by filling in its name in the textbox. Hold F1 to get detailed help on entity filter syntax.");
        return text;
    }

    @Override
    public Class getEntityTracked() {
        return Entity.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
        fontRenderer.drawString("Entity filter", 70, 48, 0x404040);
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }

    @Override
    public void notifyTextChange(String newText) {
        filter = new EntityFilter(newText);
    }
}
