package igwmod.lib;

import java.net.URI;
import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Util{
    public static Entity getEntityForClass(Class<? extends Entity> entityClass){
        try {
            return entityClass.getConstructor(World.class).newInstance(FMLClientHandler.instance().getClient().world);
        } catch(Exception e) {
            IGWLog.error("[LocatedEntity.java] An entity class doesn't have a constructor with a single World parameter! Entity = " + entityClass.getName());
            e.printStackTrace();
            return null;
        }
    }

    private static HashMap<String, ModContainer> entityNames;
    private static boolean reflectionFailed;

    public static String getModIdForEntity(Class<? extends Entity> entity){
        if(reflectionFailed) return "minecraft";
        if(entityNames == null) {
            try {
                entityNames = (HashMap<String, ModContainer>)ReflectionHelper.findField(EntityRegistry.class, "entityNames").get(EntityRegistry.instance());
            } catch(Exception e) {
                IGWLog.warning("IGW-Mod failed to perform reflection! A result of this is that wiki pages related to Entities will not be found. Report to MineMaarten please!");
                e.printStackTrace();
                reflectionFailed = true;
                return "minecraft";
            }
        }
        EntityRegistration entityReg = EntityRegistry.instance().lookupModSpawn(entity, true);
        if(entityReg == null) return "minecraft";
        ModContainer mod = entityNames.get(entityReg.getEntityName());
        if(mod == null) {
            IGWLog.info("Couldn't find the owning mod of the entity " + entityReg.getEntityName() + " even though it's registered through the EntityRegistry!");
            return "minecraft";
        } else {
            return mod.getModId().toLowerCase();
        }
    }

    public static void openBrowser(String url){
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new URI(url));
        } catch(Throwable throwable) {
            IGWLog.error("Couldn\'t open link");
            throwable.printStackTrace();
        }
    }
}
