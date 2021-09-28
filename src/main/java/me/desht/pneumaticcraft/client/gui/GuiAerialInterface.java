package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.inventory.ContainerAerialInterface;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface.FeedMode;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.interfacingRF.info.title"),
                Textures.GUI_BUILDCRAFT_ENERGY, 0xFFA02222, false)
                .setText(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.aerialInterface.interfacingRF.info"));

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(leftPos + 20, topPos + 20, storage)));

        if (te.dispenserUpgradeInserted) {
            // Experience Tab
            List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
            if (availableXp.size() > 0) {
                WidgetAnimatedStat xpStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.liquidXp.info.title"),
                        new ItemStack(Items.EXPERIENCE_BOTTLE), 0xFF55FF55, false);
                xpStat.setText(getLiquidXPText()).setForegroundColor(0xFF000000);
                xpButton = new WidgetButtonExtended(20, 15, 20, 20, StringTextComponent.EMPTY, b -> {
                    te.curXPFluidIndex++;
                    if (te.curXPFluidIndex >= availableXp.size()) {
                        te.curXPFluidIndex = -1;
                    }
                    setupXPButton();
                }).withTag("xpType");
                setupXPButton();
                xpStat.addSubWidget(xpButton);
                xpStat.setReservedLines(3);
            }

            // Feeding Tab
            feedModeTab = addAnimatedStat(xlate(te.feedMode.getTranslationKey()), te.feedMode.getIconStack(), 0xFFFFA000, false);
            feedModeTab.setMinimumExpandedDimensions(80, 42);

            for (int i = 0; i < FeedMode.values().length; i++) {
                FeedMode mode = FeedMode.values()[i];
                List<ITextComponent> l = new ArrayList<>();
                l.add(xlate(mode.getTranslationKey()).withStyle(TextFormatting.YELLOW));
                l.addAll(GuiUtils.xlateAndSplit(mode.getDescTranslationKey()));
                WidgetButtonExtended button = new WidgetButtonExtended(5 + 25 * i, 20, 20, 20)
                        .withTag(mode.toString())
                        .setRenderStacks(mode.getIconStack())
                        .setTooltipText(l);
                feedModeTab.addSubWidget(button);
                modeButtons[i] = button;
            }

            addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.interfacingFood"), new ItemStack(Items.BREAD), 0xFFA0A0A0, false)
                    .setText(xlate("pneumaticcraft.gui.tab.info.aerialInterface.removeDispenser")).setForegroundColor(0xFF000000);

        } else {
            addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.interfacingItems"), new ItemStack(Blocks.CHEST), 0xFFA0A0A0, false)
                    .setText(xlate("pneumaticcraft.gui.tab.info.aerialInterface.insertDispenser")).setForegroundColor(0xFF000000);
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
                feedModeTab.setMessage(xlate(te.feedMode.getTranslationKey()));
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
            xpButton.setTooltipText(ImmutableList.of(
                    fluidStack.getDisplayName(),
                    new StringTextComponent(modName).withStyle(TextFormatting.ITALIC, TextFormatting.BLUE))
            );
        } else {
            xpButton.setRenderStacks(new ItemStack(Items.BUCKET));
            xpButton.setTooltipText(xlate("pneumaticcraft.gui.tooltip.aerial_interface.xpDisabled"));
        }
    }

    private List<ITextComponent> getLiquidXPText() {
        List<ITextComponent> liquidXpText = new ArrayList<>(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.aerialInterface.liquidXp.info"));
        liquidXpText.add(StringTextComponent.EMPTY);
        List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
        if (availableXp.isEmpty()) {
            liquidXpText.add(xlate("pneumaticcraft.gui.misc.none").withStyle(TextFormatting.BLACK, TextFormatting.ITALIC));
        } else {
            for (Fluid f : availableXp) {
                FluidStack stack = new FluidStack(f, 1000);
                String modName = ModNameCache.getModName(f.getRegistryName().getNamespace());
                StringTextComponent modNameText = new StringTextComponent(" (" + modName + ")");
                liquidXpText.add(Symbols.bullet().withStyle(TextFormatting.BLACK)
                        .append(stack.getDisplayName().copy().withStyle(TextFormatting.BLACK))
                        .append(modNameText.withStyle(TextFormatting.DARK_BLUE))
                );
            }
        }
        return liquidXpText;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.getPressure() >= te.getMinWorkingPressure() && te.isConnectedToPlayer) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage",
                    PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_AERIAL_INTERFACE, 1)).withStyle(TextFormatting.BLACK));
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> textList) {
        super.addProblems(textList);

        if (te.getPlayerName().isEmpty()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.aerialInterface.noPlayer"));
        } else if (!te.isConnectedToPlayer) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.aerialInterface.playerOffline", te.getPlayerName()));
        }
    }

    @Override
    protected void addInformation(List<ITextComponent> curInfo) {
        if (te.getPlayerName() != null && !te.getPlayerName().isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.tab.info.aerialInterface.linked", te.getPlayerName()));
        }
    }
}
