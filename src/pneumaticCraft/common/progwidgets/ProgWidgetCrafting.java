package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetImportExport;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetCrafting extends ProgWidget implements ICraftingWidget, ICountWidget{
    private boolean useCount;
    private int count;

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        boolean usingVariables = false;
        for(int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter)getConnectedParameters()[y];
            for(int x = 0; x < 3 && itemFilter != null; x++) {
                if(!itemFilter.getVariable().equals("")) usingVariables = true;
                itemFilter = (ProgWidgetItemFilter)itemFilter.getConnectedParameters()[0];
            }
        }
        if(!usingVariables && getRecipeResult(PneumaticCraft.proxy.getClientWorld()) == null) {
            curInfo.add("gui.progWidget.crafting.error.noCraftingRecipe");
        }
    }

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class, ProgWidgetItemFilter.class, ProgWidgetItemFilter.class};
    }

    @Override
    public String getWidgetString(){
        return "crafting";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.ENDER_PLANT_DAMAGE;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CRAFTING;
    }

    @Override
    protected boolean hasBlacklist(){
        return false;
    }

    @Override
    public InventoryCrafting getCraftingGrid(){
        InventoryCrafting invCrafting = new InventoryCrafting(new Container(){
            @Override
            public boolean canInteractWith(EntityPlayer p_75145_1_){
                return false;
            }

        }, 3, 3);
        for(int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter)getConnectedParameters()[y];
            for(int x = 0; x < 3 && itemFilter != null; x++) {
                invCrafting.setInventorySlotContents(y * 3 + x, itemFilter.getFilter());
                itemFilter = (ProgWidgetItemFilter)itemFilter.getConnectedParameters()[0];
            }
        }
        return invCrafting;
    }

    private ItemStack getRecipeResult(World world){
        return CraftingManager.getInstance().findMatchingRecipe(getCraftingGrid(), world);
    }

    public static IRecipe getRecipe(World world, ICraftingWidget widget){
        InventoryCrafting craftingGrid = widget.getCraftingGrid();
        for(IRecipe recipe : (List<IRecipe>)CraftingManager.getInstance().getRecipeList()) {
            if(recipe.matches(craftingGrid, world)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public void renderExtraInfo(){
        ItemStack recipe = getRecipeResult(PneumaticCraft.proxy.getClientWorld());
        if(recipe != null) {
            ProgWidgetItemFilter.drawItemStack(recipe, 8, getHeight() / 2 - 8, recipe.stackSize + "");
        }
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAICrafting(drone, (ICraftingWidget)widget);
    }

    public static class DroneAICrafting extends EntityAIBase{

        private final ICraftingWidget widget;
        private final IDroneBase drone;

        public DroneAICrafting(IDroneBase drone, ICraftingWidget widget){
            this.drone = drone;
            this.widget = widget;
        }

        @Override
        public boolean shouldExecute(){
            IRecipe recipe = ProgWidgetCrafting.getRecipe(drone.getWorld(), widget);
            if(recipe == null) return false;
            InventoryCrafting craftingGrid = widget.getCraftingGrid();
            for(int crafted = 0; !((ICountWidget)widget).useCount() || crafted < ((ICountWidget)widget).getCount(); crafted++) {
                List<ItemStack>[] equivalentsList = new List[9];
                for(int i = 0; i < equivalentsList.length; i++) {
                    ItemStack originalStack = craftingGrid.getStackInSlot(i);
                    if(originalStack != null) {
                        List<ItemStack> equivalents = new ArrayList<ItemStack>();
                        for(int j = 0; j < drone.getInventory().getSizeInventory(); j++) {
                            ItemStack droneStack = drone.getInventory().getStackInSlot(j);
                            if(droneStack != null && (droneStack.getItem() == originalStack.getItem() || PneumaticCraftUtils.isSameOreDictStack(droneStack, originalStack))) {
                                equivalents.add(droneStack);
                            }
                        }
                        if(equivalents.isEmpty()) return false;
                        equivalentsList[i] = equivalents;
                    }
                }

                int[] curIndexes = new int[9];
                boolean first = true;
                boolean hasCrafted = false;
                while(first || count(curIndexes, equivalentsList)) {
                    first = false;
                    InventoryCrafting craftMatrix = new InventoryCrafting(new Container(){
                        @Override
                        public boolean canInteractWith(EntityPlayer p_75145_1_){
                            return false;
                        }

                    }, 3, 3);
                    for(int i = 0; i < 9; i++) {
                        ItemStack stack = equivalentsList[i] == null ? null : equivalentsList[i].get(curIndexes[i]);
                        craftMatrix.setInventorySlotContents(i, stack);
                    }
                    if(recipe.matches(craftMatrix, drone.getWorld())) {
                        if(craft(recipe.getCraftingResult(craftMatrix), craftMatrix)) {
                            hasCrafted = true;
                            break;
                        }
                    }
                }
                if(!hasCrafted) return false;
            }
            return false;
        }

        private boolean count(int[] curIndexes, List<ItemStack>[] equivalentsList){
            for(int i = 0; i < equivalentsList.length; i++) {
                List<ItemStack> list = equivalentsList[i];
                curIndexes[i]++;
                if(list == null || curIndexes[i] >= list.size()) {
                    curIndexes[i] = 0;
                } else {
                    return true;
                }
            }
            return false;
        }

        public boolean craft(ItemStack craftedStack, InventoryCrafting craftMatrix){
            for(int i = 0; i < craftMatrix.getSizeInventory(); i++) {
                int requiredCount = 0;
                ItemStack stack = craftMatrix.getStackInSlot(i);
                if(stack != null) {
                    for(int j = 0; j < craftMatrix.getSizeInventory(); j++) {
                        if(stack == craftMatrix.getStackInSlot(j)) {
                            requiredCount++;
                        }
                    }
                    if(requiredCount > stack.stackSize) return false;
                }
            }

            FMLCommonHandler.instance().firePlayerCraftingEvent(drone.getFakePlayer(), craftedStack, craftMatrix);

            for(int i = 0; i < craftMatrix.getSizeInventory(); ++i) {
                ItemStack itemstack1 = craftMatrix.getStackInSlot(i);

                if(itemstack1 != null) {
                    if(itemstack1.getItem().hasContainerItem(itemstack1)) {
                        ItemStack itemstack2 = itemstack1.getItem().getContainerItem(itemstack1);

                        if(itemstack2 != null && itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage()) {
                            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(drone.getFakePlayer(), itemstack2));
                            continue;
                        }

                        ItemStack remainder = IOHelper.insert(drone.getInventory(), itemstack2.copy(), 0, false);
                        if(remainder != null) {
                            Vec3 pos = drone.getPosition();
                            EntityItem item = new EntityItem(drone.getWorld(), pos.xCoord, pos.yCoord, pos.zCoord, remainder);
                            drone.getWorld().spawnEntityInWorld(item);
                        }
                    }
                    itemstack1.stackSize--;//As this stack references to the Drones stacks in its inventory, we can do this.
                }
            }

            for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                ItemStack stack = drone.getInventory().getStackInSlot(i);
                if(stack != null && stack.stackSize <= 0) {
                    drone.getInventory().setInventorySlotContents(i, null);
                }
            }

            ItemStack remainder = IOHelper.insert(drone.getInventory(), craftedStack, 0, false);
            if(remainder != null) {
                Vec3 pos = drone.getPosition();
                EntityItem item = new EntityItem(drone.getWorld(), pos.xCoord, pos.yCoord, pos.zCoord, remainder);
                drone.getWorld().spawnEntityInWorld(item);
            }
            return true;
        }
    }

    @Override
    public boolean useCount(){
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount){
        this.useCount = useCount;
    }

    @Override
    public int getCount(){
        return count;
    }

    @Override
    public void setCount(int count){
        this.count = count;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("useCount", useCount);
        tag.setInteger("count", count);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        useCount = tag.getBoolean("useCount");
        count = tag.getInteger("count");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetImportExport(this, guiProgrammer){
            @Override
            protected boolean showSides(){
                return false;
            }
        };
    }
}
