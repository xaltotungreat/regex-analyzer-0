package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamValueType;
import org.eclipselabs.real.core.util.IRealCoreConstants;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOConstants;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class NewPartStage extends ConveyorStageBase {


    public NewPartStage() {
        super(StageID.NEW_PART);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(final ConvSearchRequest req, ConvProductContext params) {
        final MPart newSR = (MPart)req.getModelService().cloneSnippet(req.getApplication(), IEclipse4Constants.APP_MODEL_SEARCH_RESULT_SNIPPET, null);
        newSR.setElementId("SR" + params.getSearchID());
        if (req.getTabTitle() == null) {
            newSR.setLabel(req.getDso().getSearchObject().getSearchObjectName());
        } else {
            newSR.setLabel(req.getTabTitle());
        }
        String iconPath = req.getIconURI();
        if (iconPath == null) {
            iconPath = req.getDso().getIconPath(IDisplaySOConstants.ICON_PROPERTY_PART);
            if (iconPath == null) {
                iconPath = IEclipse4Constants.APP_MODEL_ICON_SEARCH;
            } else {
                iconPath = IEclipse4Constants.APP_MODEL_ICON_PATH_PREFIX + iconPath;
            }
        }

        newSR.setIconURI(iconPath);
        final StringBuilder tabTooltip = new StringBuilder();
        if ((params.getCurrentParamMap() == null) || (params.getCurrentParamMap().isEmpty())) {
            tabTooltip.append("Group=" + req.getDso().getSearchObject().getSearchObjectGroup());
        } else {
            for (Map.Entry<ReplaceParamKey, IReplaceParam<?>> currParam : params.getCurrentParamMap().entrySet()) {
                String value = currParam.getValue().getValue().toString();
                if (ReplaceParamValueType.DATE.equals(currParam.getValue().getType())) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(IRealCoreConstants.DEFAULT_FORMAT_DATE_LONG, IRealCoreConstants.DEFAULT_DATE_LOCALE);
                    value = fmt.format(((IReplaceParam<LocalDateTime>)currParam.getValue()).getValue());
                }
                tabTooltip.append(currParam.getKey().getRPName() + "=" + value + ",");
            }
            tabTooltip.deleteCharAt(tabTooltip.length() - 1);
        }
        newSR.setTooltip(tabTooltip.toString());
        params.setSearchPart(newSR);

        req.getUiSynch().syncExec(new Runnable() {

            @Override
            public void run() {
                req.getSearchTabsStack().getChildren().add(newSR);
                req.getPartService().showPart(newSR, PartState.ACTIVATE);
                /*((GUISearchResult)newSR.getObject()).beginNewViewResult(params.getSearchID(),
                        req.getDso().getSearchObject().getSearchObjectName(), tabTooltip.toString());*/
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
