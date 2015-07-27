package pneumaticCraft.client.gui.tubemodule;

import java.awt.Rectangle;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTooltipArea;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.block.tubes.TubeModuleRedstoneReceiving;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdatePressureModule;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiPressureModule extends GuiTubeModule{

    private GuiTextField lowerBoundField;
    private GuiTextField higherBoundField;
    private GuiCheckBox advancedMode;
    private int graphLowY;
    private int graphHighY;
    private int graphLeft;
    private int graphRight;
    private boolean waitTillRelease = true;

    public GuiPressureModule(EntityPlayer player, int x, int y, int z){
        super(player, x, y, z);
        ySize = 191;
    }

    public GuiPressureModule(TubeModule module){
        super(module);
        ySize = 191;
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        addLabel("lower", guiLeft + 10, guiTop + 30);
        addLabel("bar", guiLeft + 45, guiTop + 42);
        addLabel("higher", guiLeft + 140, guiTop + 30);

        String title = I18n.format("item." + module.getType() + ".name");
        addLabel(title, width / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 5);

        lowerBoundField = new GuiTextField(fontRendererObj, xStart + 10, yStart + 41, 30, 10);
        lowerBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
        higherBoundField = new GuiTextField(fontRendererObj, xStart + 140, yStart + 41, 30, 10);
        higherBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));

        graphLowY = guiTop + 153;
        graphHighY = guiTop + 93;
        graphLeft = guiLeft + 22;
        graphRight = guiLeft + 172;
        addWidget(new WidgetTooltipArea(graphLeft - 20, graphHighY, 25, graphLowY - graphHighY, "gui.redstone"));
        addWidget(new WidgetTooltipArea(graphLeft, graphLowY - 5, graphRight - graphLeft, 25, "gui.threshold"));
        addWidget((IGuiWidget)new GuiAnimatedStat(this, "gui.tab.info", Textures.GUI_INFO_LOCATION, xStart, yStart + 5, 0xFF8888FF, null, true).setText("gui.tab.info.tubeModule"));
        advancedMode = new GuiCheckBox(0, guiLeft + 6, guiTop + 15, 0xFF000000, "gui.tubeModule.advancedConfig").setTooltip(I18n.format("gui.tubeModule.advancedConfig.tooltip"));
        advancedMode.checked = true;
        addWidget(advancedMode);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_TUBE_MODULE;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_LIGHTING);
        if(!lowerBoundField.isFocused()) lowerBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
        if(!higherBoundField.isFocused()) higherBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
        int scrollbarLowerBoundX = (int)(guiLeft + 16 + (158 - 11) * (module.lowerBound / (module.maxValue + 1)));
        int scrollbarHigherBoundX = (int)(guiLeft + 16 + (158 - 11) * (module.higherBound / (module.maxValue + 1)));

        if(!Mouse.isButtonDown(0)) waitTillRelease = false;
        if(!waitTillRelease && Mouse.isButtonDown(0)) {
            if(new Rectangle(guiLeft + 11, guiTop + 59, 158, 15).contains(mouseX, mouseY)) {
                module.higherBound = (float)(mouseX - 6 - (guiLeft + 11)) / (158 - 11) * module.maxValue;
                if(module.higherBound < -1) module.higherBound = -1;
                if(module.higherBound > module.maxValue) module.higherBound = module.maxValue;
                //higherBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));
                NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 1, module.higherBound));
            } else if(new Rectangle(guiLeft + 11, guiTop + 73, 158, 15).contains(mouseX, mouseY)) {
                module.lowerBound = (float)(mouseX - 6 - (guiLeft + 11)) / (158 - 11) * module.maxValue;
                if(module.lowerBound < -1) module.lowerBound = -1;
                if(module.lowerBound > module.maxValue) module.lowerBound = module.maxValue;
                // lowerBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
                NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 0, module.lowerBound));
            }
        }

        drawTexturedModalRect(scrollbarLowerBoundX, guiTop + 73, 183, 0, 15, 12);
        drawTexturedModalRect(scrollbarHigherBoundX, guiTop + 59, 183, 0, 15, 12);

        lowerBoundField.drawTextBox();
        higherBoundField.drawTextBox();

        /*
         * Draw graph
         */

        drawVerticalLine(graphLeft, graphHighY, graphLowY, 0xFF000000);
        for(int i = 0; i < 16; i++) {
            boolean longer = i % 5 == 0;
            if(longer) {
                fontRendererObj.drawString(i + "", graphLeft - 5 - fontRendererObj.getStringWidth(i + ""), graphHighY + (graphLowY - graphHighY) * (15 - i) / 15 - 3, 0xFF000000);
                drawHorizontalLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, i == 0 ? 0xFF000000 : 0x33000000);

            }
            drawHorizontalLine(graphLeft - (longer ? 5 : 3), graphLeft + 3, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, 0xFF000000);
        }
        for(int i = 0; i < 31; i++) {
            boolean longer = i % 5 == 0;
            if(longer) {
                fontRendererObj.drawString(i + "", graphLeft + (graphRight - graphLeft) * i / 30 - fontRendererObj.getStringWidth(i + "") / 2 + 1, graphLowY + 6, 0xFF000000);
                drawVerticalLine(graphLeft + (graphRight - graphLeft) * i / 30, graphHighY, graphLowY - 2, 0x33000000);
            }
            drawVerticalLine(graphLeft + (graphRight - graphLeft) * i / 30, graphLowY - 3, graphLowY + (longer ? 5 : 3), 0xFF000000);
        }

        /*
         * Draw the current redstone strength
         */

        if(module instanceof TubeModuleRedstoneReceiving) {
            ((TubeModuleRedstoneReceiving)module).onNeighborBlockUpdate();
            drawHorizontalLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - ((TubeModuleRedstoneReceiving)module).getReceivingRedstoneLevel()) / 15, 0xFFFF0000);
            String status = "Current threshold: " + PneumaticCraftUtils.roundNumberTo(((TubeModuleRedstoneReceiving)module).getThreshold(), 1) + " bar";
            fontRendererObj.drawString(status, guiLeft + xSize / 2 - fontRendererObj.getStringWidth(status) / 2, guiTop + 173, 0xFF000000);
        }

        /*
         * Draw the data in the graph 
         */
        double zLevel = 90;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_LINE_STRIP);
        t.setColorOpaque_I(0);
        for(int i = 0; i < 16; i++) {
            double y = graphHighY + (graphLowY - graphHighY) * (15 - i) / 15;
            double x = graphLeft + (graphRight - graphLeft) * module.getThreshold(i) / 30;
            t.addVertex(x, y, zLevel);
        }
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        boolean wasFocused = lowerBoundField.isFocused();
        lowerBoundField.mouseClicked(par1, par2, par3);
        if(wasFocused && !lowerBoundField.isFocused()) {
            try {
                module.lowerBound = Float.parseFloat(lowerBoundField.getText());
                if(module.lowerBound < -1) module.lowerBound = -1;
                if(module.lowerBound > 30) module.lowerBound = 30;
                //   lowerBoundField.setText(module.lowerBound + "");
                NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 0, module.lowerBound));
            } catch(Exception e) {}
        }

        wasFocused = higherBoundField.isFocused();
        higherBoundField.mouseClicked(par1, par2, par3);
        if(wasFocused && !higherBoundField.isFocused()) {
            try {
                module.higherBound = Float.parseFloat(higherBoundField.getText());
                if(module.higherBound < -1) module.higherBound = -1;
                if(module.higherBound > 30) module.higherBound = 30;
                //  higherBoundField.setText(module.higherBound + "");
                NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 1, module.higherBound));
            } catch(Exception e) {}
        }
    }

    @Override
    protected void keyTyped(char par1, int par2){
        if(lowerBoundField.isFocused() && par2 != 1) {
            lowerBoundField.textboxKeyTyped(par1, par2);
            //  NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else if(higherBoundField.isFocused() && par2 != 1) {
            higherBoundField.textboxKeyTyped(par1, par2);
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        super.actionPerformed(widget);
        if(widget.getID() == 0) {
            module.advancedConfig = ((GuiCheckBox)widget).checked;
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 2, module.advancedConfig ? 1 : 0));
        }
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(!module.advancedConfig) mc.displayGuiScreen(new GuiPressureModuleSimple(module));
    }
}
