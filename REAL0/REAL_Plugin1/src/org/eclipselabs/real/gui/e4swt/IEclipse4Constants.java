package org.eclipselabs.real.gui.e4swt;

public interface IEclipse4Constants {

    public static final String MAIN_WINDOW_TITLE = "Regular Expressions Analyzer for Logs";
    public static final String DEFINING_PLUGIN_NAME = "REAL_Plugin1";


    public static final String DATA_SEARCH_OBJECT_TREE_KEY = "Search Object Tree.SOData";
    public static final String DATA_TREE_ELEMENT_STATE_KEY = "Search Object Tree.TreeElementState";
    public static final String DATA_REQ_LOG_TYPES_SET = "Search Object Tree.RequiredLogTypes";
    public static final String DATA_GLOBAL_LOG_FILE_TYPE_STATE = "Search Object Tree.GlobalLogFileTypeState";

    public static final String DATA_GLOBAL_OOI_KEY = "GlobalOOI";
    public static final String DATA_GLOBAL_OOI_STRING_KEY = "GlobalOOI.StringKey";
    public static final String DATA_LOCAL_OOI_KEY = "LocalOOI";
    public static final String DATA_NAMED_BOOKMARK_KEY = "NamedBookmarkKey";
    public static final String DATA_NAMED_BOOKMARK_STRING_KEY = "NamedBookmarkKey.StringKey";

    public static final String CONTEXT_SEARCH_RESULT_TEXT_SELECTED = "Search Result.Selected Text";
    public static final String CONTEXT_SEARCH_RESULT_SELECTION_START = "Search Result.Selection Start";
    public static final String CONTEXT_SEARCH_RESULT_SELECTION_END = "Search Result.Selection End";
    public static final String CONTEXT_SEARCH_OBJECT_TREE_SELECTED = "Search Object Tree.Selected";
    public static final String CONTEXT_SEARCH_RESULT_ACTIVE_TAB = "Search Result.Active Tab";
    public static final String CONTEXT_SEARCH_RESULT_ACTIVE_VIEW_TAB = "Search Result.Active View Tab";
    public static final String CONTEXT_FIND_DIALOG = "FindDialog";
    public static final String CONTEXT_LOG_FILES_INFO_DIALOG = "LogFilesInfoDialog";
    public static final String CONTEXT_GLOBAL_SEARCH_OPERATIONS_CACHE = "GlobalSearchOperationsCahe";

    public static final String CONTEXT_GUIOOI_SELECTED_ITEM = "GUIOOI.Selected Item";
    public static final String CONTEXT_GLOBAL_OOI_LIST = "GlobalOOIList";
    public static final String CONTEXT_GLOBAL_OOI_LIST_CHANGED = "GlobalOOIListChanged";

    public static final String CONTEXT_GLOBAL_OOI_PENDING_LIST = "GlobalOOIPendingList";
    public static final String CONTEXT_GLOBAL_OOI_PENDING_LIST_CHANGED = "GlobalOOIPendingListChanged";

    public static final String CONTEXT_LOCAL_OOI_LIST = "LocalOOIList";
    public static final String CONTEXT_LOCAL_OOI_LIST_CHANGED = "LocalOOIListChanged";
    public static final String CONTEXT_LOCAL_BOOKMARKS_LIST = "LocalBookmarksList";
    public static final String CONTEXT_LOCAL_BOOKMARKS_LIST_CHANGED = "LocalBookmarksListChanged";

    public static final String LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY = "SearchID";

    public static final String STYLING_CLASS_SR_STYLED_TEXT = "SRStyledText";
    public static final String STYLING_CLASS_SR_STYLED_TABLE = "SRStyledTable";
    public static final String STYLING_CLASS_CTABFOLDER_LOAD_WS = "CTabFolderLoadWS";
    public static final String STYLING_CLASS_CTABITEM_LOAD_WS = "CTabItemLoadWS";
    public static final String STYLING_CLASS_CHECKBOX_WHITE = "CheckBoxWhite";

    public static final String APP_MODEL_PSTACK_SEARCH_RESULTS = "perspective0.sash.searchresults_stack";
    public static final String APP_MODEL_PART_SEARCH_OBJECTS_TREE = "perspective0.sash.searchtree_stack.sotree";
    public static final String APP_MODEL_SEARCH_RESULT_SNIPPET = "gui_search_result";
    public static final String APP_MODEL_POPUP_MENU_SEARCH_TREE = "perspective0.sash.searchtree_stack.sotree.popupmenu";
    public static final String APP_MODEL_POPUP_MENU_SEARCH_RESULT = "gui_search_result.text_popupmenu";

    public static final String APP_MODEL_PART_GLOBAL_OOI_TREE = "perspective0.sash.searchtree_stack.ooi";
    public static final String APP_MODEL_POPUP_MENU_GLOBAL_OOI_TREE = "perspective0.sash.searchtree_stack.global_objects.popupmenu";

