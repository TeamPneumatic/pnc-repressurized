package net.minecraft.entity;

import net.minecraft.item.Item;

public class EntityUtils{
    /**
     * Method placed here so that we get access to this method, because it's protected.
     * @param entity
     * @return
     */
    public static Item getLivingDrop(EntityLiving entity){
        return entity.getDropItem();
    }
}
