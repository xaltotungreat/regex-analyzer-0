package org.eclipselabs.real.gui.e4swt.conveyor.stage.build;

import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.IConveyorStage;

public interface IStageTreeBuilder {

    Integer DETAIL_LEVEL_DEFAULT = 1;

    public Integer getDetailLevel();
    public IConveyorStage buildStageTree(ConvSearchRequest req);
}
