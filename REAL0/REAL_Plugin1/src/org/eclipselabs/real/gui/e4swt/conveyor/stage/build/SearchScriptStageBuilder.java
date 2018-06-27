package org.eclipselabs.real.gui.e4swt.conveyor.stage.build;

import java.util.function.Predicate;

import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.e4swt.Eclipse4GUIBridge;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.CreateSearchInfoStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.CreateSearchRequestInCurrentStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.CreateSearchRequestStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.GUIUpdater;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.IConveyorStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.InstallBookmarksStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.InstallOOIStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.NewPartStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.ParamsDialogStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.ProcessResultCurrentStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.ProcessResultStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.RootStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.SelectPartStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.SetPositionsStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.ShowNewTabStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.StartGUIUpdateStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.SubmitSearchCurrentStage;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.SubmitSearchStageSS;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class SearchScriptStageBuilder implements IStageTreeBuilder {

    @Override
    public IConveyorStage buildStageTree(ConvSearchRequest req) {
        IConveyorStage root = new RootStage();
        // first create the GUI status formatter
        GUIUpdater.IStatusFormatter formatter;
        if (req.isSearchInCurrent()) {
            formatter = new GUIUpdater.IStatusFormatter() {

                @Override
                public void updateStatus(GUISearchResult partSRObj, ISearchProgressMonitor searchMonitor, String searchID) {
                    partSRObj.setSearchStatus(searchID, "SearchObject Name: " + searchMonitor.getCurrentSOName()
                            + " Searched files: " + searchMonitor.getCompletedSOFiles() + "/" + searchMonitor.getTotalSOFiles()
                            + " Found objects: " + searchMonitor.getObjectsFound());
                }
            };

        } else {
            formatter = new GUIUpdater.IStatusFormatter() {

                @Override
                public void updateStatus(GUISearchResult partSRObj, ISearchProgressMonitor searchMonitor, String searchID) {
                    partSRObj.setSearchStatus(searchID, "SearchObject Name: " + searchMonitor.getCurrentSOName()
                            + " Searched files: " + searchMonitor.getCompletedSOFiles() + "/" + searchMonitor.getTotalSOFiles()
                            + " Found objects: " + searchMonitor.getObjectsFound());
                }
            };
        }
        IConveyorStage processRes;
        if (req.isSearchInCurrent()) {
            Predicate<IConveyorStage> complPred = (RequestInformationLevel.EXTENDED.equals(req.getInfoLevel()))?(null)
                    :(stage -> true);
            processRes = new ProcessResultCurrentStage(complPred);
        } else {
            Predicate<IConveyorStage> complPred = (RequestInformationLevel.EXTENDED.equals(req.getInfoLevel()))?(null)
                    :(stage -> true);
            processRes = new ProcessResultStage(req.isInstallGOSilently(), complPred);
        }
        IConveyorStage executionPoint = root.addChild(new CreateSearchInfoStage()).addChild(new ParamsDialogStage()).
                addChild((req.isSearchInCurrent()?(new SelectPartStage()):(new NewPartStage()))).
                addChild(new ShowNewTabStage()).
                addChild((req.isSearchInCurrent()?(new CreateSearchRequestInCurrentStage()):(new CreateSearchRequestStage())));
        IConveyorStage afterResultPoint = executionPoint.
                addChild((req.isSearchInCurrent()?(new SubmitSearchCurrentStage()):(new SubmitSearchStageSS()))).
                addChild(processRes);
        int guiUpdate = PerformanceUtils.getIntProperty(IEclipse4Constants.PERF_CONST_GUI_UPDATE_INTERVAL,
                IEclipse4Constants.PERF_CONST_GUI_UPDATE_INTERVAL_DEFAULT);
        executionPoint.addChild(new StartGUIUpdateStage(Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor(),
                        formatter, guiUpdate));

        if (RequestInformationLevel.EXTENDED.equals(req.getInfoLevel())) {
            afterResultPoint.addChild(new InstallOOIStage()).addChild(new InstallBookmarksStage()).
                addChild(new SetPositionsStage(stage -> true));
        }
        return root;
    }

    @Override
    public Integer getDetailLevel() {
        return DETAIL_LEVEL_DEFAULT;
    }

}
