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

package me.desht.pneumaticcraft.common.block.entity.drone;

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IGUITextFieldSensitive;
import me.desht.pneumaticcraft.common.drone.ProgWidgetSerializer;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.inventory.ProgrammerMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.registry.*;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ProgrammerBlockEntity extends AbstractTickingBlockEntity implements IGUITextFieldSensitive, MenuProvider {
    private static final int PROGRAM_SLOT = 0;
    private static final int INVENTORY_SIZE = 1;
    private static final int MAX_HISTORY_SIZE = 20;

    public final List<IProgWidget> progWidgets = new ArrayList<>();

    private final ProgrammerItemHandler inventory = new ProgrammerItemHandler();

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

    private final List<Tag> history = new LinkedList<>(); // Used to undo/redo.
    private int historyIndex;

    public ProgrammerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PROGRAMMER.get(), pos, state);

        history.add(new ListTag());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inventory.deserializeNBT(provider, tag.getCompound("Items"));
        displayedStack = inventory.getStackInSlot(0);
        programOnInsert = tag.getBoolean("ProgramOnInsert");
        history.addAll(tag.getList("history", Tag.TAG_LIST));
        if (history.isEmpty()) {
            history.add(new ListTag());
        }
        readProgWidgetsFromNBT(tag.getList(IProgrammable.NBT_WIDGETS, Tag.TAG_COMPOUND), provider);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Items", inventory.serializeNBT(provider));
        tag.put("history", Util.make(new ListTag(), l -> l.addAll(history)));
        tag.putBoolean("ProgramOnInsert", programOnInsert);
        tag.put(IProgrammable.NBT_WIDGETS, ProgWidgetSerializer.putWidgetsToNBT(provider, progWidgets));
    }

    public List<IProgWidget> mergeWidgetsFromNBT(List<IProgWidget> mergedWidgets) {
        List<IProgWidget> result = new ArrayList<>(progWidgets);

        if (!progWidgets.isEmpty() && !mergedWidgets.isEmpty()) {
            // move merged widgets so that they definitely don't overlap any existing widgets
            PuzzleExtents extents1 = getPuzzleExtents(progWidgets);
            PuzzleExtents extents2 = getPuzzleExtents(mergedWidgets);
            for (IProgWidget w : mergedWidgets) {
                w.setX(w.getX() - extents2.x() + extents1.x() + extents1.width() + 10);
                w.setY(w.getY() - extents2.y() + extents1.y());
            }
        }

        mergedWidgets.forEach(w -> {
            if (w instanceof ProgWidgetStart) {
                // any start widget in the merged import is replaced with a label/text widget pair
                ProgWidgetLabel lab = new ProgWidgetLabel();
                lab.setX(w.getX());
                lab.setY(w.getY());
                result.add(lab);
                ProgWidgetText text = ProgWidgetText.withText("Merge #" + nonNullLevel().getGameTime());
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

    public void readProgWidgetsFromNBT(Tag tag, HolderLookup.Provider provider) {
        progWidgets.clear();
        progWidgets.addAll(ProgWidgetSerializer.getWidgetsFromNBT(provider, tag));
        updatePuzzleConnections(progWidgets);
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
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public void setText(int textFieldID, String text) {
        ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT).copy();
        if (textFieldID == 0 && !stack.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(text));
            inventory.setStackInSlot(PROGRAM_SLOT, stack);
        }
    }

    @Override
    public String getText(int textFieldID) {
        return inventory.getStackInSlot(PROGRAM_SLOT).getHoverName().getString();
    }

    private void tryImport(boolean merge) {
        ItemStack stack = inventory.getStackInSlot(PROGRAM_SLOT);
        SavedDroneProgram program = stack.get(ModDataComponents.SAVED_DRONE_PROGRAM);
//        CompoundTag nbt = stack.isEmpty() ? null : stack.getTag();
        if (program != null) {
            List<IProgWidget> widgets = merge ? mergeWidgetsFromNBT(program.buildProgram()) : program.buildProgram();
            setProgWidgets(widgets, null);
        } else if (!merge) {
            setProgWidgets(Collections.emptyList(), null);
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
            stack.set(ModDataComponents.SAVED_DRONE_PROGRAM, SavedDroneProgram.create(progWidgets));
            if (player instanceof ServerPlayer sp) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.HUD_INIT_COMPLETE.get(), SoundSource.BLOCKS, getBlockPos(), 1.0f, 1.0f, false), sp);
                ModCriterionTriggers.PROGRAM_DRONE.get().trigger(sp);
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
                    int inserted = IOHelper.getInventoryForBlock(te, d.getOpposite()).map(h -> {
                        ItemStack excess = ItemHandlerHelper.insertItem(h, stack.copyWithCount(toInsert), false);
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
                ItemHandlerHelper.giveItemToPlayer(player, stack.copyWithCount(size));
            } else {
                PneumaticCraftUtils.dropItemOnGround(stack.copyWithCount(size), getLevel(), getBlockPos());
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
        return iStack.getOrDefault(ModDataComponents.SAVED_DRONE_PROGRAM, SavedDroneProgram.EMPTY).buildProgram();
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
                found += IOHelper.getInventoryForBlock(te, d.getOpposite())
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
                    total += IOHelper.getInventoryForBlock(te, dir.getOpposite())
                            .map(handler -> IOHelper.countItems(handler, stack -> stack.getItem() == ModItems.PROGRAMMING_PUZZLE.get()))
                            .orElse(0);
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

    private void saveToHistory(HolderLookup.Provider provider) {
        Tag tag = ProgWidgetSerializer.putWidgetsToNBT(provider, progWidgets);
        if (history.isEmpty() || !history.get(historyIndex).equals(tag)) {
            while (history.size() > historyIndex + 1) {
                history.remove(historyIndex + 1);
            }
            history.add(tag);
            if (history.size() > MAX_HISTORY_SIZE) {
                history.removeFirst();
            }
            historyIndex = history.size() - 1;
            updateUndoRedoState();
        }
    }

    private void undo() {
        if (canUndo) {
            historyIndex--;
            readProgWidgetsFromNBT(history.get(historyIndex), nonNullLevel().registryAccess());
            updateUndoRedoState();
            syncToClient(null);
        }
    }

    private void redo() {
        if (canRedo) {
            historyIndex++;
            readProgWidgetsFromNBT(history.get(historyIndex), nonNullLevel().registryAccess());
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
            saveToHistory(nonNullLevel().registryAccess());
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
                    NetworkHandler.sendToPlayer(PacketProgrammerSync.forBlockEntity(this), player);
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
            return itemStack.getItem() instanceof IProgrammable p && p.canProgram(itemStack);
        }
    }

    private record PuzzleExtents(int x, int y, int width, int height) {
    }
}
