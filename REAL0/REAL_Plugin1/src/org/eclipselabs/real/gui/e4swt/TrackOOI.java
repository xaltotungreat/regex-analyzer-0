package org.eclipselabs.real.gui.e4swt;

import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipselabs.real.gui.e4swt.parts.GUIOOI;

public class TrackOOI extends RunAndTrack {

    protected String globalObjectsPartId;
    
    public TrackOOI(String globObjPartID) {
        globalObjectsPartId = globObjPartID;
    }

    @Override
    public boolean changed(IEclipseContext context) {
        MWindow mainWindow = context.get(MWindow.class);
        EModelService modelService = context.get(EModelService.class);
        MPart ooiPart = (MPart)modelService.find(globalObjectsPartId, mainWindow);
        if (ooiPart != null) {
            GUIOOI guiObj = (GUIOOI)ooiPart.getObject();
            Boolean globObjChanged = (Boolean)context.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED);
            if ((globObjChanged != null) && (globObjChanged)) {
                if (guiObj != null) {
                    guiObj.updateGlobalOOI((Map<String, GlobalOOIInfo>)mainWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST));
                }
                context.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, false);
            }
            
            Boolean localObjChanged = (Boolean)context.get(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST_CHANGED);
            if ((localObjChanged != null) && (localObjChanged)) {
                if (guiObj != null) {
                    guiObj.updateLocalOOI((Map<String, OOIInfo>)mainWindow.getContext().get(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST));
                }
                context.set(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST_CHANGED, false);
            }
            
            Boolean bookmarksObjChanged = (Boolean)context.get(IEclipse4Constants.CONTEXT_LOCAL_BOOKMARKS_LIST_CHANGED);
            if ((bookmarksObjChanged != null) && (bookmarksObjChanged)) {
                if (guiObj != null) {
                    guiObj.updateLocalBookmaks((List<NamedBookmark>)mainWindow.getContext().get(IEclipse4Constants.CONTEXT_LOCAL_BOOKMARKS_LIST));
                }
                context.set(IEclipse4Constants.CONTEXT_LOCAL_BOOKMARKS_LIST_CHANGED, false);
            }
        }
        return true;
    }

}
