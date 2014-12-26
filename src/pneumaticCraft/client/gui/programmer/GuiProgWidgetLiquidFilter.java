package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetFluidFilter;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.client.gui.widget.WidgetVerticalScrollbar;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidFilter;
import pneumaticCraft.lib.Textures;

public class GuiProgWidgetLiquidFilter extends GuiProgWidgetOptionBase<ProgWidgetLiquidFilter>{

    private static final int GRID_WIDTH = 8;
    private static final int GRID_HEIGHT = 6;
    private WidgetFluidFilter mainFilter;
    private WidgetTextField searchField;
    private WidgetVerticalScrollbar scrollbar;
    private int lastScroll;

    public GuiProgWidgetLiquidFilter(ProgWidgetLiquidFilter widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected ResourceLocation getTexture(){
        return new ResourceLocation(Textures.GUI_ITEM_SEARCHER_LOCATION);
    }

    @Override
    public void initGui(){
        super.initGui();

        mainFilter = new WidgetFluidFilter(-1, guiLeft + 124, guiTop + 25).setFluid(widget.getFluid());
        addWidget(mainFilter);

        for(int x = 0; x < GRID_WIDTH; x++) {
            for(int y = 0; y < GRID_HEIGHT; y++) {
                addWidget(new WidgetFluidFilter(x + y * GRID_WIDTH, guiLeft + 8 + x * 18, guiTop + 52 + y * 18));
            }
        }

        searchField = new WidgetTextField(Minecraft.getMinecraft().fontRenderer, guiLeft + 10, guiTop + 30, 90, 10);
        addWidget(searchField);

        scrollbar = new WidgetVerticalScrollbar(guiLeft + 155, guiTop + 47, 112);
        scrollbar.setListening(true);
        addWidget(scrollbar);

        addValidFluids();
    }

    private void addValidFluids(){

        List<Fluid> fluids = new ArrayList<Fluid>();

        for(Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            if(fluid.getLocalizedName(new FluidStack(fluid, 1)).toLowerCase().contains(searchField.getText())) {
                fluids.add(fluid);
            }
        }

        scrollbar.setStates(Math.max(0, (fluids.size() - GRID_WIDTH * GRID_HEIGHT + GRID_WIDTH - 1) / GRID_WIDTH));

        int offset = scrollbar.getState() * GRID_WIDTH;
        for(IGuiWidget widget : widgets) {
            if(widget.getID() >= 0 && widget instanceof WidgetFluidFilter) {
                int idWithOffset = widget.getID() + offset;
                ((WidgetFluidFilter)widget).setFluid(idWithOffset >= 0 && idWithOffset < fluids.size() ? fluids.get(idWithOffset) : null);
            }
        }
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(lastScroll != scrollbar.getState()) {
            lastScroll = scrollbar.getState();
            addValidFluids();
        }
    }

    @Override
    public void keyTyped(char key, int keyCode){
        super.keyTyped(key, keyCode);
        addValidFluids();
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        if(widget == mainFilter) {
            ((WidgetFluidFilter)widget).setFluid(null);
        } else if(widget instanceof WidgetFluidFilter) {
            mainFilter.setFluid(((WidgetFluidFilter)widget).getFluid());
        }
        this.widget.setFluid(mainFilter.getFluid());
        super.actionPerformed(widget);
    }
}
