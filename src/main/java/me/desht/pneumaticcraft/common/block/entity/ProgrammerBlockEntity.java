/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.inventory.ProgrammerMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ProgrammerBlockEntity extends AbstractTickingBlockEntity implements IGUITextFieldSensitive, MenuProvider {
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
    @DescSynced
    public ItemStack displayedStack = ItemStack.EMPTY;

    private ListTag history = new ListTag(); //Used to undo/redo.
    private int historyIndex;

    public ProgrammerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROGRAMMER.get(), pos, state);

        saveToHistory();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        displayedStack = inventory.getStackInSlot(0);
        programOnInsert = tag.getBoolean("ProgramOnInsert");
        history = tag.getList("history", 10);
        if (history.size() == 0) saveToHistory();
        readProgWidgetsFromNBT(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.put("history", history);
        tag.putBoolean("ProgramOnInsert", programOnInsert);
        writeProgWidgetsToNBT(tag);
    }

    public List<IProgWidget> mergeWidgetsFromNBT(CompoundTag tag) {
        List<IProgWidget> mergedWidgets = WidgetSerializer.getWidgetsFromNBT(tag);
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
                text.string = "Merge #" + nonNullLevel().getGameTime();
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

    public void readProgWidgetsFromNBT(CompoundTag tag) {
        progWidgets.clear();
        progWidgets.addAll(WidgetSerializer.getWidgetsFromNBT(tag));
        updatePuzzleConnections(progWidgets);
    }

    public CompoundTag writeProgWidgetsToNBT(CompoundTag tag) {
        WidgetSerializer.putWidgetsToNBT(progWidgets, tag);
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        switch (tag) {
            case "program_when" -> {
                programOnInsert = !programOnInsert;
                setChanged();
            }
            case "import" -> tryImport(shiftHeld);
            case "export" -> tryProgramDrone(player);
            case "undo" -> undo();
            case "redo" -> redo();
        }
        sendDescriptionPacket();
    }

    @Nonnull
    public ItemStack getItemInProgrammingSlot() {
        return inventory.getStackInSlot(PROGRAM_SLOT);
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap(Direction side) {
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
            stack.setHoverName(Component.literal(text));
            inventory.setStackInSlot(PROGRAM_SLOT, stack);
        }
    }

    @Override
    public String getText(int textFieldID) {
        return inventory.getStackInSlot(PROGRAM_SLOT).getHoverName().getString();
    }

    private void tryImport(boolean merge) {
        ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
        CompoundTag nbt = stack.isEmpty() ? null : stack.getTag();
        if (nbt != null) {
            List<IProgWidget> widgets = merge ? mergeWidgetsFromNBT(nbt) : WidgetSerializer.getWidgetsFromNBT(nbt);
            setProgWidgets(widgets, null);
        } else {
            if (!merge) setProgWidgets(Collections.emptyList(), null);
        }
    }

    public void tryProgramDrone(Player player) {
        if (inventory.getStackInSlot(PROGRAM_SLOT).getItem() instanceof IProgrammable) {
            if (player == null || !player.isCreative()) {
                int required = getRequiredPuzzleCount();
                if (required > 0) {
                    if (!takePuzzlePieces(player, true)) {
                        if (player instanceof ServerPlayer) {
                            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundSource.BLOCKS, getBlockPos(), 1.0f, 1.5f, false), (ServerPlayer) player);
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
            if (player instanceof ServerPlayer) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.HUD_INIT_COMPLETE.get(), SoundSource.BLOCKS, getBlockPos(), 1.0f, 1.0f, false), (ServerPlayer) player);
                AdvancementTriggers.PROGRAM_DRONE.trigger((ServerPlayer) player);
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
    private void returnPuzzlePieces(@Nullable Player player, int count) {
        ItemStack stack = new ItemStack(ModItems.PROGRAMMING_PUZZLE.get());

        // try to insert puzzle pieces into adjacent inventory(s)
        for (Direction d : DirectionUtil.VALUES) {
            BlockEntity te = getCachedNeighbor(d);
            if (te != null) {
                while (count > 0) {
                    int toInsert = Math.min(count, stack.getMaxStackSize());
                    int inserted = te.getCapability(ForgeCapabilities.ITEM_HANDLER, d.getOpposite()).map(h -> {
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
                PneumaticCraftUtils.dropItemOnGround(ItemHandlerHelper.copyStackWithSize(stack, size), getLevel(), getBlockPos());
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
            return (required - dronePieces) * stackInSlot.getCount();
        } else {
            return 0;
        }
    }

    public static List<IProgWidget> getProgWidgets(ItemStack iStack) {
        if (NBTUtils.hasTag(iStack, IProgrammable.NBT_WIDGETS)) {
            return WidgetSerializer.getWidgetsFromNBT(Objects.requireNonNull(iStack.getTag()));
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
    private boolean takePuzzlePieces(@Nullable Player player, final boolean simulate) {
        int required = getRequiredPuzzleCount();
        if (required <= 0) return true;

        int found = 0;

        // look in player's inventory
        if (player != null) {
            found += extractPuzzlePieces(new PlayerMainInvWrapper(player.getInventory()), required, simulate);
            if (found >= required) return true;
        }

        // look in adjacent inventories
        for (Direction d : DirectionUtil.VALUES) {
            BlockEntity te = getCachedNeighbor(d);
            if (te != null) {
                final int r = required - found;
                found += te.getCapability(ForgeCapabilities.ITEM_HANDLER, d.getOpposite())
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
    public void tickServer() {
        super.tickServer();

        if ((nonNullLevel().getGameTime() & 0xf) == 0 && countPlayersUsing() > 0) {
            int total = 0;
            for (Direction dir : DirectionUtil.VALUES) {
                BlockEntity te = getCachedNeighbor(dir);
                if (te != null) {
                    total += IOHelper.countItems(te.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()),
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
        CompoundTag tag = new CompoundTag();
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
        setChanged();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ProgrammerMenu(i, playerInventory, getBlockPos());
    }

    /**
     * Replace the prog widget list when an update packet is received or an import is done.
     *
     * @param widgets the new widget list
     * @param player player who just made this change, may be null (used for syncing - ignored clientside)
     */
    public void setProgWidgets(List<IProgWidget> widgets, Player player) {
        progWidgets.clear();
        progWidgets.addAll(widgets);
        updatePuzzleConnections(progWidgets);
        if (!nonNullLevel().isClientSide) {
            setChanged();
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
    private void syncToClient(Player updatingPlayer) {
        if (!nonNullLevel().isClientSide) {
            List<ServerPlayer> players = nonNullLevel().getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(5));
            for (ServerPlayer player : players) {
                if (player != updatingPlayer && player.containerMenu instanceof ProgrammerMenu) {
                    NetworkHandler.sendToPlayer(new PacketProgrammerUpdate(this), player);
                }
            }
        }
    }

    private class ProgrammerItemHandler extends BaseItemStackHandler {
        ProgrammerItemHandler() {
            super(ProgrammerBlockEntity.this, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (programOnInsert && slot == PROGRAM_SLOT && !getStackInSlot(slot).isEmpty() && !te.getLevel().isClientSide) {
                tryProgramDrone(null);
            }
            displayedStack = getStackInSlot(slot);
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
