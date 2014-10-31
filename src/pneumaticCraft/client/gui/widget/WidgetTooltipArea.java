package pneumaticCraft.client.gui.widget;

import java.util.List;

public class WidgetTooltipArea extends WidgetBase{

    public String[] tooltip;

    public WidgetTooltipArea(int x, int y, int width, int height, String... tooltip){
        super(0, x, y, width, height);
        this.tooltip = tooltip;
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){}

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed){
        for(String str : tooltip)
            curTip.add(str);
    }
}
