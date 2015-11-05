package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

/**
 *  IMPORTANT: WHEN CHANGING THE PACKAGE OF THIS CLASS, ALSO EDIT GUIANIMATEDSTATSUPPLIER.JAVA!!
 */

public class GuiAnimatedStat implements IGuiAnimatedStat, IGuiWidget, IWidgetListener{

    public static final int ANIMATED_STAT_SPEED = 10;
    private IGuiAnimatedStat affectingStat;
    private ItemStack iStack;
    private String texture = "";
    private final GuiScreen gui;
    private final List<String> textList = new ArrayList<String>();
    private final List<IGuiWidget> widgets = new ArrayList<IGuiWidget>();
    private int baseX;
    private int baseY;
    private int affectedY;
    private int width;
    private int height;

    private int oldBaseX;
    private int oldAffectedY;
    private int oldWidth;
    private int oldHeight;
    private boolean isClicked = false;
    private int minWidth = 17;
    private int minHeight = 17;
    private final int backGroundColor;
    private String title;
    private boolean leftSided; // this boolean determines if the stat is going
    // to expand to the left or right.
    private boolean doneExpanding;
    private RenderItem itemRenderer;
    private float textSize;
    private float textScale = 1F;
    private IWidgetListener listener;
    private int curScroll;
    private static final int MAX_LINES = 12;
    private int lastMouseX, lastMouseY;

    public GuiAnimatedStat(GuiScreen gui, String title, int xPos, int yPos, int backGroundColor,
            IGuiAnimatedStat affectingStat, boolean leftSided){
        this.gui = gui;
        baseX = xPos;
        baseY = yPos;
        this.affectingStat = affectingStat;
        width = minWidth;
        height = minHeight;
        this.backGroundColor = backGroundColor;
        setTitle(title);
        texture = "";
        this.leftSided = leftSided;
        if(gui != null) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
            if(sr.getScaledWidth() < 520) {
                textSize = (sr.getScaledWidth() - 220) * 0.0033F;
            } else {
                textSize = 1F;
            }
        } else {
            textSize = 1;
        }

