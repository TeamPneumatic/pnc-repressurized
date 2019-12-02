package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.ITooltipSupplier;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GuiPneumaticScreenBase extends Screen {

    public int guiLeft, guiTop, xSize, ySize;

    public GuiPneumaticScreenBase(ITextComponent title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    protected void addLabel(String text, int x, int y) {
        addButton(new WidgetLabel(x, y, text));
    }

    protected void removeWidget(Widget widget) {
        buttons.remove(widget);
        children.remove(widget);
    }

    protected abstract ResourceLocation getTexture();

    @Override
    public void tick() {
        super.tick();

        buttons.stream().filter(w -> w instanceof ITickable).forEach(w -> ((ITickable) w).tick());
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        GlStateManager.color4f(1f, 1f, 1f, 1.0f);
        if (getTexture() != null) {
            minecraft.getTextureManager().bindTexture(getTexture());
            blit(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
        super.render(x, y, partialTicks);

        GlStateManager.enableTexture();
        GlStateManager.color4f(0.25f, 0.25f, 0.25f, 1.0f);

        List<String> tooltip = new ArrayList<>();
        boolean shift = PneumaticCraftRepressurized.proxy.isSneakingInGui();
        for (Widget widget : buttons) {
            if (widget instanceof ITooltipSupplier && widget.isHovered()) {
                ((ITooltipSupplier) widget).addTooltip(x, y, tooltip, shift);
            }
        }
        if (!tooltip.isEmpty()) {
            List<String> localizedTooltip = new ArrayList<>();
            for (String line : tooltip) {
                String localizedLine = I18n.format(line);
                for (String wrappedLine : localizedLine.split("\\\\n")) {
                    String[] lines = WordUtils.wrap(wrappedLine, 50).split(System.getProperty("line.separator"));
                    localizedTooltip.addAll(Arrays.asList(lines));
                }
            }
            renderTooltip(localizedTooltip, x, y, font);
        }
        GlStateManager.color4f(0.25f, 0.25f, 0.25f, 1.0f);
    }

//    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
//        super.mouseClicked(mouseX, mouseY, mouseButton);
//
//        // new list creation necessary to avoid a comod exception
//        LinkedList<IGuiWidget> l = new LinkedList<>();
//        widgets.forEach(w -> {
//            if (!(w instanceof WidgetComboBox && ((WidgetComboBox) w).isFocused())) {
//                // ensure any focused combobox is added last
//                l.addFirst(w);
//            } else {
//                l.add(w);
//            }
//        });
//
//        l.forEach(widget -> {
//            if (widget.getBounds().contains(mouseX, mouseY)) {
//                widget.onMouseClicked(mouseX, mouseY, mouseButton);
//            } else {
//                widget.onMouseClickedOutsideBounds(mouseX, mouseY, mouseButton);
//            }
//        });
//    }
//
//    @Override
//    protected void keyTyped(char key, int keyCode) throws IOException {
//        if (keyCode == 1) {
//            super.keyTyped(key, keyCode);
//        } else {
//            for (IGuiWidget widget : widgets) {
//                widget.onKey(key, keyCode);
//            }
//        }
//    }
//
//    @Override
//    public void actionPerformed(IGuiWidget widget) {
//        if (widget instanceof IGuiAnimatedStat) {
//            boolean leftSided = ((IGuiAnimatedStat) widget).isLeftSided();
//            for (IGuiWidget w : widgets) {
//                if (w instanceof IGuiAnimatedStat) {
//                    IGuiAnimatedStat stat = (IGuiAnimatedStat) w;
//                    if (widget != stat && stat.isLeftSided() == leftSided) {//when the stat is on the same side, close it.
//                        stat.closeWindow();
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public void updateScreen() {
//        super.updateScreen();
//        for (IGuiWidget widget : widgets) {
//            widget.update();
//        }
//    }
//
//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//        for (IGuiWidget widget : widgets) {
//            widget.handleMouseInput();
//        }
//    }
//
//    @Override
//    public void onKeyTyped(IGuiWidget widget) {
//    }
//
//    @Override
//    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3) {
//        widgets.clear();
//        super.setWorldAndResolution(par1Minecraft, par2, par3);
//    }
}
