package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.client.AreaShowManager;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.progwidgets.IAreaProvider;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.IVariableWidget;
import pneumaticCraft.common.progwidgets.WidgetRegistrator;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class TileEntityProgrammer extends TileEntityBase implements IInventory, IGUITextFieldSensitive{
    public final List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
    @GuiSynced
    public int redstoneMode;
    private ItemStack[] inventory = new ItemStack[1];

    public static final int PROGRAM_SLOT = 0;

    //Client side variables that are used to prevent resetting.
    public int translatedX, translatedY, zoomState;
    public boolean showInfo = true, showFlow = true;
    @GuiSynced
    public boolean canUndo, canRedo;
    private NBTTagList history = new NBTTagList();//Used to undo/redo.
    private int historyIndex;

    public TileEntityProgrammer(){
        saveToHistory();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");

        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
        history = tag.getTagList("history", 10);
        if(history.tagCount() == 0) saveToHistory();
    }

    @Override
    public void readFromPacket(NBTTagCompound tag){
        super.readFromPacket(tag);
        readProgWidgetsFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("redstoneMode", redstoneMode);

        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Items", tagList);
        tag.setTag("history", history);
    }

    @Override
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        writeProgWidgetsToNBT(tag);
    }

    public void readProgWidgetsFromNBT(NBTTagCompound tag){
        progWidgets.clear();
        getWidgetsFromNBT(tag, progWidgets);
    }

    public void writeProgWidgetsToNBT(NBTTagCompound tag){
        setWidgetsToNBT(progWidgets, tag);
    }

    public static List<IProgWidget> getWidgetsFromNBT(NBTTagCompound tag){
        List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
        getWidgetsFromNBT(tag, progWidgets);
        return progWidgets;
    }

    public static void getWidgetsFromNBT(NBTTagCompound tag, List<IProgWidget> progWidgets){
        NBTTagList widgetTags = tag.getTagList("widgets", 10);
        for(int i = 0; i < widgetTags.tagCount(); i++) {
            NBTTagCompound widgetTag = widgetTags.getCompoundTagAt(i);
            String widgetName = widgetTag.getString("name");
            for(IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                if(widgetName.equals(widget.getWidgetString())) {//create the right progWidget for the given id tag.
                    IProgWidget addedWidget = widget.copy();
                    addedWidget.readFromNBT(widgetTag);
                    progWidgets.add(addedWidget);
                    break;
                }
            }
        }
        updatePuzzleConnections(progWidgets);
    }

    public static void setWidgetsToNBT(List<IProgWidget> widgets, NBTTagCompound tag){
        NBTTagList widgetTags = new NBTTagList();
        for(IProgWidget widget : widgets) {
            NBTTagCompound widgetTag = new NBTTagCompound();
            widget.writeToNBT(widgetTag);
            widgetTags.appendTag(widgetTag);
        }
        tag.setTag("widgets", widgetTags);
    }

    public static void updatePuzzleConnections(List<IProgWidget> progWidgets){
        for(IProgWidget widget : progWidgets) {
            widget.setParent(null);
            Class<? extends IProgWidget>[] parameters = widget.getParameters();
            if(parameters != null) {
                for(int i = 0; i < parameters.length * 2; i++) {
                    widget.setParameter(i, null);
                }
            }
            if(widget.hasStepOutput()) widget.setOutputWidget(null);
        }
        for(IProgWidget checkedWidget : progWidgets) {
            //check for connection to the right of the checked widget.
            Class<? extends IProgWidget>[] parameters = checkedWidget.getParameters();
            if(parameters != null) {
                for(IProgWidget widget : progWidgets) {
                    if(widget != checkedWidget && checkedWidget.getX() + checkedWidget.getWidth() / 2 == widget.getX()) {
                        for(int i = 0; i < parameters.length; i++) {
                            if(checkedWidget.canSetParameter(i) && parameters[i] == widget.returnType() && checkedWidget.getY() + i * 11 == widget.getY()) {
                                checkedWidget.setParameter(i, widget);
                                widget.setParent(checkedWidget);
                            }
                        }
                    }
                }
            }

            //check for connection to the bottom of the checked widget.
            if(checkedWidget.hasStepOutput()) {
                for(IProgWidget widget : progWidgets) {
                    if(widget.hasStepInput() && widget.getX() == checkedWidget.getX() && widget.getY() == checkedWidget.getY() + checkedWidget.getHeight() / 2) {
                        checkedWidget.setOutputWidget(widget);
                    }
                }
            }
        }

        //go again for the blacklist (as those are mirrored)
        for(IProgWidget checkedWidget : progWidgets) {
            if(checkedWidget.returnType() == null) { //if it's a program (import/export inventory, attack entity) rather than a parameter (area, item filter).
                Class<? extends IProgWidget>[] parameters = checkedWidget.getParameters();
                if(parameters != null) {
                    for(int i = 0; i < parameters.length; i++) {
                        if(checkedWidget.canSetParameter(i)) {
                            for(IProgWidget widget : progWidgets) {
                                if(parameters[i] == widget.returnType()) {
                                    if(widget != checkedWidget && widget.getX() + widget.getWidth() / 2 == checkedWidget.getX() && widget.getY() == checkedWidget.getY() + i * 11) {
                                        IProgWidget root = widget;
                                        while(root.getParent() != null) {
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
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        switch(buttonID){
            case 0:
                if(++redstoneMode > 1) redstoneMode = 0;
                break;
            case 1:
                NBTTagCompound tag = inventory[PROGRAM_SLOT] != null ? inventory[PROGRAM_SLOT].getTagCompound() : null;
                if(tag != null) readProgWidgetsFromNBT(tag);
                else progWidgets.clear();
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
    public void setText(int textFieldID, String text){
        if(textFieldID == 0 && inventory[PROGRAM_SLOT] != null) {
            inventory[PROGRAM_SLOT].setStackDisplayName(text);
        }
    }

    @Override
    public String getText(int textFieldID){
        return inventory[PROGRAM_SLOT] != null ? inventory[PROGRAM_SLOT].getDisplayName() : "";
    }

    private void tryProgramDrone(EntityPlayer player){
        if(inventory[PROGRAM_SLOT] != null) {
            if(player == null || !player.capabilities.isCreativeMode) {
                List<ItemStack> requiredStacks = getRequiredPuzzleStacks();
                for(ItemStack stack : requiredStacks) {
                    if(!hasEnoughPuzzleStacks(player, stack)) return;
                }
                for(ItemStack stack : requiredStacks) {
                    int left = stack.stackSize;
                    if(player != null) {
                        for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if(PneumaticCraftUtils.areStacksEqual(stack, player.inventory.getStackInSlot(i), true, true, false, false)) {
                                left -= player.inventory.decrStackSize(i, left).stackSize;
                                if(left <= 0) break;
                            }
                        }
                    }
                    if(left > 0) {
                        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                            IInventory neighbor = IOHelper.getInventoryForTE(getWorldObj().getTileEntity(xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ));
                            for(int slot : IOHelper.getAccessibleSlotsForInventory(neighbor, d.getOpposite())) {
                                if(IOHelper.canExtractItemFromInventory(neighbor, stack, slot, d.getOpposite().ordinal())) {
                                    ItemStack neighborStack = neighbor.getStackInSlot(slot);
                                    if(PneumaticCraftUtils.areStacksEqual(neighborStack, stack, true, true, false, false)) {
                                        left -= neighbor.decrStackSize(slot, left).stackSize;
                                        if(left <= 0) break;
                                    }
                                }
                            }
                        }
                    }
                }
                List<ItemStack> returnedStacks = getReturnedPuzzleStacks();
                for(ItemStack stack : returnedStacks) {
                    for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                        IInventory neighbor = IOHelper.getInventoryForTE(getWorldObj().getTileEntity(xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ));
                        stack = IOHelper.insert(neighbor, stack, d.getOpposite().ordinal(), false);
                        if(stack == null) break;
                    }
                    if(player != null && stack != null) {
                        if(!player.inventory.addItemStackToInventory(stack)) {
                            player.dropPlayerItemWithRandomChoice(stack.copy(), false);
                            stack = null;
                        }
                    }
                    if(stack != null) {
                        worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, stack));
                    }
                }
            }
            NBTTagCompound tag = inventory[PROGRAM_SLOT].getTagCompound();
            if(tag == null) {
                tag = new NBTTagCompound();
                inventory[PROGRAM_SLOT].setTagCompound(tag);
            }
            writeProgWidgetsToNBT(tag);
        }
    }

    public List<ItemStack> getRequiredPuzzleStacks(){
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if(((IProgrammable)inventory[PROGRAM_SLOT].getItem()).usesPieces(inventory[PROGRAM_SLOT])) {
            Map<Integer, Integer> tePieces = getPuzzleSummary(progWidgets);
            Map<Integer, Integer> dronePieces = getPuzzleSummary(getProgWidgets(inventory[PROGRAM_SLOT]));
            for(Integer includedWidget : tePieces.keySet()) {
                Integer existingWidgets = dronePieces.get(includedWidget);
                if(existingWidgets != null) {
                    Integer neededWidgets = tePieces.get(includedWidget);
                    if(neededWidgets > existingWidgets) {
                        ItemStack stack = ItemProgrammingPuzzle.getStackForColor(includedWidget);
                        stack.stackSize = (neededWidgets - existingWidgets) * inventory[PROGRAM_SLOT].stackSize;
                        stacks.add(stack);
                    }
                } else {
                    ItemStack stack = ItemProgrammingPuzzle.getStackForColor(includedWidget);
                    stack.stackSize = tePieces.get(includedWidget) * inventory[PROGRAM_SLOT].stackSize;
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public List<ItemStack> getReturnedPuzzleStacks(){
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if(((IProgrammable)inventory[PROGRAM_SLOT].getItem()).usesPieces(inventory[PROGRAM_SLOT])) {
            Map<Integer, Integer> tePieces = getPuzzleSummary(progWidgets);
            Map<Integer, Integer> dronePieces = getPuzzleSummary(getProgWidgets(inventory[PROGRAM_SLOT]));

            for(Integer availableWidget : dronePieces.keySet()) {
                Integer requiredWidget = tePieces.get(availableWidget);
                if(requiredWidget != null) {
                    Integer availableWidgets = dronePieces.get(availableWidget);
                    if(availableWidgets > requiredWidget) {
                        ItemStack stack = ItemProgrammingPuzzle.getStackForColor(availableWidget);
                        stack.stackSize = (availableWidgets - requiredWidget) * inventory[PROGRAM_SLOT].stackSize;
                        while(stack.stackSize > stack.getMaxStackSize()) {
                            stacks.add(stack.splitStack(stack.getMaxStackSize()));
                        }
                        stacks.add(stack);
                    }
                } else {
                    ItemStack stack = ItemProgrammingPuzzle.getStackForColor(availableWidget);
                    stack.stackSize = dronePieces.get(availableWidget) * inventory[PROGRAM_SLOT].stackSize;
                    while(stack.stackSize > stack.getMaxStackSize()) {
                        stacks.add(stack.splitStack(stack.getMaxStackSize()));
                    }
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public static List<IProgWidget> getProgWidgets(ItemStack iStack){
        if(NBTUtil.hasTag(iStack, "widgets")) {
            return TileEntityProgrammer.getWidgetsFromNBT(iStack.getTagCompound());
        } else {
            return new ArrayList<IProgWidget>();
        }
    }

    public boolean hasEnoughPuzzleStacks(EntityPlayer player, ItemStack stack){
        int amountLeft = stack.stackSize;
        if(player != null) {
            for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack playerStack = player.inventory.getStackInSlot(i);
                if(PneumaticCraftUtils.areStacksEqual(playerStack, stack, true, true, false, false)) {
                    amountLeft -= playerStack.stackSize;
                    if(amountLeft <= 0) return true;
                }
            }
        }

        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            IInventory neighbor = IOHelper.getInventoryForTE(getWorldObj().getTileEntity(xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ));
            for(int slot : IOHelper.getAccessibleSlotsForInventory(neighbor, d.getOpposite())) {
                if(IOHelper.canExtractItemFromInventory(neighbor, stack, slot, d.getOpposite().ordinal())) {
                    ItemStack neighborStack = neighbor.getStackInSlot(slot);
                    if(PneumaticCraftUtils.areStacksEqual(neighborStack, stack, true, true, false, false)) {
                        amountLeft -= neighborStack.stackSize;
                        if(amountLeft <= 0) return true;
                    }
                }
            }
        }

        return false;
    }

    public static Map<Integer, Integer> getPuzzleSummary(List<IProgWidget> widgets){
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for(IProgWidget widget : widgets) {
            if(widget.getCraftingColorIndex() != -1) {
                if(!map.containsKey(widget.getCraftingColorIndex())) {
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
     * @return
     */
    public Set<String> getAllVariables(){
        Set<String> variables = new HashSet<String>();
        for(IProgWidget widget : progWidgets) {
            if(widget instanceof IVariableWidget) ((IVariableWidget)widget).addVariables(variables);
        }
        variables.remove("");
        return variables;
    }

    // INVENTORY METHODS

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        if(redstoneMode == 1 && slot == 0 && itemStack != null) {
            tryProgramDrone(null);
        }
    }

    @Override
    public String getInventoryName(){
        return Blockss.programmer.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        if(i == PROGRAM_SLOT && itemstack != null && (!(itemstack.getItem() instanceof IProgrammable) || !((IProgrammable)itemstack.getItem()).canProgram(itemstack))) return false;
        return true;
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
    }

    public boolean previewArea(int widgetX, int widgetY){
        for(IProgWidget w : progWidgets) {
            if(w.getX() == widgetX && w.getY() == widgetY && w instanceof IAreaProvider) {
                Set<ChunkPosition> area = new HashSet<ChunkPosition>();
                ((IAreaProvider)w).getArea(area);
                AreaShowManager.getInstance().showArea(area, 0x00FF00, this);
            }
        }
        return true;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    public void saveToHistory(){
        NBTTagCompound tag = new NBTTagCompound();
        writeProgWidgetsToNBT(tag);
        if(history.tagCount() == 0 || !history.getCompoundTagAt(historyIndex).equals(tag)) {
            while(history.tagCount() > historyIndex + 1) {
                history.removeTag(historyIndex + 1);
            }
            history.appendTag(tag);
            if(history.tagCount() > 20) history.removeTag(0);//Only save up to 20 steps back. 
            historyIndex = history.tagCount() - 1;
            updateUndoRedoState();
        }
    }

    public void undo(){
        if(canUndo) {
            historyIndex--;
            readProgWidgetsFromNBT(history.getCompoundTagAt(historyIndex));
            updateUndoRedoState();
        }
    }

    public void redo(){
        if(canRedo) {
            historyIndex++;
            readProgWidgetsFromNBT(history.getCompoundTagAt(historyIndex));
            updateUndoRedoState();
        }
    }

    private void updateUndoRedoState(){
        canUndo = historyIndex > 0;
        canRedo = historyIndex < history.tagCount() - 1;
    }
}
