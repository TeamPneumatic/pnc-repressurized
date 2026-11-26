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

package me.desht.pneumaticcraft.common.inventory.handler;

import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.IntPredicate;

/**
 * Combines several handlers into one, with rules for which indices may be inserted into and extracted from; e.g.
 * input slots which can only be inserted into, and output slots which can only be extracted from.
 * <p>
 * Can also be used as an {@link IndexModifier}, e.g. for GUI slots, if the combined handlers are
 * {@link StacksResourceHandler}s.
 */
public class IOCombinedHandler<T extends Resource> extends CombinedResourceHandler<T> implements IndexModifier<T> {
    private final IntPredicate canInsert;
    private final IntPredicate canExtract;

    @SafeVarargs
    public IOCombinedHandler(IntPredicate canInsert, IntPredicate canExtract, ResourceHandler<T>... handlers) {
        super(handlers);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        return canInsert.test(index) ? super.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (int index = 0; index < size() && inserted < amount; index++) {
            inserted += insert(index, resource, amount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        return canExtract.test(index) ? super.extract(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        for (int index = 0; index < size() && extracted < amount; index++) {
            extracted += extract(index, resource, amount - extracted, transaction);
        }
        return extracted;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(int index, T resource, int amount) {
        int handlerIndex = getHandlerIndex(index);
        if (getHandlerFromIndex(handlerIndex) instanceof StacksResourceHandler<?, ?> stacks) {
            ((StacksResourceHandler<?, T>) stacks).set(getSlotFromIndex(index, handlerIndex), resource, amount);
        } else {
            throw new IllegalStateException("handler for index " + index + " can't be modified directly");
        }
    }
}
