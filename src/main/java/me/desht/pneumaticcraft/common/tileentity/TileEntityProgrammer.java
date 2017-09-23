package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.AreaShowManager;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.IVariableWidget;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.*;

public class TileEntityProgrammer extends TileEntityBase implements IGUITextFieldSensitive {
    private static final int INVENTORY_SIZE = 1;

    public final List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
    @GuiSynced
    public int redstoneMode;

    private ProgrammerItemHandler inventory = new ProgrammerItemHandler();

    public static final int PROGRAM_SLOT = 0;

    //Client side variables that are used to prevent resetting.
    public int translatedX, translatedY, zoomState;
    public boolean showInfo = true, showFlow = true;
    @GuiSynced
    public boolean canUndo, canRedo;
    private NBTTagList history = new NBTTagList();//Used to undo/redo.
    private int historyIndex;

    public TileEntityProgrammer() {
        saveToHistory();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        history = tag.getTagList("history", 10);
        if (history.tagCount() == 0) saveToHistory();
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        readProgWidgetsFromNBT(tag);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setTag("Items", inventory.serializeNBT());
        tag.setTag("history", history);
        return tag;
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        writeProgWidgetsToNBT(tag);
    }

    public void readProgWidgetsFromNBT(NBTTagCompound tag) {
        progWidgets.clear();
        getWidgetsFromNBT(tag, progWidgets);
    }

    public void writeProgWidgetsToNBT(NBTTagCompound tag) {
        setWidgetsToNBT(progWidgets, tag);
    }

    @Nonnull
    public ItemStack getIteminProgrammingSlot() {
        return inventory.getStackInSlot(PROGRAM_SLOT);
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    public static List<IProgWidget> getWidgetsFromNBT(NBTTagCompound tag) {
        List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
        getWidgetsFromNBT(tag, progWidgets);
        return progWidgets;
    }

    private static void getWidgetsFromNBT(NBTTagCompound tag, List<IProgWidget> progWidgets) {
        NBTTagList widgetTags = tag.getTagList("widgets", 10);
        for (int i = 0; i < widgetTags.tagCount(); i++) {
            NBTTagCompound widgetTag = widgetTags.getCompoundTagAt(i);
            String widgetName = widgetTag.getString("name");
            for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                if (widgetName.equals(widget.getWidgetString())) { //create the right progWidget for the given id tag.
                    IProgWidget addedWidget = widget.copy();
                    addedWidget.readFromNBT(widgetTag);
                    progWidgets.add(addedWidget);
                    break;
                }
            }
        }
        updatePuzzleConnections(progWidgets);
    }

    public static void setWidgetsToNBT(List<IProgWidget> widgets, NBTTagCompound tag) {
        NBTTagList widgetTags = new NBTTagList();
        for (IProgWidget widget : widgets) {
            NBTTagCompound widgetTag = new NBTTagCompound();
            widget.writeToNBT(widgetTag);
            widgetTags.appendTag(widgetTag);
        }
        tag.setTag("widgets", widgetTags);
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
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        switch (buttonID) {
            case 0:
                if (++redstoneMode > 1) redstoneMode = 0;
                break;
            case 1:
                ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
                NBTTagCompound tag = stack.isEmpty() ? null : stack.getTagCompound();
                if (tag != null)
                    readProgWidgetsFromNBT(tag);
                else
                    progWidgets.clear();
                break;
            case 2:
                tryProgramDrone(player);
                break;
            case 9:
                undo();
                break;
            case 10:
                redo();
                break;
        }
        sendDescriptionPacket();
    }

