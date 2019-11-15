package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.AreaShowManager;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.config.aux.ProgWidgetConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammer;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.IVariableWidget;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileEntityProgrammer extends TileEntityTickableBase implements IGUITextFieldSensitive, INamedContainerProvider {
    private static final int PROGRAM_SLOT = 0;
    private static final int INVENTORY_SIZE = 1;

    public final List<IProgWidget> progWidgets = new ArrayList<>();
    @GuiSynced
    public int redstoneMode;

    private final ProgrammerItemHandler inventory = new ProgrammerItemHandler();
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> inventory);

    // Client side variables that are used to prevent resetting.
    public int translatedX, translatedY, zoomState;
    public boolean showInfo = true, showFlow = true;
    @GuiSynced
    public boolean canUndo, canRedo;
    private ListNBT history = new ListNBT();//Used to undo/redo.
    private int historyIndex;
    @DescSynced
    @LazySynced
    public boolean recentreStartPiece = false;

    public TileEntityProgrammer() {
        super(ModTileEntityTypes.PROGRAMMER);

        saveToHistory();
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        redstoneMode = tag.getInt("redstoneMode");
        inventory.deserializeNBT(tag.getCompound("Items"));
        history = tag.getList("history", 10);
        if (history.size() == 0) saveToHistory();
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        readProgWidgetsFromNBT(tag);
        recentreStartPiece = tag.getBoolean("recentreStartPiece");
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("redstoneMode", redstoneMode);
        tag.put("Items", inventory.serializeNBT());
        tag.put("history", history);
        return tag;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        writeProgWidgetsToNBT(tag);
        tag.putBoolean("recentreStartPiece", recentreStartPiece);
        recentreStartPiece = false;
    }

    public void readProgWidgetsFromNBT(CompoundNBT tag) {
        progWidgets.clear();
        getWidgetsFromNBT(tag, progWidgets);
    }

    public CompoundNBT writeProgWidgetsToNBT(CompoundNBT tag) {
        setWidgetsToNBT(progWidgets, tag);
        return tag;
    }

    @Nonnull
    public ItemStack getIteminProgrammingSlot() {
        return inventory.getStackInSlot(PROGRAM_SLOT);
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
    }

    public static List<IProgWidget> getWidgetsFromNBT(CompoundNBT tag) {
        List<IProgWidget> progWidgets = new ArrayList<>();
        getWidgetsFromNBT(tag, progWidgets);
        return progWidgets;
    }

    private static void getWidgetsFromNBT(CompoundNBT tag, List<IProgWidget> progWidgets) {
        ListNBT widgetTags = tag.getList("widgets", NBT.TAG_COMPOUND);
        for (int i = 0; i < widgetTags.size(); i++) {
            CompoundNBT widgetTag = widgetTags.getCompound(i);
            String widgetName = widgetTag.getString("name");
            if (!ProgWidgetConfig.blacklistedPieces.contains(widgetName)) {
                IProgWidget widget = WidgetRegistrator.getWidgetFromName(widgetName);
                if (widget != null) {
                    IProgWidget addedWidget = widget.copy();
                    addedWidget.readFromNBT(widgetTag);
                    progWidgets.add(addedWidget);
                }
            }
        }
        updatePuzzleConnections(progWidgets);
    }

    public static CompoundNBT setWidgetsToNBT(List<IProgWidget> widgets, CompoundNBT tag) {
        ListNBT widgetTags = new ListNBT();
        for (IProgWidget widget : widgets) {
            CompoundNBT widgetTag = new CompoundNBT();
            widget.writeToNBT(widgetTag);
            widgetTags.add(widgetTags.size(), widgetTag);
        }
        tag.put("widgets", widgetTags);
        return tag;
    }

    public static void updatePuzzleConnections(List<IProgWidget> progWidgets) {
        for (IProgWidget widget : progWidgets) {
            widget.setParent(null);
            Class<? extends IProgWidget>[] parameters = widget.getParameters();
            if (parameters != null) {
                for (int i = 0; i < parameters.length * 2; i++) {
                    widget.setParameter(i, null);
                }
            }
            if (widget.hasStepOutput()) widget.setOutputWidget(null);
        }
        for (IProgWidget checkedWidget : progWidgets) {
            //check for connection to the right of the checked widget.
            Class<? extends IProgWidget>[] parameters = checkedWidget.getParameters();
            if (parameters != null) {
                for (IProgWidget widget : progWidgets) {
                    if (widget != checkedWidget && checkedWidget.getX() + checkedWidget.getWidth() / 2 == widget.getX()) {
                        for (int i = 0; i < parameters.length; i++) {
                            if (checkedWidget.canSetParameter(i) && parameters[i] == widget.returnType() && checkedWidget.getY() + i * 11 == widget.getY()) {
                                checkedWidget.setParameter(i, widget);
                                widget.setParent(checkedWidget);
                            }
                        }
                    }
                }
            }

            //check for connection to the bottom of the checked widget.
            if (checkedWidget.hasStepOutput()) {
                for (IProgWidget widget : progWidgets) {
                    if (widget.hasStepInput() && widget.getX() == checkedWidget.getX() && widget.getY() == checkedWidget.getY() + checkedWidget.getHeight() / 2) {
                        checkedWidget.setOutputWidget(widget);
                    }
                }
            }
        }

        //go again for the blacklist (as those are mirrored)
        for (IProgWidget checkedWidget : progWidgets) {
            if (checkedWidget.returnType() == null) { //if it's a program (import/export inventory, attack entity) rather than a parameter (area, item filter).
                Class<? extends IProgWidget>[] parameters = checkedWidget.getParameters();
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        if (checkedWidget.canSetParameter(i)) {
                            for (IProgWidget widget : progWidgets) {
                                if (parameters[i] == widget.returnType()) {
                                    if (widget != checkedWidget && widget.getX() + widget.getWidth() / 2 == checkedWidget.getX() && widget.getY() == checkedWidget.getY() + i * 11) {
                                        IProgWidget root = widget;
                                        while (root.getParent() != null) {
                                            root = root.getParent();
                                        }
                                        checkedWidget.setParameter(i + parameters.length, root);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        switch (tag) {
            case IGUIButtonSensitive.REDSTONE_TAG:
                if (++redstoneMode > 1) redstoneMode = 0;
                break;
            case "import":
                ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
                CompoundNBT nbt = stack.isEmpty() ? null : stack.getTag();
                if (nbt != null) {
                    readProgWidgetsFromNBT(nbt);
                    recentreStartPiece = true;
                } else {
                    progWidgets.clear();
                }
                break;
            case "program":
                tryProgramDrone(player);
                break;
            case "undo":
                undo();
                break;
            case "redo":
                redo();
                break;
        }
        sendDescriptionPacket();
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public void setText(int textFieldID, String text) {
        ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT).copy();
        if (textFieldID == 0 && !stack.isEmpty()) {
            stack.setDisplayName(new StringTextComponent(text));
            inventory.setStackInSlot(PROGRAM_SLOT, stack);
        }
    }

    @Override
    public String getText(int textFieldID) {
        return inventory.getStackInSlot(PROGRAM_SLOT).getDisplayName().getFormattedText();
    }

    private void tryProgramDrone(PlayerEntity player) {
        if (inventory.getStackInSlot(PROGRAM_SLOT).getItem() instanceof IProgrammable) {
            if (player == null || !player.isCreative()) {
                int required = getRequiredPuzzleCount();
                if (required > 0) {
                    if (!takePuzzleStacks(player, true)) return;
                    takePuzzleStacks(player, false);
                } else if (required < 0) {
                    ItemStack stack = new ItemStack(ModItems.PROGRAMMING_PUZZLE);
                    while (required < 0) {
                        int size = Math.min(required, stack.getMaxStackSize());
                        ItemHandlerHelper.giveItemToPlayer(player, ItemHandlerHelper.copyStackWithSize(stack, size));
                        required += size;
                    }
                }
            }
            ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
            if (!stack.hasTag()) {
                stack.setTag(new CompoundNBT());
            }
            writeProgWidgetsToNBT(stack.getTag());
            if (player != null) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.HUD_INIT_COMPLETE, SoundCategory.BLOCKS, getPos(), 1.0f, 1.0f, false), (ServerPlayerEntity) player);
                AdvancementTriggers.PROGRAM_DRONE.trigger((ServerPlayerEntity) player);
            }
        }
    }

    /**
     * Get the number of puzzle pieces required to program the drone (or other item) in the programming slot.  This can
     * be negative, which means pieces would be returned when programming the drone.
     *
     * @return a piece count
     */
    public int getRequiredPuzzleCount() {
        ItemStack stackInSlot = inventory.getStackInSlot(PROGRAM_SLOT);
        if (!stackInSlot.isEmpty() && ((IProgrammable) stackInSlot.getItem()).usesPieces(stackInSlot)) {
            int dronePieces = getProgWidgets(stackInSlot).size();
            return progWidgets.size() - dronePieces;
        } else {
            return 0;
        }
    }

    public static List<IProgWidget> getProgWidgets(ItemStack iStack) {
        if (NBTUtil.hasTag(iStack, "widgets")) {
            return TileEntityProgrammer.getWidgetsFromNBT(iStack.getTag());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Extract puzzle stacks from the player, then from adjacent inventories.
     *
     * @param player the player, may be null
     * @param simulate true if extraction should only be simulated
     * @return true if enough stacks to fulfill the current programming required are available
     */
    private boolean takePuzzleStacks(@Nullable PlayerEntity player, final boolean simulate) {
        int required = getRequiredPuzzleCount();
        if (required <= 0) return true;

        int found = 0;

        // look in player's inventory
        if (player != null) {
            found += extractPuzzlePieces(new PlayerMainInvWrapper(player.inventory), required, simulate);
            if (found >= required) return true;
        }

        // look in adjacent inventories
        for (Direction d : Direction.VALUES) {
            TileEntity te = getTileCache()[d.getIndex()].getTileEntity();
            if (te != null) {
                final int r = required - found;
                found += te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite())
                        .map(h -> extractPuzzlePieces(h, r, simulate))
                        .orElse(0);
                if (found >= required) return true;
            }
        }
        return false;
    }


    private int extractPuzzlePieces(IItemHandler handler, int max, boolean simulate) {
        int n = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (stackInSlot.getItem() instanceof ItemProgrammingPuzzle) {
                ItemStack extracted = handler.extractItem(i, Math.min(max, stackInSlot.getMaxStackSize()), simulate);
                n += extracted.getCount();
                if (n >= max) return n;
            }
        }
        return n;
    }

    public Set<String> getAllVariables() {
        Set<String> variables = new HashSet<>();
        for (IProgWidget widget : progWidgets) {
            if (widget instanceof IVariableWidget) ((IVariableWidget) widget).addVariables(variables);
        }
        variables.remove("");
        return variables;
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void previewArea(int widgetX, int widgetY) {
        for (IProgWidget w : progWidgets) {
            if (w.getX() == widgetX && w.getY() == widgetY && w instanceof IAreaProvider) {
                Set<BlockPos> area = new HashSet<>();
                ((IAreaProvider) w).getArea(area);
                AreaShowManager.getInstance().showArea(area, 0x9000FF00, this);
            }
        }
    }

    public void saveToHistory() {
        CompoundNBT tag = new CompoundNBT();
        writeProgWidgetsToNBT(tag);
        if (history.size() == 0 || !history.getCompound(historyIndex).equals(tag)) {
            while (history.size() > historyIndex + 1) {
                history.remove(historyIndex + 1);
            }
            history.add(tag);
            if (history.size() > 20) history.remove(0);//Only save up to 20 steps back.
            historyIndex = history.size() - 1;
            updateUndoRedoState();
        }
    }

    private void undo() {
        if (canUndo) {
            historyIndex--;
            readProgWidgetsFromNBT(history.getCompound(historyIndex));
            updateUndoRedoState();
        }
    }

    private void redo() {
        if (canRedo) {
            historyIndex++;
            readProgWidgetsFromNBT(history.getCompound(historyIndex));
            updateUndoRedoState();
        }
    }

    private void updateUndoRedoState() {
        canUndo = historyIndex > 0;
        canRedo = historyIndex < history.size() - 1;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerProgrammer(i, playerInventory, getPos());
    }

    private class ProgrammerItemHandler extends BaseItemStackHandler {
        ProgrammerItemHandler() {
            super(TileEntityProgrammer.this, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (redstoneMode == 1 && slot == PROGRAM_SLOT && !getStackInSlot(slot).isEmpty()) {
                tryProgramDrone(null);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.getItem() instanceof IProgrammable && ((IProgrammable) itemStack.getItem()).canProgram(itemStack);
        }
    }
}
