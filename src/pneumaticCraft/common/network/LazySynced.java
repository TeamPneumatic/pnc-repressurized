package pneumaticCraft.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields marked with this and also @DescSynced will be included in a desc packet. However, changes to this field won't cause a desc
 * packet to be sent.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LazySynced{

}
