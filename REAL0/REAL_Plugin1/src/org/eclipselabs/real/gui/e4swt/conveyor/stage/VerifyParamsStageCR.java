package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.LogFileController;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class VerifyParamsStageCR extends ConveyorStageBase {

    private static final Logger log = LogManager.getLogger(VerifyParamsStageCR.class);

    public VerifyParamsStageCR() {
        super(StageID.VERIFY_PARAMS);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final IKeyedSearchObject<?,?> currSO = req.getDso().getSearchObject();
        boolean proceed = params.isProceed();

        if (req.isSearchInCurrent()) {
            IKeyedComplexSearchObject<?, ?, ?, ?, ?, String> complexSo = (IKeyedComplexSearchObject<?, ?, ?, ?, ?, String>)currSO;
            // verify views exists for a search in current
            if (complexSo.getViewCount() == 0) {
                log.error("Search in current selected but the SO contains no views " + complexSo.getSearchObjectName());
                proceed = false;
                params.setAbortMessage("Search in current selected bu the SO contains no views " + complexSo.getSearchObjectName());
            }
        } else {
            // verify the aggregate is available if this is a new search
            if (LogFileController.INSTANCE.getLogAggregate(currSO.getLogFileType()) == null) {
                log.error("No log aggregate found " + currSO.getLogFileType().getLogTypeName());
                proceed = false;
                params.setAbortMessage("No log aggregate found " + currSO.getLogFileType().getLogTypeName());
            } else if (LogFileController.INSTANCE.getLogAggregate(currSO.getLogFileType()).isEmpty()) {
                log.error("Log aggregate contains no files " + currSO.getLogFileType().getLogTypeName());
                proceed = false;
                params.setAbortMessage("Log aggregate contains no files " + currSO.getLogFileType().getLogTypeName());
            }
        }
        params.setProceed(proceed);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
