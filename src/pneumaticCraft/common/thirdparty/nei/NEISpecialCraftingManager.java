package pneumaticCraft.common.thirdparty.nei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public abstract class NEISpecialCraftingManager extends PneumaticCraftPlugins{

    private ResourceLocation texture;
    private List<String> text;

    protected void setText(String localizationKey){
        text = PneumaticCraftUtils.convertStringIntoList(I18n.format(localizationKey), 30);
    }

    @Override
    public String getGuiTexture(){
        return Textures.GUI_NEI_MISC_RECIPES;
    }

    @Override
    public void drawBackground(int recipe){
        if(text != null) {
            for(int i = 0; i < text.size(); i++) {
                Minecraft.getMinecraft().fontRenderer.drawString(text.get(i), 5, 20 + i * 10, 0xFF000000);
            }
        }

        if(texture == null) texture = new ResourceLocation(getGuiTexture());
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        GL11.glColor4f(1, 1, 1, 1);
        Gui.func_146110_a(40, 79, 0, 0, 82, 18, 256, 256);
        drawProgressBar(63, 80, 82, 0, 38, 18, cycleticks % 48 / 48F, 0);
    }
}
