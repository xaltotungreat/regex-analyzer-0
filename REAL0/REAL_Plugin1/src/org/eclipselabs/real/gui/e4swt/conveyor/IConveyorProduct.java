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
 * In this analogy the product is not created but executed. The product usually consists
 * of several stages that may be executed sequentially or in parallel.
 *
 * @author Vadim Korkin
 *
 */
public interface IConveyorProduct {

    /**
     * This method begins execution of this product
     * @param req the request for this product
     * @return the context i.e. a collection of parameters and values accumulated
     *     during product execution
     */
    public ConvProductContext executeRequest(ConvSearchRequest req);
}
