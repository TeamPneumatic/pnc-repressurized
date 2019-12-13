package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GuiPneumaticContainerBase<C extends ContainerPneumaticBase<T>, T extends TileEntityBase> extends ContainerScreen<C> {
    public final T te;
    private IGuiAnimatedStat lastLeftStat, lastRightStat;
    private WidgetAnimatedStat pressureStat;
    private WidgetAnimatedStat redstoneTab;
    WidgetAnimatedStat problemTab;
    WidgetButtonExtended redstoneButton;
    protected boolean firstUpdate = true;
    private final List<IGuiAnimatedStat> statWidgets = new ArrayList<>();
    private int sendDelay = -1;

    public GuiPneumaticContainerBase(C container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        this.te = container.te;
    }

    @Override
    public void init() {
        super.init();

        lastLeftStat = lastRightStat = null;
        if (shouldAddPressureTab() && te instanceof TileEntityPneumaticBase) {
            pressureStat = this.addAnimatedStat("gui.tab.pressure", new ItemStack(ModBlocks.PRESSURE_TUBE), 0xFF00AA00, false);
        }
        if (shouldAddProblemTab()) {
            problemTab = addAnimatedStat("gui.tab.problems", 0xFFA0A0A0, false);
        }
        if (te != null) {
            if (shouldAddInfoTab()) {
                addInfoTab("gui.tab.info.tile." + te.getType().getRegistryName().getPath());
            }
            if (shouldAddRedstoneTab() && te instanceof IRedstoneControl) {
                addRedstoneTab();
            }
            if (te instanceof IHeatExchanger) {
                addAnimatedStat("gui.tab.info.heat.title", new ItemStack(Items.BLAZE_POWDER), 0xFFFF5500, false).setText("gui.tab.info.heat");
            }
            if (shouldAddUpgradeTab()) {
                addUpgradeTab();
            }
            if (shouldAddSideConfigTabs()) {
                addSideConfiguratorTabs();
            }
        }
    }

    private WidgetAnimatedStat addAnimatedStat(String title, StatIcon icon, int color, boolean leftSided) {
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

    protected WidgetAnimatedStat addAnimatedStat(String title, @Nonnull ItemStack icon, int color, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), color, leftSided);
    }

    protected WidgetAnimatedStat addAnimatedStat(String title, ResourceLocation icon, int color, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), color, leftSided);
    }

    protected WidgetAnimatedStat addAnimatedStat(String title, int color, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.NONE, color, leftSided);
    }

    protected void addLabel(String text, int x, int y) {
        addButton(new WidgetLabel(x, y, text));
    }

    protected void addLabel(String text, int x, int y, int color) {
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
        redstoneTab = addAnimatedStat("gui.tab.redstoneBehaviour", new ItemStack(Items.REDSTONE), 0xFFCC0000, true);
        List<String> curInfo = new ArrayList<>();
        curInfo.add(I18n.format(te.getRedstoneTabTitle()));
        int width = getWidestRedstoneLabel();
        redstoneTab.addPadding(curInfo,4, width / font.getStringWidth(" "));
        Rectangle2d buttonRect = redstoneTab.getButtonScaledRectangle(-width - 12, 24, width + 10, 20);
        redstoneButton = new WidgetButtonExtended(buttonRect.getX(), buttonRect.getY(), buttonRect.getWidth(), buttonRect.getHeight(), "-", button -> { }).withTag(IGUIButtonSensitive.REDSTONE_TAG);
        redstoneTab.addSubWidget(redstoneButton);
    }

    private void addUpgradeTab() {
        String upgrades = "gui.tab.upgrades.tile." + te.getType().getRegistryName().getPath();
        String translatedUpgrades = I18n.format(upgrades);
        List<String> upgradeText = new ArrayList<>();
        if (te instanceof TileEntityPneumaticBase) {
            upgradeText.add("gui.tab.upgrades.volume");
            upgradeText.add("gui.tab.upgrades.security");
        }
        if (te instanceof IHeatExchanger) {
            upgradeText.add("gui.tab.upgrades.volumeCapacity");
        }
        if (!translatedUpgrades.equals(upgrades)) upgradeText.add(upgrades);

        addExtraUpgradeText(upgradeText);

        if (upgradeText.size() > 0)
            addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF6060FF, true).setText(upgradeText);
    }

    protected void addExtraUpgradeText(List<String> upgradeText) {
    }

    private int getWidestRedstoneLabel() {
        int max = 0;
        for (int i = 0; i < te.getRedstoneModeCount(); i++) {
            max = Math.max(max, font.getStringWidth(I18n.format(te.getRedstoneButtonText(i))));
        }
        return max;
    }

    private void addSideConfiguratorTabs() {
        for (SideConfigurator sc : ((ISideConfigurable) te).getSideConfigurators()) {
            WidgetAnimatedStat stat = addAnimatedStat(sc.getTranslationKey(), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER), 0xFF90C0E0, false);
            stat.addPadding(7, 16);

            int yTop = 15, xLeft = 25;
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.TOP, xLeft + 22, yTop));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.LEFT, xLeft, yTop + 22));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.FRONT, xLeft + 22, yTop + 22));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.RIGHT, xLeft + 44, yTop + 22));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.BOTTOM, xLeft + 22, yTop + 44));
            stat.addSubWidget(makeSideConfButton(sc, RelativeFace.BACK, xLeft + 44, yTop + 44));
        }
    }

    private WidgetButtonExtended makeSideConfButton(final SideConfigurator sideConfigurator, RelativeFace relativeFace, int x, int y) {
        WidgetButtonExtended button = new WidgetButtonExtended(x, y, 20, 20, "", b -> {
            WidgetButtonExtended gbs = (WidgetButtonExtended) b;
            ((ISideConfigurable) te).getSideConfigurators().stream()
                    .filter(sc -> sc.handleButtonPress(gbs.getTag()))
                    .findFirst()
                    .ifPresent(sc -> sc.setupButton(gbs));
        }).withTag("SideConf." + relativeFace.toString());
        sideConfigurator.setupButton(button);
        return button;
    }

    protected void addInfoTab(String info) {
        IGuiAnimatedStat stat = addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true);
        stat.setText(info);
        if (!ThirdPartyManager.instance().docsProvider.docsProviderInstalled()) {
            stat.appendText(Arrays.asList("", "gui.tab.info.assistIGW"));
        }
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
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j) {
        if (shouldDrawBackground()) {
            RenderUtils.glColorHex(0xFF000000 | getBackgroundTint());
            bindGuiTexture();
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            blit(xStart, yStart, 0, 0, xSize, ySize);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        if (getInvNameOffset() != null) {
            String containerName = title.getFormattedText();
            font.drawString(containerName, xSize / 2f - font.getStringWidth(containerName) / 2f + getInvNameOffset().x, 6 + getInvNameOffset().y, getTitleColor());
        }

        if (getInvTextOffset() != null) {
            font.drawString(I18n.format("container.inventory"), 8 + getInvTextOffset().x, ySize - 94 + getInvTextOffset().y, 0x404040);
        }

        if (pressureStat != null) {
            Point gaugeLocation = getGaugeLocation();
            if (gaugeLocation != null) {
                TileEntityPneumaticBase pneu = (TileEntityPneumaticBase) te;
                GuiUtils.drawPressureGauge(font, -1, pneu.criticalPressure, pneu.dangerPressure, te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : -Float.MAX_VALUE, pneu.getPressure(), gaugeLocation.x - guiLeft, gaugeLocation.y - guiTop);
            }
        }
    }

    protected void bindGuiTexture() {
        ResourceLocation guiTexture = getGuiTexture();
        if (guiTexture != null) {
            minecraft.getTextureManager().bindTexture(guiTexture);
            GlStateManager.enableTexture();
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
    public void render(int x, int y, float partialTick) {
        renderBackground();

        super.render(x, y, partialTick);

        renderHoveredToolTip(x, y);

        List<String> tooltip = new ArrayList<>();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.disableLighting();
        for (Widget widget : buttons) {
            if (widget instanceof ITooltipProvider && widget.isHovered() && widget.visible) {
                ((ITooltipProvider) widget).addTooltip(x, y, tooltip, PneumaticCraftRepressurized.proxy.isSneakingInGui());
            }
        }

        if (tooltip.size() > 0) {
            drawHoveringString(tooltip, x, y, font);
            tooltip.clear();
        }
    }

    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4, yStart + ySize / 4 + 4);
    }

    protected int getTitleColor() { return 0x404040; }

    protected Point getInvNameOffset() {
        return new Point(0, 0);
    }

    protected Point getInvTextOffset() {
        return new Point(0, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (sendDelay > 0 && --sendDelay <= 0) {
            doDelayedAction();
            sendDelay = -1;
        }

        buttons.stream().filter(w -> w instanceof ITickable).forEach(w -> ((ITickable) w).tick());

        if (pressureStat != null) {
            List<String> pressureText = new ArrayList<>();
            addPressureStatInfo(pressureText);
            pressureStat.setText(pressureText);
        }
        if (problemTab != null && ((Minecraft.getInstance().world.getGameTime() & 0x7) == 0 || firstUpdate)) {
            handleProblemsTab();
        }
        if (redstoneTab != null) {
            redstoneButton.setMessage(I18n.format(te.getRedstoneButtonText(((IRedstoneControl) te).getRedstoneMode())));
        }

        firstUpdate = false;
    }

    private void handleProblemsTab() {
        List<String> problemText = new ArrayList<>();
        addProblems(problemText);
        int nProbs = problemText.size();
        addWarnings(problemText);
        int nWarnings = problemText.size() - nProbs;
        addInformation(problemText);

        if (nProbs > 0) {
            problemTab.setTexture(Textures.GUI_PROBLEMS_TEXTURE);
            problemTab.setTitle("gui.tab.problems");
            problemTab.setBackgroundColor(0xFFFF0000);
        } else if (nWarnings > 0) {
            problemTab.setTexture(Textures.GUI_WARNING_TEXTURE);
            problemTab.setTitle("gui.tab.problems.warning");
            problemTab.setBackgroundColor(0xFFC0C000);
        } else {
            problemTab.setTexture(Textures.GUI_NO_PROBLEMS_TEXTURE);
            problemTab.setTitle("gui.tab.problems.noProblems");
            problemTab.setBackgroundColor(0xFFA0FFA0);
        }
        if (problemText.isEmpty()) problemText.add("");
        problemTab.setText(problemText);
    }

    protected void addPressureStatInfo(List<String> pressureStatText) {
        TileEntityPneumaticBase pneumaticTile = (TileEntityPneumaticBase) te;
        IAirHandler airHandler = pneumaticTile.getAirHandler(null);
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(pneumaticTile.getPressure(), 1) + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (airHandler.getAir() + airHandler.getVolume()) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + pneumaticTile.getDefaultVolume() + " mL.");
        int volumeLeft = airHandler.getVolume() - pneumaticTile.getDefaultVolume();
        if (volumeLeft > 0) {
            pressureStatText.add("\u00a70" + volumeLeft + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + airHandler.getVolume() + " mL.");
        }
    }

    /**
     * Use this to add problem information; situations that prevent the machine from operating.
     *
     * @param curInfo string list to append to
     */
    protected void addProblems(List<String> curInfo) {
        if (te instanceof IMinWorkingPressure) {
            IMinWorkingPressure minWork = (IMinWorkingPressure) te;
            if (((TileEntityPneumaticBase) te).getPressure() < minWork.getMinWorkingPressure()) {
                curInfo.add("gui.tab.problems.notEnoughPressure");
                curInfo.add(I18n.format("gui.tab.problems.applyPressure", minWork.getMinWorkingPressure()));
            }
        }
    }

    /**
     * Use this to add informational messages to the problems tab, which don't actually count as problems.
     *
     * @param curInfo string list to append to, which may already contain some problem text
     */
    protected void addInformation(List<String> curInfo) {
    }

    /**
     * Use this to add warning messages; the machine will run but with potential problems.
     *
     * @param curInfo string list to append to, which may already contain some problem text
     */
    protected void addWarnings(List<String> curInfo) {
        if (te instanceof IRedstoneControlled && !te.redstoneAllows()) {
            IRedstoneControlled redstoneControlled = (IRedstoneControlled) te;
            curInfo.add("gui.tab.problems.redstoneDisallows");
            if (redstoneControlled.getRedstoneMode() == 1) {
                curInfo.add("gui.tab.problems.provideRedstone");
            } else {
                curInfo.add("gui.tab.problems.removeRedstone");
            }
        }
    }

    void sendPacketToServer(String tag) {
        NetworkHandler.sendToServer(new PacketGuiButton(tag));
    }

    void drawHoveringString(List<String> text, int x, int y, FontRenderer fontRenderer) {
        net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(text, x, y, width, height, -1, fontRenderer);
    }

    WidgetButtonExtended getButtonFromRectangle(String tag, Rectangle2d buttonSize, String buttonText, Button.IPressable pressable) {
        return new WidgetButtonExtended(buttonSize.getX(), buttonSize.getY(), buttonSize.getWidth(), buttonSize.getHeight(), buttonText, pressable).withTag(tag);
    }

    WidgetButtonExtended getInvisibleButtonFromRectangle(String tag, Rectangle2d buttonSize, Button.IPressable pressable) {
        return new WidgetButtonExtended(buttonSize.getX(), buttonSize.getY(), buttonSize.getWidth(), buttonSize.getHeight(), "", pressable).withTag(tag);
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
        MainWindow mw = Minecraft.getInstance().mainWindow;
        int i = mw.getScaledWidth();
        int j = mw.getScaledHeight();
        init(Minecraft.getInstance(), i, j);
        buttons.stream().filter(widget -> widget instanceof ITickable).forEach(w -> ((ITickable) w).tick());
    }

    void sendDelayed(int ticks) {
        sendDelay = ticks;
    }

    protected void doDelayedAction() {
        // nothing; override in subclasses
    }
}
