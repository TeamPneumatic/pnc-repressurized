package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.remote.TextVariableParser;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiPneumaticContainerBase<Tile extends TileEntityBase> extends GuiContainer implements IWidgetListener {

    public final Tile te;
    private final ResourceLocation guiTexture;
    /**
     * Any GuiAnimatedStat added to this list will be tracked for mouseclicks, tooltip renders, rendering,updating (resolution and expansion).
     */
    protected final List<IGuiWidget> widgets = new ArrayList<>();
    private IGuiAnimatedStat lastLeftStat, lastRightStat;

    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat redstoneTab;
    GuiAnimatedStat problemTab;
    GuiButtonSpecial redstoneButton;
    private boolean hasInit; //Fix for some weird race condition occuring in 1.8 where drawing is called before initGui().
    protected boolean firstUpdate = true;

    public GuiPneumaticContainerBase(Container par1Container, Tile te, String guiTexture) {
        super(par1Container);
        this.te = te;
        this.guiTexture = guiTexture != null ? new ResourceLocation(guiTexture) : null;
    }

    private GuiAnimatedStat addAnimatedStat(String title, StatIcon icon, int color, boolean leftSided) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize + 1), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        stat.setBeveled(true);
        addWidget(stat);
        if (leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
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

    protected void addWidget(IGuiWidget widget) {
        widgets.add(widget);
        widget.setListener(this);
    }

    protected void addWidgets(Iterable<IGuiWidget> widgets) {
        for (IGuiWidget widget : widgets) {
            addWidget(widget);
        }
    }

    protected void addLabel(String text, int x, int y) {
        addWidget(new WidgetLabel(x, y, text));
    }

    protected void addLabel(String text, int x, int y, int color) {
        addWidget(new WidgetLabel(x, y, text, color));
    }

    protected void removeWidget(IGuiWidget widget) {
        widgets.remove(widget);
    }

    @Override
    public void initGui() {
        super.initGui();
        lastLeftStat = lastRightStat = null;
        if (shouldAddPressureTab() && te instanceof TileEntityPneumaticBase) {
            pressureStat = this.addAnimatedStat("gui.tab.pressure", new ItemStack(Blockss.PRESSURE_TUBE), 0xFF00AA00, false);
        }
        if (shouldAddProblemTab()) {
            problemTab = addAnimatedStat("gui.tab.problems", 0xFFA0A0A0, false);
        }
        if (te != null) {
            if (shouldAddInfoTab()) {
                addInfoTab("gui.tab.info." + te.getName());
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
        hasInit = true;
    }

    private void addRedstoneTab() {
        redstoneTab = addAnimatedStat("gui.tab.redstoneBehaviour", new ItemStack(Items.REDSTONE), 0xFFCC0000, true);
        List<String> curInfo = new ArrayList<>();
        curInfo.add(I18n.format(te.getRedstoneTabTitle()));
        int width = getWidestRedstoneLabel();
        redstoneTab.addPadding(curInfo,4, width / fontRenderer.getStringWidth(" "));
        Rectangle buttonRect = redstoneTab.getButtonScaledRectangle(-width - 12, 24, width + 10, 20);
        redstoneButton = new GuiButtonSpecial(0, buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height, "-");
        redstoneTab.addWidget(redstoneButton);
    }

    private void addUpgradeTab() {
        String upgrades = "gui.tab.upgrades." + te.getName();
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
            max = Math.max(max, fontRenderer.getStringWidth(I18n.format(te.getRedstoneButtonText(i))));
        }
        return max;
    }

    private void addSideConfiguratorTabs() {
        for (SideConfigurator sc : ((ISideConfigurable) te).getSideConfigurators()) {
            GuiAnimatedStat stat = addAnimatedStat(sc.getTranslationKey(), new ItemStack(Blockss.OMNIDIRECTIONAL_HOPPER), 0xFF90C0E0, false);
            stat.addPadding(7, 16);

            int yTop = 15, xLeft = 25;
            stat.addWidget(makeSideConfButton(sc, RelativeFace.TOP, xLeft + 22, yTop));
            stat.addWidget(makeSideConfButton(sc, RelativeFace.LEFT, xLeft, yTop + 22));
            stat.addWidget(makeSideConfButton(sc, RelativeFace.FRONT, xLeft + 22, yTop + 22));
            stat.addWidget(makeSideConfButton(sc, RelativeFace.RIGHT, xLeft + 44, yTop + 22));
            stat.addWidget(makeSideConfButton(sc, RelativeFace.BOTTOM, xLeft + 22, yTop + 44));
            stat.addWidget(makeSideConfButton(sc, RelativeFace.BACK, xLeft + 44, yTop + 44));
        }
    }

    private GuiButtonSpecial makeSideConfButton(SideConfigurator sc, RelativeFace relativeFace, int x, int y) {
        GuiButtonSpecial button = new GuiButtonSpecial(sc.getButtonId(relativeFace), x, y, 20, 20, "");
        sc.setupButton(button);
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

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j) {
        if (shouldDrawBackground()) {
            drawDefaultBackground();
            RenderUtils.glColorHex(0xFF000000 | getBackgroundTint());
            bindGuiTexture();
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableLighting();
        widgets.forEach(widget -> widget.render(i, j, partialTicks));
        widgets.forEach(widget -> widget.postRender(i, j, partialTicks));

        if (pressureStat != null) {
            Point gaugeLocation = getGaugeLocation();
            if (gaugeLocation != null) {
                TileEntityPneumaticBase pneu = (TileEntityPneumaticBase) te;
                GuiUtils.drawPressureGauge(fontRenderer, -1, pneu.criticalPressure, pneu.dangerPressure, te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : -Float.MAX_VALUE, pneu.getPressure(), gaugeLocation.x, gaugeLocation.y, zLevel);
            }
        }
    }

    protected boolean shouldDrawBackground() {
        return true;
    }

    protected void bindGuiTexture() {
        if (guiTexture != null) {
            mc.getTextureManager().bindTexture(guiTexture);
            GlStateManager.enableTexture2D();
        }
    }

    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4, yStart + ySize / 4 + 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        if (te != null && getInvNameOffset() != null) {
            String containerName = I18n.format(te.getName() + ".name");
            fontRenderer.drawString(containerName, xSize / 2 - fontRenderer.getStringWidth(containerName) / 2 + getInvNameOffset().x, 6 + getInvNameOffset().y, getTitleColor());
        }
        if (getInvTextOffset() != null)
            fontRenderer.drawString(I18n.format("container.inventory"), 8 + getInvTextOffset().x, ySize - 94 + getInvTextOffset().y, 0x404040);
    }

    protected int getTitleColor() { return 0x404040; }

    protected Point getInvNameOffset() {
        return new Point(0, 0);
    }

    protected Point getInvTextOffset() {
        return new Point(0, 0);
    }

    @Override
    public void drawScreen(int x, int y, float partialTick) {
        if (!hasInit) return;
        super.drawScreen(x, y, partialTick);

        List<String> tooltip = new ArrayList<>();
        for (Object obj : buttonList) {
            if (obj instanceof GuiButtonSpecial) {
                GuiButtonSpecial button = (GuiButtonSpecial) obj;
                if (button.visible && button.x < x && button.x + button.getWidth() > x && button.y < y && button.y + button.getHeight() > y) {
                    button.getTooltip(tooltip);
                }
            }
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableLighting();
        for (IGuiWidget widget : widgets) {
            if (widget.getBounds().contains(x, y))
                widget.addTooltip(x, y, tooltip, PneumaticCraftRepressurized.proxy.isSneakingInGui());
        }
        if (shouldParseVariablesInTooltips()) {
            for (int i = 0; i < tooltip.size(); i++) {
                tooltip.set(i, new TextVariableParser(tooltip.get(i)).parse());
            }
        }

        if (tooltip.size() > 0) {
            drawHoveringString(tooltip, x, y, fontRenderer);
            tooltip.clear();
        }

        renderHoveredToolTip(x, y);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        for (IGuiWidget widget : widgets)
            widget.update();

        if (pressureStat != null) {
            List<String> pressureText = new ArrayList<>();
            addPressureStatInfo(pressureText);
            pressureStat.setText(pressureText);
        }
        if (problemTab != null && ((Minecraft.getMinecraft().world.getTotalWorldTime() & 0x7) == 0 || firstUpdate)) {
            handleProblemsTab();
        }
        if (redstoneTab != null) {
            redstoneButton.displayString = I18n.format(te.getRedstoneButtonText(((IRedstoneControl) te).getRedstoneMode()));
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
        int nInfo = problemText.size() - nWarnings;

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

    @Override
    protected void actionPerformed(GuiButton button) {
        sendPacketToServer(button.id);
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (IGuiWidget widget : widgets) {
            if (widget.getBounds().contains(mouseX, mouseY)) widget.onMouseClicked(mouseX, mouseY, mouseButton);
            else widget.onMouseClickedOutsideBounds(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget instanceof IGuiAnimatedStat) {
            boolean leftSided = ((IGuiAnimatedStat) widget).isLeftSided();
            widgets.stream()
                    .filter(w -> w instanceof IGuiAnimatedStat)
                    .map(w -> (IGuiAnimatedStat) w)
                    .filter(stat -> widget != stat && stat.isLeftSided() == leftSided) // when the stat is on the same side, close it.
                    .forEach(IGuiAnimatedStat::closeWindow);
        } else if (te instanceof ISideConfigurable && widget instanceof GuiButtonSpecial) {
            ((ISideConfigurable) te).getSideConfigurators().stream()
                    .filter(sc -> sc.handleButtonPress(widget.getID()))
                    .findFirst()
                    .ifPresent(sc -> sc.setupButton((GuiButtonSpecial) widget));
        }
        sendPacketToServer(widget.getID());
    }

    protected void sendPacketToServer(int id) {
        NetworkHandler.sendToServer(new PacketGuiButton(id));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        widgets.forEach(IGuiWidget::handleMouseInput);
    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        for (IGuiWidget widget : widgets) {
            if (widget.onKey(key, keyCode)) return;
        }
        super.keyTyped(key, keyCode);
    }

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3) {
        widgets.clear();
        super.setWorldAndResolution(par1Minecraft, par2, par3);
    }

    public void drawHoveringString(List<String> text, int x, int y, FontRenderer fontRenderer) {
        drawHoveringText(text, x, y, fontRenderer);
    }

    public static void drawTexture(ResourceLocation texture, int x, int y) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + 16, 0).tex(0.0, 1.0).endVertex();
        wr.pos(x + 16, y + 16, 0).tex(1.0, 1.0).endVertex();
        wr.pos(x + 16, y, 0).tex(1.0, 0.0).endVertex();
        wr.pos(x, y, 0).tex(0.0, 0.0).endVertex();
        Tessellator.getInstance().draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    public GuiButtonSpecial getButtonFromRectangle(int buttonID, Rectangle buttonSize, String buttonText) {
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, buttonText);
    }

    public GuiButtonSpecial getInvisibleButtonFromRectangle(int buttonID, Rectangle buttonSize) {
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, "");
    }

    public WidgetTextField getTextFieldFromRectangle(Rectangle textFieldSize) {
        return new WidgetTextField(fontRenderer, textFieldSize.x, textFieldSize.y, textFieldSize.width, textFieldSize.height);
    }

    @Override
    public int getGuiLeft() {
        return guiLeft;
    }

    @Override
    public int getGuiTop() {
        return guiTop;
    }

    public List<Rectangle> getTabRectangles() {
        return widgets.stream()
                .filter(w -> w instanceof IGuiAnimatedStat)
                .map(IGuiWidget::getBounds)
                .collect(Collectors.toList());
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
    }

    protected void refreshScreen() {
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        setWorldAndResolution(Minecraft.getMinecraft(), i, j);
        widgets.stream().filter(widget -> widget instanceof GuiAnimatedStat).forEach(IGuiWidget::update);
    }

    protected boolean shouldParseVariablesInTooltips() {
        return false;
    }
}
