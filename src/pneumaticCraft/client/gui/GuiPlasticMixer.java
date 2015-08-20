package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.inventory.ContainerPlasticMixer;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlasticMixer extends GuiPneumaticContainerBase<TileEntityPlasticMixer>{
    private GuiButtonSpecial[] buttons;
    private GuiCheckBox lockSelection;

    public GuiPlasticMixer(InventoryPlayer player, TileEntityPlasticMixer te){
        super(new ContainerPlasticMixer(player, te), te, Textures.GUI_PLASTIC_MIXER);
    }

    @Override
    public void initGui(){
        super.initGui();

        addWidget(new WidgetTemperature(0, guiLeft + 55, guiTop + 25, 295, 500, te.getLogic(0)));
        addWidget(new WidgetTemperature(1, guiLeft + 82, guiTop + 25, 295, 500, te.getLogic(1), PneumaticValues.PLASTIC_MIXER_MELTING_TEMP));
        addWidget(new WidgetTank(3, guiLeft + 152, guiTop + 14, te.getFluidTank()));

        GuiAnimatedStat stat = addAnimatedStat("gui.tab.plasticMixer.plasticSelection", new ItemStack(Itemss.plastic, 1, 1), 0xFF005500, false);
        List<String> text = new ArrayList<String>();
        for(int i = 0; i < 12; i++) {
            text.add("                      ");
        }
        stat.setTextWithoutCuttingString(text);

        buttons = new GuiButtonSpecial[16];
        for(int x = 0; x < 4; x++) {
            for(int y = 0; y < 4; y++) {
                int index = y * 4 + x;
                ItemStack plastic = new ItemStack(Itemss.plastic, 1, index);
                buttons[index] = new GuiButtonSpecial(index + 1, x * 21 + 4, y * 21 + 30, 20, 20, "").setRenderStacks(plastic).setTooltipText(plastic.getDisplayName());
                stat.addWidget(buttons[index]);
            }
        }
        stat.addWidget(lockSelection = new GuiCheckBox(17, 4, 18, 0xFF000000, "gui.plasticMixer.lockSelection").setChecked(te.lockSelection).setTooltip(I18n.format("gui.plasticMixer.lockSelection.tooltip")));
    }

    @Override
    public String getRedstoneButtonText(int mode){
        return mode == 3 ? "gui.tab.redstoneBehaviour.plasticMixer.button.selectOnSignal" : super.getRedstoneButtonText(mode);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        for(int i = 0; i < buttons.length; i++) {
            buttons[i].enabled = te.selectedPlastic != i;
        }
        lockSelection.checked = te.lockSelection;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);

        fontRendererObj.drawString("Upgr.", 15, 19, 4210752);
        fontRendererObj.drawString("Hull", 56, 16, 4210752);
        fontRendererObj.drawString("Item", 88, 16, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        for(int i = 0; i < 3; i++) {
            double percentage = (double)te.dyeBuffers[i] / TileEntityPlasticMixer.DYE_BUFFER_MAX;
            drawVerticalLine(guiLeft + 123, guiTop + 37 + i * 18, guiTop + 37 - MathHelper.clamp_int((int)(percentage * 16), 1, 15) + i * 18, 0xFF000000 | 0xFF0000 >> 8 * i);
        }
    }

    @Override
    protected Point getInvNameOffset(){
        return new Point(0, -1);
    }

    @Override
    protected Point getInvTextOffset(){
        return null;
    }

    @Override
    protected void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getFluidTank().getFluidAmount() == 0) {
            if(te.getStackInSlot(4) == null) {
                curInfo.add("gui.tab.problems.plasticMixer.noPlastic");
            } else {
                curInfo.add("gui.tab.problems.notEnoughHeat");
            }
        } else {
            if(te.getStackInSlot(4) != null) {
                if(te.getLogic(1).getTemperature() >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP && te.getFluidTank().getCapacity() - te.getFluidTank().getFluidAmount() < 1000) {
                    curInfo.add("gui.tab.problems.plasticMixer.plasticLiquidOverflow");
                }
            }
        }
        if(te.getStackInSlot(TileEntityPlasticMixer.INV_DYE_RED) == null) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.dye, 1, 1).getDisplayName()));
        }
        if(te.getStackInSlot(TileEntityPlasticMixer.INV_DYE_GREEN) == null) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.dye, 1, 2).getDisplayName()));
        }
        if(te.getStackInSlot(TileEntityPlasticMixer.INV_DYE_BLUE) == null) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.dye, 1, 4).getDisplayName()));
        }

        if(curInfo.size() == 0) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noProblems"));
        }
    }
}
