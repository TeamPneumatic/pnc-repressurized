package pneumaticCraft.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;

import org.apache.commons.lang3.math.NumberUtils;

import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketChangeGPSToolCoordinate;

public class GuiGPSTool extends GuiScreen{

    private final GuiTextField[] textFields = new GuiTextField[3];
    private static final int TEXTFIELD_WIDTH = 40;
    private final ChunkPosition oldGPSLoc;

    public GuiGPSTool(ChunkPosition gpsLoc){
        oldGPSLoc = gpsLoc;
    }

    @Override
    public void initGui(){
        String[] oldText = new String[3];
        if(textFields[0] == null) {
            oldText[0] = oldGPSLoc.chunkPosX + "";
            oldText[1] = oldGPSLoc.chunkPosY + "";
            oldText[2] = oldGPSLoc.chunkPosZ + "";
        } else {
            for(int i = 0; i < 3; i++)
                oldText[i] = textFields[i].getText();
        }
        int xMiddle = width / 2;
        int yMiddle = height / 2;
        for(int i = 0; i < 3; i++) {
            textFields[i] = new GuiTextField(fontRendererObj, xMiddle - TEXTFIELD_WIDTH / 2, yMiddle - 27 + i * 22, TEXTFIELD_WIDTH, fontRendererObj.FONT_HEIGHT);
            textFields[i].setText(oldText[i]);
        }

        for(int i = 0; i < 3; i++) {
            buttonList.add(new GuiButton(0 + i * 4, xMiddle - 49 - TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "-10"));
            buttonList.add(new GuiButton(1 + i * 4, xMiddle - 25 - TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "-1"));
            buttonList.add(new GuiButton(2 + i * 4, xMiddle + 3 + TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "+1"));
            buttonList.add(new GuiButton(3 + i * 4, xMiddle + 27 + TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "+10"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button){
        int index = button.id / 4;
        int curValue = Integer.parseInt(textFields[index].getText());
        switch(button.id - index * 4){
            case 0:
                curValue -= 10;
                break;
            case 1:
                curValue--;
                break;
            case 2:
                curValue++;
                break;
            case 3:
                curValue += 10;
                break;
        }
        if(index == 1) {
            curValue = MathHelper.clamp_int(curValue, 0, 255);
        }
        textFields[index].setText(curValue + "");
    }

    @Override
    public void drawScreen(int par1, int par2, float par3){
        drawDefaultBackground();
        super.drawScreen(par1, par2, par3);
        for(GuiTextField field : textFields)
            field.drawTextBox();

        int xMiddle = width / 2;
        int yMiddle = height / 2;
        int stringX = xMiddle - 60 - TEXTFIELD_WIDTH / 2;
        drawCenteredString(fontRendererObj, "GPS Tool", xMiddle, yMiddle - 44, 0xFFFFFFFF);
        drawString(fontRendererObj, "X:", stringX, yMiddle - 22 - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(fontRendererObj, "Y:", stringX, yMiddle - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(fontRendererObj, "Z:", stringX, yMiddle + 22 - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
    }

    @Override
    protected void keyTyped(char par1, int par2){
        super.keyTyped(par1, par2);
        for(GuiTextField field : textFields) {
            String oldText = field.getText();
            field.textboxKeyTyped(par1, par2);
            if(!field.getText().equals("") && !field.getText().equals("-") && !NumberUtils.isNumber(field.getText()) || field.getText().contains(".")) {
                field.setText(oldText);
            }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        for(GuiTextField field : textFields) {
            boolean focused = field.isFocused();
            field.mouseClicked(par1, par2, par3);
            if(!field.isFocused() && focused && !NumberUtils.isNumber(field.getText())) {
                field.setText("0");
            }
        }
        super.mouseClicked(par1, par2, par3);
    }

    @Override
    public void onGuiClosed(){
        int x = NumberUtils.isNumber(textFields[0].getText()) ? Integer.parseInt(textFields[0].getText()) : 0;
        int y = NumberUtils.isNumber(textFields[1].getText()) ? Integer.parseInt(textFields[1].getText()) : 0;
        int z = NumberUtils.isNumber(textFields[2].getText()) ? Integer.parseInt(textFields[2].getText()) : 0;
        if(oldGPSLoc.chunkPosX != x || oldGPSLoc.chunkPosY != y || oldGPSLoc.chunkPosZ != z) {
            NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(x, y, z));
        }
    }
}
