package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammer;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityProgrammer extends TileEntityTickableBase implements IGUITextFieldSensitive, INamedContainerProvider {
    private static final int PROGRAM_SLOT = 0;
    private static final int INVENTORY_SIZE = 1;

    public final List<IProgWidget> progWidgets = new ArrayList<>();

    private final ProgrammerItemHandler inventory = new ProgrammerItemHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    // Client side variables that are used to prevent resetting.
    public double translatedX;
    public double translatedY;
    public int zoomState;
    public boolean showInfo = true, showFlow = true;

    @GuiSynced
    public boolean recentreStartPiece = false;
    @GuiSynced
    public boolean canUndo;
    @GuiSynced
    public boolean canRedo;
    @GuiSynced
    public boolean programOnInsert; // false = program drone on button click, true = program when inserted
    @GuiSynced
    public int availablePuzzlePieces;  // puzzle pieces in adjacent inventories (pieces in player inv are counted in programmer gui)

    private ListNBT history = new ListNBT(); //Used to undo/redo.
    private int historyIndex;

    public TileEntityProgrammer() {
        super(ModTileEntities.PROGRAMMER.get());

        saveToHistory();
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains(NBTKeys.NBT_REDSTONE_MODE)) {
            // TODO remove in 1.17 - legacy compat
            programOnInsert = tag.getInt(NBTKeys.NBT_REDSTONE_MODE) == 1;
        } else {
            programOnInsert = tag.getBoolean("ProgramOnInsert");
        }
        history = tag.getList("history", 10);
        if (history.size() == 0) saveToHistory();
        readProgWidgetsFromNBT(tag);
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.put("history", history);
        tag.putBoolean("ProgramOnInsert", programOnInsert);
        writeProgWidgetsToNBT(tag);
        return tag;
    }

    public List<IProgWidget> mergeWidgetsFromNBT(CompoundNBT tag) {
        List<IProgWidget> mergedWidgets = getWidgetsFromNBT(tag);
        List<IProgWidget> result = new ArrayList<>(progWidgets);

        if (!progWidgets.isEmpty() && !mergedWidgets.isEmpty()) {
            // move merged widgets so they definitely don't overlap any existing widgets
            PuzzleExtents extents1 = getPuzzleExtents(progWidgets);
            PuzzleExtents extents2 = getPuzzleExtents(mergedWidgets);
            for (IProgWidget w : mergedWidgets) {
                w.setX(w.getX() - extents2.getX() + extents1.getX() + extents1.getWidth() + 10);
                w.setY(w.getY() - extents2.getY() + extents1.getY());
            }
        }

        mergedWidgets.forEach(w -> {
            if (w instanceof ProgWidgetStart) {
                // any start widget in the merged import is replaced with a label/text widget pair
                ProgWidgetLabel lab = new ProgWidgetLabel();
                lab.setX(w.getX());
                lab.setY(w.getY());
                result.add(lab);
                ProgWidgetText text = new ProgWidgetText();
                text.string = "Merge #" + world.getGameTime();
                text.setX(lab.getX() + lab.getWidth() / 2);
                text.setY(lab.getY());
                result.add(text);
            } else {
                result.add(w);
            }
        });

        return result;
    }

    /**
     * Get the smallest bounding box which fully encloses the widgets in the given list.
     * Used when merging widget lists.
     *
     * @param widgets the widget list
     * @return a bounding box
     */
    private PuzzleExtents getPuzzleExtents(List<IProgWidget> widgets) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (IProgWidget w : widgets) {
            minX = Math.min(minX, w.getX());
            maxX = Math.max(maxX, w.getX() + w.getWidth());
            minY = Math.min(minY, w.getY());
            maxY = Math.max(maxY, w.getY() + w.getHeight());
        }
        return new PuzzleExtents(minX, minY, maxX - minX, maxY - minY);
    }

    public void readProgWidgetsFromNBT(CompoundNBT tag) {
        progWidgets.clear();
        progWidgets.addAll(getWidgetsFromNBT(tag));
        updatePuzzleConnections(progWidgets);
    }

    public CompoundNBT writeProgWidgetsToNBT(CompoundNBT tag) {
        putWidgetsToNBT(progWidgets, tag);
        return tag;
    }

    public static List<IProgWidget> getWidgetsFromNBT(CompoundNBT tag) {
        List<IProgWidget> newWidgets = new ArrayList<>();
        ListNBT widgetTags = tag.getList(IProgrammable.NBT_WIDGETS, NBT.TAG_COMPOUND);
        for (int i = 0; i < widgetTags.size(); i++) {
            IProgWidget addedWidget = ProgWidget.fromNBT(widgetTags.getCompound(i));
            if (addedWidget != null) {
                if (addedWidget.isAvailable()) {
                    newWidgets.add(addedWidget);
                } else {
                    Log.warning("ignoring unavailable widget type: " + addedWidget.getType());
                }
            }
        }
        return newWidgets;
    }

    public static CompoundNBT putWidgetsToNBT(List<IProgWidget> widgets, CompoundNBT tag) {
        ListNBT widgetTags = new ListNBT();
        for (IProgWidget widget : widgets) {
            CompoundNBT widgetTag = new CompoundNBT();
            widget.writeToNBT(widgetTag);
            widgetTags.add(widgetTags.size(), widgetTag);
        }
        tag.put(IProgrammable.NBT_WIDGETS, widgetTags);
        return tag;
    }

    public static void updatePuzzleConnections(List<IProgWidget> progWidgets) {
        for (IProgWidget widget : progWidgets) {
            widget.setParent(null);
            List<ProgWidgetType<?>> parameters = widget.getParameters();
            for (int i = 0; i < parameters.size() * 2; i++) {
                widget.setParameter(i, null);
            }
            if (widget.hasStepOutput()) widget.setOutputWidget(null);
        }

        for (IProgWidget checkedWidget : progWidgets) {
            // check for connection to the right of the checked widget.
            List<ProgWidgetType<?>> parameters = checkedWidget.getParameters();
            if (!parameters.isEmpty()) {
                for (IProgWidget widget : progWidgets) {
                    if (widget != checkedWidget && checkedWidget.getX() + checkedWidget.getWidth() / 2 == widget.getX()) {
                        for (int i = 0; i < parameters.size(); i++) {
                            if (checkedWidget.canSetParameter(i)
                                    && parameters.get(i) == widget.returnType()
                                    && checkedWidget.getY() + i * 11 == widget.getY())
                            {
                                checkedWidget.setParameter(i, widget);
                                widget.setParent(checkedWidget);
                            }
                        }
                    }
                }
            }

            // check for connection to the bottom of the checked widget.
            if (checkedWidget.hasStepOutput()) {
                for (IProgWidget widget : progWidgets) {
                    if (widget.hasStepInput()
                            && widget.getX() == checkedWidget.getX()
                            && widget.getY() == checkedWidget.getY() + checkedWidget.getHeight() / 2)
                    {
                        checkedWidget.setOutputWidget(widget);
                    }
                }
            }
        }

        // go again for the blacklist (as those are mirrored)
        for (IProgWidget checkedWidget : progWidgets) {
            if (checkedWidget.returnType() == null) {
                // this is a program widget rather than a parameter widget (area, item filter).
                List<ProgWidgetType<?>> parameters = checkedWidget.getParameters();
                for (int i = 0; i < parameters.size(); i++) {
                    if (checkedWidget.canSetParameter(i)) {
                        for (IProgWidget widget : progWidgets) {
                            if (parameters.get(i) == widget.returnType()
                                    && widget != checkedWidget
                                    && widget.getX() + widget.getWidth() / 2 == checkedWidget.getX()
                                    && widget.getY() == checkedWidget.getY() + i * 11) {
                                IProgWidget root = widget;
                                while (root.getParent() != null) {
                                    root = root.getParent();
                                }
                                checkedWidget.setParameter(i + parameters.size(), root);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        switch (tag) {
            case "program_when":
                programOnInsert = !programOnInsert;
                break;
            case "import":
                tryImport(shiftHeld);
                break;
            case "export":
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

    @Nonnull
    public ItemStack getItemInProgrammingSlot() {
        return inventory.getStackInSlot(PROGRAM_SLOT);
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
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
        return inventory.getStackInSlot(PROGRAM_SLOT).getDisplayName().getString();
    }

    private void tryImport(boolean merge) {
        ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
        CompoundNBT nbt = stack.isEmpty() ? null : stack.getTag();
        if (nbt != null) {
            List<IProgWidget> widgets = merge ? mergeWidgetsFromNBT(nbt) : getWidgetsFromNBT(nbt);
            setProgWidgets(widgets, null);
        } else {
            if (!merge) setProgWidgets(Collections.emptyList(), null);
        }
    }

    public void tryProgramDrone(PlayerEntity player) {
        if (inventory.getStackInSlot(PROGRAM_SLOT).getItem() instanceof IProgrammable) {
            if (player == null || !player.isCreative()) {
                int required = getRequiredPuzzleCount();
                if (required > 0) {
                    if (!takePuzzlePieces(player, true)) {
                        if (player instanceof ServerPlayerEntity) {
                            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundCategory.BLOCKS, getPos(), 1.0f, 1.5f, false), (ServerPlayerEntity) player);
                        }
                        return;
                    }
                    takePuzzlePieces(player, false);
                } else if (required < 0) {
                    returnPuzzlePieces(player, -required);
                }
            }
            ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
            writeProgWidgetsToNBT(stack.getOrCreateTag());
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.HUD_INIT_COMPLETE.get(), SoundCategory.BLOCKS, getPos(), 1.0f, 1.0f, false), (ServerPlayerEntity) player);
                AdvancementTriggers.PROGRAM_DRONE.trigger((ServerPlayerEntity) player);
            }
        }
    }

    /**
     * Return excess puzzle pieces to adjacent inventories, or the player if non-null. If a null player is passed, any
     * pieces which can't be inserted will be dropped on the ground.
     *
     * @param player the player, may be null
     * @param count the number of puzzle pieces to return
     */
    private void returnPuzzlePieces(@Nullable PlayerEntity player, int count) {
        ItemStack stack = new ItemStack(ModItems.PROGRAMMING_PUZZLE.get());

        // try to insert puzzle pieces into adjacent inventory(s)
        for (Direction d : DirectionUtil.VALUES) {
            TileEntity te = getCachedNeighbor(d);
            if (te != null) {
                while (count > 0) {
                    int toInsert = Math.min(count, stack.getMaxStackSize());
                    int inserted = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite()).map(h -> {
                        ItemStack excess = ItemHandlerHelper.insertItem(h, ItemHandlerHelper.copyStackWithSize(stack, toInsert), false);
                        return toInsert - excess.getCount();
                    }).orElse(0);
                    if (inserted == 0) break;
                    count -= inserted;
                }
            }
            if (count <= 0) return;
        }
        // give any remaining to player directly, or drop on ground
        while (count > 0) {
            int size = Math.min(count, stack.getMaxStackSize());
            if (player != null) {
                ItemHandlerHelper.giveItemToPlayer(player, ItemHandlerHelper.copyStackWithSize(stack, size));
            } else {
                PneumaticCraftUtils.dropItemOnGround(ItemHandlerHelper.copyStackWithSize(stack, size), getWorld(), getPos());
            }
            count -= size;
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
            List<IProgWidget> inDrone = getProgWidgets(stackInSlot);
            int dronePieces = (int) inDrone.stream().filter(p -> !p.freeToUse()).count();
            int required = (int) progWidgets.stream().filter(p -> !p.freeToUse()).count();
            return required - dronePieces;
        } else {
            return 0;
        }
    }

    public static List<IProgWidget> getProgWidgets(ItemStack iStack) {
        if (NBTUtils.hasTag(iStack, IProgrammable.NBT_WIDGETS)) {
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
    private boolean takePuzzlePieces(@Nullable PlayerEntity player, final boolean simulate) {
        int required = getRequiredPuzzleCount();
        if (required <= 0) return true;

        int found = 0;

        // look in player's inventory
        if (player != null) {
            found += extractPuzzlePieces(new PlayerMainInvWrapper(player.inventory), required, simulate);
            if (found >= required) return true;
        }

        // look in adjacent inventories
        for (Direction d : DirectionUtil.VALUES) {
            TileEntity te = getCachedNeighbor(d);
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
            if (stackInSlot.getItem() == ModItems.PROGRAMMING_PUZZLE.get()) {
                ItemStack extracted = handler.extractItem(i, Math.min(max - n, stackInSlot.getMaxStackSize()), simulate);
                n += extracted.getCount();
                if (n >= max) return n;
            }
        }
        return n;
    }

    public Set<String> getAllVariables() {
        Set<String> variables = new HashSet<>();
        for (IProgWidget widget : progWidgets) {
            if (widget instanceof IVariableWidget) {
                ((IVariableWidget) widget).addVariables(variables);
            }
        }
        variables.remove("");
        return variables;
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote && (world.getGameTime() & 0xf) == 0 && countPlayersUsing() > 0) {
            int total = 0;
            for (Direction dir : DirectionUtil.VALUES) {
                TileEntity te = getCachedNeighbor(dir);
                if (te != null) {
                    total += IOHelper.countItems(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()),
                            stack -> stack.getItem() == ModItems.PROGRAMMING_PUZZLE.get());
                }
            }
            availablePuzzlePieces = total;
        }
    }

    public void previewArea(IProgWidget progWidget) {
        if (progWidget == null) {
            AreaRenderManager.getInstance().removeHandlers(this);
        } else if (progWidget instanceof IAreaProvider) {
            Set<BlockPos> area = new HashSet<>();
            ((IAreaProvider) progWidget).getArea(area);
            AreaRenderManager.getInstance().showArea(area, 0x9000FF00, this);
        }
    }

    private void saveToHistory() {
        CompoundNBT tag = new CompoundNBT();
        writeProgWidgetsToNBT(tag);
        if (history.size() == 0 || !history.getCompound(historyIndex).equals(tag)) {
            while (history.size() > historyIndex + 1) {
                history.remove(historyIndex + 1);
            }
            history.add(tag);
            if (history.size() > 20) history.remove(0); // Only save up to 20 steps back.
            historyIndex = history.size() - 1;
            updateUndoRedoState();
        }
    }

    private void undo() {
        if (canUndo) {
            historyIndex--;
            readProgWidgetsFromNBT(history.getCompound(historyIndex));
            updateUndoRedoState();
            syncToClient(null);
        }
    }

    private void redo() {
        if (canRedo) {
            historyIndex++;
            readProgWidgetsFromNBT(history.getCompound(historyIndex));
            updateUndoRedoState();
            syncToClient(null);
        }
    }

    private void updateUndoRedoState() {
        canUndo = historyIndex > 0;
        canRedo = historyIndex < history.size() - 1;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerProgrammer(i, playerInventory, getPos());
    }

    /**
     * Replace the prog widget list when an update packet is received or an import is done.
     *
     * @param widgets the new widget list
     * @param player player who just made this change, may be null (used for syncing - ignored clientside)
     */
    public void setProgWidgets(List<IProgWidget> widgets, PlayerEntity player) {
        progWidgets.clear();
        progWidgets.addAll(widgets);
        updatePuzzleConnections(progWidgets);
        if (!world.isRemote) {
            saveToHistory();
            syncToClient(player);
        }
    }

    /**
     * Send a sync packet to all interested clients, which may or may not include the player who caused the sync
     * (if the sync is due to a change they made, they don't need to be synced).
     *
     * @param updatingPlayer the player doing the updating; if non-null, sync packet will not be sent to this player
     */
    private void syncToClient(PlayerEntity updatingPlayer) {
        if (!getWorld().isRemote) {
            List<ServerPlayerEntity> players = world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(5));
            for (ServerPlayerEntity player : players) {
                if (player != updatingPlayer && player.openContainer instanceof ContainerProgrammer) {
                    NetworkHandler.sendToPlayer(new PacketProgrammerUpdate(this), player);
                }
            }
        }
    }

    private class ProgrammerItemHandler extends BaseItemStackHandler {
        ProgrammerItemHandler() {
            super(TileEntityProgrammer.this, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (programOnInsert && slot == PROGRAM_SLOT && !getStackInSlot(slot).isEmpty() && !te.getWorld().isRemote) {
                tryProgramDrone(null);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.getItem() instanceof IProgrammable && ((IProgrammable) itemStack.getItem()).canProgram(itemStack);
        }
    }

    // yep, this is basically Rectangle2d, but that's client only, so...
    private static class PuzzleExtents {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        PuzzleExtents(int xIn, int yIn, int widthIn, int heightIn) {
            this.x = xIn;
            this.y = yIn;
            this.width = widthIn;
            this.height = heightIn;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean contains(int x, int y) {
            return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
        }
    }
}
