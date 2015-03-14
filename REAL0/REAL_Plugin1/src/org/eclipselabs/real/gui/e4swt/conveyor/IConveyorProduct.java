package org.eclipselabs.real.gui.e4swt.conveyor;


/**
 * The basic interface for a conveyor product
 * The conveyor product is analogous to the real world conveyor product
 * As in the real world it is composed from different parts.
 * The main difference from the real world product is that the end result
 * is not one class that is returned.
 * Rather some stages create some parts of the end result (open a new tab, update the GUI)
 * The returned result is the context - the information about the product
 * that has been accumulated during execution
 * @author Vadim Korkin
 *
 */
public interface IConveyorProduct {

    public ConvProductContext executeRequest(ConvSearchRequest req);
}
