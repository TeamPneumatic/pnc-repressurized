/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.Window;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.ITickableWidget;
import me.desht.pneumaticcraft.api.crafting.recipe.PneumaticCraftRecipe;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.block.entity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractPneumaticCraftContainerScreen<C extends AbstractPneumaticCraftMenu<T>, T extends AbstractPneumaticCraftBlockEntity> extends AbstractContainerScreen<C> {
    public final T te;
    private IGuiAnimatedStat lastLeftStat, lastRightStat;
    private WidgetAnimatedStat pressureStat;
    private WidgetAnimatedStat redstoneTab;
    WidgetAnimatedStat problemTab;
    private final List<WidgetButtonExtended> redstoneButtons = new ArrayList<>();
    boolean firstUpdate = true;
    private final List<IGuiAnimatedStat> statWidgets = new ArrayList<>();
    private int sendDelay = -1;

    public AbstractPneumaticCraftContainerScreen(C container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
        this.te = container.blockEntity;
    }

    @Override
    public void init() {
        super.init();

        lastLeftStat = lastRightStat = null;
        if (shouldAddPressureTab() && te instanceof AbstractAirHandlingBlockEntity) {
            pressureStat = this.addAnimatedStat(xlate("pneumaticcraft.gui.tab.pressure"), new ItemStack(ModBlocks.PRESSURE_TUBE.get()), 0xFF00AA00, false);
            pressureStat.setForegroundColor(0xFF000000);
        }
        if (shouldAddProblemTab()) {
            problemTab = addAnimatedStat(xlate("pneumaticcraft.gui.tab.problems"), 0xFFA0A0A0, false);
            problemTab.setForegroundColor(0xFF000000);
            problemTab.setMinimumExpandedDimensions(0, 16);
        }
        if (te != null) {
            if (shouldAddInfoTab()) {
                addInfoTab(GuiUtils.xlateAndSplit(ICustomTooltipName.getTranslationKey(new ItemStack(te.getBlockState().getBlock()), false)));
            }
            if (shouldAddRedstoneTab() && te instanceof IRedstoneControl) {
                addRedstoneTab(((IRedstoneControl<?>) te).getRedstoneController());
            }
            if (te instanceof IHeatExchangingTE && ((IHeatExchangingTE) te).shouldShowGuiHeatTab()) {
                addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.heat.title"),
                        new ItemStack(Items.BLAZE_POWDER), 0xFFE05500, false)
                        .setText(xlate("pneumaticcraft.gui.tab.info.heat"));
            }
            if (shouldAddUpgradeTab()) {
                addUpgradeTab();
            }
            if (shouldAddSideConfigTabs()) {
                addSideConfiguratorTabs();
            }
            if (te instanceof AbstractAirHandlingBlockEntity airHandler) {
                // ensure all handlers are known, so we can get their upgrades right
                airHandler.initializeHullAirHandlers();
            }
        }
    }

    private WidgetAnimatedStat addAnimatedStat(Component title, StatIcon icon, int color, boolean leftSided) {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;

        WidgetAnimatedStat stat = new WidgetAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : imageWidth + 1), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        stat.setBeveled(true);
        addRenderableWidget(stat);
        if (leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        statWidgets.add(stat);
        return stat;
    }

    protected WidgetAnimatedStat addAnimatedStat(Component title, @Nonnull ItemStack icon, int backgroundColor, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), backgroundColor, leftSided);
    }

    protected WidgetAnimatedStat addAnimatedStat(Component title, ResourceLocation icon, int backgroundColor, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), backgroundColor, leftSided);
    }

    protected WidgetAnimatedStat addAnimatedStat(Component title, int backgroundColor, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.NONE, backgroundColor, leftSided);
    }

    protected WidgetLabel addLabel(Component text, int x, int y) {
        return addRenderableWidget(new WidgetLabel(x, y, text));
    }

    protected WidgetLabel addLabel(Component text, int x, int y, int color) {
        return addRenderableWidget(new WidgetLabel(x, y, text, color));
    }

    void removeWidget(AbstractWidget widget) {
        super.removeWidget(widget);
        if (widget instanceof IGuiAnimatedStat) statWidgets.remove(widget);
    }

    public List<IGuiAnimatedStat> getStatWidgets() {
        return statWidgets;
    }

    private void addRedstoneTab(RedstoneController<?> redstoneController) {
        redstoneTab = addAnimatedStat(xlate("pneumaticcraft.gui.tab.redstoneBehaviour"), new ItemStack(Items.REDSTONE), 0xFFCC0000, true);

        redstoneButtons.clear();
        int nModes = redstoneController.getModeCount();
        int bx = -23 * nModes;
        for (int i = 0; i < nModes; i++) {
            redstoneButtons.add(createRedstoneModeButton(bx, i, redstoneController.getModeDetails(i)));
            bx += 23;
        }
        redstoneButtons.forEach(b -> redstoneTab.addSubWidget(b));

        redstoneTab.setText(redstoneController.getRedstoneTabTitle());
        redstoneTab.setMinimumExpandedDimensions(23 * nModes + 5, 46);
    }

    private WidgetButtonExtended createRedstoneModeButton(int x, int idx, RedstoneController.RedstoneMode<?> mode) {
        WidgetButtonExtended b = new WidgetButtonExtended(x, 24, 20, 20, Component.empty()).withTag("redstone:" + idx);
        mode.getTexture().ifLeft(b::setRenderStacks).ifRight(b::setRenderedIcon);
        b.setTooltip(Tooltip.create(xlate(mode.getTranslationKey())));
        return b;
    }

    protected void addJeiFilterInfoTab() {
        if (ModList.get().isLoaded(ModIds.JEI)) {
            addAnimatedStat(Component.literal("JEI"), Textures.GUI_JEI_LOGO, 0xFFCEEDCE, true)
                    .setText(xlate("pneumaticcraft.gui.jei.filterDrag").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    protected String upgradeCategory() {
        ResourceLocation regName = PneumaticCraftUtils.getRegistryName(BuiltInRegistries.BLOCK_ENTITY_TYPE, te.getType()).orElseThrow();
        return PneumaticCraftUtils.modDefaultedString(regName);
    }

    private void addUpgradeTab() {
        List<Component> text = new ArrayList<>();
        ApplicableUpgradesDB.getInstance().getApplicableUpgrades(te).keySet().stream()
                .sorted(Comparator.comparing(o -> o.getItemStack().getHoverName().getString()))
                .forEach(upgrade -> {
                    if (isUpgradeAvailable(upgrade)) {
                        int max = ApplicableUpgradesDB.getInstance().getMaxUpgrades(te, upgrade);
                        text.add(upgrade.getItemStack().getHoverName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE));
                        text.add(xlate("pneumaticcraft.gui.tab.upgrades.max", max).withStyle(ChatFormatting.GRAY));
                        String upgradeName = PneumaticCraftUtils.modDefaultedString(upgrade.getId());
                        String k = "pneumaticcraft.gui.tab.upgrades." + upgradeCategory() + "." + upgradeName;
                        text.addAll(I18n.exists(k) ? GuiUtils.xlateAndSplit(k) : GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.upgrades.generic." + upgradeName));
                        text.add(Component.empty());
                    }
                });
        if (!text.isEmpty()) {
            addAnimatedStat(xlate("pneumaticcraft.gui.tab.upgrades"), Textures.GUI_UPGRADES_LOCATION, 0xFF1C53A8, true)
                    .setText(text).setForegroundColor(0xFF000000);
        }
    }

    protected boolean isUpgradeAvailable(PNCUpgrade upgrade) {
        return true;
    }

    private void addSideConfiguratorTabs() {
        for (SideConfigurator<?> sc : ((ISideConfigurable) te).getSideConfigurators()) {
            WidgetAnimatedStat stat = addAnimatedStat(xlate(sc.getTranslationKey()), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFF90C0E0, false);
            stat.setMinimumExpandedDimensions(80, 80);

            int yTop = 15, xLeft = 25;
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.TOP, xLeft + 22, yTop));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.LEFT, xLeft, yTop + 22));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.FRONT, xLeft + 22, yTop + 22));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.RIGHT, xLeft + 44, yTop + 22));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.BOTTOM, xLeft + 22, yTop + 44));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.BACK, xLeft + 44, yTop + 44));
        }
    }

    private SideConfiguratorButton makeSideConfButton(final SideConfigurator<?> sideConfigurator, RelativeFace relativeFace, int x, int y) {
        SideConfiguratorButton button = (SideConfiguratorButton) new SideConfiguratorButton(x, y, relativeFace, b -> {
            SideConfiguratorButton gbs = (SideConfiguratorButton) b;
            if (sideConfigurator.handleButtonPress(gbs.getTag(), Screen.hasShiftDown())) {
                setupSideConfiguratorButton(sideConfigurator, gbs);
            }
        }).withTag(sideConfigurator.getButtonTag(relativeFace));
        setupSideConfiguratorButton(sideConfigurator, button);
        return button;
    }

    private void setupSideConfiguratorButton(SideConfigurator<?> sc, SideConfiguratorButton button) {
        RelativeFace relativeFace = button.relativeFace;
        SideConfigurator.ConnectionEntry<?> c = sc.getEntry(relativeFace);
        if (c != null && c.texture() != null) {
            button.setTexture(c.texture());
        } else {
            button.setRenderedIcon(Textures.GUI_X_BUTTON);
        }
        button.setTooltip(Tooltip.create(
                Component.literal(relativeFace.toString()).withStyle(ChatFormatting.YELLOW)
                        .append("\n").append(sc.getFaceLabel(relativeFace))
        ));
    }

    protected void addInfoTab(List<Component> info) {
        IGuiAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info"), Textures.GUI_INFO_LOCATION, 0xFF8888FF, true);
        stat.setForegroundColor(0xFF000000);
        stat.setText(info);
        if (!ThirdPartyManager.instance().getDocsProvider().isInstalled()) {
            stat.appendText(Arrays.asList(Component.empty(), xlate("pneumaticcraft.gui.tab.info.installDocsProvider")));
        }
    }

    protected void addInfoTab(Component info) {
        addInfoTab(Collections.singletonList(info));
    }

    protected boolean shouldAddRedstoneTab() {
        return true;
    }

    protected boolean shouldAddPressureTab() {
        return true;
    }

    protected boolean shouldAddUpgradeTab() {
        return true;
    }

    protected boolean shouldAddInfoTab() {
        return true;
    }

    protected boolean shouldAddProblemTab() {
        return true;
    }

    protected boolean shouldAddSideConfigTabs() { return te instanceof ISideConfigurable; }

    protected OptionalInt getBackgroundTint() { return OptionalInt.empty(); }

    protected boolean shouldDrawBackground() {
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int i, int j) {
        if (shouldDrawBackground()) {
            int xStart = (width - imageWidth) / 2;
            int yStart = (height - imageHeight) / 2;
            getBackgroundTint().ifPresent(tint -> {
                float[] c = RenderUtils.decomposeColorF(tint);
                graphics.setColor(c[1], c[2], c[3], c[0]);
            });
            graphics.blit(getGuiTexture(), xStart, yStart, 0, 0, imageWidth, imageHeight);
            getBackgroundTint().ifPresent(tint -> graphics.setColor(1f, 1f, 1f, 1f));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        if (getInvNameOffset() != null) {
            graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2 + getInvNameOffset().x(), 5 + getInvNameOffset().y(), getTitleColor(), false);
        }

        if (getInvTextOffset() != null) {
            graphics.drawString(font, xlate("container.inventory"), 8 + getInvTextOffset().x(), imageHeight - 94 + getInvTextOffset().y(), 0x404040, false);
        }

        if (pressureStat != null) {
            PointXY gaugeLocation = getGaugeLocation();
            if (gaugeLocation != null) {
                AbstractAirHandlingBlockEntity pneu = (AbstractAirHandlingBlockEntity) te;
                float minWorking = te instanceof IMinWorkingPressure min ? min.getMinWorkingPressure() : -Float.MAX_VALUE;
                PressureGaugeRenderer2D.drawPressureGauge(graphics, font, -1, pneu.getCriticalPressure(), pneu.getDangerPressure(), minWorking, pneu.getPressure(), gaugeLocation.x() - leftPos, gaugeLocation.y() - topPos);
            }
        }
    }

    protected abstract ResourceLocation getGuiTexture();

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        for (IGuiAnimatedStat w : statWidgets) {
            if (w.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTick) {
        super.render(graphics, x, y, partialTick);
        renderTooltip(graphics, x, y);
    }

    protected PointXY getGaugeLocation() {
        return new PointXY(getGuiLeft() + imageWidth * 3 / 4, getGuiTop() + imageHeight / 4 + 4);
    }

    protected int getTitleColor() { return 0x404040; }

    protected PointXY getInvNameOffset() {
        return PointXY.ZERO;
    }

    protected PointXY getInvTextOffset() {
        return PointXY.ZERO;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (sendDelay > 0 && --sendDelay <= 0) {
            doDelayedAction();
            sendDelay = -1;
        }

        for (Renderable renderable : renderables) {
            if (renderable instanceof ITickableWidget t) {
                t.tickWidget();
            }
        }

        if (pressureStat != null && pressureStat.isDoneExpanding()) {
            List<Component> pressureText = new ArrayList<>();
            addPressureStatInfo(pressureText);
            pressureStat.setText(pressureText);
        }
        if (problemTab != null && ((ClientUtils.getClientLevel().getGameTime() & 0x7) == 0 || firstUpdate)) {
            handleProblemsTab();
        }
        if (redstoneTab != null && te instanceof IRedstoneControl<?> rc) {
            redstoneTab.setExtraTooltipText(Collections.singletonList(rc.getRedstoneController().getDescription()));
            for (int i = 0; i < redstoneButtons.size(); i++) {
                redstoneButtons.get(i).active = i != rc.getRedstoneMode();
            }
        }

        firstUpdate = false;
    }

    private void handleProblemsTab() {
        List<Component> problemText = new ArrayList<>();
        addProblems(problemText);
        int nProbs = problemText.size();
        addWarnings(problemText);
        int nWarnings = problemText.size() - nProbs;
        addInformation(problemText);

        if (nProbs > 0) {
            problemTab.setTexture(Textures.GUI_PROBLEMS_TEXTURE);
            problemTab.setMessage(xlate("pneumaticcraft.gui.tab.problems"));
            problemTab.setBackgroundColor(0xFFFF0000);
        } else if (nWarnings > 0) {
            problemTab.setTexture(Textures.GUI_WARNING_TEXTURE);
            problemTab.setMessage(xlate("pneumaticcraft.gui.tab.problems.warning"));
            problemTab.setBackgroundColor(0xFFC0C000);
        } else {
            problemTab.setTexture(Textures.GUI_NO_PROBLEMS_TEXTURE);
            problemTab.setMessage(xlate("pneumaticcraft.gui.tab.problems.noProblems"));
            problemTab.setBackgroundColor(0xFF80E080);
        }
        problemTab.setText(problemText);
    }

    protected void addPressureStatInfo(List<Component> pressureStatText) {
        PNCCapabilities.getAirHandler(te).ifPresent(airHandler -> {
            float curPressure = airHandler.getPressure();
            int volume = airHandler.getVolume();
            int upgrades = te.getUpgrades(ModUpgrades.VOLUME.get());
            airHandler.setVolumeUpgrades(upgrades);
            addPressureInfo(pressureStatText, curPressure, volume, airHandler.getBaseVolume(), upgrades);
        });
    }

    public void addPressureInfo(List<Component> text, float curPressure, int volume, int baseVolume, int upgrades) {
        text.add(xlate("pneumaticcraft.gui.tooltip.pressure",
                PneumaticCraftUtils.roundNumberTo(curPressure, 2)));
        text.add(xlate("pneumaticcraft.gui.tooltip.air", String.format("%,d", Math.round(curPressure * volume))));
        text.add(xlate("pneumaticcraft.gui.tooltip.baseVolume", String.format("%,d", baseVolume)));
        if (volume > baseVolume) {
            text.add(Component.literal(Symbols.TRIANGLE_RIGHT + " " + upgrades + " x ")
                    .append(ModUpgrades.VOLUME.get().getItemStack().getHoverName())
            );
            addExtraVolumeModifierInfo(text);
            text.add(xlate("pneumaticcraft.gui.tooltip.effectiveVolume", String.format("%,d",volume)));
        }
    }

    protected void addExtraVolumeModifierInfo(List<Component> text) {
        // nothing, override in subclasses
    }

    /**
     * Use this to add problem information; situations that prevent the machine from operating.
     *
     * @param curInfo string list to append to
     */
    protected void addProblems(List<Component> curInfo) {
        if (te instanceof IMinWorkingPressure) {
            float min = ((IMinWorkingPressure) te).getMinWorkingPressure();
            float pressure = ((AbstractAirHandlingBlockEntity) te).getPressure();
            if (min > 0 && pressure < min) {
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.notEnoughPressure"));
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.applyPressure", min));
            } else if (min < 0 && pressure > min) {
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.notEnoughVacuum"));
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.applyVacuum", min));
            }
        }
    }

    /**
     * Use this to add informational messages to the problems tab, which don't actually count as problems.
     *
     * @param curInfo string list to append to, which may already contain some problem text
     */
    protected void addInformation(List<Component> curInfo) {
    }

    /**
     * Use this to add warning messages; the machine will run but with potential problems.
     *
     * @param curInfo string list to append to, which may already contain some problem text
     */
    protected void addWarnings(List<Component> curInfo) {
        if (te instanceof IRedstoneControl<?> teR) {
            if (!teR.getRedstoneController().isEmitter() && !teR.getRedstoneController().shouldRun()) {
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.redstoneDisallows"));
                if (teR.getRedstoneMode() == 1) {
                    curInfo.add(xlate("pneumaticcraft.gui.tab.problems.provideRedstone"));
                } else {
                    curInfo.add(xlate("pneumaticcraft.gui.tab.problems.removeRedstone"));
                }
            }
        }
    }

    void sendGUIButtonPacketToServer(String tag) {
        NetworkHandler.sendToServer(new PacketGuiButton(tag));
    }

    public List<Rect2i> getTabRectangles() {
        return getStatWidgets().stream()
                .map(IGuiAnimatedStat::getBounds)
                .collect(Collectors.toList());
    }

    void refreshScreen() {
        Window mw = Minecraft.getInstance().getWindow();
        int i = mw.getGuiScaledWidth();
        int j = mw.getGuiScaledHeight();
        init(Minecraft.getInstance(), i, j);
        renderables.stream().filter(widget -> widget instanceof Tickable).forEach(w -> ((Tickable) w).tick());
    }

    /**
     * Schedule a delayed action to be done some time in the future. Calling this again will reset the delay.
     * Useful to avoid excessive network traffic if sending updates to the server from a textfield change.
     * @param ticks number of ticks to delay
     */
    protected void sendDelayed(int ticks) {
        sendDelay = ticks;
    }

    /**
     * Run the delayed action set up by sendDelayed()
     */
    protected void doDelayedAction() {
        // nothing; override in subclasses
    }

    @Override
    public void removed() {
        if (sendDelay > 0) doDelayedAction();  // ensure any pending delayed action is done

        super.removed();
    }

    protected boolean shouldParseVariablesInTooltips() {
        return false;
    }

    /**
     * Called when the client has received a PacketUpdateGui to sync something from the server-side container
     */
    public void onGuiUpdate() {
        // nothing; override in subclasses
    }

    public Collection<ItemStack> getTargetItems() {
        return Collections.emptyList();
    }

    public Collection<FluidStack> getTargetFluids() {
        return Collections.emptyList();
    }

    <R extends PneumaticCraftRecipe> Optional<RecipeHolder<R>> getCurrentRecipe(PneumaticCraftRecipeType<R> type) {
        String id = te.getCurrentRecipeIdSynced();
        return id.isEmpty() ? Optional.empty() :
                type.getRecipe(ClientUtils.getClientLevel(), new ResourceLocation(id));
    }

    protected RegistryAccess registryAccess() {
        if (minecraft == null || minecraft.player == null) {
            throw new IllegalStateException("can't get registry access from null minecraft/player!");
        }
        return minecraft.player.registryAccess();
    }

    private static class SideConfiguratorButton extends WidgetButtonExtended {
        private final RelativeFace relativeFace;

        public SideConfiguratorButton(int startX, int startY, RelativeFace relativeFace, OnPress pressable) {
            super(startX, startY, 20, 20, Component.empty(), pressable);
            this.relativeFace = relativeFace;
        }
    }
}
