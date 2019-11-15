package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat.StatIcon;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

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
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat redstoneTab;
    GuiAnimatedStat problemTab;
    GuiButtonSpecial redstoneButton;
    protected boolean firstUpdate = true;
    private final List<IGuiAnimatedStat> statWidgets = new ArrayList<>();

    public GuiPneumaticContainerBase(C container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        this.te = container.te;
    }

    private GuiAnimatedStat addAnimatedStat(String title, StatIcon icon, int color, boolean leftSided) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize + 1), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
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

    protected GuiAnimatedStat addAnimatedStat(String title, @Nonnull ItemStack icon, int color, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), color, leftSided);
    }

    protected GuiAnimatedStat addAnimatedStat(String title, ResourceLocation icon, int color, boolean leftSided) {
        return addAnimatedStat(title, StatIcon.of(icon), color, leftSided);
    }

    protected GuiAnimatedStat addAnimatedStat(String title, int color, boolean leftSided) {
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
                addInfoTab("gui.tab.info." + te.getType().getRegistryName().getPath());
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

    private void addRedstoneTab() {
        redstoneTab = addAnimatedStat("gui.tab.redstoneBehaviour", new ItemStack(Items.REDSTONE), 0xFFCC0000, true);
        List<String> curInfo = new ArrayList<>();
        curInfo.add(I18n.format(te.getRedstoneTabTitle()));
        int width = getWidestRedstoneLabel();
        redstoneTab.addPadding(curInfo,4, width / font.getStringWidth(" "));
        Rectangle buttonRect = redstoneTab.getButtonScaledRectangle(-width - 12, 24, width + 10, 20);
        redstoneButton = new GuiButtonSpecial(buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height, "-", button -> { }).withTag(IGUIButtonSensitive.REDSTONE_TAG);
        redstoneTab.addSubWidget(redstoneButton);
    }

    private void addUpgradeTab() {
        String upgrades = "gui.tab.upgrades." + te.getType().getRegistryName().getPath();
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
            GuiAnimatedStat stat = addAnimatedStat(sc.getTranslationKey(), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER), 0xFF90C0E0, false);
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

    private GuiButtonSpecial makeSideConfButton(final SideConfigurator sideConfigurator, RelativeFace relativeFace, int x, int y) {
        GuiButtonSpecial button = new GuiButtonSpecial(x, y, 20, 20, "", b -> {
            GuiButtonSpecial gbs = (GuiButtonSpecial) b;
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
                GuiUtils.drawPressureGauge(font, -1, pneu.criticalPressure, pneu.dangerPressure, te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : -Float.MAX_VALUE, pneu.getPressure(), gaugeLocation.x, gaugeLocation.y);
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
    public void render(int x, int y, float partialTick) {
        renderBackground();

        super.render(x, y, partialTick);

        renderHoveredToolTip(x, y);

        List<String> tooltip = new ArrayList<>();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.disableLighting();
        for (Widget widget : buttons) {
            if (widget instanceof ITooltipSupplier && widget.isHovered()) {
                ((ITooltipSupplier) widget).addTooltip(x, y, tooltip, PneumaticCraftRepressurized.proxy.isSneakingInGui());
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
            problemTab.setBackGroundColor(0xFFFF0000);
        } else if (nWarnings > 0) {
            problemTab.setTexture(Textures.GUI_WARNING_TEXTURE);
            problemTab.setTitle("gui.tab.problems.warning");
            problemTab.setBackGroundColor(0xFFC0C000);
        } else {
            problemTab.setTexture(Textures.GUI_NO_PROBLEMS_TEXTURE);
            problemTab.setTitle("gui.tab.problems.noProblems");
            problemTab.setBackGroundColor(0xFFA0FFA0);
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

    public static void drawTexture(ResourceLocation texture, int x, int y) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + 16, 0).tex(0.0, 1.0).endVertex();
        wr.pos(x + 16, y + 16, 0).tex(1.0, 1.0).endVertex();
        wr.pos(x + 16, y, 0).tex(1.0, 0.0).endVertex();
        wr.pos(x, y, 0).tex(0.0, 0.0).endVertex();
        Tessellator.getInstance().draw();
    }

    GuiButtonSpecial getButtonFromRectangle(String tag, Rectangle buttonSize, String buttonText, Button.IPressable pressable) {
        return new GuiButtonSpecial(buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, buttonText, pressable).withTag(tag);
    }

    GuiButtonSpecial getInvisibleButtonFromRectangle(String tag, Rectangle buttonSize, Button.IPressable pressable) {
        return new GuiButtonSpecial(buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, "", pressable).withTag(tag);
    }

    WidgetTextField getTextFieldFromRectangle(Rectangle textFieldSize) {
        return new WidgetTextField(font, textFieldSize.x, textFieldSize.y, textFieldSize.width, textFieldSize.height);
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

//    void refreshScreen() {
//        MainWindow mw = Minecraft.getInstance().mainWindow;
////        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
//        int i = mw.getScaledWidth();
//        int j = mw.getScaledHeight();
//        init(Minecraft.getInstance(), i, j);
////        setWorldAndResolution(Minecraft.getMinecraft(), i, j);
//        widgets.stream().filter(widget -> widget instanceof GuiAnimatedStat).forEach(IGuiWidget::update);
//    }
}
