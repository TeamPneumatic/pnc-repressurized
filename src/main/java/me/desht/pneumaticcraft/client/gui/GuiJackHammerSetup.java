package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerJackhammerSetup;
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemDrillBit.DrillBitType;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.item.ItemJackHammer.DigMode;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.EnumMap;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiJackHammerSetup extends GuiPneumaticContainerBase<ContainerJackhammerSetup, TileEntityBase> {
    private final EnumMap<DigMode,WidgetButtonExtended> typeButtons = new EnumMap<>(DigMode.class);
    private WidgetButtonExtended selectorButton;

    public GuiJackHammerSetup(ContainerJackhammerSetup container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        imageHeight = 182;
    }

    @Override
    public void init() {
        super.init();

        ItemStack hammerStack = inventory.player.getItemInHand(menu.getHand());

        DigMode digMode = ItemJackHammer.getDigMode(hammerStack);

        if (digMode != null) {
            addButton(selectorButton = new WidgetButtonExtended(leftPos + 127, topPos + 67, 20, 20,
                    StringTextComponent.EMPTY, b -> toggleShowChoices()))
                    .setRenderedIcon(digMode.getGuiIcon());

            int xBase = 147 - 20 * DigMode.values().length;
            for (DigMode dm : DigMode.values()) {
                WidgetButtonExtended button = new WidgetButtonExtended(leftPos + xBase, topPos + 47, 20, 20,
                        StringTextComponent.EMPTY, b -> selectDigMode(dm))
                        .setRenderedIcon(dm.getGuiIcon())
                        .withTag("digmode:" + dm.toString());
                xBase += 20;
                button.visible = false;
                typeButtons.put(dm, button);
                addButton(button);
            }
        }

        addButton(new WidgetTooltipArea(leftPos + 96, topPos + 19, 18, 18) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shiftPressed) {
                if (!menu.slots.get(1).hasItem()) {
                    curTip.add(xlate("pneumaticcraft.gui.tooltip.jackhammer.enchantedBookTip"));
                }
            }
        });
    }

    private void selectDigMode(DigMode digMode) {
        // communication to server handled via PacketGuiButton
        typeButtons.values().forEach(button -> button.visible = false);
        selectorButton.setRenderedIcon(digMode.getGuiIcon());
    }

    private void toggleShowChoices() {
        typeButtons.values().forEach(button -> button.visible = !button.visible);
    }

    @Override
    public void tick() {
        super.tick();

        updateDigModeButtons();
    }

    private void updateDigModeButtons() {
        ItemStack drillStack = menu.getSlot(0).getItem();
        DrillBitType bitType = drillStack.getItem() instanceof ItemDrillBit ?
                ((ItemDrillBit) drillStack.getItem()).getType() :
                DrillBitType.NONE;

        ItemStack hammerStack = inventory.player.getItemInHand(menu.getHand());
        DigMode digMode = ItemJackHammer.getDigMode(hammerStack);
        if (digMode == null) digMode = DigMode.MODE_1X1;

        typeButtons.forEach((dm, button) -> button.active = dm.getBitType().getTier() <= bitType.getTier());

        if (digMode.getBitType().getTier() > bitType.getTier() && digMode != DigMode.MODE_1X1) {
            // jackhammer currently has a selected dig type of a tier too high for the installed drill bit
            digMode = DigMode.MODE_1X1;
            NetworkHandler.sendToServer(new PacketGuiButton("digmode:" + digMode.toString()));
        }

        selectorButton.setRenderedIcon(digMode.getGuiIcon());
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (!menu.slots.get(0).hasItem()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.jackhammer.noBit"));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_JACKHAMMER_SETUP;
    }
}
