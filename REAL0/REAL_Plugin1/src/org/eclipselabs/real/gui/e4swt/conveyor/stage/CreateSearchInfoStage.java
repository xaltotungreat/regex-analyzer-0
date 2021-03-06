package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.gui.core.util.SearchInfo;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class CreateSearchInfoStage extends ConveyorStageBase {

    public CreateSearchInfoStage() {
        super(StageID.CREATE_SEARCH_INFO);
    }

    @Override
    public CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        // get the ID from the current milliseconds
        // but it is worth considering a transition to UUID
        String searchID = (req.getOldSearchID() != null)?req.getOldSearchID():String.valueOf(System.currentTimeMillis());
        SearchInfo info;
        LocalDateTime searchTime = LocalDateTime.now();
        info = new SearchInfo(searchTime);
        info.setSearchID(searchID);
        info.setParamsFromSO(req.getDso().getSearchObject());

        // set the results
        params.setSearchID(searchID);
        params.setSearchInfo(info);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
