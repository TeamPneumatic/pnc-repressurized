package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.text.WordUtils;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.tileentity.TileEntityAerialInterface;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAerialInterface extends GuiPneumaticContainerBase<TileEntityAerialInterface>{
    private final GuiButtonSpecial[] modeButtons = new GuiButtonSpecial[3];

    public GuiAerialInterface(InventoryPlayer player, TileEntityAerialInterface te){

        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        if(PneumaticCraftAPIHandler.getInstance().liquidXPs.size() > 0) addAnimatedStat("gui.tab.info.aerialInterface.liquidXp.info.title", new ItemStack(Items.water_bucket), 0xFF55FF55, false).setText(getLiquidXPText());
        if(Loader.isModLoaded(ModIds.COFH_CORE)) {
            addAnimatedStat("gui.tab.info.aerialInterface.interfacingRF.info.title", new ItemStack(Items.glowstone_dust), 0xFFFF2222, false).setText("gui.tab.info.aerialInterface.interfacingRF.info");
        }

        if(te.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
            GuiAnimatedStat optionStat = addAnimatedStat("gui.tab.aerialInterface.feedMode", new ItemStack(Items.beef), 0xFFFFCC00, false);
            List<String> text = new ArrayList<String>();
            for(int i = 0; i < 4; i++)
                text.add("                 ");
            optionStat.setTextWithoutCuttingString(text);

            GuiButtonSpecial button = new GuiButtonSpecial(1, 5, 20, 20, 20, "");
            button.setRenderStacks(new ItemStack(Items.beef));
            button.setTooltipText(I18n.format("gui.tab.aerialInterface.feedMode.feedFullyUtilize"));
            optionStat.addWidget(button);
            modeButtons[0] = button;

            button = new GuiButtonSpecial(2, 30, 20, 20, 20, "");
            button.setRenderStacks(new ItemStack(Items.apple));
            button.setTooltipText(I18n.format("gui.tab.aerialInterface.feedMode.feedWhenPossible"));
            optionStat.addWidget(button);
            modeButtons[1] = button;

            button = new GuiButtonSpecial(3, 55, 20, 20, 20, "");
            button.setRenderStacks(new ItemStack(Items.golden_apple));
            button.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.tab.aerialInterface.feedMode.utilizeFullHealthElsePossible"), 40).split(System.getProperty("line.separator"))));
            optionStat.addWidget(button);
            modeButtons[2] = button;
        } else {
            for(int i = 0; i < modeButtons.length; i++)
                modeButtons[i] = null;
        }
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(te.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
            if(modeButtons[0] != null) {
                for(int i = 0; i < modeButtons.length; i++) {
                    modeButtons[i].enabled = te.feedMode != i;
                }
            } else {
                refreshScreen();
            }
        } else if(modeButtons[0] != null) {
            refreshScreen();
        }
    }

    private List<String> getLiquidXPText(){
        List<String> liquidXpText = new ArrayList<String>();
        liquidXpText.add("gui.tab.info.aerialInterface.liquidXp.info");
        for(Fluid fluid : PneumaticCraftAPIHandler.getInstance().liquidXPs.keySet()) {
            liquidXpText.add(EnumChatFormatting.DARK_AQUA + new FluidStack(fluid, 1).getLocalizedName() + " (" + fluid.getName() + ")");
        }
        return liquidXpText;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);

    }

    @Override
    public String getRedstoneButtonText(int mode){
        return te.redstoneMode == 0 ? "gui.tab.redstoneBehaviour.button.never" : "gui.tab.redstoneBehaviour.aerialInterface.button.playerConnected";
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        if(te.getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && te.isConnectedToPlayer) {
            pressureStatText.add(EnumChatFormatting.GRAY + "Usage:");
            pressureStatText.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_AERIAL_INTERFACE, 1) + " mL/tick.");
        }
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(te.playerName.equals("")) {
            textList.add("\u00a77There isn't a player set!");
            textList.add(EnumChatFormatting.BLACK + "Replace the machine.");
        } else if(!te.isConnectedToPlayer) {
            textList.add(EnumChatFormatting.GRAY + te.playerName + " can not be found on the server!");
            textList.add(EnumChatFormatting.BLACK + "Insists he/she comes back.");
        }

        if(textList.size() == 0) {
            textList.add("gui.tab.problems.noProblems");
            textList.add(I18n.format("gui.tab.problems.aerialInterface.linked", te.playerName));
        }
    }
}
