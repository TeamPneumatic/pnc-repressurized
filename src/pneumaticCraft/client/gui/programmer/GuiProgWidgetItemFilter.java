package pneumaticCraft.client.gui.programmer;

import java.util.Arrays;

import net.minecraft.client.gui.GuiButton;
import pneumaticCraft.client.gui.GuiInventorySearcher;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.GuiSearcher;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetItemFilter;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiProgWidgetItemFilter extends GuiProgWidgetOptionBase{
    private GuiSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiCheckBox checkBoxUseDamage;
    private GuiCheckBox checkBoxUseNBT;
    private GuiCheckBox checkBoxUseOreDict;
    private GuiCheckBox checkBoxUseModSimilarity;
    private final ProgWidgetItemFilter widg;
    private GuiButton incButton, decButton;

    // private GuiAnimatedStat metaInfoStat;

    public GuiProgWidgetItemFilter(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
        widg = (ProgWidgetItemFilter)widget;
    }

    @Override
    public void initGui(){
        super.initGui();

        //  metaInfoStat = new GuiAnimatedStat(this, "Metadata?", Textures.GUI_INFO_LOCATION, xStart + xSize, yStart + 5, 0xFF00AA00, null, false);

        buttonList.add(new GuiButton(0, guiLeft + 4, guiTop + 20, 70, 20, "Search item..."));
        buttonList.add(new GuiButton(1, guiLeft + 78, guiTop + 20, 100, 20, "Search inventory..."));
        decButton = new GuiButton(2, guiLeft + 140, guiTop + 63, 10, 20, "-");
        incButton = new GuiButton(3, guiLeft + 167, guiTop + 63, 10, 20, "+");
        buttonList.add(decButton);
        buttonList.add(incButton);
        checkBoxUseDamage = new GuiCheckBox(0, guiLeft + 4, guiTop + 50, 0xFF000000, "Use metadata / damage values");
        checkBoxUseDamage.setTooltip(Arrays.asList(new String[]{"Check to handle differently damaged tools", "or different colors of Wool as different."}));
        checkBoxUseDamage.checked = widg.useMetadata;
        addWidget(checkBoxUseDamage);
        checkBoxUseNBT = new GuiCheckBox(2, guiLeft + 4, guiTop + 86, 0xFF000000, "Use NBT");
        checkBoxUseNBT.setTooltip(Arrays.asList(new String[]{"Check to handle items like Enchanted Books", "or Firework as different."}));
        checkBoxUseNBT.checked = widg.useNBT;
        addWidget(checkBoxUseNBT);
        checkBoxUseOreDict = new GuiCheckBox(3, guiLeft + 4, guiTop + 98, 0xFF000000, "Use Ore Dictionary");
        checkBoxUseOreDict.setTooltip(Arrays.asList(new String[]{"Check to handle items registered in the", "Ore Dictionary (like Wood) as the same."}));
        checkBoxUseOreDict.checked = widg.useOreDict;
        addWidget(checkBoxUseOreDict);
        checkBoxUseModSimilarity = new GuiCheckBox(4, guiLeft + 4, guiTop + 110, 0xFF000000, "Use Mod similarity");
        checkBoxUseModSimilarity.setTooltip(Arrays.asList(new String[]{"Check to handle items from the", "same mod as the same."}));
        checkBoxUseModSimilarity.checked = widg.useModSimilarity;
        addWidget(checkBoxUseModSimilarity);

        checkBoxUseDamage.enabled = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked;
        incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
        decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
        checkBoxUseNBT.enabled = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked;
        checkBoxUseOreDict.enabled = !checkBoxUseModSimilarity.checked;
        checkBoxUseModSimilarity.enabled = !checkBoxUseOreDict.checked;

        if(searchGui != null) widg.filter = searchGui.getSearchStack();
        if(invSearchGui != null) widg.filter = invSearchGui.getSearchStack();
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id == 0) {
            searchGui = new GuiSearcher(FMLClientHandler.instance().getClient().thePlayer);
            searchGui.setSearchStack(widg.filter);
            FMLClientHandler.instance().showGuiScreen(searchGui);
        } else if(button.id == 1) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().thePlayer);
            invSearchGui.setSearchStack(widg.filter);
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
        } else if(button.id == 2) {
            if(--widg.specificMeta < 0) widg.specificMeta = 15;
        } else if(button.id == 3) {
            if(++widg.specificMeta > 15) widg.specificMeta = 0;
        }
        super.actionPerformed(button);
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget){
        GuiCheckBox checkBox = (GuiCheckBox)guiWidget;
        switch(checkBox.getID()){
            case 0:
                widg.useMetadata = checkBox.checked;
                incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                break;
            case 2:
                widg.useNBT = checkBox.checked;
                break;
            case 3:
                widg.useOreDict = checkBox.checked;
                checkBoxUseDamage.enabled = !checkBox.checked;
                checkBoxUseNBT.enabled = !checkBox.checked;
                checkBoxUseModSimilarity.enabled = !checkBox.checked;
                incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                break;
            case 4:
                widg.useModSimilarity = checkBox.checked;
                checkBoxUseDamage.enabled = !checkBox.checked;
                checkBoxUseNBT.enabled = !checkBox.checked;
                checkBoxUseOreDict.enabled = !checkBox.checked;
                incButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                decButton.enabled = checkBoxUseDamage.enabled && checkBoxUseDamage.checked;
                break;
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        String value = String.valueOf(widg.specificMeta);
        fontRendererObj.drawString(value, guiLeft + 158 - fontRendererObj.getStringWidth(value) / 2, guiTop + 68, checkBoxUseDamage.enabled && checkBoxUseDamage.checked ? 0xFF000000 : 0xFF888888);
        fontRendererObj.drawString("Specific block metadata:", guiLeft + 14, guiTop + 68, checkBoxUseDamage.enabled && checkBoxUseDamage.checked ? 0xFF000000 : 0xFF888888);
    }
}
