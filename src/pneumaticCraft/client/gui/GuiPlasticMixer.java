package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerPlasticMixer;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlasticMixer extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_PLASTIC_MIXER);
    private final TileEntityPlasticMixer te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;

    public GuiPlasticMixer(InventoryPlayer player, TileEntityPlasticMixer teLightBox){

        super(new ContainerPlasticMixer(player, teLightBox));
        ySize = 176;
        te = teLightBox;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        infoStat.setText(GuiConstants.INFO_LIGHT_BOX);
        upgradeStat.setText(GuiConstants.UPGRADES_LIGHT_BOX);
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());

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
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

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
