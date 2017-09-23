package igwmod.api;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * Event fired on MinecraftForge.EVENT_BUS when the IGW GUI gets opened by looking at an entity and pressing 'i'. You could change the entity displayed in the 
 * top left corner by setting the 'entity' field. If you want the default entity, don't change it.
 * When you don't change pageOpened, it will default to assets/igwmod/wiki/entity/<Entity.getEntityString(entity)>
 * For info about the pageOpened field, look at {@link BlockWikiEvent}.
 */

public class EntityWikiEvent extends EntityEvent{
    public String pageOpened; //current page this gui will go to. It contains the default location, but can be changed.

    public EntityWikiEvent(Entity entity){
        super(entity);
    }

}
