package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

/**
 * The basic interface for the conveyor stage. The stages create a structure that
 * is similar to a chain. The root stage is the first link, in the execute method it first executes itself
 * then its children. The second stage does the same etc.
 * The stage may be executed either synchronously (in the current thread) or asynchronously (in an executor).
 * Every stage can set to be executed asynchronously.
 * The stage may be canceled but the exact mechanism is implementation-specific.
 *
 * @author Vadim Korkin
 *
 */
public interface IConveyorStage {

    /**
     * Returns true is this stage is to be executed asynchronously
     * @return true is this stage is to be executed asynchronously false otherwise
     */
    boolean isExecuteAsync();
    /**
     * Set true to execute this stage asynchronously false otherwise
     * @param executeAsync true if this stage is to be executed asynchronously false otherwise
     */
    void setExecuteAsync(boolean executeAsync);
    /**
     * The main method to execute the stage. In this method first this stage is executed then its children.
     * The returned future completes when this stage and all its children have completed execution.
     * @param req the search request
     * @param params the context of this operation
     * @return the future that completes when this stage and all its children have completed execution
     */
    CompletableFuture<Void> execute(ConvSearchRequest req, ConvProductContext params);
    /**
     * Interrupt and abort execution. This operation is applicable only to long-running stages when there is
     * some mechanism to cancel in the middle of execution.
     * @param mayInterrupt if it is possible to interrupt running tasks or have to cancel gracefully
     */
    void cancel(boolean mayInterrupt);

    /**
     * Add a new child to this stage
     * @param st the new child
     * @return the added stage (the same st with maybe some modifications)
     */
    IConveyorStage addChild(IConveyorStage st);
    /**
     * Sets the parent for this stage. The chain must be traversable in both directions.
     * @param pt the parent for this stage
     */
    void setParent(IConveyorStage pt);

}
