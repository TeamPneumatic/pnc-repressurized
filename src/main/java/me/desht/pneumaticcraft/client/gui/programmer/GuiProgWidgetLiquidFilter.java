package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetFluidFilter;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetVerticalScrollbar;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetLiquidFilter extends GuiProgWidgetOptionBase<ProgWidgetLiquidFilter> {

    private static final int GRID_WIDTH = 8;
    private static final int GRID_HEIGHT = 6;
    private WidgetFluidFilter mainFilter;
    private WidgetVerticalScrollbar scrollbar;
    private int lastScroll;
    private final List<WidgetFluidFilter> visibleFluidWidgets = new ArrayList<>();

    public GuiProgWidgetLiquidFilter(ProgWidgetLiquidFilter widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_ITEM_SEARCHER_LOCATION;
    }

    @Override
    public void init() {
        super.init();

        mainFilter = new WidgetFluidFilter(guiLeft + 124, guiTop + 25, b -> b.setFluid(null)).setFluid(progWidget.getFluid());
        addButton(mainFilter);

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                WidgetFluidFilter f = new WidgetFluidFilter(guiLeft + 8 + x * 18, guiTop + 52 + y * 18, b -> mainFilter.setFluid(b.getFluid()));
                addButton(f);
                visibleFluidWidgets.add(f);
            }
        }

        WidgetTextField searchField = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 90, 10);
        addButton(searchField);
        searchField.setFocused2(true);
        searchField.func_212954_a(s -> addValidFluids());

        scrollbar = new WidgetVerticalScrollbar(guiLeft + 155, guiTop + 47, 112);
        scrollbar.setListening(true);
        addButton(scrollbar);

        addValidFluids();
    }

    private void addValidFluids() {

        List<Fluid> fluids = new ArrayList<>();

        // todo 1.14 fluids
//        String filter = searchField.getText();
//        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
//            if (filter.isEmpty() || fluid.getLocalizedName(new FluidStack(fluid, 1)).toLowerCase().contains(filter)) {
//                fluids.add(fluid);
//            }
//        }
//        fluids.sort(Comparator.comparing(Fluid::getName));

        scrollbar.setStates(Math.max(0, (fluids.size() - GRID_WIDTH * GRID_HEIGHT + GRID_WIDTH - 1) / GRID_WIDTH));

        int offset = scrollbar.getState() * GRID_WIDTH;
        for (int i = 0; i < visibleFluidWidgets.size(); i++) {
            if (i + offset < fluids.size()) {
                visibleFluidWidgets.get(i).setFluid(fluids.get(i + offset));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastScroll != scrollbar.getState()) {
            lastScroll = scrollbar.getState();
            addValidFluids();
        }
    }
}
