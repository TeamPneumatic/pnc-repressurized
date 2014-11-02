package pneumaticCraft.client.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.Charsets;
import org.lwjgl.input.Keyboard;

import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketAphorismTileUpdate;
import pneumaticCraft.common.thirdparty.DramaSplash;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;

public class GuiAphorismTile extends GuiScreen{
    public final TileEntityAphorismTile tile;
    private final List<String> textLines;
    public int cursorY;
    public int cursorX;
    public int updateCounter;
    private static Random rand = new Random();
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");

    public GuiAphorismTile(TileEntityAphorismTile tile){
        this.tile = tile;
        textLines = tile.getTextLines();
        if(textLines.size() == 1 && textLines.get(0).equals("")) {
            textLines.set(0, getSplashText());
        }
        NetworkHandler.sendToServer(new PacketAphorismTileUpdate(tile));
    }

    @Override
    public void updateScreen(){
        updateCounter++;
    }

    @Override
    protected void keyTyped(char par1, int par2){
        if(par2 == 1) {
            NetworkHandler.sendToServer(new PacketAphorismTileUpdate(tile));
        } else if(par2 == 200) {
            cursorY--;
            if(cursorY < 0) cursorY = textLines.size() - 1;
        } else if(par2 == 208 || par2 == 156) {
            cursorY++;
            if(cursorY >= textLines.size()) cursorY = 0;
        } else if(par2 == 28) {
            cursorY++;
            textLines.add(cursorY, "");
        } else if(par2 == 14) {
            if(textLines.get(cursorY).length() > 0) {
                textLines.set(cursorY, textLines.get(cursorY).substring(0, textLines.get(cursorY).length() - 1));
            } else if(textLines.size() > 1) {
                textLines.remove(cursorY);
                cursorY--;
                if(cursorY < 0) cursorY = 0;
            }
        } else if(ChatAllowedCharacters.isAllowedCharacter(par1)) {
            textLines.set(cursorY, textLines.get(cursorY) + par1);
        }

        super.keyTyped(par1, par2);
    }

    @Override
    public void initGui(){
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed(){
        Keyboard.enableRepeatEvents(false);
    }

    private String getSplashText(){
        String splashText = DramaSplash.cachedLine;
        if(splashText == null) {
            splashText = "";
            try {
                String s;
                ArrayList arraylist = new ArrayList();
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));

                while((s = bufferedreader.readLine()) != null) {
                    s = s.trim();

                    if(!s.isEmpty()) {
                        arraylist.add(s);
                    }
                }

                do {
                    splashText = (String)arraylist.get(rand.nextInt(arraylist.size()));
                } while(splashText.hashCode() == 125780783);
            } catch(IOException ioexception) {

            }
        } else {
            DramaSplash.newDrama();
        }
        return splashText;
    }
}
