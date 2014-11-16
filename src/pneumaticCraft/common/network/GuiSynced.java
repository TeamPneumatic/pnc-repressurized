package pneumaticCraft.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Fields marked with this annotation in a TileEntity class will be automatically synced to players with the container open (provided that the 
 * container extends ContainerPneumaticBase).
 * Supported field types are int, float, double, boolean, String, int[], float[], double[], boolean[] and String[].
 */
public @interface GuiSynced{

}
