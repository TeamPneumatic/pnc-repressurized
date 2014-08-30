package pneumaticCraft.common.sensor.pollSensors.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.EntityPollSensor;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityInRangeSensor extends EntityPollSensor{

    @Override
    public String getSensorPath(){
        return super.getSensorPath() + "/Within Range";
    }

    @Override
    public boolean needsTextBox(){
        return true;
    }

    @Override
    public int getRedstoneValue(List<Entity> entities, String textboxText){
        int entitiesFound = 0;
        if(textboxText.equals("")) {
            return Math.min(15, entities.size());
        } else {
            for(Entity entity : entities) {
                if(PneumaticCraftUtils.isEntityValidForFilter(textboxText, entity)) entitiesFound++;
            }
        }
        return Math.min(15, entitiesFound);
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "Emits a redstone level for every entity within range. You can select a specific entity by filling in its name in the textbox. For instance for Creepers type 'Creeper', or for Player1 type 'Player1'. You can also select an entity type. If you want to detect mobs, you can type '@mob'. All selectable entity types are @mob, @animal, @living, @player, @item, @minecart.");
        return text;
    }

    @Override
    public Class getEntityTracked(){
        return Entity.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){
        fontRenderer.drawString("Entity filter", 195, 48, 4210752);
    }

    @Override
    public Rectangle needsSlot(){
        return null;
    }
}
