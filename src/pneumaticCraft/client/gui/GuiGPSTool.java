package pneumaticCraft.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketChangeGPSToolCoordinate;

public class GuiGPSTool extends GuiPneumaticScreenBase{

    private final WidgetTextFieldNumber[] textFields = new WidgetTextFieldNumber[3];
    private WidgetTextField variableField;
    private static final int TEXTFIELD_WIDTH = 40;
    private final ChunkPosition oldGPSLoc;
    private String oldVarName;
    private static final int[] BUTTON_ACTIONS = {-10, -1, 1, 10};

    public GuiGPSTool(ChunkPosition gpsLoc, String oldVarName){
        oldGPSLoc = gpsLoc;
        this.oldVarName = oldVarName;
    }

    @Override
    public void initGui(){
        super.initGui();
        int[] oldText = new int[3];
        if(textFields[0] == null) {
            oldText[0] = oldGPSLoc.chunkPosX;
            oldText[1] = oldGPSLoc.chunkPosY;
            oldText[2] = oldGPSLoc.chunkPosZ;
        } else {
            for(int i = 0; i < 3; i++)
                oldText[i] = textFields[i].getValue();
        }
        int xMiddle = width / 2;
        int yMiddle = height / 2;
        for(int i = 0; i < 3; i++) {
            textFields[i] = new WidgetTextFieldNumber(fontRendererObj, xMiddle - TEXTFIELD_WIDTH / 2, yMiddle - 27 + i * 22, TEXTFIELD_WIDTH, fontRendererObj.FONT_HEIGHT);
            textFields[i].setValue(oldText[i]);
            if(i == 1) {
                textFields[i].minValue = 0;
                textFields[i].maxValue = 255;
            }
            addWidget(textFields[i]);
        }

        for(int i = 0; i < 3; i++) {
            buttonList.add(new GuiButton(0 + i * 4, xMiddle - 49 - TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "-10"));
            buttonList.add(new GuiButton(1 + i * 4, xMiddle - 25 - TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "-1"));
            buttonList.add(new GuiButton(2 + i * 4, xMiddle + 3 + TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "+1"));
            buttonList.add(new GuiButton(3 + i * 4, xMiddle + 27 + TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20, "+10"));
        }

        if(variableField != null) oldVarName = variableField.getText();
        variableField = new WidgetTextField(fontRendererObj, xMiddle - 50, yMiddle + 60, 100, fontRendererObj.FONT_HEIGHT);
        variableField.setText(oldVarName);
        addWidget(variableField);

        String var = I18n.format("gui.progWidget.coordinate.variable");
        addLabel(var, xMiddle - 62 - fontRendererObj.getStringWidth(var), yMiddle + 61);
        addLabel("#", xMiddle - 60, yMiddle + 61);
    }

    @Override
    protected void actionPerformed(GuiButton button){
        textFields[button.id / 4].setValue(textFields[button.id / 4].getValue() + BUTTON_ACTIONS[button.id % 4]);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3){
        drawDefaultBackground();
        super.drawScreen(par1, par2, par3);

        int xMiddle = width / 2;
        int yMiddle = height / 2;
        int stringX = xMiddle - 60 - TEXTFIELD_WIDTH / 2;
        drawCenteredString(fontRendererObj, new ItemStack(Itemss.GPSTool).getDisplayName(), xMiddle, yMiddle - 44, 0xFFFFFFFF);
        drawString(fontRendererObj, "X:", stringX, yMiddle - 22 - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(fontRendererObj, "Y:", stringX, yMiddle - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(fontRendererObj, "Z:", stringX, yMiddle + 22 - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
    }

    @Override
    public void onGuiClosed(){
        ChunkPosition newPos = new ChunkPosition(textFields[0].getValue(), textFields[1].getValue(), textFields[2].getValue());
        NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(newPos.chunkPosX, newPos.equals(oldGPSLoc) ? -1 : newPos.chunkPosY, newPos.chunkPosZ, variableField.getText()));
    }

    @Override
    protected ResourceLocation getTexture(){
        return null;
    }
}
