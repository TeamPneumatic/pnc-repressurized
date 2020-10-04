package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class GuiPneumaticContainerBase<C extends ContainerPneumaticBase<T>, T extends TileEntityBase> extends ContainerScreen<C> {
    public final T te;
    private IGuiAnimatedStat lastLeftStat, lastRightStat;
    private WidgetAnimatedStat pressureStat;
    private WidgetAnimatedStat redstoneTab;
    WidgetAnimatedStat problemTab;
    private WidgetButtonExtended redstoneButton;
    boolean firstUpdate = true;
    private final List<IGuiAnimatedStat> statWidgets = new ArrayList<>();
    private int sendDelay = -1;
    boolean redstoneAllows;

    public GuiPneumaticContainerBase(C container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        this.te = container.te;
    }

    @Override
    public void init() {
        super.init();

        lastLeftStat = lastRightStat = null;
        if (shouldAddPressureTab() && te instanceof TileEntityPneumaticBase) {
            pressureStat = this.addAnimatedStat(xlate("pneumaticcraft.gui.tab.pressure"), new ItemStack(ModBlocks.PRESSURE_TUBE.get()), 0xFF00AA00, false);
            pressureStat.setForegroundColor(0xFF000000);
        }
        if (shouldAddProblemTab()) {
            problemTab = addAnimatedStat(xlate("pneumaticcraft.gui.tab.problems"), 0xFFA0A0A0, false);
            problemTab.setMinimumExpandedDimensions(0, 16);
        }
        if (te != null) {
            if (shouldAddInfoTab()) {
                addInfoTab(GuiUtils.xlateAndSplit(ICustomTooltipName.getTranslationKey(new ItemStack(te.getBlockState().getBlock()), false)));
            }
            if (shouldAddRedstoneTab() && te instanceof IRedstoneControl) {
                addRedstoneTab();
            }
            if (te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).isPresent()) {
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
        }
    }

    private WidgetAnimatedStat addAnimatedStat(ITextComponent title, StatIcon icon, int color, boolean leftSided) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        WidgetAnimatedStat stat = new WidgetAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize + 1), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        stat.setBeveled(true);
        addButton(stat);
        if (leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        statWidgets.add(stat);
        return stat;
    }

    protected WidgetAnimatedStat addAnimatedStat(ITextComponent title, @Nonnull ItemStack icon, int backgroundColor, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), backgroundColor, leftSided);
    }

    protected WidgetAnimatedStat addAnimatedStat(ITextComponent title, ResourceLocation icon, int backgroundColor, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), backgroundColor, leftSided);
    }

    protected WidgetAnimatedStat addAnimatedStat(ITextComponent title, int backgroundColor, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.NONE, backgroundColor, leftSided);
    }

    protected void addLabel(ITextComponent text, int x, int y) {
        addButton(new WidgetLabel(x, y, text));
    }

    protected void addLabel(ITextComponent text, int x, int y, int color) {
        addButton(new WidgetLabel(x, y, text, color));
    }

    void removeWidget(Widget widget) {
        buttons.remove(widget);
        children.remove(widget);
        if (widget instanceof IGuiAnimatedStat) statWidgets.remove(widget);
    }

    public List<IGuiAnimatedStat> getStatWidgets() {
        return statWidgets;
    }

    private void addRedstoneTab() {
        redstoneTab = addAnimatedStat(xlate("pneumaticcraft.gui.tab.redstoneBehaviour"), new ItemStack(Items.REDSTONE), 0xFFCC0000, true);
        int width = getWidestRedstoneLabel();
        redstoneTab.setText(te.getRedstoneTabTitle());
        redstoneTab.setMinimumExpandedDimensions(width, 45);
        redstoneButton = new WidgetButtonExtended(-width - 12, 24, width + 10, 18, StringTextComponent.EMPTY, button -> { })
                .withTag(IGUIButtonSensitive.REDSTONE_TAG);
        redstoneTab.addSubWidget(redstoneButton);
    }

    protected void addJeiFilterInfoTab() {
        if (ModList.get().isLoaded(ModIds.JEI)) {
            addAnimatedStat(new StringTextComponent("JEI"), Textures.GUI_JEI_LOGO, 0xFFCEEDCE, true)
                    .setText(xlate("pneumaticcraft.gui.jei.filterDrag").mergeStyle(TextFormatting.DARK_GRAY));
        }
    }

    protected String upgradeCategory() {
        return te.getType().getRegistryName().getPath();
    }

    private void addUpgradeTab() {
        List<ITextComponent> text = new ArrayList<>();
        te.getApplicableUpgrades().keySet().stream()
                .sorted(Comparator.comparing(o -> o.getItemStack().getDisplayName().getString()))
                .forEach(upgrade -> {
                    int max = te.getApplicableUpgrades().get(upgrade);
                    text.add(upgrade.getItemStack().getDisplayName().deepCopy().mergeStyle(TextFormatting.WHITE, TextFormatting.UNDERLINE));
                    text.add(xlate("pneumaticcraft.gui.tab.upgrades.max", max).mergeStyle(TextFormatting.GRAY));
                    String upgradeName = upgrade.toString().toLowerCase(Locale.ROOT);
                    String k = "pneumaticcraft.gui.tab.upgrades." + upgradeCategory() + "." + upgradeName;
                    text.addAll(I18n.hasKey(k) ? GuiUtils.xlateAndSplit(k) : GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.upgrades.generic." + upgradeName));
                    text.add(StringTextComponent.EMPTY);
                });
        if (!text.isEmpty()) {
            addAnimatedStat(xlate("pneumaticcraft.gui.tab.upgrades"), Textures.GUI_UPGRADES_LOCATION, 0xFF244BB3, true)
                    .setText(text).setForegroundColor(0xFF000000);
        }
    }

    protected void addExtraUpgradeText(List<ITextComponent> upgradeText) {
    }

    private int getWidestRedstoneLabel() {
        int max = 0;
        for (int i = 0; i < te.getRedstoneModeCount(); i++) {
            max = Math.max(max, font.getStringPropertyWidth(te.getRedstoneButtonText(i)));
        }
        return max;
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

    private WidgetButtonExtended makeSideConfButton(final SideConfigurator<?> sideConfigurator, RelativeFace relativeFace, int x, int y) {
        WidgetButtonExtended button = new WidgetButtonExtended(x, y, 20, 20, StringTextComponent.EMPTY, b -> {
            WidgetButtonExtended gbs = (WidgetButtonExtended) b;
            ((ISideConfigurable) te).getSideConfigurators().stream()
                    .filter(sc -> sc.handleButtonPress(gbs.getTag()))
                    .findFirst()
                    .ifPresent(sc -> setupSideConfiguratorButton(sc, gbs));
        }).withTag("SideConf." + relativeFace.toString());
        setupSideConfiguratorButton(sideConfigurator, button);
        return button;
    }

    private void setupSideConfiguratorButton(SideConfigurator<?> sc, WidgetButtonExtended button) {
        try {
            RelativeFace relativeFace = RelativeFace.valueOf(button.getTag().split("\\.")[1]);
            SideConfigurator.ConnectionEntry<?> c = sc.getEntry(relativeFace);
            if (c != null && c.getTexture() != null) {
                button.setTexture(c.getTexture());
            } else {
                button.setRenderedIcon(Textures.GUI_X_BUTTON);
            }
            button.setTooltipText(ImmutableList.of(
                    new StringTextComponent(relativeFace.toString()).mergeStyle(TextFormatting.YELLOW),
                    sc.getFaceLabel(relativeFace)
            ));
        } catch (IllegalArgumentException e) {
            Log.warning("Bad tag '" + button.getTag() + "'");
        }
    }

    protected void addInfoTab(List<ITextComponent> info) {
        IGuiAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info"), Textures.GUI_INFO_LOCATION, 0xFF8888FF, true);
        stat.setForegroundColor(0xFF000000);
        stat.setText(info);
        if (!ThirdPartyManager.instance().getDocsProvider().isInstalled()) {
            stat.appendText(Arrays.asList(StringTextComponent.EMPTY, xlate("pneumaticcraft.gui.tab.info.installDocsProvider")));
        }
    }

    protected void addInfoTab(ITextComponent info) {
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

    protected int getBackgroundTint() { return 0xFFFFFF; }

    protected boolean shouldDrawBackground() {
        return true;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int i, int j) {
        if (shouldDrawBackground()) {
            GuiUtils.glColorHex(0xFF000000 | getBackgroundTint());
            bindGuiTexture();
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            blit(matrixStack, xStart, yStart, 0, 0, xSize, ySize);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        if (getInvNameOffset() != null) {
            font.func_238422_b_(matrixStack, title.func_241878_f(), xSize / 2f - font.getStringPropertyWidth(title) / 2f + getInvNameOffset().x, 5 + getInvNameOffset().y, getTitleColor());
        }

        if (getInvTextOffset() != null) {
            font.drawString(matrixStack, I18n.format("container.inventory"), 8 + getInvTextOffset().x, ySize - 94 + getInvTextOffset().y, 0x404040);
        }

        if (pressureStat != null) {
            PointXY gaugeLocation = getGaugeLocation();
            if (gaugeLocation != null) {
                TileEntityPneumaticBase pneu = (TileEntityPneumaticBase) te;
                float minWorking = te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : -Float.MAX_VALUE;
                PressureGaugeRenderer2D.drawPressureGauge(matrixStack, font, -1, pneu.criticalPressure, pneu.dangerPressure, minWorking, pneu.getPressure(), gaugeLocation.x - guiLeft, gaugeLocation.y - guiTop);
            }
        }
    }

    void bindGuiTexture() {
        ResourceLocation guiTexture = getGuiTexture();
        if (guiTexture != null) {
            minecraft.getTextureManager().bindTexture(guiTexture);
            RenderSystem.enableTexture();
        }
    }

    protected abstract ResourceLocation getGuiTexture();

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        for (IGuiAnimatedStat w : statWidgets) {
            if (((WidgetAnimatedStat) w).mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTick) {
        renderBackground(matrixStack);

        super.render(matrixStack, x, y, partialTick);

        renderHoveredTooltip(matrixStack, x, y);

        List<ITextComponent> tooltip = new ArrayList<>();
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.disableLighting();
        for (Widget widget : buttons) {
            if (widget instanceof ITooltipProvider && widget.isHovered() && widget.visible) {
                ((ITooltipProvider) widget).addTooltip(x, y, tooltip, Screen.hasShiftDown());
            }
        }
        if (shouldParseVariablesInTooltips()) {
            for (int i = 0; i < tooltip.size(); i++) {
                tooltip.set(i, new StringTextComponent(new TextVariableParser(tooltip.get(i).getString()).parse()));
            }
        }

        if (!tooltip.isEmpty()) {
            int max = Math.min(xSize * 4 / 3, width / 3);
            renderTooltip(matrixStack, GuiUtils.wrapTextComponentList(tooltip, max, font), x, y);
        }
    }

    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + xSize * 3 / 4, yStart + ySize / 4 + 4);
    }

    protected int getTitleColor() { return 0x404040; }

    protected PointXY getInvNameOffset() {
        return PointXY.ZERO;
    }

    protected PointXY getInvTextOffset() {
        return PointXY.ZERO;
    }

    @Override
    public void tick() {
        super.tick();

        if (sendDelay > 0 && --sendDelay <= 0) {
            doDelayedAction();
            sendDelay = -1;
        }

        buttons.stream().filter(w -> w instanceof ITickableWidget).forEach(w -> ((ITickableWidget) w).tickWidget());

        if (pressureStat != null) {
            List<ITextComponent> pressureText = new ArrayList<>();
            addPressureStatInfo(pressureText);
            pressureStat.setText(pressureText);
        }
        if (problemTab != null && ((Minecraft.getInstance().world.getGameTime() & 0x7) == 0 || firstUpdate)) {
            handleProblemsTab();
        }
        if (redstoneTab != null) {
            redstoneButton.setMessage(te.getRedstoneButtonText(((IRedstoneControl) te).getRedstoneMode()));
        }
        if (te instanceof IRedstoneControlled) {
            redstoneAllows = te.redstoneAllows();
        }

        firstUpdate = false;
    }

    private void handleProblemsTab() {
        List<ITextComponent> problemText = new ArrayList<>();
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

    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(airHandler -> {
            float curPressure = airHandler.getPressure();
            int volume = airHandler.getVolume();
            int upgrades = te.getUpgrades(EnumUpgrade.VOLUME);
            airHandler.setVolumeUpgrades(upgrades);
            addPressureInfo(pressureStatText, curPressure, volume, airHandler.getBaseVolume(), upgrades);
        });
    }

    void addPressureInfo(List<ITextComponent> text, float curPressure, int volume, int baseVolume, int upgrades) {
        text.add(xlate("pneumaticcraft.gui.tooltip.pressure",
                PneumaticCraftUtils.roundNumberTo(curPressure, 2)));
        text.add(xlate("pneumaticcraft.gui.tooltip.air", String.format("%,d", Math.round(curPressure * volume))));
        text.add(xlate("pneumaticcraft.gui.tooltip.baseVolume", String.format("%,d", baseVolume)));
        if (volume > baseVolume) {
            text.add(new StringTextComponent(GuiConstants.TRIANGLE_RIGHT + " " + upgrades + " x ")
                    .append(EnumUpgrade.VOLUME.getItemStack().getDisplayName())
            );
            text.add(xlate("pneumaticcraft.gui.tooltip.effectiveVolume", String.format("%,d",volume)));
        }
    }

    /**
     * Use this to add problem information; situations that prevent the machine from operating.
     *
     * @param curInfo string list to append to
     */
    protected void addProblems(List<ITextComponent> curInfo) {
        if (te instanceof IMinWorkingPressure) {
            float min = ((IMinWorkingPressure) te).getMinWorkingPressure();
            float pressure = ((TileEntityPneumaticBase) te).getPressure();
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
    protected void addInformation(List<ITextComponent> curInfo) {
    }

    /**
     * Use this to add warning messages; the machine will run but with potential problems.
     *
     * @param curInfo string list to append to, which may already contain some problem text
     */
    protected void addWarnings(List<ITextComponent> curInfo) {
        if (te instanceof IRedstoneControlled && !redstoneAllows) {
            IRedstoneControlled redstoneControlled = (IRedstoneControlled) te;
            curInfo.add(xlate("pneumaticcraft.gui.tab.problems.redstoneDisallows"));
            if (redstoneControlled.getRedstoneMode() == 1) {
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.provideRedstone"));
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.tab.problems.removeRedstone"));
            }
        }
    }

    void sendGUIButtonPacketToServer(String tag) {
        NetworkHandler.sendToServer(new PacketGuiButton(tag));
    }

    void drawHoveringString(MatrixStack matrixStack, List<? extends ITextProperties> text, int x, int y, FontRenderer fontRenderer) {
        net.minecraftforge.fml.client.gui.GuiUtils.drawHoveringText(matrixStack, text, x, y, width, height, -1, fontRenderer);
    }

    WidgetButtonExtended getButtonFromRectangle(String tag, Rectangle2d buttonSize, String buttonText, Button.IPressable pressable) {
        return new WidgetButtonExtended(buttonSize.getX(), buttonSize.getY(), buttonSize.getWidth(), buttonSize.getHeight(), buttonText, pressable).withTag(tag);
    }

    WidgetButtonExtended getInvisibleButtonFromRectangle(String tag, Rectangle2d buttonSize, Button.IPressable pressable) {
        return new WidgetButtonExtended(buttonSize.getX(), buttonSize.getY(), buttonSize.getWidth(), buttonSize.getHeight(), StringTextComponent.EMPTY, pressable).withTag(tag);
    }

    WidgetTextField getTextFieldFromRectangle(Rectangle2d textFieldSize) {
        return new WidgetTextField(font, textFieldSize.getX(), textFieldSize.getY(), textFieldSize.getWidth(), textFieldSize.getHeight());
    }

    @Override
    public int getGuiLeft() {
        return guiLeft;
    }

    @Override
    public int getGuiTop() {
        return guiTop;
    }

    public Collection<Rectangle2d> getTabRectangles() {
        return getStatWidgets().stream()
                .map(IGuiAnimatedStat::getBounds)
                .collect(Collectors.toList());
    }

    void refreshScreen() {
        MainWindow mw = Minecraft.getInstance().getMainWindow();
        int i = mw.getScaledWidth();
        int j = mw.getScaledHeight();
        init(Minecraft.getInstance(), i, j);
        buttons.stream().filter(widget -> widget instanceof ITickable).forEach(w -> ((ITickable) w).tick());
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
    public void onClose() {
        if (sendDelay > 0) doDelayedAction();  // ensure any pending delayed action is done

        super.onClose();
    }

    protected boolean shouldParseVariablesInTooltips() {
        return false;
    }
}
