package me.desht.pneumaticcraft.api.client.assembly_machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.ItemStack;

public interface IAssemblyRenderOverriding {
    /**
     * This method will be called just before the IO Unit's held stack is being rendered. You can modify the passed
     * matrix stack here to rotate the model, for example. You don't need to call .push() or .pop() on the matrix.
     * You can also choose to do the whole rendering yourself; you'll need to return false then to indicate that
     * PneumaticCraft shouldn't render the item.
     *
     * @param matrixStack the matrix; only apply transformations to this, never directly via OpenGL
     * @param renderedStack itemStack that is being rendered
     * @return true if PneumaticCraft should render the item (after your changes), or false to cancel rendering.
     */
    boolean applyRenderChangeIOUnit(MatrixStack matrixStack, ItemStack renderedStack);

    /**
     * Same deal as with the {@link #applyRenderChangeIOUnit(MatrixStack, ItemStack)}, but now for the Assembly Platform.
     *
     * @param matrixStack the matrix; only apply transformations to this, never directly via OpenGL
     * @param renderedStack itemStack that is being rendered
     * @return true if PneumaticCraft should render the item (after your changes), or false to cancel rendering.
     */
    boolean applyRenderChangePlatform(MatrixStack matrixStack, ItemStack renderedStack);

    /**
     * Should return the distance the claw travels before it is gripped to the stack.
     * By default it's 0.0875F for items and 0.00625F for blocks, 0.09375 when the claw is completely closed.
     *
     * @param renderedStack
     * @return
     */
    float getIOUnitClawShift(ItemStack renderedStack);

    /**
     * Should return the distance the claw travels before it is gripped to the stack.
     * By default it's 0.0875F for items and 0.00625F for blocks, 0.09375 when the claw is completely closed.
     *
     * @param renderedStack
     * @return
     */
    float getPlatformClawShift(ItemStack renderedStack);

}
