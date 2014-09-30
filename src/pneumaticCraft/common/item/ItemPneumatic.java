package pneumaticCraft.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.lib.Textures;

public class ItemPneumatic extends Item{
    private boolean hasTexture;

    public ItemPneumatic(){
        setCreativeTab(PneumaticCraft.tabPneumaticCraft);
    }

    public ItemPneumatic(String textureLocation){
        this();
        setTextureName(Textures.ICON_LOCATION + textureLocation);
        setUnlocalizedName(textureLocation);
    }

    @Override
    public Item setTextureName(String p_111206_1_){
        super.setTextureName(p_111206_1_);
        hasTexture = true;
        return this;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister){
        if(hasTexture) super.registerIcons(par1IconRegister);
    }
}
