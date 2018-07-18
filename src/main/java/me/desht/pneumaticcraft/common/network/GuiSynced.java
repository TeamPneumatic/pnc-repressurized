package me.desht.pneumaticcraft.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields marked with this annotation in a TileEntity class will be automatically synced to players with the container
 * open (provided that the container extends ContainerPneumaticBase).  Note that @LazySynced does not apply to these
 * fields.
 * <p>
 * Supported field types are int, float, double, boolean, String, int[], float[], double[], boolean[] and String[], as
 * well as ItemStack, FluidTank and ItemStackHandler and their corresponding arrays.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GuiSynced {

}
