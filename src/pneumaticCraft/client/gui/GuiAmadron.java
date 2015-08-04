package pneumaticCraft.client.gui;

import igwmod.network.NetworkHandler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.client.gui.widget.WidgetVerticalScrollbar;
import pneumaticCraft.common.inventory.ContainerAmadron;
import pneumaticCraft.common.inventory.ContainerAmadron.EnumProblemState;
import pneumaticCraft.common.network.PacketAmadronOrderUpdate;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.lib.Textures;

public class GuiAmadron extends GuiPneumaticContainerBase{
    private WidgetTextField searchBar;
    private WidgetVerticalScrollbar scrollbar;
    private int page;
    private final List<WidgetAmadronOffer> widgetOffers = new ArrayList<WidgetAmadronOffer>();
    private boolean needsRefreshing;
    private boolean hadProblem = false;

    public GuiAmadron(InventoryPlayer playerInventory){
        super(new ContainerAmadron(playerInventory.player), null, Textures.GUI_AMADRON);
        xSize = 176;
        ySize = 202;
    }

    @Override
    public void initGui(){
        super.initGui();
        String amadron = I18n.format("gui.amadron");
        addLabel(amadron, guiLeft + xSize / 2 - mc.fontRenderer.getStringWidth(amadron) / 2, guiTop + 5);

        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.hopper), 0xFF00AAFF, true).setText("gui.tab.info.ghostSlotInteraction");

        searchBar = new WidgetTextField(mc.fontRenderer, guiLeft + 6, guiTop + 38, 73, mc.fontRenderer.FONT_HEIGHT);
        addWidget(searchBar);

        scrollbar = new WidgetVerticalScrollbar(1, guiLeft + 157, guiTop + 51, 145);
        scrollbar.setStates(1);
        scrollbar.setListening(true);
        addWidget(scrollbar);

        addWidget(new GuiButtonSpecial(1, guiLeft + 100, guiTop + 10, 50, 20, I18n.format("gui.amadron.button.order")));
        addWidget(new GuiButtonSpecial(2, guiLeft + 100, guiTop + 32, 50, 20, I18n.format("gui.amadron.button.addTrade")));

        updateVisibleOffers();
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        ContainerAmadron container = (ContainerAmadron)inventorySlots;
        if(needsRefreshing || page != scrollbar.getState()) {
            setPage(scrollbar.getState());
        }
        for(WidgetAmadronOffer offer : widgetOffers) {
            offer.setShoppingAmount(container.getShoppingCartAmount(offer.getOffer()));
        }
        if(!hadProblem && container.problemState != EnumProblemState.NO_PROBLEMS) {
            problemTab.openWindow();
        }
        hadProblem = container.problemState != EnumProblemState.NO_PROBLEMS;
    }

    public void setPage(int page){
        this.page = page;
        updateVisibleOffers();
    }

    public void updateVisibleOffers(){
        needsRefreshing = false;
        final ContainerAmadron container = (ContainerAmadron)inventorySlots;
        int invSize = ContainerAmadron.ROWS * 2;
        container.clearStacks();
        List<AmadronOffer> offers = container.offers;
        List<AmadronOffer> visibleOffers = new ArrayList<AmadronOffer>();
        int skippedOffers = 0;
        int applicableOffers = 0;
        for(AmadronOffer offer : offers) {
            if(offer.passesQuery(searchBar.getText())) {
                applicableOffers++;
                if(skippedOffers < page * invSize) {
                    skippedOffers++;
                } else if(visibleOffers.size() < invSize) {
                    visibleOffers.add(offer);
                }
            }
        }

        scrollbar.setStates(Math.max(1, (applicableOffers + invSize - 1) / invSize - 1));

        widgets.removeAll(widgetOffers);
        for(int i = 0; i < visibleOffers.size(); i++) {
            AmadronOffer offer = visibleOffers.get(i);
            if(offer.getInput() instanceof ItemStack) container.setStack(i * 2, (ItemStack)offer.getInput());
            if(offer.getOutput() instanceof ItemStack) container.setStack(i * 2 + 1, (ItemStack)offer.getOutput());

            WidgetAmadronOffer widget = new WidgetAmadronOffer(i, guiLeft + 6 + 74 * (i % 2), guiTop + 52 + 36 * (i / 2), offer, container.buyableOffers[offers.indexOf(offer)]){
                @Override
                public void onMouseClicked(int mouseX, int mouseY, int button){
                    NetworkHandler.sendToServer(new PacketAmadronOrderUpdate(container.offers.indexOf(getOffer()), button, PneumaticCraft.proxy.isSneakingInGui()));
                }
            };
            addWidget(widget);
            widgetOffers.add(widget);
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){
        super.onKeyTyped(widget);
        needsRefreshing = true;
        scrollbar.setCurrentState(0);
    }

    @Override
    public void actionPerformed(IGuiWidget widget){

        super.actionPerformed(widget);
    }

    @Override
    protected Point getInvTextOffset(){
        return null;
    }

    @Override
    protected void addProblems(List curInfo){
        super.addProblems(curInfo);
        EnumProblemState problemState = ((ContainerAmadron)inventorySlots).problemState;
        if(problemState != EnumProblemState.NO_PROBLEMS) {
            curInfo.add(problemState.getLocalizationKey());
        }
    }
}
