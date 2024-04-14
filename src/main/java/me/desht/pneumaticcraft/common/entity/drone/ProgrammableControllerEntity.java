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

package me.desht.pneumaticcraft.common.entity.drone;

import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammableControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Special client-only entity used for rendering the programmable controller's "minidrone".
 */
public class ProgrammableControllerEntity extends AbstractDroneEntity {
    private ProgrammableControllerBlockEntity controller;
    private float propSpeed = 0f;

    public ProgrammableControllerEntity(EntityType<ProgrammableControllerEntity> type, Level world) {
        super(type, world);

        this.blocksBuilding = false;
    }

    public void setController(ProgrammableControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        if (level().isClientSide && controller != null) {
            BlockEntity te = level().getBlockEntity(controller.getBlockPos());
            if (te != controller) {
                // expire stale minidrones
                discard();
            } else {
                if (controller.isIdle) {
                    propSpeed = Math.max(0, propSpeed - 0.04F);
                } else {
                    propSpeed = Math.min(1, propSpeed + 0.04F);
                }
                oldPropRotation = propRotation;
                propRotation += propSpeed;
            }
        }
    }

    @Override
    public double getLaserOffsetY() {
        return 0.45;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public BlockPos getDugBlock() {
        return controller == null ? null : controller.getDugPosition();
    }

    @Override
    public ItemStack getDroneHeldItem() {
        return controller == null ? ItemStack.EMPTY : controller.heldItem;
    }

    @Override
    public BlockPos getTargetedBlock() {
        return controller.getTargetPos();
    }

    @Override
    public Component getOwnerName() {
        return Component.literal(controller.ownerNameClient);
    }

    @Override
    public String getLabel() {
        return controller.label == null ? "<?>" : controller.label;
    }

    @Override
    public boolean isTeleportRangeLimited() {
        return true;  // not very relevant since the PC minidrone doesn't need to teleport anyway
    }

    public BlockPos getControllerPos() {
        return controller.getBlockPos();
    }

    public ProgrammableControllerBlockEntity getController() {
        return controller;
    }
}
