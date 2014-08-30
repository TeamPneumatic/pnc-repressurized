// Code used from the Backpack Mod,
// https://github.com/Eydamos/Minecraft-Backpack-Mod

package pneumaticCraft.common.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pneumaticCraft.client.gui.GuiPneumaticInventoryItem;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;

public class InventoryPneumaticInventoryItem extends InventoryBasic{
    // the title of the backpack
    protected String inventoryTitle;
    // the original ItemStack to compare with the player inventory
    public ItemStack armorStack;
    private GuiPneumaticInventoryItem gui;
    private final TileEntityChargingStation te;

    // if class is reading from NBT tag
    protected boolean reading = false;

    public InventoryPneumaticInventoryItem(TileEntityChargingStation te){
        super("", false, getInventorySize(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX)));
        this.te = te;
        armorStack = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);

        // check if inventory exists if not create one
        if(!hasInventory()) {
            createInventory();
        }
        loadInventory();
    }

    /**
     * Is called whenever something is changed in the inventory.
     */
    @Override
    public void markDirty(){
        super.markDirty();
        // if reading from NBT don't write
        if(!reading) {
            saveInventory();
        }
    }

    public void setGui(GuiPneumaticInventoryItem gui){
        this.gui = gui;
    }

    /**
     * This method is called when the chest opens the inventory. It loads the
     * content of the inventory and its title.
     */
    @Override
    public void openInventory(){
        loadInventory();
    }

    /**
     * This method is called when the chest closes the inventory. It then throws
     * out every backpack which is inside the backpack and saves the inventory.
     */
    @Override
    public void closeInventory(){
        saveInventory();
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getInventoryName(){
        return "Pneumatic Helmet";
    }

    protected static int getInventorySize(ItemStack is){
        return 9;
    }

    /**
     * Returns if an Inventory is saved in the NBT.
     * 
     * @return True when the NBT is not null and the NBT has key "Inventory"
     *         otherwise false.
     */
    protected boolean hasInventory(){
        return NBTUtil.hasTag(armorStack, "Inventory");
    }

    /**
     * Creates the Inventory Tag in the NBT with an empty inventory.
     */
    protected void createInventory(){
        writeToNBT();
    }

    /**
     * If there is no inventory create one. Then load the content and title of
     * the inventory from the NBT
     */
    public void loadInventory(){
        readFromNBT();
    }

    /**
     * Saves the actual content of the inventory to the NBT.
     */
    public void saveInventory(){
        writeToNBT();
        updateToChargingStation();
    }

    private void updateToChargingStation(){
        te.setInventorySlotContents(TileEntityChargingStation.CHARGE_INVENTORY_INDEX, armorStack);
    }

    /**
     * Writes a NBT Node with inventory.
     * 
     * @param outerTag
     *            The NBT Node to write to.
     * @return The written NBT Node.
     */
    protected void writeToNBT(){
        NBTTagList itemList = new NBTTagList();
        for(int i = 0; i < getSizeInventory(); i++) {
            if(getStackInSlot(i) != null) {
                NBTTagCompound slotEntry = new NBTTagCompound();
                slotEntry.setByte("Slot", (byte)i);
                getStackInSlot(i).writeToNBT(slotEntry);
                itemList.appendTag(slotEntry);
            }
        }
        // save content in Inventory->Items
        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag("Items", itemList);
        NBTUtil.setCompoundTag(armorStack, "Inventory", inventory);
        // return outerTag;
    }

    /**
     * Reads the inventory from a NBT Node.
     * 
     * @param outerTag
     *            The NBT Node to read from.
     */
    protected void readFromNBT(){
        reading = true;
        NBTTagList itemList = NBTUtil.getCompoundTag(armorStack, "Inventory").getTagList("Items", 10);
        for(int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound slotEntry = itemList.getCompoundTagAt(i);
            int j = slotEntry.getByte("Slot");

            if(j >= 0 && j < getSizeInventory()) {
                setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(slotEntry));
            }
        }
        reading = false;
    }

}
