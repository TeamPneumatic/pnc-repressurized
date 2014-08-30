package pneumaticCraft.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import pneumaticCraft.lib.Textures;

public class ItemPneumatic extends Item{
    private boolean hasTexture;

    public ItemPneumatic(){}

    public ItemPneumatic(String textureLocation){
        setTextureName(Textures.ICON_LOCATION + textureLocation);
        hasTexture = true;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister){
        if(hasTexture) super.registerIcons(par1IconRegister);
    }
}
