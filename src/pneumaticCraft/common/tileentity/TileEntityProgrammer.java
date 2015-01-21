package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.client.AreaShowHandler;
import pneumaticCraft.client.AreaShowManager;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.progwidgets.IAreaProvider;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetBlockCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetBlockRightClick;
import pneumaticCraft.common.progwidgets.ProgWidgetDig;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneConditionEntity;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneConditionItem;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneConditionLiquid;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneConditionPressure;
import pneumaticCraft.common.progwidgets.ProgWidgetDropItem;
import pneumaticCraft.common.progwidgets.ProgWidgetEmitRedstone;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityAttack;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityExport;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityImport;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityRightClick;
import pneumaticCraft.common.progwidgets.ProgWidgetGoToLocation;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryExport;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryImport;
import pneumaticCraft.common.progwidgets.ProgWidgetItemFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetItemInventoryCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetJump;
import pneumaticCraft.common.progwidgets.ProgWidgetLabel;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidExport;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidImport;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidInventoryCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetPickupItem;
import pneumaticCraft.common.progwidgets.ProgWidgetPlace;
import pneumaticCraft.common.progwidgets.ProgWidgetPressureCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetRedstoneCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetRename;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.common.progwidgets.ProgWidgetString;
import pneumaticCraft.common.progwidgets.ProgWidgetTeleport;
import pneumaticCraft.common.progwidgets.ProgWidgetWait;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityProgrammer extends TileEntityBase implements IInventory{
    public List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
    public static List<IProgWidget> registeredWidgets = new ArrayList<IProgWidget>();
    @SideOnly(Side.CLIENT)
    private static AreaShowHandler previewedArea;
    public int redstoneMode;//for later use
    private ItemStack[] inventory = new ItemStack[1];

    public static final int PROGRAM_SLOT = 0;

    //Client side variables that are used to prevent resetting.
    public int translatedX, translatedY, zoomState;
    public boolean[] filters = new boolean[IProgWidget.WidgetCategory.values().length];
    public boolean showInfo, showFlow;

    public TileEntityProgrammer(){
        Arrays.fill(filters, true);
    }

    static {
        registeredWidgets.add(new ProgWidgetStart());
        registeredWidgets.add(new ProgWidgetArea());
        registeredWidgets.add(new ProgWidgetString());
        registeredWidgets.add(new ProgWidgetItemFilter());
        registeredWidgets.add(new ProgWidgetLiquidFilter());
        registeredWidgets.add(new ProgWidgetEntityAttack());
        registeredWidgets.add(new ProgWidgetDig());
        registeredWidgets.add(new ProgWidgetPlace());
        registeredWidgets.add(new ProgWidgetBlockRightClick());
        registeredWidgets.add(new ProgWidgetEntityRightClick());
        registeredWidgets.add(new ProgWidgetPickupItem());
        registeredWidgets.add(new ProgWidgetDropItem());
        registeredWidgets.add(new ProgWidgetInventoryExport());
        registeredWidgets.add(new ProgWidgetInventoryImport());
        registeredWidgets.add(new ProgWidgetLiquidExport());
        registeredWidgets.add(new ProgWidgetLiquidImport());
        registeredWidgets.add(new ProgWidgetEntityExport());
        registeredWidgets.add(new ProgWidgetEntityImport());
        registeredWidgets.add(new ProgWidgetGoToLocation());
        registeredWidgets.add(new ProgWidgetTeleport());
        registeredWidgets.add(new ProgWidgetEmitRedstone());
        registeredWidgets.add(new ProgWidgetLabel());
        registeredWidgets.add(new ProgWidgetJump());
        registeredWidgets.add(new ProgWidgetWait());
        registeredWidgets.add(new ProgWidgetRename());
        registeredWidgets.add(new ProgWidgetRedstoneCondition());
        registeredWidgets.add(new ProgWidgetItemInventoryCondition());
        registeredWidgets.add(new ProgWidgetBlockCondition());
        registeredWidgets.add(new ProgWidgetLiquidInventoryCondition());
        registeredWidgets.add(new ProgWidgetEntityCondition());
        registeredWidgets.add(new ProgWidgetPressureCondition());
        registeredWidgets.add(new ProgWidgetDroneConditionItem());
        registeredWidgets.add(new ProgWidgetDroneConditionLiquid());
        registeredWidgets.add(new ProgWidgetDroneConditionEntity());
        registeredWidgets.add(new ProgWidgetDroneConditionPressure());
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

    }

    @Override
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        writeProgWidgetsToNBT(tag);
    }

    public void readProgWidgetsFromNBT(NBTTagCompound tag){
        progWidgets = getWidgetsFromNBT(tag);
    }

    public void writeProgWidgetsToNBT(NBTTagCompound tag){
        setWidgetsToNBT(progWidgets, tag);
    }

    public static List<IProgWidget> getWidgetsFromNBT(NBTTagCompound tag){
        List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
        NBTTagList widgetTags = tag.getTagList("widgets", 10);
        for(int i = 0; i < widgetTags.tagCount(); i++) {
            NBTTagCompound widgetTag = widgetTags.getCompoundTagAt(i);
            String widgetName = widgetTag.getString("name");
            for(IProgWidget widget : registeredWidgets) {
                if(widgetName.equals(widget.getWidgetString())) {//create the right progWidget for the given id tag.
                    IProgWidget addedWidget = widget.copy();
                    addedWidget.readFromNBT(widgetTag);
                    progWidgets.add(addedWidget);
                    break;
                }
            }
        }
        updatePuzzleConnections(progWidgets);
        return progWidgets;
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
                if(redstoneMode++ > 1) redstoneMode = 1;
                break;
            case 1:
                NBTTagCompound tag = inventory[PROGRAM_SLOT] != null ? inventory[PROGRAM_SLOT].getTagCompound() : null;
                if(tag != null) readProgWidgetsFromNBT(tag);
                else progWidgets = new ArrayList<IProgWidget>();
                break;
            case 2:
                if(inventory[PROGRAM_SLOT] != null) {
                    if(!player.capabilities.isCreativeMode) {
                        List<ItemStack> requiredStacks = getRequiredPuzzleStacks();
                        for(ItemStack stack : requiredStacks) {
                            if(!hasEnoughPuzzleStacks(player, stack)) return;
                        }
                        for(ItemStack stack : requiredStacks) {
                            int left = stack.stackSize;
                            for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
                                if(PneumaticCraftUtils.areStacksEqual(stack, player.inventory.getStackInSlot(i), true, true, false, false)) {
                                    left -= player.inventory.decrStackSize(i, left).stackSize;
                                    if(left <= 0) break;
                                }
                            }
                        }
                        List<ItemStack> returnedStacks = getReturnedPuzzleStacks();
                        for(ItemStack stack : returnedStacks) {
                            if(!player.inventory.addItemStackToInventory(stack)) {
                                player.dropPlayerItemWithRandomChoice(stack.copy(), false);
                            }
                        }
                    }
                    tag = inventory[PROGRAM_SLOT].getTagCompound();
                    if(tag == null) {
                        tag = new NBTTagCompound();
                        inventory[PROGRAM_SLOT].setTagCompound(tag);
                    }
                    writeProgWidgetsToNBT(tag);
                }
                break;
        }
        sendDescriptionPacket();
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
        for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack playerStack = player.inventory.getStackInSlot(i);
            if(PneumaticCraftUtils.areStacksEqual(playerStack, stack, true, true, false, false)) {
                amountLeft -= playerStack.stackSize;
                if(amountLeft <= 0) return true;
            }
        }
        return false;
    }

    public static Map<Integer, Integer> getPuzzleSummary(List<IProgWidget> widgets){
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for(IProgWidget widget : widgets) {
            if(!map.containsKey(widget.getCraftingColorIndex())) {
                map.put(widget.getCraftingColorIndex(), 1);
            } else {
                map.put(widget.getCraftingColorIndex(), map.get(widget.getCraftingColorIndex()) + 1);
            }
        }
        return map;
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
                AreaShowManager.getInstance().removeHandler(previewedArea);
                Set<ChunkPosition> area = ((IAreaProvider)w).getArea();
                previewedArea = AreaShowManager.getInstance().showArea(area, 0x00FF00, this);
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
}