        affectedY = baseY;
        if(affectingStat != null) {
            affectedY += affectingStat.getAffectedY() + affectingStat.getHeight();
        }
    }

    public GuiAnimatedStat(GuiScreen gui, int backgroundColor){
        this(gui, "", 0, 0, backgroundColor, null, false);
    }

    public GuiAnimatedStat(GuiScreen gui, int backgroundColor, ItemStack icon){
        this(gui, backgroundColor);
        iStack = icon;
    }

    public GuiAnimatedStat(GuiScreen gui, int backgroundColor, String texture){
        this(gui, backgroundColor);
        this.texture = texture;
    }

    public GuiAnimatedStat(GuiScreen gui, String title, ItemStack icon, int xPos, int yPos, int backGroundColor,
            IGuiAnimatedStat affectingStat, boolean leftSided){
        this(gui, title, xPos, yPos, backGroundColor, affectingStat, leftSided);
        iStack = icon;
    }

    public GuiAnimatedStat(GuiScreen gui, String title, String texture, int xPos, int yPos, int backGroundColor,
            IGuiAnimatedStat affectingStat, boolean leftSided){
        this(gui, title, xPos, yPos, backGroundColor, affectingStat, leftSided);
        this.texture = texture;
    }

    @Override
    public void setParentStat(IGuiAnimatedStat stat){
        affectingStat = stat;
    }

    public void addWidget(IGuiWidget widget){
        widgets.add(widget);
        widget.setListener(this);
    }

    public void removeWidget(IGuiWidget widget){
        widgets.remove(widget);
    }

    @Override
    public Rectangle getButtonScaledRectangle(int origX, int origY, int width, int height){
        int scaledX = (int)(origX * textSize);
        int scaledY = (int)(origY * textSize);

        //scaledX = (int)(origX * textSize);
        //scaledY = (int)(origY * textSize);
        return new Rectangle(scaledX, scaledY, (int)(width * textSize), (int)(height * textSize));
    }

    @Override
    public void scaleTextSize(float scale){
        textSize *= scale;
        textScale = scale;

        for(IGuiWidget widget : widgets) {
            if(widget.getID() == -1000) {
                widgets.remove(widget);
                break;
            }
        }
        onTextChange();
    }

    @Override
    public boolean isLeftSided(){
        return leftSided;
    }

    @Override
    public void setLeftSided(boolean leftSided){
        this.leftSided = leftSided;
    }

    @Override
    public IGuiAnimatedStat setText(List<String> text){
        textList.clear();
        for(String line : text) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(line), (int)(GuiConstants.maxCharPerLineLeft / textScale)));
        }
        onTextChange();
        return this;
    }

    @Override
    public IGuiAnimatedStat setText(String text){
        textList.clear();
        textList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(text), (int)(GuiConstants.maxCharPerLineLeft / textScale)));
        onTextChange();
        return this;
    }

    @Override
    public void setTextWithoutCuttingString(List<String> text){
        textList.clear();
        textList.addAll(text);
        onTextChange();
    }

    public void onTextChange(){
        if(textList.size() > MAX_LINES) {
            for(IGuiWidget widget : widgets) {
                if(widget.getID() == -1000) return;
            }
            curScroll = 0;
            /*Rectangle upRect = getButtonScaledRectangle(2, 24, 20, 20);
            addWidget(new GuiButtonSpecial(-1000, upRect.x, upRect.y, upRect.width, upRect.height, "^"));
            Rectangle downRect = getButtonScaledRectangle(2, 44, 20, 20);
            addWidget(new GuiButtonSpecial(-1001, downRect.x, downRect.y, downRect.width, downRect.height, "V"));
             */
            addWidget(new WidgetVerticalScrollbar(-1000, leftSided ? -16 : 2, 20, (int)((MAX_LINES * 10 - 20) * textSize)).setStates(textList.size() - MAX_LINES));
        } else {
            Iterator<IGuiWidget> iterator = widgets.iterator();
            while(iterator.hasNext()) {
                IGuiWidget widget = iterator.next();
                if(widget.getID() == -1000) {
                    iterator.remove();
                    curScroll = 0;
                }
            }
        }
    }

    @Override
    public void setMinDimensionsAndReset(int minWidth, int minHeight){
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        width = minWidth;
        height = minHeight;
    }

    @Override
    public void update(){
        oldBaseX = baseX;
        oldAffectedY = affectedY;
        oldWidth = width;
        oldHeight = height;

        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        doneExpanding = true;
        if(isClicked) {
            // calculate the width and height needed for the box to fit the
            // strings.
            int maxWidth = fontRenderer.getStringWidth(title);
            int maxHeight = 12;
            if(textList.size() > 0) {
                maxHeight += 4 + Math.min(MAX_LINES, textList.size()) * 10;
            }
            maxHeight = (int)(maxHeight * textSize);
            for(String line : textList) {
                if(fontRenderer.getStringWidth(line) > maxWidth) maxWidth = fontRenderer.getStringWidth(line);
            }
            maxWidth = (int)(maxWidth * textSize);
            maxWidth += 20;
            // expand the box

            for(int i = 0; i < ANIMATED_STAT_SPEED; i++) {
                if(width < maxWidth) {
                    width++;
                    doneExpanding = false;
                }
                if(height < maxHeight) {
                    height++;
                    doneExpanding = false;
                }
                if(width > maxWidth) width--;
                if(height > maxHeight) height--;
            }

            if(doneExpanding) {
                for(IGuiWidget widget : widgets) {
                    if(widget.getID() == -1000) {
                        curScroll = ((WidgetVerticalScrollbar)widget).getState();
                    }
                }
            }

        } else {
            for(int i = 0; i < ANIMATED_STAT_SPEED; i++) {
                if(width > minWidth) width--;
                if(height > minHeight) height--;
            }
            doneExpanding = false;
        }

        affectedY = baseY;
        if(affectingStat != null) {
            affectedY += affectingStat.getAffectedY() + affectingStat.getHeight();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        float zLevel = 0;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int renderBaseX = (int)(oldBaseX + (baseX - oldBaseX) * partialTicks);
        int renderAffectedY = (int)(oldAffectedY + (affectedY - oldAffectedY) * partialTicks);
        int renderWidth = (int)(oldWidth + (width - oldWidth) * partialTicks);
        int renderHeight = (int)(oldHeight + (height - oldHeight) * partialTicks);

        if(leftSided) renderWidth *= -1;
        Gui.drawRect(renderBaseX, renderAffectedY /* + 1 */, renderBaseX + renderWidth /*- 1*/, renderAffectedY + renderHeight, backGroundColor);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(3.0F);
        GL11.glColor4d(0, 0, 0, 1);
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.addVertex(renderBaseX, renderAffectedY, zLevel);
        tess.addVertex(renderBaseX + renderWidth, renderAffectedY, zLevel);
        tess.addVertex(renderBaseX + renderWidth, renderAffectedY + renderHeight, zLevel);
        tess.addVertex(renderBaseX, renderAffectedY + renderHeight, zLevel);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if(leftSided) renderWidth *= -1;
        // if done expanding, draw the information
        if(doneExpanding) {
            GL11.glPushMatrix();
            GL11.glTranslated(renderBaseX + (leftSided ? -renderWidth : 16), renderAffectedY, 0);
            GL11.glScaled(textSize, textSize, textSize);
            GL11.glTranslated(-renderBaseX - (leftSided ? -renderWidth : 16), -renderAffectedY, 0);
            fontRenderer.drawStringWithShadow(title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + 2, 0xFFFF00);
            for(int i = curScroll; i < textList.size() && i < curScroll + MAX_LINES; i++) {

                if(textList.get(i).contains("\u00a70") || textList.get(i).contains(EnumChatFormatting.DARK_RED.toString())) {
                    fontRenderer.drawString(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * 10 + 12, 0xFFFFFF);
                } else {
                    fontRenderer.drawStringWithShadow(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * 10 + 12, 0xFFFFFF);
                }
            }
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glTranslated(renderBaseX, renderAffectedY, 0);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            for(IGuiWidget widget : widgets)
                widget.render(mouseX - renderBaseX, mouseY - renderAffectedY, partialTicks);
            GL11.glPopMatrix();
        }
        if(renderHeight > 16 && renderWidth > 16) {
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if(iStack == null) {
                if(texture.contains(Textures.GUI_LOCATION)) {
                    GuiPneumaticContainerBase.drawTexture(texture, renderBaseX - (leftSided ? 16 : 0), renderAffectedY);
                } else {
                    fontRenderer.drawString(texture, renderBaseX - (leftSided ? 16 : 0), renderAffectedY, 0xFFFFFFFF);
                }
            } else if(gui != null || !(iStack.getItem() instanceof ItemBlock)) {
                if(itemRenderer == null) itemRenderer = new RenderItem();
                itemRenderer.zLevel = 1;
                GL11.glPushMatrix();
                GL11.glTranslated(0, 0, -50);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.enableGUIStandardItemLighting();
                itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, FMLClientHandler.instance().getClient().renderEngine, iStack, renderBaseX - (leftSided ? 16 : 0), renderAffectedY);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glPopMatrix();
            }
        }
    }

    /*
     * button: 0 = left 1 = right 2 = middle
     */
    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){
        if(button == 0) {
            isClicked = !isClicked;
            listener.actionPerformed(this);
        }
        mouseX -= baseX;
        mouseY -= affectedY;
        for(IGuiWidget widget : widgets) {
            if(widget.getBounds().contains(mouseX, mouseY)) {
                widget.onMouseClicked(mouseX, mouseY, button);
                isClicked = true;
            }
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button){

    }

    @Override
    public void closeWindow(){
        isClicked = false;
    }

    @Override
    public void openWindow(){
        isClicked = true;
    }

    @Override
    public boolean isClicked(){
        return isClicked;
    }

    @Override
    public int getAffectedY(){
        return affectedY;
    }

    @Override
    public int getBaseX(){
        return baseX;
    }

    @Override
    public int getBaseY(){
        return baseY;
    }

    @Override
    public int getHeight(){
        return height;
    }

    @Override
    public int getWidth(){
        return width;
    }

    @Override
    public void setBaseY(int y){
        baseY = y;
    }

    @Override
    public void setTitle(String title){
        this.title = I18n.format(title);
    }

    @Override
    public boolean isDoneExpanding(){
        return doneExpanding;
    }

    @Override
    public void setBaseX(int x){
        baseX = x;
    }

    @Override
    public String getTitle(){
        return title;
    }

    @Override
    public Rectangle getBounds(){
        return new Rectangle(baseX - (leftSided ? width : 0), affectedY, width, height);
    }

    @Override
    public void setListener(IWidgetListener gui){
        listener = gui;
    }

    @Override
    public int getID(){
        return -1;
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        isClicked = !isClicked;
        listener.actionPerformed(widget);
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){
        listener.onKeyTyped(widget);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed){

        if(mouseIsHoveringOverIcon(mouseX, mouseY)) {
            curTooltip.add(title);
        }

        for(IGuiWidget widget : widgets)
            if(isMouseOverWidget(widget, mouseX, mouseY)) widget.addTooltip(mouseX, mouseY, curTooltip, shiftPressed);
    }

    private boolean mouseIsHoveringOverIcon(int x, int y){
        if(leftSided) {
            return x <= baseX && x >= baseX - 16 && y >= affectedY && y <= affectedY + 16;
        } else {
            return x >= baseX && x <= baseX + 16 && y >= affectedY && y <= affectedY + 16;
        }
    }

    @Override
    public boolean onKey(char key, int keyCode){
        for(IGuiWidget widget : widgets)
            if(widget.onKey(key, keyCode)) return true;
        return false;
    }

    private boolean isMouseOverWidget(IGuiWidget widget, int mouseX, int mouseY){
        Rectangle rect = getBounds();
        mouseX -= rect.x;
        mouseY -= rect.y;
        return widget.getBounds().contains(mouseX, mouseY);
    }

    @Override
    public void handleMouseInput(){
        if(getBounds().contains(lastMouseX, lastMouseY)) {
            handleMouseWheel(Mouse.getDWheel());
        }
    }

    public boolean handleMouseWheel(int mouseWheel){
        for(IGuiWidget widget : widgets) {
            widget.handleMouseInput();
            if(widget.getID() == -1000) {
                int wheel = -mouseWheel;
                wheel = MathHelper.clamp_int(wheel, -1, 1);
                ((WidgetVerticalScrollbar)widget).currentScroll += (float)wheel / (textList.size() - MAX_LINES);
                return true;
            }
        }
        return false;
    }

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick){

    }

}
