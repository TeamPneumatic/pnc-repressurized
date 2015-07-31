package pneumaticCraft.client.gui.widget;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.common.recipes.AmadronOffer;

public class WidgetAmadronOffer extends WidgetBase{
    private final AmadronOffer offer;
    public Map<Point, ItemStack> stacks = new HashMap<Point, ItemStack>();
    private final List<IGuiWidget> widgets = new ArrayList<IGuiWidget>();

    public WidgetAmadronOffer(int id, int x, int y, AmadronOffer offer){
        super(id, x, y, 73, 50);
        this.offer = offer;
        if(offer.getInput() instanceof ItemStack) {
            stacks.put(new Point(x + 20, y + 20), (ItemStack)offer.getInput());
        } else {
            widgets.add(new WidgetFluidStack(0, x + 20, y + 20, (FluidStack)offer.getInput()));
        }
        if(offer.getOutput() instanceof ItemStack) {
            stacks.put(new Point(x + 60, y + 20), (ItemStack)offer.getOutput());
        } else {
            widgets.add(new WidgetFluidStack(0, x + 60, y + 20, (FluidStack)offer.getOutput()));
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        Gui.drawRect(x, y, x + (int)getBounds().getWidth(), y + (int)getBounds().getHeight(), 0xFF555555);
        for(IGuiWidget widget : widgets) {
            widget.render(mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed){
        super.addTooltip(mouseX, mouseY, curTip, shiftPressed);
        for(IGuiWidget widget : widgets) {
            if(widget.getBounds().contains(mouseX, mouseY)) widget.addTooltip(mouseX, mouseY, curTip, shiftPressed);
        }
    }
}