    public static final String APP_MODEL_COMMAND_EXECUTE_SEARCH = "e4swt.command.execute_search";
    public static final String APP_MODEL_COMMAND_SEARCH_IN_CURRENT = "e4swt.command.search_in_current";
    public static final String APP_MODEL_COMMAND_INTERNAL_SEARCH_OLD = "e4swt.command.internal_search";
    public static final String APP_MODEL_COMMAND_INTERNAL_SEARCH = "e4swt.command.internal_search";
    public static final String APP_MODEL_COMMAND_SET_LOG_TYPE_STATE = "e4swt.command.set_logtype_state";
    public static final String APP_MODEL_COMMAND_ADD_STRING_KEY = "e4swt.command.add_string_key";
    public static final String APP_MODEL_COMMAND_REMOVE_STRING_KEY = "e4swt.command.remove_string_key";
    public static final String APP_MODEL_COMMAND_REMOVE_FROM_GLOBAL_OOI = "e4swt.command.remove_from_global_ooi";
    public static final String APP_MODEL_COMMAND_REMOVE_ALL_GLOBAL_OOI = "e4swt.command.remove_all_global_ooi";
    public static final String APP_MODEL_COMMAND_REMOVE_FROM_LOCAL_OOI = "e4swt.command.remove_from_local_ooi";
    public static final String APP_MODEL_COMMAND_REMOVE_NAMED_BOOKMARK = "e4swt.command.remove_named_bookmark";
    public static final String APP_MODEL_COMMAND_FIND_IN_CURRENT = "e4swt.command.find_in_current";
    public static final String APP_MODEL_COMMAND_SELECT_IN_CURRENT = "e4swt.command.select_in_current";

    public static final String APP_MODEL_TOOLBAR_HMI_LOG_TYPE_STATE_ID = "toolbar0.active_log_types";
    public static final String APP_MODEL_POPUP_MENU_VIEW = "toolbar0.active_log_types";
    public static final String APP_MODEL_WINDOW_ABOUT = "e4swt.aboutwindow";
    public static final String APP_MODEL_SASH_MAIN = "perspective0.sash";

    public static final String APP_MODEL_COMMAND_PARAM_LOG_TYPE_NAME = "e4swt.command.set_logtype_state.type_name";
    public static final String APP_MODEL_COMMAND_PARAM_LOG_TYPE_NEW_STATE = "e4swt.command.set_logtype_state.new_state";

    public static final String APP_MODEL_COMMAND_PARAM_FIND_TEXT = "e4swt.command.find_in_current.param.text";
    public static final String APP_MODEL_COMMAND_PARAM_FIND_DIRECTION = "e4swt.command.find_in_current.param.direction";
    public static final String APP_MODEL_COMMAND_PARAM_FIND_WRAP = "e4swt.command.find_in_current.param.wrap";
    public static final String APP_MODEL_COMMAND_PARAM_FIND_IS_REGEX = "e4swt.command.find_in_current.param.is_regex";

    public static final String APP_MODEL_COMMAND_PARAM_INTSEARCH_REQ_KEY = "e4swt.command.internal_search.param.reqkey";

    public static final String APP_MODEL_COMMAND_PARAM_CURRSEARCH_DSO_KEY = "e4swt.command.search_in_current.param.dsokey";
    public static final String APP_MODEL_COMMAND_PARAM_CURRSEARCH_PARAMS_KEY = "e4swt.command.search_in_current.param.paramskey";

    public static final String APP_MODEL_COMMAND_PARAM_BOOKMARK_START = "e4swt.command.select_in_current.param.start";
    public static final String APP_MODEL_COMMAND_PARAM_BOOKMARK_END = "e4swt.command.select_in_current.param.end";
    public static final String APP_MODEL_COMMAND_PARAM_SEL_BOOKMARK_UUID = "e4swt.command.remove_named_bookmark.param.sel_bookmark_uuid";

    public static final String APP_MODEL_ICON_PATH_PREFIX = "platform:/plugin/" + DEFINING_PLUGIN_NAME + "/";
    public static final String APP_MODEL_ICON_SEARCH = "platform:/plugin/" + DEFINING_PLUGIN_NAME + "/icons/24x24/mail-find.png";
    public static final String APP_MODEL_ICON_SEARCH_IN_CURRENT = "platform:/plugin/" + DEFINING_PLUGIN_NAME + "/icons/24x24/edit-find-10.png";
    public static final String APP_MODEL_ICON_INTERVAL = "platform:/plugin/" + DEFINING_PLUGIN_NAME + "/icons/24x24/Letter-I-lg-icon24x24.png";
    public static final String APP_MODEL_IMAGE_ABOUTSTARS = "images/AboutStars.bmp";

    public static final String PATH_TREE_ICON_COMPLEX_REGEX = "icons/16x16/CR1.png";
    public static final String PATH_TREE_ICON_INTERVAL = "icons/16x16/I1.png";
    public static final String PATH_TREE_ICON_EVENT_CHAIN = "icons/16x16/EC1.png";
    public static final String PATH_TREE_ICON_SEARCH_SCRIPT = "icons/16x16/SCR1.png";
    public static final String PATH_WORKSPACE_PERSIST_SCHEMA = "schemas/schema1.xsd";

    public static final String PACKAGE_WORKSPACE_PERSIST = "org.eclipselabs.real.gui.e4swt.persist";

    public static final Integer HANDLER_OPERATIONS_CACHE_SIZE = 5;

    public static final String PERF_CONST_GUI_UPDATE_INTERVAL = "org.eclipselabs.real.gui.UpdateIntervalms";
    public static final int PERF_CONST_GUI_UPDATE_INTERVAL_DEFAULT = 1000;
}
