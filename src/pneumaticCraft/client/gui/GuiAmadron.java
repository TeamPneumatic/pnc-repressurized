package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.client.gui.widget.WidgetVerticalScrollbar;
import pneumaticCraft.common.inventory.ContainerAmadron;
import pneumaticCraft.common.inventory.ContainerAmadron.EnumProblemState;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketAmadronOrderUpdate;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class GuiAmadron extends GuiPneumaticContainerBase{
    private WidgetTextField searchBar;
    private WidgetVerticalScrollbar scrollbar;
    private int page;
    private final List<WidgetAmadronOffer> widgetOffers = new ArrayList<WidgetAmadronOffer>();
    private boolean needsRefreshing;
    private boolean hadProblem = false;
    private GuiButtonSpecial addTradeButton;

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
        addLabel(I18n.format("gui.search"), guiLeft + 76 - mc.fontRenderer.getStringWidth(I18n.format("gui.search")), guiTop + 41);

        addInfoTab(I18n.format("gui.tooltip.item.amadronTablet"));
        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.hopper), 0xFF00AAFF, true).setText("gui.tab.info.ghostSlotInteraction");
        addAnimatedStat("gui.tab.amadron.disclaimer.title", new ItemStack(Items.writable_book), 0xFF0000FF, true).setText("gui.tab.amadron.disclaimer");

        searchBar = new WidgetTextField(mc.fontRenderer, guiLeft + 79, guiTop + 40, 73, mc.fontRenderer.FONT_HEIGHT);
        addWidget(searchBar);

        scrollbar = new WidgetVerticalScrollbar(-1, guiLeft + 156, guiTop + 54, 142);
        scrollbar.setStates(1);
        scrollbar.setListening(true);
        addWidget(scrollbar);

        List<String> tooltip = PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.amadron.button.order.tooltip"), 40);
        addWidget(new GuiButtonSpecial(1, guiLeft + 6, guiTop + 15, 72, 20, I18n.format("gui.amadron.button.order")).setTooltipText(tooltip));
        addTradeButton = new GuiButtonSpecial(2, guiLeft + 80, guiTop + 15, 72, 20, I18n.format("gui.amadron.button.addTrade"));
        addWidget(addTradeButton);

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
            offer.setCanBuy(container.buyableOffers[container.offers.indexOf(offer.getOffer())]);
            offer.setShoppingAmount(container.getShoppingCartAmount(offer.getOffer()));
        }
        if(!hadProblem && container.problemState != EnumProblemState.NO_PROBLEMS) {
            problemTab.openWindow();
        }
        hadProblem = container.problemState != EnumProblemState.NO_PROBLEMS;
        addTradeButton.enabled = container.currentOffers < container.maxOffers;
        List<String> tooltip = PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.amadron.button.addTrade.tooltip"), 40);
        tooltip.add((addTradeButton.enabled ? EnumChatFormatting.GRAY : EnumChatFormatting.RED) + I18n.format("gui.amadron.button.addTrade.tooltip.offerCount", container.currentOffers, container.maxOffers == Integer.MAX_VALUE ? "\u221E" : container.maxOffers));
        addTradeButton.setTooltipText(tooltip);
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

            WidgetAmadronOffer widget = new WidgetAmadronOffer(i, guiLeft + 6 + 73 * (i % 2), guiTop + 55 + 35 * (i / 2), offer){
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
