package pneumaticCraft.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Fields marked with this annotation in a TileEntity class will be automatically synced to any players within 64 blocks of this TileEntity.
 * Supported field types are int, float, double, boolean, String, int[], float[], double[], boolean[] and String[].
 */
public @interface DescSynced{

}
