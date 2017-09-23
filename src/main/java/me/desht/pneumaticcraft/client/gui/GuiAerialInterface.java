package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiAerialInterface extends GuiPneumaticContainerBase<TileEntityAerialInterface> {
    private final GuiButtonSpecial[] modeButtons = new GuiButtonSpecial[3];

    public GuiAerialInterface(InventoryPlayer player, TileEntityAerialInterface te) {

        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (PneumaticCraftAPIHandler.getInstance().liquidXPs.size() > 0)
            addAnimatedStat("gui.tab.info.aerialInterface.liquidXp.info.title", new ItemStack(Items.WATER_BUCKET), 0xFF55FF55, false).setText(getLiquidXPText());
        if (Loader.isModLoaded(ModIds.COFH_CORE)) {
            addAnimatedStat("gui.tab.info.aerialInterface.interfacingRF.info.title", new ItemStack(Items.GLOWSTONE_DUST), 0xFFFF2222, false).setText("gui.tab.info.aerialInterface.interfacingRF.info");
        }

        if (te.getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            GuiAnimatedStat optionStat = addAnimatedStat("gui.tab.aerialInterface.feedMode", new ItemStack(Items.BEEF), 0xFFFFCC00, false);
            List<String> text = new ArrayList<String>();
            for (int i = 0; i < 4; i++)
                text.add("                 ");
            optionStat.setTextWithoutCuttingString(text);

            GuiButtonSpecial button = new GuiButtonSpecial(1, 5, 20, 20, 20, "");
            button.setRenderStacks(new ItemStack(Items.BEEF));
            button.setTooltipText(I18n.format("gui.tab.aerialInterface.feedMode.feedFullyUtilize"));
            optionStat.addWidget(button);
            modeButtons[0] = button;

            button = new GuiButtonSpecial(2, 30, 20, 20, 20, "");
            button.setRenderStacks(new ItemStack(Items.APPLE));
            button.setTooltipText(I18n.format("gui.tab.aerialInterface.feedMode.feedWhenPossible"));
            optionStat.addWidget(button);
            modeButtons[1] = button;

            button = new GuiButtonSpecial(3, 55, 20, 20, 20, "");
            button.setRenderStacks(new ItemStack(Items.GOLDEN_APPLE));
            button.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.tab.aerialInterface.feedMode.utilizeFullHealthElsePossible"), 40).split(System.getProperty("line.separator"))));
            optionStat.addWidget(button);
            modeButtons[2] = button;
        } else {
            for (int i = 0; i < modeButtons.length; i++)
                modeButtons[i] = null;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (te.getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            if (modeButtons[0] != null) {
                for (int i = 0; i < modeButtons.length; i++) {
                    modeButtons[i].enabled = te.feedMode != i;
                }
            } else {
                refreshScreen();
            }
        } else if (modeButtons[0] != null) {
            refreshScreen();
        }
    }

    private List<String> getLiquidXPText() {
        List<String> liquidXpText = new ArrayList<String>();
        liquidXpText.add("gui.tab.info.aerialInterface.liquidXp.info");
        for (Fluid fluid : PneumaticCraftAPIHandler.getInstance().liquidXPs.keySet()) {
            liquidXpText.add(TextFormatting.DARK_AQUA + new FluidStack(fluid, 1).getLocalizedName() + " (" + fluid.getName() + ")");
        }
        return liquidXpText;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 53, 19, 4210752);

    }

    @Override
    public String getRedstoneButtonText(int mode) {
        return te.redstoneMode == 0 ? "gui.tab.redstoneBehaviour.button.never" : "gui.tab.redstoneBehaviour.aerialInterface.button.playerConnected";
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        if (te.getPressure() > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && te.isConnectedToPlayer) {
            pressureStatText.add(TextFormatting.GRAY + "Usage:");
            pressureStatText.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_AERIAL_INTERFACE, 1) + " mL/tick.");
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.playerName.equals("")) {
            textList.add("\u00a77There isn't a player set!");
            textList.add(TextFormatting.BLACK + "Replace the machine.");
        } else if (!te.isConnectedToPlayer) {
            textList.add(TextFormatting.GRAY + te.playerName + " can not be found on the server!");
            textList.add(TextFormatting.BLACK + "Insists he/she comes back.");
        }

        if (textList.size() == 0) {
            textList.add("gui.tab.problems.noProblems");
            textList.add(I18n.format("gui.tab.problems.aerialInterface.linked", te.playerName));
        }
    }
}
