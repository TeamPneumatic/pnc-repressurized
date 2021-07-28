package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.DroneAICrafting;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.recipes.CraftingRecipeCache;
import me.desht.pneumaticcraft.common.util.DummyContainer;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCrafting extends ProgWidget implements ICraftingWidget, ICountWidget, ISidedWidget {
    private static final boolean[] NO_SIDES = new boolean[6];

    private boolean useCount;
    private int count;
    private boolean usingVariables;

    public ProgWidgetCrafting() {
        super(ModProgWidgets.CRAFTING.get());
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        getCraftingGrid(); // to set up usingVariables
        if (!usingVariables && getRecipeResult(ClientUtils.getClientWorld()) == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.crafting.error.noCraftingRecipe"));
        }
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);

        ItemStack stack = getRecipeResult(ClientUtils.getClientWorld());
        if (!stack.isEmpty()) {
            curTooltip.add(stack.getDisplayName().deepCopy().mergeStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
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
        usingVariables = false;
        CraftingInventory invCrafting = new CraftingInventory(new DummyContainer(), 3, 3);
        for (int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter) getConnectedParameters()[y];
            for (int x = 0; x < 3 && itemFilter != null; x++) {
                if (!itemFilter.getVariable().isEmpty()) usingVariables = true;
                invCrafting.setInventorySlotContents(y * 3 + x, itemFilter.getFilter());
                itemFilter = (ProgWidgetItemFilter) itemFilter.getConnectedParameters()[0];
            }
        }
        return invCrafting;
    }

    public ItemStack getRecipeResult(World world) {
        CraftingInventory grid = getCraftingGrid();
        return getRecipe(world, grid).map(r -> r.getCraftingResult(grid)).orElse(ItemStack.EMPTY);
    }

    @Override
    public Optional<ICraftingRecipe> getRecipe(World world, CraftingInventory grid) {
        // no caching if using variables, because the item can change at any item
        return usingVariables ?
                world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, grid, world) :
                CraftingRecipeCache.INSTANCE.getCachedRecipe(world, grid);
    }

    public static IRecipe<CraftingInventory> getRecipe(World world, ICraftingWidget widget) {
        return widget.getRecipe(world, widget.getCraftingGrid()).orElse(null);
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
        if (useCount) tag.putBoolean("useCount", true);
        tag.putInt("count", count);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        useCount = tag.getBoolean("useCount");
        count = tag.getInt("count");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(useCount);
        buf.writeVarInt(count);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        useCount = buf.readBoolean();
        count = buf.readVarInt();
    }
}
