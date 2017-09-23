package igwmod;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;

public class TextureSupplier{
    private static HashMap<String, ResourceLocation> textureMap = new HashMap<String, ResourceLocation>();

    public static ResourceLocation getTexture(String objectName){
        if(!textureMap.containsKey(objectName)) {
            textureMap.put(objectName, new ResourceLocation(objectName));
        }
        return textureMap.get(objectName);
    }

}
