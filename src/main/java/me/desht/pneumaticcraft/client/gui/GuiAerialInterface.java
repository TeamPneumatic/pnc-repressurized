package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.inventory.ContainerAerialInterface;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface.FeedMode;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiAerialInterface extends GuiPneumaticContainerBase<ContainerAerialInterface,TileEntityAerialInterface> {
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[FeedMode.values().length];
    private WidgetButtonExtended xpButton;
    private WidgetAnimatedStat feedModeTab;

    public GuiAerialInterface(ContainerAerialInterface container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat("pneumaticcraft.gui.tab.info.aerialInterface.interfacingRF.info.title",
                Textures.GUI_BUILDCRAFT_ENERGY, 0xFFA02222, false).setText("pneumaticcraft.gui.tab.info.aerialInterface.interfacingRF.info");

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage)));

        if (te.dispenserUpgradeInserted) {
            // Experience Tab
            List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
            if (availableXp.size() > 0) {
                WidgetAnimatedStat xpStat = addAnimatedStat("pneumaticcraft.gui.tab.info.aerialInterface.liquidXp.info.title",
                        new ItemStack(Items.EXPERIENCE_BOTTLE), 0xFF55FF55, false);
                xpStat.setText(getLiquidXPText());
                xpButton = new WidgetButtonExtended(20, 15, 20, 20, "", b -> {
                    te.curXPFluidIndex++;
                    if (te.curXPFluidIndex >= availableXp.size()) {
                        te.curXPFluidIndex = -1;
                    }
                    setupXPButton();
                }).withTag("xpType");
                setupXPButton();
                xpStat.addSubWidget(xpButton);
            }

            // Feeding Tab
            feedModeTab = addAnimatedStat(te.feedMode.getTranslationKey(), te.feedMode.getIconStack(), 0xFFFFA000, false);
            feedModeTab.addPadding(4, 16);

            for (int i = 0; i < FeedMode.values().length; i++) {
                FeedMode mode = FeedMode.values()[i];
                WidgetButtonExtended button = new WidgetButtonExtended(5 + 25 * i, 20, 20, 20, "")
                        .withTag(mode.toString());
                button.setRenderStacks(mode.getIconStack());
                List<String> tooltip = new ArrayList<>();
                tooltip.add(TextFormatting.YELLOW + I18n.format(mode.getTranslationKey()));
                tooltip.addAll(PneumaticCraftUtils.splitString(I18n.format(mode.getDescTranslationKey()), 35));
                button.setTooltipText(tooltip);
                feedModeTab.addSubWidget(button);
                modeButtons[i] = button;
            }

            addAnimatedStat("pneumaticcraft.gui.tab.info.aerialInterface.interfacingFood", new ItemStack(Items.BREAD), 0xFFA0A0A0, false)
                    .setText("pneumaticcraft.gui.tab.info.aerialInterface.removeDispenser");

        } else {
            addAnimatedStat("pneumaticcraft.gui.tab.info.aerialInterface.interfacingItems", new ItemStack(Blocks.CHEST), 0xFFA0A0A0, false)
                    .setText("pneumaticcraft.gui.tab.info.aerialInterface.insertDispenser");
            Arrays.fill(modeButtons, null);
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
                feedModeTab.setTitle(te.feedMode.getTranslationKey());
            } else {
                refreshScreen();
            }
        } else if (modeButtons[0] != null) {
            refreshScreen();
        }
    }

    private void setupXPButton() {
        List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
        Fluid fluid = te.curXPFluidIndex >= 0 && te.curXPFluidIndex < availableXp.size() ?
                availableXp.get(te.curXPFluidIndex) : Fluids.EMPTY;
        if (fluid != Fluids.EMPTY) {
            FluidStack fluidStack = new FluidStack(fluid, 1000);
            xpButton.setRenderStacks(FluidUtil.getFilledBucket(fluidStack));
            String modName = ModNameCache.getModName(fluid.getRegistryName().getNamespace());
            xpButton.setTooltipText(ImmutableList.of(fluidStack.getDisplayName().getFormattedText(), TextFormatting.ITALIC.toString() + TextFormatting.BLUE + modName));
        } else {
            xpButton.setRenderStacks(new ItemStack(Items.BUCKET));
            xpButton.setTooltipText(I18n.format("pneumaticcraft.gui.tooltip.aerial_interface.xpDisabled"));
        }
    }

    private List<String> getLiquidXPText() {
        List<String> liquidXpText = new ArrayList<>();
        liquidXpText.add("");
        liquidXpText.add("");
        liquidXpText.add("");
        liquidXpText.add("pneumaticcraft.gui.tab.info.aerialInterface.liquidXp.info");
        liquidXpText.add("");
        List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
        if (availableXp.isEmpty()) {
            liquidXpText.add(TextFormatting.ITALIC + "None");
        } else {
            for (Fluid f : availableXp) {
                FluidStack stack = new FluidStack(f, 1000);
                String modName = ModNameCache.getModName(f.getRegistryName().getNamespace());
                liquidXpText.add(TextFormatting.BLACK.toString() + GuiConstants.BULLET + " "
                        + stack.getDisplayName().getFormattedText()
                        + TextFormatting.DARK_BLUE + " (" + modName + ")");
            }
        }
        return liquidXpText;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.getPressure() >= te.getMinWorkingPressure() && te.isConnectedToPlayer) {
            pressureStatText.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tooltip.airUsage",
                    PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_AERIAL_INTERFACE, 1)));
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);

        if (te.playerName.equals("")) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.aerialInterface.noPlayer"));
        } else if (!te.isConnectedToPlayer) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.aerialInterface.playerOffline", te.playerName));
        }
    }

    @Override
    protected void addInformation(List<String> curInfo) {
        if (te.playerName != null && !te.playerName.isEmpty()) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.info.aerialInterface.linked", te.playerName));
        }
    }
}
