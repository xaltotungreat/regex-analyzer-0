package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;


public abstract class ConveyorStageBase implements IConveyorStage {

    private static final Logger log = LogManager.getLogger(ConveyorStageBase.class);

    protected StageID stageID;

    protected boolean executeAsync;

    protected Predicate<IConveyorStage> completionPredicate;

    protected CompletableFuture<Void> executedStage = null;

    protected IConveyorStage parentStage;

    protected volatile List<IConveyorStage> children = new ArrayList<IConveyorStage>();

    protected volatile boolean canceled = false;

    public ConveyorStageBase(StageID id) {
        stageID = id;
    }

    public ConveyorStageBase(StageID id, Predicate<IConveyorStage> pred) {
        this(id);
        completionPredicate = pred;
    }

    @Override
    public synchronized CompletableFuture<Void> execute(ConvSearchRequest req, ConvProductContext params) {
        CompletableFuture<Void> result;
        if (params.isProceed()) {
            if (executedStage == null) {
                CompletableFuture<Void> currStage = executeInternal(req, params);
                // set complete exactly after this stage executeInternal
                if (completionPredicate != null) {
                    currStage = currStage.thenAccept((a) -> {
                        if (completionPredicate.test(this)) {
                            params.setComplete(true);
                        }
                    });
                }
                List<CompletableFuture<Void>> allFs = new ArrayList<CompletableFuture<Void>>();
                allFs.add(currStage);
                if (children.size() == 1) {
                    IConveyorStage childStage = children.get(0);
                    CompletableFuture<Void> childFuture;
                    if (childStage.isExecuteAsync()) {
                        childFuture = currStage.thenComposeAsync((a) -> childStage.execute(req, params), params.getAsyncExecutorService());
                    } else {
                        childFuture = currStage.thenCompose((a) -> childStage.execute(req, params));
                    }
                    allFs.add(childFuture);
                } else if (children.size() > 1) {
                    for (IConveyorStage childStage : children) {
                        CompletableFuture<Void> childFuture = currStage.thenComposeAsync((a) -> childStage.execute(req, params), params.getAsyncExecutorService());
                        allFs.add(childFuture);
                    }
                }
                executedStage = CompletableFuture.allOf(allFs.toArray(new CompletableFuture[0]));
            }
            result = executedStage;
        } else {
            result = CompletableFuture.completedFuture(null);
            log.warn("Search aborted " + params.getAbortMessage());
        }
        return result;
    }

    protected abstract CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params);


    @Override
    public synchronized void cancel(boolean mayInterrupt) {
        if (executedStage != null) {
            if (!executedStage.isDone()) {
                log.debug("cancel EXECUTED");
                executedStage.cancel(true);
                cancelInternal(mayInterrupt);
            }
            for (IConveyorStage childStage : children) {
                childStage.cancel(mayInterrupt);
            }
        }
    }

    public abstract void cancelInternal(boolean mayInterrupt);

    @Override
    public IConveyorStage addChild(IConveyorStage st) {
        children.add(st);
        st.setParent(this);
        return st;
    }

    @Override
    public void setParent(IConveyorStage pt) {
        parentStage = pt;
    }

    @Override
    public boolean isExecuteAsync() {
        return executeAsync;
    }

    @Override
    public void setExecuteAsync(boolean executeAsync) {
        this.executeAsync = executeAsync;
    }

    public synchronized Predicate<IConveyorStage> getCompletionPredicate() {
        return completionPredicate;
    }

    public synchronized void setCompletionPredicate(Predicate<IConveyorStage> completionPredicate) {
        this.completionPredicate = completionPredicate;
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

}
