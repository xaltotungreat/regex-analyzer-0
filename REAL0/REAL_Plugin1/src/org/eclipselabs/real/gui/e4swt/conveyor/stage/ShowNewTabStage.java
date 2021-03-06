package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamValueType;
import org.eclipselabs.real.core.util.IRealCoreConstants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class ShowNewTabStage extends ConveyorStageBase {

    public ShowNewTabStage() {
        super(StageID.NEW_TAB);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final StringBuilder tabTooltip = new StringBuilder();
        final StringBuilder tabText = new StringBuilder();
     // set the title for the tab
        if (req.getTabTitle() == null) {
            tabText.append(req.getDso().getDisplayName());
        } else {
            tabText.append(req.getTabTitle());
        }

        // set the tooltip
        if ((params.getCurrentParamMap() == null) || (params.getCurrentParamMap().isEmpty())) {
            tabTooltip.append("Group=" + req.getDso().getSearchObject().getSearchObjectGroup());
        } else {
            Iterator<Map.Entry<ReplaceableParamKey, IReplaceableParam<?>>> mapIter = params.getCurrentParamMap().entrySet().iterator();
            while((tabTooltip.length() < 40) && (mapIter.hasNext())) {
                Map.Entry<ReplaceableParamKey, IReplaceableParam<?>> currParam = mapIter.next();
                String value = currParam.getValue().getValue().toString();
                if (ReplaceableParamValueType.DATE.equals(currParam.getValue().getType())) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(IRealCoreConstants.DEFAULT_FORMAT_DATE_LONG, IRealCoreConstants.DEFAULT_DATE_LOCALE);
                    value = fmt.format(((IReplaceableParam<LocalDateTime>)currParam.getValue()).getValue());
                }
                tabTooltip.append(currParam.getKey().getRPName() + "=" + value + ",");
            }
            tabTooltip.deleteCharAt(tabTooltip.length() - 1);
            if ((tabTooltip.length() < 20) && (req.getTabTitle() == null)) {
                tabText.append(" " + tabTooltip.toString());
            }
        }

        req.getUiSynch().syncExec(new Runnable() {

            @Override
            public void run() {
                ((GUISearchResult)params.getSearchPart().getObject()).beginNewViewResult(params.getSearchID(),
                        tabText.toString(), tabTooltip.toString());
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
