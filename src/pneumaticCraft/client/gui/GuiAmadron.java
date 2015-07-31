package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.common.inventory.ContainerAmadron;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.lib.Textures;

public class GuiAmadron extends GuiPneumaticContainerBase{

    public GuiAmadron(InventoryPlayer playerInventory){
        super(new ContainerAmadron(), null, Textures.GUI_AMADRON);
        xSize = 176;
        ySize = 202;
    }

    @Override
    public void initGui(){
        super.initGui();
        List<AmadronOffer> offers = PneumaticRecipeRegistry.getInstance().amadronOffers;
        for(int i = 0; i < offers.size(); i++) {
            AmadronOffer offer = offers.get(i);
            WidgetAmadronOffer widget = new WidgetAmadronOffer(i, guiLeft + 6 + 74 * (i % 2), guiTop + 28 + 60 * (i / 2), offer);
            addWidget(widget);
        }
    }

}
