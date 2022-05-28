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

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.KeybindingButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum PneumaticHelmetRegistry implements IPneumaticHelmetRegistry {
    INSTANCE;

    public static PneumaticHelmetRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Supplier<? extends IEntityTrackEntry> entry) {
        EntityTrackHandler.getInstance().register(entry);
    }

    @Override
    public void registerBlockTrackEntry(Supplier<? extends IBlockTrackEntry> entry) {
        BlockTrackHandler.getInstance().register(entry);
    }

    @Override
    public void addHUDMessage(Component title, List<Component> message, int duration, int backColor) {
        HUDHandler.getInstance().addMessage(title, message, duration, backColor);
    }

    @Override
    public void addHackable(Class<? extends Entity> entityClazz, Supplier<? extends IHackableEntity> iHackable) {
        CommonArmorRegistry.getInstance().addHackable(entityClazz, iHackable);
    }

    @Override
    public void addHackable(Block block, Supplier<? extends IHackableBlock> iHackable) {
        CommonArmorRegistry.getInstance().addHackable(block, iHackable);
    }

    @Override
    public void addHackable(TagKey<Block> blockTag, Supplier<? extends IHackableBlock> iHackable) {
        CommonArmorRegistry.getInstance().addHackable(blockTag, iHackable);
    }

    @Override
    public List<IHackableEntity> getCurrentEntityHacks(Entity entity) {
        return CommonArmorRegistry.getInstance().getCurrentEntityHacks(entity);
    }

    @Override
    public void registerUpgradeHandler(IArmorUpgradeHandler<?> handler) {
        CommonArmorRegistry.getInstance().registerUpgradeHandler(handler);
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

}
