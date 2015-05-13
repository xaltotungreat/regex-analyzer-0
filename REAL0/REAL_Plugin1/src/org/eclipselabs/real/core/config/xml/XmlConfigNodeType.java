package org.eclipselabs.real.core.config.xml;

import org.w3c.dom.Node;

public enum XmlConfigNodeType {
    // regex config
    REGEX_CONFIG("regex_config"),
    GROUP("group"),
    REPLACE_PARAMS("replaceParams"),
    COMPLEX_REGEXES("complexRegexes"),
    SEARCH_SCRIPTS("searchScripts"),

    COMPLEX_REGEX("complexRegex"),
    SEARCH_SCRIPT("searchScript"),
    REGEXES("regexes"),
    KEYED_REGEXES("keyed_regexes"),
    MAIN_REGEXES("main_regexes"),
    SELECTION_REGEXES("selection_regexes"),
    SORT_REGEXES("sortRegexes"),
    DATE_INFO("date_info"),
    SCRIPT_TEXT("scriptText"),
    REGEX_FLAGS("regex_flags"),
    VIEWS("views"),
    TAGS("tags"),
    DESCRIPTION("description"),
    REQUIRED_LOG_TYPES("required_log_types"),
    VIEW("view"),
    VIEW_NAMES("viewNames"),
    VIEW_NAME("viewName"),
    TAG("tag"),
    REGEX_FLAG("regex_flag"),
    VIEW_STRING("viewString"),
    REGEX_GROUP("regexGroup"),
    REGEX_STR("regexStr"),
    REGEX("regex"),
    DATE_FORMAT("date_format"),
    LINKED_REGEX("linkedRegex"),
    KEYED_REGEX("keyed_regex"),
    REGEX_LINE("regexLine"),
    REGEX_PARAM("regexParam"),
    //replace param
    REPLACE_PARAM("replaceParam"),
    REPLACE_NAME("replaceName"),
    REPLACE_VALUE("replaceValue"),
    //sorting
    SORT_REQUESTS("sortRequests"),
    SORT_REQUEST_DATE_TIME("sortRequestDateTime"),
    SORT_REQUEST_REGEX("sortRequestRegex"),
    SORT_REQUEST_REGEX_COMPLEX("sortRequestRegexComplex"),
    //acceptance
    ACCEPTANCES("acceptances"),
    REGEX_ACCEPTANCE("regexAcceptance"),
    DT_INTERVAL_ACCEPTANCE("dtIntervalAcceptance"),
    DT_INTERVAL_GUESS("dtIntervalGuess"),
    // ref
    REF_STRING("refString"),
    REF_NAME("refName"),
    REF_GROUP("refGroup"),
    REF_TAGS("refTags"),
    REF_REPLACE_PARAMS("refReplaceParams"),
    REF_REPLACE_PARAM("refReplaceParam"),
    REF_REGEXES("refRegexes"),
    REF_MAIN_REGEXES("refMainRegexes"),
    REF_SELECTION_REGEXES("refSelection_regexes"),
    REF_REGEX("refRegex"),
    REF_SORT_REQUESTS("refSortRequests"),
    REF_SORT_REQUEST("refSortRequest"),
    REF_ACCEPTANCES("refAcceptances"),
    REF_ACCEPTANCE("refAcceptance"),
    REF_REGEX_FLAGS("refRegexFlags"),
    REF_DATE_INFO("refDateInfo"),
    REF_VIEWS("refViews"),
    REF_VIEW("refView"),
    // main ref types
    REF_KEYED_REGEX("refKeyed_regex"),
    REF_COMPLEX_REGEX("refComplexRegex"),
    REF_DISTINCT_COMPLEX_REGEX("refDistinctComplexRegex"),
    REF_INTERVAL("refInterval"),
    REF_SEARCH_SCRIPT("refSearchScript"),

    // gui config
    GUI_CONFIG("gui_config"),
    SEARCH_OBJECT_TREE("search_object_tree"),
    FOLDER("folder"),
    //Gui templates,
    TEMPLATE("template"),
    SELECTORS("selectors"),
    SELECTOR("selector"),
    NAME_REGEXES("name_regexes"),
    GROUP_REGEXES("group_regexes"),
    TAG_REGEXES("tag_regexes"),
    SORT_REQUEST_KEYS("sortRequestKeys"),
    TAG_REGEX("tag_regex"),
    TEXT_VIEW("text_view"),
    SHORT_VIEWS("short_views"),
    SHORT_VIEW("short_view"),
    SORT_REQUEST_KEY("sortRequestKey"),
    SORT_REQUEST_KEY_PARAM("sortRequestKeyParam"),
    ICON_SETS("iconSets"),
    ICON_SET("iconSet"),
    ICON_PATH("iconPath"),
    GUI_PROPERTIES("gui_properties"),
    GUI_PROPERTY("gui_property"),

    // log types config
    LOG_TYPES("log_types"),
    LOG_TYPE("log_type"),
    STATE("state"),
    FILENAME_PATTERNS("filename_patterns"),
    FILENAME_PATTERN("filename_pattern")
    ;

    private String nodeName;

    private XmlConfigNodeType(String aNodeName) {
        nodeName = aNodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Boolean equalsNode(Node elem) {
        return nodeName.equals(elem.getNodeName());
    }
}
