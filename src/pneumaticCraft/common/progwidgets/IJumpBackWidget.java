package pneumaticCraft.common.progwidgets;

/**
 * Implemented by widgets that have custom program flow behaviour when a program reaches the end.
 * It is put onto a stack when executed normally, and then popped off when reaching the end of a program to call it again.
 */
public interface IJumpBackWidget{

}
