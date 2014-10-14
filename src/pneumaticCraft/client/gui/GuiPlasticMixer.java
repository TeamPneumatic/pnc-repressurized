package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.inventory.ContainerPlasticMixer;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlasticMixer extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_PLASTIC_MIXER);
    private final TileEntityPlasticMixer te;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;

    public GuiPlasticMixer(InventoryPlayer player, TileEntityPlasticMixer te){

        super(new ContainerPlasticMixer(player, te));
        ySize = 176;
        this.te = te;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.plasticMixer");
        problemStat = addAnimatedStat("gui.tab.problems", Textures.GUI_PROBLEMS_TEXTURE, 0xFFFF0000, false);
        addAnimatedStat("gui.tab.heat", new ItemStack(Blocks.fire), 0xFFFF5500, false).setText("gui.tab.info.heat");
        redstoneBehaviourStat = addAnimatedStat("gui.tab.redstoneBehaviour", new ItemStack(Items.redstone), 0xFFCC0000, true);
        addAnimatedStat("gui.tab.upgrade", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText("gui.tab.upgrade.volumeCapacity");
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());

        addWidget(new WidgetTemperature(0, guiLeft + 55, guiTop + 25, 295, 500, te.getLogic(0)));
        addWidget(new WidgetTemperature(1, guiLeft + 82, guiTop + 25, 295, 500, te.getLogic(1), PneumaticValues.PLASTIC_MIXER_MELTING_TEMP));
        addWidget(new WidgetTemperature(2, guiLeft + 128, guiTop + 29, 295, 500, te.getLogic(2), PneumaticValues.PLASTIC_MIXER_MELTING_TEMP));
        addWidget(new WidgetTank(3, guiLeft + 145, guiTop + 14, te.getFluidTank()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory").substring(0, 3) + ".", 8, ySize - 106 + 2, 4210752);
        fontRendererObj.drawString("Upgr.", 15, 19, 4210752);
        fontRendererObj.drawString("Hull", 56, 16, 4210752);
        fontRendererObj.drawString("Item", 88, 16, 4210752);
        fontRendererObj.drawString("Liquid", 131, 5, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        // pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());

    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Emit redstone if         "); // the spaces are
                                                          // there to create
                                                          // space for the
                                                          // button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        /*  if(te.getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX) {
              textList.add("\u00a77Not enough pressure");
              textList.add("\u00a70Add air to the input");
          }
          if(te.getStackInSlot(TileEntityUVLightBox.PCB_INDEX) == null) {
              textList.add("\u00a77No PCB to expose.");
              textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put in an Empy PCB.", GuiConstants.maxCharPerLineLeft));
          }
          if(textList.size() == 0) {
              textList.add("\u00a77No problems");
          }*/
        return textList;
    }
}