    @Override
    public void setText(int textFieldID, String text) {
        ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT).copy();
        if (textFieldID == 0 && !stack.isEmpty()) {
            stack.setStackDisplayName(text);
            inventory.setStackInSlot(PROGRAM_SLOT, stack);
        }
    }

    @Override
    public String getText(int textFieldID) {
        return inventory.getStackInSlot(PROGRAM_SLOT).getDisplayName();
    }

    private void tryProgramDrone(EntityPlayer player) {
        if (!inventory.getStackInSlot(PROGRAM_SLOT).isEmpty()) {
            if (player == null || !player.capabilities.isCreativeMode) {
                List<ItemStack> requiredStacks = getRequiredPuzzleStacks();
                for (ItemStack stack : requiredStacks) {
                    if (!hasEnoughPuzzleStacks(player, stack)) return;
                }
                for (ItemStack stack : requiredStacks) {
                    int left = stack.getCount();
                    if (player != null) {
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (PneumaticCraftUtils.areStacksEqual(stack, player.inventory.getStackInSlot(i), true, true, false, false)) {
                                left -= player.inventory.decrStackSize(i, left).getCount();
                                if (left <= 0) break;
                            }
                        }
                    }
                    if (left > 0) {
                        for (EnumFacing d : EnumFacing.VALUES) {
                            IItemHandler neighbor = IOHelper.getInventoryForTE(getWorld().getTileEntity(getPos().offset(d)), d.getOpposite());
                            for (int slot = 0; slot < neighbor.getSlots(); slot++) {
                                ItemStack neighborStack = neighbor.extractItem(slot, left, true);
                                if (PneumaticCraftUtils.areStacksEqual(neighborStack, stack, true, true, false, false)) {
                                    neighborStack = neighbor.extractItem(slot, left, false);
                                    left -= neighborStack.getCount();
                                    if (left <= 0) break;
                                }
                            }
                        }
                    }
                }
                List<ItemStack> returnedStacks = getReturnedPuzzleStacks();
                for (ItemStack stack : returnedStacks) {
                    for (EnumFacing d : EnumFacing.VALUES) {
                        TileEntity te = getWorld().getTileEntity(getPos().offset(d));
                        if (te != null) {
                            stack = IOHelper.insert(te, stack, d.getOpposite(), false);
                            if (stack.isEmpty()) break;
                        }
                    }
                    if (player != null && !stack.isEmpty()) {
                        if (!player.inventory.addItemStackToInventory(stack)) {
                            player.dropItem(stack.copy(), false);
                            stack = ItemStack.EMPTY;
                        }
                    }
                    if (!stack.isEmpty()) {
                        getWorld().spawnEntity(new EntityItem(getWorld(), getPos().getX() + 0.5, getPos().getY() + 1.5, getPos().getZ() + 0.5, stack));
                    }
                }
            }
            ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
                inventory.setStackInSlot(PROGRAM_SLOT, stack);
            }
            writeProgWidgetsToNBT(stack.getTagCompound());
        }
    }

    public List<ItemStack> getRequiredPuzzleStacks() {
        ItemStack stackInSlot = inventory.getStackInSlot(PROGRAM_SLOT);
        List<ItemStack> stacks = new ArrayList<>();
        if (!stackInSlot.isEmpty() && ((IProgrammable) stackInSlot.getItem()).usesPieces(stackInSlot)) {
            Map<Integer, Integer> tePieces = getPuzzleSummary(progWidgets);
            Map<Integer, Integer> dronePieces = getPuzzleSummary(getProgWidgets(stackInSlot));
            for (Integer includedWidget : tePieces.keySet()) {
                Integer existingWidgets = dronePieces.get(includedWidget);
                if (existingWidgets != null) {
                    Integer neededWidgets = tePieces.get(includedWidget);
                    if (neededWidgets > existingWidgets) {
                        ItemStack stack = ItemProgrammingPuzzle.getStackForColor(includedWidget);
                        stack.setCount((neededWidgets - existingWidgets) * stackInSlot.getCount());
                        stacks.add(stack);
                    }
                } else {
                    ItemStack stack = ItemProgrammingPuzzle.getStackForColor(includedWidget);
                    stack.setCount(tePieces.get(includedWidget) * stackInSlot.getCount());
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public List<ItemStack> getReturnedPuzzleStacks() {
        ItemStack stackInSlot = inventory.getStackInSlot(PROGRAM_SLOT);
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if (!stackInSlot.isEmpty() && ((IProgrammable) stackInSlot.getItem()).usesPieces(stackInSlot)) {
            Map<Integer, Integer> tePieces = getPuzzleSummary(progWidgets);
            Map<Integer, Integer> dronePieces = getPuzzleSummary(getProgWidgets(stackInSlot));

            for (Integer availableWidget : dronePieces.keySet()) {
                Integer requiredWidget = tePieces.get(availableWidget);
                if (requiredWidget != null) {
                    Integer availableWidgets = dronePieces.get(availableWidget);
                    if (availableWidgets > requiredWidget) {
                        ItemStack stack = ItemProgrammingPuzzle.getStackForColor(availableWidget);
                        stack.setCount((availableWidgets - requiredWidget) * stackInSlot.getCount());
                        while (stack.getCount() > stack.getMaxStackSize()) {
                            stacks.add(stack.splitStack(stack.getMaxStackSize()));
                        }
                        stacks.add(stack);
                    }
                } else {
                    ItemStack stack = ItemProgrammingPuzzle.getStackForColor(availableWidget);
                    stack.setCount(dronePieces.get(availableWidget) * stackInSlot.getCount());
                    while (stack.getCount() > stack.getMaxStackSize()) {
                        stacks.add(stack.splitStack(stack.getMaxStackSize()));
                    }
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public static List<IProgWidget> getProgWidgets(ItemStack iStack) {
        if (NBTUtil.hasTag(iStack, "widgets")) {
            return TileEntityProgrammer.getWidgetsFromNBT(iStack.getTagCompound());
        } else {
            return new ArrayList<>();
        }
    }

    public boolean hasEnoughPuzzleStacks(EntityPlayer player, ItemStack stack) {
        int amountLeft = stack.getCount();
        if (player != null) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack playerStack = player.inventory.getStackInSlot(i);
                if (PneumaticCraftUtils.areStacksEqual(playerStack, stack, true, true, false, false)) {
                    amountLeft -= playerStack.getCount();
                    if (amountLeft <= 0) return true;
                }
            }
        }

        for (EnumFacing d : EnumFacing.VALUES) {
            TileEntity te = getWorld().getTileEntity(getPos().offset(d));
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite())) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack neighborStack = handler.getStackInSlot(slot);
                    if (PneumaticCraftUtils.areStacksEqual(neighborStack, stack, true, true, false, false)) {
                        amountLeft -= neighborStack.getCount();
                        if (amountLeft <= 0) return true;
                    }
                }
            }
        }

        return false;
    }

    public static Map<Integer, Integer> getPuzzleSummary(List<IProgWidget> widgets) {
        Map<Integer, Integer> map = new HashMap<>();
        for (IProgWidget widget : widgets) {
            if (widget.getCraftingColorIndex() != -1) {
                if (!map.containsKey(widget.getCraftingColorIndex())) {
                    map.put(widget.getCraftingColorIndex(), 1);
                } else {
                    map.put(widget.getCraftingColorIndex(), map.get(widget.getCraftingColorIndex()) + 1);
                }
            }
        }
        return map;
    }

    /**
     * Returns a set with all variables that are used in the program.
     *
     * @return
     */
    public Set<String> getAllVariables() {
        Set<String> variables = new HashSet<String>();
        for (IProgWidget widget : progWidgets) {
            if (widget instanceof IVariableWidget) ((IVariableWidget) widget).addVariables(variables);
        }
        variables.remove("");
        return variables;
    }

    @Override
    public String getName() {
        return Blockss.PROGRAMMER.getUnlocalizedName();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();

    }

    public boolean previewArea(int widgetX, int widgetY) {
        for (IProgWidget w : progWidgets) {
            if (w.getX() == widgetX && w.getY() == widgetY && w instanceof IAreaProvider) {
                Set<BlockPos> area = new HashSet<BlockPos>();
                ((IAreaProvider) w).getArea(area);
                AreaShowManager.getInstance().showArea(area, 0x00FF00, this);
            }
        }
        return true;
    }

    public void saveToHistory() {
        NBTTagCompound tag = new NBTTagCompound();
        writeProgWidgetsToNBT(tag);
        if (history.tagCount() == 0 || !history.getCompoundTagAt(historyIndex).equals(tag)) {
            while (history.tagCount() > historyIndex + 1) {
                history.removeTag(historyIndex + 1);
            }
            history.appendTag(tag);
            if (history.tagCount() > 20) history.removeTag(0);//Only save up to 20 steps back.
            historyIndex = history.tagCount() - 1;
            updateUndoRedoState();
        }
    }

    public void undo() {
        if (canUndo) {
            historyIndex--;
            readProgWidgetsFromNBT(history.getCompoundTagAt(historyIndex));
            updateUndoRedoState();
        }
    }

    public void redo() {
        if (canRedo) {
            historyIndex++;
            readProgWidgetsFromNBT(history.getCompoundTagAt(historyIndex));
            updateUndoRedoState();
        }
    }

    private void updateUndoRedoState() {
        canUndo = historyIndex > 0;
        canRedo = historyIndex < history.tagCount() - 1;
    }

    private class ProgrammerItemHandler extends FilteredItemStackHandler {
        ProgrammerItemHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (redstoneMode == 1 && slot == PROGRAM_SLOT && !getStackInSlot(slot).isEmpty()) {
                tryProgramDrone(null);
            }
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return itemStack.getItem() instanceof IProgrammable && ((IProgrammable) itemStack.getItem()).canProgram(itemStack);
        }
    }
}
