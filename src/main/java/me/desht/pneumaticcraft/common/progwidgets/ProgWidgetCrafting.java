package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.ItemTagMatcher;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCrafting extends ProgWidget implements ICraftingWidget, ICountWidget, ISidedWidget {
    private static final boolean[] NO_SIDES = new boolean[6];

    private boolean useCount;
    private int count;

    public ProgWidgetCrafting() {
        super(ModProgWidgets.CRAFTING.get());
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        boolean usingVariables = false;
        for (int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter) getConnectedParameters()[y];
            for (int x = 0; x < 3 && itemFilter != null; x++) {
                if (!itemFilter.getVariable().equals("")) usingVariables = true;
                itemFilter = (ProgWidgetItemFilter) itemFilter.getConnectedParameters()[0];
            }
        }
        if (!usingVariables && getRecipeResult(PneumaticCraftRepressurized.proxy.getClientWorld()) == null) {
            curInfo.add(xlate("gui.progWidget.crafting.error.noCraftingRecipe"));
        }
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.ITEM_FILTER.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CRAFTING;
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public CraftingInventory getCraftingGrid() {
        CraftingInventory invCrafting = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity p_75145_1_) {
                return false;
            }

        }, 3, 3);
        for (int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter) getConnectedParameters()[y];
            for (int x = 0; x < 3 && itemFilter != null; x++) {
                invCrafting.setInventorySlotContents(y * 3 + x, itemFilter.getFilter());
                itemFilter = (ProgWidgetItemFilter) itemFilter.getConnectedParameters()[0];
            }
        }
        return invCrafting;
    }

    private ItemStack getRecipeResult(World world) {
        CraftingInventory grid = getCraftingGrid();
        Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, grid, world);
        return recipe.map(r -> r.getCraftingResult(grid)).orElse(ItemStack.EMPTY);
    }

    private static IRecipe<CraftingInventory> getRecipe(World world, ICraftingWidget widget) {
        CraftingInventory grid = widget.getCraftingGrid();
        return world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, grid, world).orElse(null);
    }

    @Override
    public void renderExtraInfo() {
        ItemStack recipe = getRecipeResult(PneumaticCraftRepressurized.proxy.getClientWorld());
        if (recipe != null) {
            ProgWidgetItemFilter.drawItemStack(recipe, 8, getHeight() / 2 - 8, recipe.getCount() + "");
        }
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICrafting(drone, (ICraftingWidget) widget);
    }

    @Override
    public void setSides(boolean[] sides) {
    }

    @Override
    public boolean[] getSides() {
        return NO_SIDES;
    }

    public static class DroneAICrafting extends Goal {
        private final ICraftingWidget widget;
        private final IDroneBase drone;

        DroneAICrafting(IDroneBase drone, ICraftingWidget widget) {
            this.drone = drone;
            this.widget = widget;
        }

        @Override
        public boolean shouldExecute() {
            IRecipe<CraftingInventory> recipe = ProgWidgetCrafting.getRecipe(drone.world(), widget);
            if (recipe == null) return false;

            CraftingInventory craftingGrid = widget.getCraftingGrid();
            for (int crafted = 0; !((ICountWidget) widget).useCount() || crafted < ((ICountWidget) widget).getCount(); crafted++) {
                List<ItemStack>[] equivalentsList = new List[9];
                for (int i = 0; i < equivalentsList.length; i++) {
                    ItemStack originalStack = craftingGrid.getStackInSlot(i);
                    if (!originalStack.isEmpty()) {
                        List<ItemStack> equivalents = new ArrayList<>();
                        for (int j = 0; j < drone.getInv().getSlots(); j++) {
                            ItemStack droneStack = drone.getInv().getStackInSlot(j);
                            if (!droneStack.isEmpty() && (droneStack.getItem() == originalStack.getItem() || ItemTagMatcher.matchTags(droneStack, originalStack))) {
                                equivalents.add(droneStack);
                            }
                        }
                        if (equivalents.isEmpty()) return false;
                        equivalentsList[i] = equivalents;
                    }
                }

                int[] curIndexes = new int[9];
                boolean first = true;
                boolean hasCrafted = false;
                while (first || count(curIndexes, equivalentsList)) {
                    first = false;
                    CraftingInventory craftMatrix = new CraftingInventory(new Container(null, -1) {
                        @Override
                        public boolean canInteractWith(PlayerEntity p_75145_1_) {
                            return false;
                        }

                    }, 3, 3);
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = equivalentsList[i] == null ? ItemStack.EMPTY : equivalentsList[i].get(curIndexes[i]);
                        craftMatrix.setInventorySlotContents(i, stack);
                    }
                    if (recipe.matches(craftMatrix, drone.world())) {
                        if (craft(recipe.getCraftingResult(craftMatrix), craftMatrix)) {
                            hasCrafted = true;
                            break;
                        }
                    }
                }
                if (!hasCrafted) return false;
            }
            return false;
        }

        private boolean count(int[] curIndexes, List<ItemStack>[] equivalentsList) {
            for (int i = 0; i < equivalentsList.length; i++) {
                List<ItemStack> list = equivalentsList[i];
                curIndexes[i]++;
                if (list == null || curIndexes[i] >= list.size()) {
                    curIndexes[i] = 0;
                } else {
                    return true;
                }
            }
            return false;
        }

        public boolean craft(ItemStack craftedStack, CraftingInventory craftMatrix) {
            for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
                int requiredCount = 0;
                ItemStack stack = craftMatrix.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    for (int j = 0; j < craftMatrix.getSizeInventory(); j++) {
                        if (stack == craftMatrix.getStackInSlot(j)) {
                            requiredCount++;
                        }
                    }
                    if (requiredCount > stack.getCount()) return false;
                }
            }

            BasicEventHooks.firePlayerCraftingEvent(drone.getFakePlayer(), craftedStack, craftMatrix);

            for (int i = 0; i < craftMatrix.getSizeInventory(); ++i) {
                ItemStack itemstack1 = craftMatrix.getStackInSlot(i);

                if (!itemstack1.isEmpty()) {
                    if (itemstack1.getItem().hasContainerItem(itemstack1)) {
                        ItemStack itemstack2 = itemstack1.getItem().getContainerItem(itemstack1);

                        if (!itemstack2.isEmpty() && itemstack2.isDamageable() && itemstack2.getDamage() > itemstack2.getMaxDamage()) {
                            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(drone.getFakePlayer(), itemstack2, Hand.MAIN_HAND));
                            continue;
                        }

                        ItemStack remainder = ItemHandlerHelper.insertItem(drone.getInv(), itemstack2.copy(), false);
                        if (!remainder.isEmpty()) {
                            Vec3d pos = drone.getDronePos();
                            ItemEntity item = new ItemEntity(drone.world(), pos.x, pos.y, pos.z, remainder);
                            drone.world().addEntity(item);
                        }
                    }
                    itemstack1.shrink(1); // As this stack references to the Drones stacks in its inventory, we can do this.
                }
            }

            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack stack = drone.getInv().getStackInSlot(i);
                if (stack.getCount() <= 0) {
                    drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                }
            }

            ItemStack remainder = ItemHandlerHelper.insertItem(drone.getInv(), craftedStack, false);
            if (!remainder.isEmpty()) {
                Vec3d pos = drone.getDronePos();
                ItemEntity item = new ItemEntity(drone.world(), pos.x, pos.y, pos.z, remainder);
                drone.world().addEntity(item);
            }
            return true;
        }
    }

    @Override
    public boolean useCount() {
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount) {
        this.useCount = useCount;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("useCount", useCount);
        tag.putInt("count", count);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        useCount = tag.getBoolean("useCount");
        count = tag.getInt("count");
    }
}
