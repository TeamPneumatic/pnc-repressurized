package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron.EnumProblemState;
import me.desht.pneumaticcraft.common.inventory.SlotUntouchable;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronInvSync;
import me.desht.pneumaticcraft.common.network.PacketAmadronOrderUpdate;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class GuiAmadron extends GuiPneumaticContainerBase<ContainerAmadron,TileEntityBase> {
    private WidgetTextField searchBar;
    private WidgetVerticalScrollbar scrollbar;
    private int page;
    private final List<WidgetAmadronOffer> widgetOffers = new ArrayList<>();
    private boolean needsRefreshing;
    private boolean hadProblem = false;
    private WidgetButtonExtended orderButton;
    private WidgetButtonExtended addTradeButton;
    private WidgetAnimatedStat customTradesTab;

    public GuiAmadron(ContainerAmadron container, PlayerInventory inv, @SuppressWarnings("unused") ITextComponent displayString) {
        super(container, inv, new StringTextComponent(""));
        xSize = 176;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();

        String amadron = I18n.format("gui.amadron.title");
        addLabel(amadron, guiLeft + xSize / 2 - font.getStringWidth(amadron) / 2, guiTop + 5, 0xFFFFFF);
        addLabel(I18n.format("gui.search"), guiLeft + 76 - font.getStringWidth(I18n.format("gui.search")), guiTop + 41, 0xFFFFFF);

        addInfoTab(I18n.format("gui.tooltip.item.pneumaticcraft.amadron_tablet"));
        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText("gui.tab.info.ghostSlotInteraction");
        addAnimatedStat("gui.tab.amadron.disclaimer.title", new ItemStack(Items.WRITABLE_BOOK), 0xFF0000FF, true)
                .setText("gui.tab.amadron.disclaimer");
        customTradesTab = addAnimatedStat("gui.tab.amadron.customTrades", new ItemStack(Items.DIAMOND), 0xFFD07000, false);
        customTradesTab.addPadding(6, 10);
        searchBar = new WidgetTextField(font, guiLeft + 79, guiTop + 40, 73, font.FONT_HEIGHT);
        searchBar.setFocused2(true);
        searchBar.setResponder(s -> sendDelayed(8));
        addButton(searchBar);
        setFocused(searchBar);

        addButton(scrollbar = new WidgetVerticalScrollbar(guiLeft + 156, guiTop + 54, 142).setStates(1).setListening(true));

        List<String> tooltip = PneumaticCraftUtils.splitString(I18n.format("gui.amadron.button.order.tooltip"), 40);
        orderButton = new WidgetButtonExtended(guiLeft + 52, guiTop + 16, 72, 20, I18n.format("gui.amadron.button.order")).setTooltipText(tooltip).withTag("order");
        addButton(orderButton);

        addTradeButton = new WidgetButtonExtended(16, 26, 20, 20, "")
                .setRenderStacks(new ItemStack(Items.EMERALD)).withTag("addPlayerTrade");
        tooltip = new ArrayList<>();
        tooltip.add(I18n.format("gui.amadron.button.addTrade"));
        tooltip.addAll(PneumaticCraftUtils.splitString(I18n.format("gui.amadron.button.addTrade.tooltip"), 40));
        addTradeButton.setTooltipText(tooltip);
        customTradesTab.addSubWidget(addTradeButton);

        needsRefreshing = true;
    }

    @Override
    protected void doDelayedAction() {
        needsRefreshing = true;
        scrollbar.setCurrentState(0);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        return scrollbar.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    @Override
    protected int getBackgroundTint() {
        return 0x068e2c;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AMADRON;
    }

    @Override
    public void tick() {
        super.tick();

        if (needsRefreshing || page != scrollbar.getState()) {
            setPage(scrollbar.getState());
        }
        for (WidgetAmadronOffer offer : widgetOffers) {
            offer.setCanBuy(container.buyableOffers[container.activeOffers.indexOf(offer.getOffer())]);
            offer.setShoppingAmount(container.getShoppingCartAmount(offer.getOffer()));
        }
        if (!hadProblem && container.problemState != EnumProblemState.NO_PROBLEMS) {
            problemTab.openWindow();
        }
        hadProblem = container.problemState != EnumProblemState.NO_PROBLEMS;
        orderButton.active = !container.isBasketEmpty();
        addTradeButton.active = container.currentOffers < container.maxOffers;
        List<String> text = new ArrayList<>();
        text.add(I18n.format("gui.amadron.button.addTrade.tooltip.offerCount",
                container.currentOffers,
                container.maxOffers == Integer.MAX_VALUE ? GuiConstants.INFINITY : container.maxOffers));
        IntStream.range(0, 3).forEach(i -> text.add(" "));
        customTradesTab.setText(text);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
        }

        return searchBar.keyPressed(keyCode, scanCode, modifiers)
                || searchBar.canWrite()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void setPage(int page) {
        this.page = page;
        updateVisibleOffers();
    }

    private void updateVisibleOffers() {
        needsRefreshing = false;
        int invSize = ContainerAmadron.ROWS * 2;
        container.clearStacks();
        List<Pair<Integer,AmadronOffer>> visibleOffers = new ArrayList<>();
        int skippedOffers = 0;
        int applicableOffers = 0;

        for (int i = 0; i < container.activeOffers.size(); i++) {
            AmadronOffer offer = container.activeOffers.get(i);
            if (offer.passesQuery(searchBar.getText())) {
                applicableOffers++;
                if (skippedOffers < page * invSize) {
                    skippedOffers++;
                } else if (visibleOffers.size() < invSize) {
                    visibleOffers.add(Pair.of(i, offer));
                }
            }
        }

        scrollbar.setStates(Math.max(1, (applicableOffers + invSize - 1) / invSize - 1));

        buttons.removeAll(widgetOffers);
        children.removeAll(widgetOffers);
        for (int i = 0; i < visibleOffers.size(); i++) {
            int offerId = visibleOffers.get(i).getLeft();
            AmadronOffer offer = visibleOffers.get(i).getRight();
            if (!offer.getInput().getItem().isEmpty()) {
                container.getSlot(i * 2).putStack(offer.getInput().getItem());
                ((SlotUntouchable) container.getSlot(i * 2)).setEnabled(true);
            }
            if (!offer.getOutput().getItem().isEmpty()) {
                container.getSlot(i * 2 + 1).putStack(offer.getOutput().getItem());
                ((SlotUntouchable) container.getSlot(i * 2 + 1)).setEnabled(true);
            }

            WidgetAmadronOffer widget = new WidgetAmadronOfferAdjustable(offerId,guiLeft + 6 + 73 * (i % 2), guiTop + 55 + 35 * (i / 2), offer);
            addButton(widget);
            widgetOffers.add(widget);
        }

        // avoid drawing phantom slot highlights where there's no widget
        for (int i = visibleOffers.size() * 2; i < container.inventorySlots.size(); i++) {
            ((SlotUntouchable) container.getSlot(i)).setEnabled(false);
        }

        // the server also needs to know what's in the tablet, or the next
        // "window items" packet will empty all the client-side slots
        NetworkHandler.sendToServer(new PacketAmadronInvSync(container.getInventory()));
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (container.problemState != EnumProblemState.NO_PROBLEMS) {
            curInfo.add(container.problemState.getTranslationKey());
        }
    }

    class WidgetAmadronOfferAdjustable extends WidgetAmadronOffer {
        private final int offerId;

        WidgetAmadronOfferAdjustable(int offerId, int x, int y, AmadronOffer offer) {
            super(x, y, offer);
            this.offerId = offerId;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if (clicked(mouseX, mouseY)) {
                NetworkHandler.sendToServer(new PacketAmadronOrderUpdate(offerId, mouseButton, Screen.hasShiftDown()));
                return true;
            } else {
                return false;
            }
        }
    }
}
