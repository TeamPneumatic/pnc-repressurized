package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
//TODO NEI dep @Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = ModIds.NEI)
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

    public GuiPneumaticContainerBase(Container par1Container, Tile te, String guiTexture) {
        super(par1Container);
        this.te = te;
        this.guiTexture = guiTexture != null ? new ResourceLocation(guiTexture) : null;
    }

    protected GuiAnimatedStat addAnimatedStat(String title, ItemStack icon, int color, boolean leftSided) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        addWidget(stat);
        if (leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        return stat;
    }

    protected GuiAnimatedStat addAnimatedStat(String title, String icon, int color, boolean leftSided) {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        addWidget(stat);
        if (leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        return stat;
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
            problemTab = addAnimatedStat("gui.tab.problems", Textures.GUI_PROBLEMS_TEXTURE, 0xFFFF0000, false);
        }
        if (shouldAddRedstoneTab() && te instanceof IRedstoneControl) {
            redstoneTab = addAnimatedStat("gui.tab.redstoneBehaviour", new ItemStack(Items.REDSTONE), 0xFFCC0000, true);
            List<String> curInfo = new ArrayList<>();
            curInfo.add(I18n.format(getRedstoneString()));
            for (int i = 0; i < 3; i++)
                curInfo.add("                                      ");// create some space for the button
            redstoneTab.setTextWithoutCuttingString(curInfo);
            Rectangle buttonRect = redstoneTab.getButtonScaledRectangle(-170, 24, 170, 20);
            redstoneButton = new GuiButtonSpecial(0, buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height, "-");//getButtonFromRectangle(0, buttonRect, "-");
            redstoneTab.addWidget(redstoneButton);
        }
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            if (shouldAddInfoTab()) {
                String info = "gui.tab.info." + te.getName();
                String translatedInfo = I18n.format(info);
                if (!translatedInfo.equals(info)) {
                    addInfoTab(translatedInfo);
                }
            }
            if (te instanceof IHeatExchanger) {
                addAnimatedStat("gui.tab.info.heat.title", new ItemStack(Items.BLAZE_POWDER), 0xFFFF5500, false).setText("gui.tab.info.heat");
            }
            if (shouldAddUpgradeTab()) {
                String upgrades = "gui.tab.upgrades." + te.getName();
                String translatedUpgrades = I18n.format(upgrades);
                List<String> upgradeText = new ArrayList<String>();
                if (te instanceof TileEntityPneumaticBase) {
                    upgradeText.add("gui.tab.upgrades.volume");
                    upgradeText.add("gui.tab.upgrades.security");
                }
                if (te instanceof IHeatExchanger) {
                    upgradeText.add("gui.tab.upgrades.volumeCapacity");
                }
                if (!translatedUpgrades.equals(upgrades)) upgradeText.add(upgrades);

                if (upgradeText.size() > 0)
                    addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText(upgradeText);
            }
        }
        hasInit = true;
    }

    protected void addInfoTab(String info) {
        if (!Loader.isModLoaded(ModIds.IGWMOD)) info += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText(info);
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

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j) {
        drawDefaultBackground();

        if (shouldDrawBackground()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
        }

        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        for (IGuiWidget widget : widgets) {
            widget.render(i, j, partialTicks);
        }
        for (IGuiWidget widget : widgets) {
            widget.postRender(i, j, partialTicks);
        }

        if (pressureStat != null) {
            TileEntityPneumaticBase pneu = (TileEntityPneumaticBase) te;
            Point gaugeLocation = getGaugeLocation();
            if (gaugeLocation != null)
                GuiUtils.drawPressureGauge(fontRenderer, -1, pneu.criticalPressure, pneu.dangerPressure, te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : -1, pneu.getPressure(), gaugeLocation.x, gaugeLocation.y, zLevel);
        }
    }

    protected boolean shouldDrawBackground() {
        return true;
    }

    protected void bindGuiTexture() {
        if (guiTexture != null) {
            mc.getTextureManager().bindTexture(guiTexture);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        if (getInvNameOffset() != null) {
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
                if (button.x < x && button.x + button.getWidth() > x && button.y < y && button.y + button.getHeight() > y) {
                    button.getTooltip(tooltip);
                }
            }
        }

        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        for (IGuiWidget widget : widgets) {
            if (widget.getBounds().contains(x, y))
                widget.addTooltip(x, y, tooltip, PneumaticCraftRepressurized.proxy.isSneakingInGui());
        }

        if (tooltip.size() > 0) {
            drawHoveringString(tooltip, x, y, fontRenderer);
            tooltip.clear();
        }

        renderHoveredToolTip(x, y);

        /* TODO boolean shift = PneumaticCraft.proxy.isSneakingInGui();
         for(IGuiWidget widget : widgets) {
             if(widget.getBounds().contains(x, y)) widget.addTooltip(tooltip, shift);
         }
         if(!tooltip.isEmpty()) {
             List<String> localizedTooltip = new ArrayList<String>();
             for(String line : tooltip) {
                 String localizedLine = I18n.format(line);
                 String[] lines = WordUtils.wrap(localizedLine, 50).split(System.getProperty("line.separator"));
                 for(String locLine : lines) {
                     localizedTooltip.add(locLine);
                 }
             }
             drawHoveringText(localizedTooltip, x, y, fontRenderer);
         }*/
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        for (IGuiWidget widget : widgets)
            widget.update();

        if (pressureStat != null) {
            List<String> curInfo = new ArrayList<>();
            addPressureStatInfo(curInfo);
            pressureStat.setText(curInfo);
        }
        if (problemTab != null) {
            List<String> curInfo = new ArrayList<>();
            addProblems(curInfo);
            if (curInfo.size() == 0) {
                curInfo.add("gui.tab.problems.noProblems");
            }
            problemTab.setText(curInfo);
        }
        if (redstoneTab != null) {
            redstoneButton.displayString = I18n.format(getRedstoneButtonText(((IRedstoneControl) te).getRedstoneMode()));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        sendPacketToServer(button.id);
    }

    public String getRedstoneButtonText(int mode) {
        switch (mode) {
            case 0:
                return "gui.tab.redstoneBehaviour.button.anySignal";
            case 1:
                return "gui.tab.redstoneBehaviour.button.highSignal";
            case 2:
                return "gui.tab.redstoneBehaviour.button.lowSignal";
        }
        return "<ERROR>";
    }

    public String getRedstoneString() {
        return te instanceof IRedstoneControlled ? "gui.tab.redstoneBehaviour.enableOn" : "gui.tab.redstoneBehaviour.emitRedstoneWhen";
    }

    protected void addPressureStatInfo(List<String> pressureStatText) {
        TileEntityPneumaticBase pneumaticTile = (TileEntityPneumaticBase) te;
        IAirHandler airHandler = pneumaticTile.getAirHandler(null);
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(pneumaticTile.getPressure(), 1) + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (airHandler.getAir() + airHandler.getVolume()) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + pneumaticTile.defaultVolume + " mL.");
        int volumeLeft = airHandler.getVolume() - pneumaticTile.defaultVolume;
        if (volumeLeft > 0) {
            pressureStatText.add("\u00a70" + volumeLeft + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + airHandler.getVolume() + " mL.");
        }
    }

    protected void addProblems(List<String> curInfo) {
        if (te instanceof IRedstoneControlled && !te.redstoneAllows()) {
            IRedstoneControlled redstoneControlled = (IRedstoneControlled) te;
            curInfo.add("gui.tab.problems.redstoneDisallows");
            if (redstoneControlled.getRedstoneMode() == 1) {
                curInfo.add("gui.tab.problems.provideRedstone");
            } else {
                curInfo.add("gui.tab.problems.removeRedstone");
            }
        }
        if (te instanceof IMinWorkingPressure) {
            IMinWorkingPressure minWork = (IMinWorkingPressure) te;
            if (((TileEntityPneumaticBase) te).getPressure() < minWork.getMinWorkingPressure()) {
                curInfo.add("gui.tab.problems.notEnoughPressure");
                curInfo.add(I18n.format("gui.tab.problems.applyPressure", minWork.getMinWorkingPressure()));
            }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);
        for (IGuiWidget widget : widgets) {
            if (widget.getBounds().contains(par1, par2)) widget.onMouseClicked(par1, par2, par3);
            else widget.onMouseClickedOutsideBounds(par1, par2, par3);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget instanceof IGuiAnimatedStat) {
            boolean leftSided = ((IGuiAnimatedStat) widget).isLeftSided();
            for (IGuiWidget w : widgets) {
                if (w instanceof IGuiAnimatedStat) {
                    IGuiAnimatedStat stat = (IGuiAnimatedStat) w;
                    if (widget != stat && stat.isLeftSided() == leftSided) {//when the stat is on the same side, close it.
                        stat.closeWindow();
                    }
                }
            }
        }
        sendPacketToServer(widget.getID());
    }

    protected void sendPacketToServer(int id) {
        NetworkHandler.sendToServer(new PacketGuiButton(id));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        for (IGuiWidget widget : widgets) {
            widget.handleMouseInput();
        }
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

    public static void drawTexture(String texture, int x, int y) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiUtils.getResourceLocation(texture));
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

    public int getGuiLeft() {
        return guiLeft;
    }

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
        for (IGuiWidget widget : widgets) {
            if (widget instanceof GuiAnimatedStat) {
                widget.update();
            }
        }
    }
}
