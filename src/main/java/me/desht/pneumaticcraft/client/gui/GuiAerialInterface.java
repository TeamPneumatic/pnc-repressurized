package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerAerialInterface;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface.FeedMode;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiAerialInterface extends GuiPneumaticContainerBase<ContainerAerialInterface,TileEntityAerialInterface> {
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[3];
    private WidgetButtonExtended xpButton;

    public GuiAerialInterface(ContainerAerialInterface container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat("gui.tab.info.aerialInterface.interfacingRF.info.title",
                Textures.GUI_BUILDCRAFT_ENERGY, 0xFFA02222, false).setText("gui.tab.info.aerialInterface.interfacingRF.info");

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage)));

        if (te.dispenserUpgradeInserted) {
            // Experience Tab
            if (PneumaticCraftAPIHandler.getInstance().liquidXPs.size() > 0) {
                WidgetAnimatedStat xpStat = addAnimatedStat("gui.tab.info.aerialInterface.liquidXp.info.title",
                        new ItemStack(Items.EXPERIENCE_BOTTLE), 0xFF55FF55, false);
                xpStat.setText(getLiquidXPText());
                xpButton = new WidgetButtonExtended(20, 15, 20, 20, "", b -> {
                    te.curXPFluidIndex++;
                    if (te.curXPFluidIndex >= PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.size()) {
                        te.curXPFluidIndex = -1;
                    }
                    setupXPButton();
                }).withTag("xpType");
                setupXPButton();
                xpStat.addSubWidget(xpButton);
            }

            // Feeding Tab
            WidgetAnimatedStat optionStat = addAnimatedStat("gui.tab.aerialInterface.feedMode",
                    new ItemStack(Items.BEEF), 0xFFFFCC00, false);
            optionStat.addPadding(4, 16);

            WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, "")
                    .withTag(FeedMode.FULLY_UTILIZE.toString());
            button.setRenderStacks(new ItemStack(Items.BEEF));
            button.setTooltipText(I18n.format("gui.tab.aerialInterface.feedMode.feedFullyUtilize"));
            optionStat.addSubWidget(button);
            modeButtons[0] = button;

            button = new WidgetButtonExtended(30, 20, 20, 20, "")
                    .withTag(FeedMode.WHEN_POSSIBLE.toString());
            button.setRenderStacks(new ItemStack(Items.APPLE));
            button.setTooltipText(I18n.format("gui.tab.aerialInterface.feedMode.feedWhenPossible"));
            optionStat.addSubWidget(button);
            modeButtons[1] = button;

            button = new WidgetButtonExtended(55, 20, 20, 20, "")
                    .withTag(FeedMode.FULLY_ELSE_WHEN_POSSIBLE.toString());
            button.setRenderStacks(new ItemStack(Items.GOLDEN_APPLE));
            button.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.tab.aerialInterface.feedMode.utilizeFullHealthElsePossible"), 40).split(System.getProperty("line.separator"))));
            optionStat.addSubWidget(button);
            modeButtons[2] = button;

            addAnimatedStat("gui.tab.info.aerialInterface.interfacingFood", new ItemStack(Items.BREAD), 0xFFA0A0A0, false)
                    .setText("gui.tab.info.aerialInterface.removeDispenser");

        } else {
            addAnimatedStat("gui.tab.info.aerialInterface.interfacingItems", new ItemStack(Blocks.CHEST), 0xFFA0A0A0, false)
                    .setText("gui.tab.info.aerialInterface.insertDispenser");
            for (int i = 0; i < modeButtons.length; i++)
                modeButtons[i] = null;
        }
    }

    @Override
    protected boolean shouldAddSideConfigTabs() {
        return !te.dispenserUpgradeInserted;
    }

    @Override
    public void tick() {
        super.tick();
        if (te.dispenserUpgradeInserted) {
            if (modeButtons[0] != null) {
                for (int i = 0; i < modeButtons.length; i++) {
                    modeButtons[i].active = te.feedMode != FeedMode.values()[i];
                }
            } else {
                refreshScreen();
            }
        } else if (modeButtons[0] != null) {
            refreshScreen();
        }
    }

    private void setupXPButton() {
        Fluid fluid = te.curXPFluidIndex >= 0 && te.curXPFluidIndex < PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.size() ?
                PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.get(te.curXPFluidIndex) : null;
        if (fluid != null) {
            FluidStack fluidStack = new FluidStack(fluid, 1000);
            xpButton.setRenderStacks(FluidUtil.getFilledBucket(fluidStack));
            String modName = ModNameCache.getModName(fluid.getRegistryName().getNamespace());
            xpButton.setTooltipText(ImmutableList.of(fluidStack.getDisplayName().getFormattedText(), TextFormatting.BLUE.toString() + TextFormatting.ITALIC + modName));
        } else {
            xpButton.setRenderStacks(new ItemStack(Items.BUCKET));
            xpButton.setTooltipText(I18n.format("gui.tooltip.aerial_interface.xpDisabled"));
        }
    }

    private List<String> getLiquidXPText() {
        List<String> liquidXpText = new ArrayList<>();
        liquidXpText.add("");
        liquidXpText.add("");
        liquidXpText.add("");
        liquidXpText.add("gui.tab.info.aerialInterface.liquidXp.info");
        liquidXpText.add("");
        if (PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.isEmpty()) {
            liquidXpText.add(TextFormatting.ITALIC + "None");
        } else {
            for (Fluid f : PneumaticCraftAPIHandler.getInstance().availableLiquidXPs) {
                FluidStack stack = new FluidStack(f, 1000);
                String modName = ModNameCache.getModName(f.getRegistryName().getNamespace());
                liquidXpText.add(TextFormatting.BLACK + "\u2022  " + stack.getDisplayName().getFormattedText() + " (" + modName + ")");
            }
        }
        return liquidXpText;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 53, 19, 0x404040);

    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
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
            textList.add("\u00a7No player set!");
            textList.add(TextFormatting.BLACK + "Break and replace the machine.");
        } else if (!te.isConnectedToPlayer) {
            textList.add(TextFormatting.GRAY + te.playerName + " is not online!");
            textList.add(TextFormatting.BLACK + "The Aerial Interface is non-functional");
            textList.add(TextFormatting.BLACK + "until they return.");
        }
    }

    @Override
    protected void addInformation(List<String> curInfo) {
        if (te.playerName != null && !te.playerName.isEmpty()) {
            curInfo.add(I18n.format("gui.tab.problems.aerialInterface.linked", te.playerName));
        }
    }
}
