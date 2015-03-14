package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.gui.core.util.SearchInfo;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConveyorMain;
import org.eclipselabs.real.gui.e4swt.dialogs.DialogResult;
import org.eclipselabs.real.gui.e4swt.dialogs.SOParamsDialog;

public class ParamsDialogStage extends ConveyorStageBase {

    private static final Logger log = LogManager.getLogger(ParamsDialogStage.class);


    public ParamsDialogStage() {
        super(StageID.PARAMS_DIALOG);
    }

    @Override
    public CompletableFuture<Void> executeInternal(ConvSearchRequest req, final ConvProductContext params) {
        SearchObjectKey soKey = new SearchObjectKey(params.getSearchInfo().getSearchObjectName(),
                params.getSearchInfo().getSearchObjectGroup(), params.getSearchInfo().getSearchObjectTags());
        Optional<SearchInfo> prevInfoOpt = ConveyorMain.INSTANCE.getOperationsCache().getLastExecutionParams(soKey);
        final Map<ReplaceParamKey, IReplaceParam<?>> prevParamMap = new HashMap<>();
        if ((prevInfoOpt.isPresent()) && (prevInfoOpt.get().getCustomReplaceTable() != null)) {
            prevParamMap.putAll(prevInfoOpt.get().getCustomReplaceTable());
        }
        final IKeyedSearchObject<?,?> currSO = req.getDso().getSearchObject();
        final List<IReplaceParam<?>> allParams = currSO.getCloneParamList();

        if ((allParams != null) && (!allParams.isEmpty()) && (req.getPreparedParams() == null)) {
            final DialogResult<Map<ReplaceParamKey, IReplaceParam<?>>> res = new DialogResult<Map<ReplaceParamKey, IReplaceParam<?>>>();
            req.getUiSynch().syncExec(new Runnable() {

                @Override
                public void run() {
                    IEclipseContext newCtxt = req.getHandlerContext().createChild();
                    newCtxt.set("SearchObjectName", currSO.getSearchObjectName());
                    SOParamsDialog paramsDialog = ContextInjectionFactory.make(SOParamsDialog.class, newCtxt);
                    String[] headers = new String[] {"Name","Value", "Hint"};
                    paramsDialog.setColumnValueIndex(1);
                    paramsDialog.setTableHeader(headers);
                    // if previous searches exist for this SO load the latest param map
                    paramsDialog.setOldValues(prevParamMap);
                    paramsDialog.setTableValues(allParams);
                    DialogResult<Map<ReplaceParamKey, IReplaceParam<?>>> intRes = paramsDialog.open();
                    res.setAction(intRes.getAction());
                    res.setResult(intRes.getResult());
                }
            });
            if (res.getAction() == SWT.OK) {
                params.setCurrentParamMap(res.getResult());
                // put to the history table if the search is confirmed
                ConveyorMain.INSTANCE.getOperationsCache().addExecutionParams(soKey, params.getSearchInfo());
                params.getSearchInfo().setCustomReplaceTable(new HashMap<ReplaceParamKey, IReplaceParam<?>>(res.getResult()));
            } else {
                log.info("Search canceled name=" + currSO.getSearchObjectName());
                params.setProceed(false);
                params.setAbortMessage("Search canceled name=" + currSO.getSearchObjectName() + " id=" + params.getSearchID());
            }
        } else if (req.getPreparedParams() != null) {
            params.setCurrentParamMap(req.getPreparedParams());
            // put to the history table if the search is confirmed
            ConveyorMain.INSTANCE.getOperationsCache().addExecutionParams(soKey, params.getSearchInfo());
            params.getSearchInfo().setCustomReplaceTable(new HashMap<ReplaceParamKey, IReplaceParam<?>>(req.getPreparedParams()));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
