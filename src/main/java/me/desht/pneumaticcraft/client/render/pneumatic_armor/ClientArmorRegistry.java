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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorStatMoveScreen;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.KeybindingButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum ClientArmorRegistry implements IClientArmorRegistry {
    INSTANCE;

    public static ClientArmorRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Supplier<? extends IEntityTrackEntry> entry) {
        EntityTrackHandler.getInstance().register(entry);
    }

    @Override
    public void registerBlockTrackEntry(ResourceLocation id, Supplier<? extends IBlockTrackEntry> entry) {
        BlockTrackHandler.getInstance().register(id, entry);
    }

    @Override
    public void addHUDMessage(Component title, List<Component> message, int duration, int backColor) {
        HUDHandler.getInstance().addMessage(title, message, duration, backColor);
    }

    @Override
    public <T extends IArmorUpgradeHandler<?>> void registerRenderHandler(T handler, IArmorUpgradeClientHandler<T> clientHandler) {
        Validate.notNull(clientHandler, "Render handler can't be null!");
        ArmorUpgradeClientRegistry.getInstance().registerHandler(handler, clientHandler);
    }

    @Override
    public IKeybindingButton makeKeybindingButton(int yPos, KeyMapping keyBinding) {
        return new KeybindingButton(30, yPos, 150, 20, xlate("pneumaticcraft.armor.gui.misc.setKey"), keyBinding);
    }

    @Override
    public ICheckboxWidget makeKeybindingCheckBox(ResourceLocation upgradeId, int xPos, int yPos, int color, Consumer<ICheckboxWidget> onPressed) {
        return WidgetKeybindCheckBox.getOrCreate(upgradeId, xPos, yPos, color, onPressed);
    }

    @Override
    public IGuiAnimatedStat makeHUDStatPanel(Component title, ItemStack icon, IArmorUpgradeClientHandler<?> clientHandler) {
        StatPanelLayout layout = ArmorHUDLayout.INSTANCE.getLayoutFor(clientHandler.getID(), clientHandler.getDefaultStatLayout());
        return new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(icon), HUDHandler.getInstance().getStatOverlayColor(), null, layout);
    }

    @Override
    public IGuiAnimatedStat makeHUDStatPanel(Component title, ResourceLocation icon, IArmorUpgradeClientHandler<?> clientHandler) {
        StatPanelLayout layout = ArmorHUDLayout.INSTANCE.getLayoutFor(clientHandler.getID(), clientHandler.getDefaultStatLayout());
        return new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(icon), HUDHandler.getInstance().getStatOverlayColor(), null, layout);
    }

    @Override
    public AbstractWidget makeStatMoveButton(int x, int y, IArmorUpgradeClientHandler<?> handler) {
        return new WidgetButtonExtended(x, y, 150, 20, xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"),
                b -> Minecraft.getInstance().setScreen(new ArmorStatMoveScreen(handler))
        );
    }

    private static final Pair<BlockPos,Direction> NO_FOCUS = Pair.of(null, null);
    @Override
    public Pair<BlockPos, Direction> getBlockTrackerFocus() {
        if (!CommonArmorHandler.getHandlerForPlayer().upgradeUsable(CommonUpgradeHandlers.blockTrackerHandler, true)) {
            return NO_FOCUS;
        }
        BlockTrackerClientHandler handler = ArmorUpgradeClientRegistry.getInstance()
                .getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class);
        return Pair.of(handler.getFocusedPos(), handler.getFocusedFace());
    }
}
