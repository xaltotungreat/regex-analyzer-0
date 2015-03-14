package org.eclipselabs.real.gui.e4swt.conveyor;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.IConveyorStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.build.IStageTreeBuilder;
import org.eclipselabs.real.gui.e4swt.event.SRPartDisposedEvent;
import org.eclipselabs.real.gui.e4swt.event.SRTabDisposedEvent;

import com.google.common.eventbus.Subscribe;

public class ConveyorProductImpl implements IConveyorProduct {

    private static final Logger log = LogManager.getLogger(ConveyorProductImpl.class);

    private volatile IConveyorStage rootStage;

    private volatile ConvProductContext context;

    private IStageTreeBuilder stageTreeBuilder;

    private volatile CompletableFuture<ConvProductContext> endFuture;

    private volatile boolean canceled = false;

    public ConveyorProductImpl(IStageTreeBuilder bld) {
        stageTreeBuilder = bld;
    }

    @Override
    public ConvProductContext executeRequest(ConvSearchRequest req) {
        CoreEventBus.INSTANCE.register(this);
        if (stageTreeBuilder == null) {
            return null;
        }
        rootStage = stageTreeBuilder.buildStageTree(req);//prepareStages(req);
        context = new ConvProductContext("Product");
        // unregister from the event bus after all the stages have completed
        CompletableFuture<Void> stagesFuture = rootStage.execute(req, context).whenComplete((a, ex) -> {
                // cleanup operations regardless of the result
                CoreEventBus.INSTANCE.unregister(this);
                context.cleanup();
            });
        endFuture = stagesFuture.thenApply((a) -> context);
        // clients may want to wait indefinitely
        if (req.getMaxWaitToComplete() == null) {
            try {
                return endFuture.get();
            } catch (InterruptedException e) {
                log.error("executeRequest",e);
            } catch (ExecutionException e) {
                if (canceled && (e.getCause() instanceof CancellationException)) {
                    // do not log if the exception should be here
                } else {
                    log.error("executeRequest",e);
                }
            }
        } else {
            try {
                return endFuture.get(req.getMaxWaitToComplete().getTimeout(), req.getMaxWaitToComplete().getTimeUnit());
            } catch (InterruptedException e) {
                log.error("executeRequest",e);
            } catch (ExecutionException e) {
                if (canceled && (e.getCause() instanceof CancellationException)) {
                    // do not log if the exception should be here
                } else {
                    log.error("executeRequest",e);
                }
            } catch (TimeoutException e) {
                // cancel all running futures
                cancelProduct();
                log.error("executeRequest product " + context.getSearchID() + " canceled due to timeout", e);
            }
        }
        return null;
    }

    private void cancelProduct() {
        context.setProceed(false);
        rootStage.cancel(true);
    }

    @Subscribe
    public void handlePartDispose(SRPartDisposedEvent event) {
        if ((context != null) && (event.getSearchID().equals(context.getSearchID()))) {
            log.info("handlePartDispose SR ID=" + event.getSearchID());
            canceled = true;
            cancelProduct();
            log.error("executeRequest product " + context.getSearchID() + " canceled due to GUI part already disposed");
        }
    }

    @Subscribe
    public void handleTabDispose(SRTabDisposedEvent event) {
        /* also check that the tab disposed is not for the main search
         if it is then don't cancel because for the main search only disposing the part
         cancels the search
         */
        if ((context != null) && (event.getSearchID().equals(context.getSearchID()))
                && !event.isTabForMainSearch()) {
            log.info("handleTabDispose SR ID=" + event.getSearchID());
            canceled = true;
            cancelProduct();
            log.error("executeRequest product " + context.getSearchID() + " canceled due to GUI part already disposed");
        }
    }

}
