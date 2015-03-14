package org.eclipselabs.real.gui.e4swt.parts;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;

public class DynMenuLogTypes {
    private static final Logger log = LogManager.getLogger(DynMenuLogTypes.class);

    @Inject
    EModelService modelService;

    @Inject
    MApplication application;

    @AboutToShow
    public void aboutToShow(List<MMenuElement> items) {
        List<LogFileType> typesList = LogFileTypes.INSTANCE.getAllTypes();
        Collections.sort(typesList, new Comparator<LogFileType>() {

            @Override
            public int compare(LogFileType o1, LogFileType o2) {
                if ((o1 != null) && (o2 != null)) {
                    return o1.getLogTypeName().compareTo(o2.getLogTypeName());
                } else if ((o1 != null) && (o2 == null)) {
                    return 1;
                } else if ((o1 == null) && (o2 != null)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        for (LogFileType lfType : typesList) {
            MHandledMenuItem hmi = modelService.createModelElement(MHandledMenuItem.class);
            hmi.setLabel(lfType.getLogTypeName());
            hmi.setType(ItemType.CHECK);
            hmi.setSelected(false);
            if (lfType.isEnabled()) {
                hmi.setSelected(true);
            }
            hmi.setElementId(IEclipse4Constants.APP_MODEL_TOOLBAR_HMI_LOG_TYPE_STATE_ID + "." + lfType.getLogTypeName());
            log.info("Creating LogTypes Menu");
            MCommand setLogTypeStateCommand = MCommandsFactory.INSTANCE.createCommand();
            setLogTypeStateCommand.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_SET_LOG_TYPE_STATE);
            setLogTypeStateCommand.setCommandName("Tmp Name");
            hmi.setCommand(setLogTypeStateCommand);

            MParameter lftName = MCommandsFactory.INSTANCE.createParameter();
            lftName.setName(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_LOG_TYPE_NAME);
            lftName.setValue(lfType.getLogTypeName());
            hmi.getParameters().add(lftName);

            /*MParameter lftState = MCommandsFactory.INSTANCE.createParameter();
            lftState.setName(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_LOG_TYPE_NEW_STATE);
            lftState.setValue(newStateStr);
            hmi.getParameters().add(lftState);*/

            items.add(hmi);
        }
    }

    @AboutToHide
    public void aboutToHide(List<MMenuElement> items) {
        // TODO Your code goes here
    }

}