package org.eclipselabs.real.gui.e4swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.ISimpleRegex;
import org.eclipselabs.real.core.regex.RegexFactory;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.ITypedObject;
import org.eclipselabs.real.core.util.TagRef;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;
import org.eclipselabs.real.gui.core.GUIConfigObjectType;
import org.eclipselabs.real.gui.core.sotree.DisplaySOImpl;
import org.eclipselabs.real.gui.core.sotree.DisplaySOSelector;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTemplateImpl;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTreeItemType;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTemplate;

public class E4SwtSearchObjectHelper implements ISearchObjectConstants {

    private static final Logger log = LogManager.getLogger(E4SwtSearchObjectHelper.class);
    
    private E4SwtSearchObjectHelper() {}
    
    public static IDisplaySO getAnyRecordForGroup(ISearchObjectGroup<String> groupName) {
        List<IRealRegex> groupReg = new ArrayList<IRealRegex>();
        ISimpleRegex regGroupName = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regGroupName.setRegexStr(groupName.getString());
        groupReg.add(regGroupName);
        List<TagRef> tagReg = new ArrayList<TagRef>();
        ISimpleRegex regNameRole = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regNameRole.setRegexStr(TAG_NAME_ROLE);
        ISimpleRegex regValueInternal = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValueInternal.setRegexStr(TAG_VALUE_INTERNAL);
        tagReg.add(new TagRef(regNameRole, regValueInternal));
        ISimpleRegex regNameFunction = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regNameFunction.setRegexStr(TAG_NAME_FUNCTION);
        ISimpleRegex regValueAnyRecord = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValueAnyRecord.setRegexStr(TAG_VALUE_ANY_RECORD);
        tagReg.add(new TagRef(regNameFunction, regValueAnyRecord));
        IDisplaySOTemplate ref = new DisplaySOTemplateImpl("No name", true);
        ref.addSelector(new DisplaySOSelector("No name", null, groupReg, tagReg));
        List<IDisplaySO> displaySOList = ref.getSearchTreeItems(
                SearchObjectController.INSTANCE.getSearchObjectRepository().getValues(new SearchObjectKey.SOKGroupPredicate(groupName)));
        if ((displaySOList != null) && (!displaySOList.isEmpty())) {
            IDisplaySO selectedDSO = displaySOList.get(0);
            selectedDSO.setViewNamePatterns(Collections.singletonList(".*"));
            return selectedDSO;
        } else {
            return null;
        }
    }
    
    public static IDisplaySO getAnyRecordForGroupUpstream(ISearchObjectGroup<String> groupName) {
        IDisplaySO result = null;
        if ((groupName != null) && (groupName.getElementCount() > 0)) {
            List<TagRef> tagReg = new ArrayList<TagRef>();
            ISimpleRegex regNameRole = RegexFactory.INSTANCE.getSimpleRegex("Name");
            regNameRole.setRegexStr(TAG_NAME_ROLE);
            ISimpleRegex regValueInternal = RegexFactory.INSTANCE.getSimpleRegex("Value");
            regValueInternal.setRegexStr(TAG_VALUE_INTERNAL);
            tagReg.add(new TagRef(regNameRole, regValueInternal));
            ISimpleRegex regNameFunction = RegexFactory.INSTANCE.getSimpleRegex("Name");
            regNameFunction.setRegexStr(TAG_NAME_FUNCTION);
            ISimpleRegex regValueAnyRecord = RegexFactory.INSTANCE.getSimpleRegex("Value");
            regValueAnyRecord.setRegexStr(TAG_VALUE_ANY_RECORD);
            tagReg.add(new TagRef(regNameFunction, regValueAnyRecord));
            result = getInternalDSOUpstream(null, Collections.singletonList(groupName), tagReg);
        }
        return result;
    }
    
    public static IDisplaySO getInternalDSOUpstream(ISearchObjectGroup<String> groupName, String tagNameRegex, String tagValueRegex) {
        List<TagRef> tagsList = new ArrayList<>();
        ISimpleRegex regNameRole = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regNameRole.setRegexStr(tagNameRegex);
        ISimpleRegex regValueInternal = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValueInternal.setRegexStr(tagValueRegex);
        tagsList.add(new TagRef(regNameRole, regValueInternal));
        return getInternalDSOUpstream(null, Collections.singletonList(groupName), tagsList);
    }
    
    public static IDisplaySO getInternalDSOUpstream(List<IRealRegex> nameRegexes, List<ISearchObjectGroup<String>> groups, List<TagRef> tags) {
        IDisplaySO result = null;
        if (groups != null) {
            List<IRealRegex> groupReg = new ArrayList<IRealRegex>();
            for (ISearchObjectGroup<String> currGroup : groups) {
                if (currGroup.getElementCount() > 0) {
                    for (int i = currGroup.getElementCount() - 1; i >= 0; i--) {
                        groupReg.clear();
                        ISearchObjectGroup<String> currSearchingGroup = currGroup.getSubGroup(i);
                        ISimpleRegex regGroupName = RegexFactory.INSTANCE.getSimpleRegex("Name");
                        regGroupName.setRegexStr(currSearchingGroup.getString("\\."));
                        groupReg.add(regGroupName);
                        IDisplaySOTemplate ref = new DisplaySOTemplateImpl("No name", true);
                        ref.addSelector(new DisplaySOSelector("No name", nameRegexes, groupReg, tags));
                        List<IDisplaySO> displaySOList = ref.getSearchTreeItems(
                                SearchObjectController.INSTANCE.getSearchObjectRepository().getValues(new SearchObjectKey.SOKGroupPredicate(currSearchingGroup)));
                        if ((displaySOList != null) && (!displaySOList.isEmpty())) {
                            IDisplaySO selectedDSO = displaySOList.get(0);
                            selectedDSO.setViewNamePatterns(Collections.singletonList(".*"));
                            result = selectedDSO;
                            break;
                        }
                    }
                }
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }
    
    public static IDisplaySO getInternalDSO(ISearchObjectGroup<String> groupName, String tagNameRegex, String tagValueRegex) {
        List<TagRef> tmpList = new ArrayList<>();
        if ((tagNameRegex != null) && (tagValueRegex != null)) {
            ISimpleRegex regName1 = RegexFactory.INSTANCE.getSimpleRegex("Name");
            regName1.setRegexStr(tagNameRegex);
            ISimpleRegex regValue1 = RegexFactory.INSTANCE.getSimpleRegex("Value");
            regValue1.setRegexStr(tagValueRegex);
            TagRef tmp = new TagRef(regName1, regValue1);
            tmpList.add(tmp);
        }
        return getInternalDSO(groupName, tmpList);
    }
    
    public static IDisplaySO getInternalDSO(ISearchObjectGroup<String> groupName, List<TagRef> tags) {
        List<IRealRegex> groupReg = new ArrayList<IRealRegex>();
        ISimpleRegex regGroupName = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regGroupName.setRegexStr(groupName.getString());
        groupReg.add(regGroupName);
        ISimpleRegex regNameRole = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regNameRole.setRegexStr(TAG_NAME_ROLE);
        ISimpleRegex regValueInternal = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValueInternal.setRegexStr(TAG_VALUE_INTERNAL);
        tags.add(new TagRef(regNameRole, regValueInternal));
        IDisplaySOTemplate ref = new DisplaySOTemplateImpl("No name", false);
        ref.addSelector(new DisplaySOSelector("No name", null, groupReg, tags));
        List<IDisplaySO> displaySOList = ref.getSearchTreeItems(
                SearchObjectController.INSTANCE.getSearchObjectRepository().getValues(new SearchObjectKey.SOKGroupPredicate(groupName)));
        if ((displaySOList != null) && (!displaySOList.isEmpty())) {
            IDisplaySO selectedDSO = displaySOList.get(0);
            selectedDSO.setViewNamePatterns(Collections.singletonList(".*"));
            return selectedDSO;
        } else {
            return null;
        }
    }
    
    public static IDisplaySO getInternalDSO(String nameRegex, ISearchObjectGroup<String> groupName, String tagNameRegex, String tagValueRegex) {
        List<IRealRegex> nameReg = new ArrayList<IRealRegex>();
        ISimpleRegex regName = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regName.setRegexStr(nameRegex);
        nameReg.add(regName);
        List<IRealRegex> groupReg = new ArrayList<IRealRegex>();
        ISimpleRegex regGroupName = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regGroupName.setRegexStr(groupName.getString());
        groupReg.add(regGroupName);
        List<TagRef> tagReg = new ArrayList<TagRef>();
        ISimpleRegex regNameRole = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regNameRole.setRegexStr(TAG_NAME_ROLE);
        ISimpleRegex regValueInternal = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValueInternal.setRegexStr(TAG_VALUE_INTERNAL);
        tagReg.add(new TagRef(regNameRole, regValueInternal));
        ISimpleRegex regName1 = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regName1.setRegexStr(tagNameRegex);
        ISimpleRegex regValue1 = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValue1.setRegexStr(tagValueRegex);
        tagReg.add(new TagRef(regName1, regValue1));
        IDisplaySOTemplate ref = new DisplaySOTemplateImpl("No name", false);
        ref.addSelector(new DisplaySOSelector("No name", nameReg, groupReg, tagReg));
        List<IDisplaySO> displaySOList = ref.getSearchTreeItems(
                SearchObjectController.INSTANCE.getSearchObjectRepository().getValues(new SearchObjectKey.SOKGroupPredicate(groupName)));
        if ((displaySOList != null) && (!displaySOList.isEmpty())) {
            IDisplaySO selectedDSO = displaySOList.get(0);
            selectedDSO.setViewNamePatterns(Collections.singletonList(".*"));
            return selectedDSO;
        } else {
            return null;
        }
    }
    
    public static IDisplaySO getInternalDSO(String nameRegex, ISearchObjectGroup<String> groupName, List<TagRef> tags) {
        List<IRealRegex> nameReg = new ArrayList<IRealRegex>();
        ISimpleRegex regName = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regName.setRegexStr(nameRegex);
        nameReg.add(regName);
        List<IRealRegex> groupReg = new ArrayList<IRealRegex>();
        ISimpleRegex regGroupName = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regGroupName.setRegexStr(groupName.getString());
        groupReg.add(regGroupName);
        ISimpleRegex regNameRole = RegexFactory.INSTANCE.getSimpleRegex("Name");
        regNameRole.setRegexStr(TAG_NAME_ROLE);
        ISimpleRegex regValueInternal = RegexFactory.INSTANCE.getSimpleRegex("Value");
        regValueInternal.setRegexStr(TAG_VALUE_INTERNAL);
        IDisplaySOTemplate ref = new DisplaySOTemplateImpl("No name", false);
        ref.addSelector(new DisplaySOSelector("No name", nameReg, groupReg, tags));
        List<IDisplaySO> displaySOList = ref.getSearchTreeItems(
                SearchObjectController.INSTANCE.getSearchObjectRepository().getValues(new SearchObjectKey.SOKGroupPredicate(groupName)));
        if ((displaySOList != null) && (!displaySOList.isEmpty())) {
            IDisplaySO selectedDSO = displaySOList.get(0);
            selectedDSO.setViewNamePatterns(Collections.singletonList(".*"));
            return selectedDSO;
        } else {
            return null;
        }
    }
    
    public static IDisplaySOTemplate getMatchingRef(IKeyedSearchObject<? extends ISearchResult<?>, ? extends ISearchResultObject> searchObject) {
        if (searchObject != null) {
            GUIConfigKey searchTreeKey = new GUIConfigKey(GUIConfigObjectType.SEARCH_OBJECT_TREE);
            DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)GUIConfigController.INSTANCE.getGUIObjectRepository().get(searchTreeKey);
            List<IDisplaySOTemplate> matchList = new ArrayList<>();
            getMatchingRef(matchList, treeRoot, searchObject);
            if (!matchList.isEmpty()) {
                return matchList.get(0);
            }
        }
        return null;
    }
    
    protected static void getMatchingRef(List<IDisplaySOTemplate> matchList, DefaultMutableTreeNode searchNode, 
            IKeyedSearchObject<? extends ISearchResult<?>, ? extends ISearchResultObject> searchObject) {
        ITypedObject<DisplaySOTreeItemType> userObj = (ITypedObject<DisplaySOTreeItemType>)searchNode.getUserObject();
        if (!DisplaySOTreeItemType.TEMPLATE.equals(userObj.getType())) {
            Enumeration chl = searchNode.children();
            while(chl.hasMoreElements()) {
                DefaultMutableTreeNode currChild = (DefaultMutableTreeNode)chl.nextElement();
                getMatchingRef(matchList, currChild, searchObject);
            }
        } else {
            IDisplaySOTemplate currRef = (IDisplaySOTemplate)searchNode.getUserObject();
            if (currRef.matchesSearchObject(searchObject)) {
                matchList.add(currRef);
            }
        }
    }
    
    public static IDisplaySO getDisplaySOForSO(IKeyedSearchObject<? extends ISearchResult<?>, ? extends ISearchResultObject> searchObject) {
        IDisplaySO res = null;
        if (searchObject != null) {
            if ((searchObject.getSearchObjectTags() == null) || 
                    ((searchObject.getSearchObjectTags() != null) && (!TAG_VALUE_USER_OBJECT.equals(searchObject.getSearchObjectTags().get(TAG_NAME_ROLE))))) {
                res = new DisplaySOImpl("Temp", searchObject, true);
                res.setDisplayName(searchObject.getSearchObjectName());
                res.setViewNamePatterns(Collections.singletonList(".*"));
            } else if (TAG_VALUE_USER_OBJECT.equals(searchObject.getSearchObjectTags().get(TAG_NAME_ROLE))) {
                GUIConfigKey searchTreeKey = new GUIConfigKey(GUIConfigObjectType.SEARCH_OBJECT_TREE);
                DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)GUIConfigController.INSTANCE.getGUIObjectRepository().get(searchTreeKey);
                List<IDisplaySO> matchList = new ArrayList<>();
                getDisplaySOFromTree(matchList, treeRoot, searchObject);
                if (!matchList.isEmpty()) {
                    res = matchList.get(0);
                }
            } else {
                log.warn("getDisplaySOForSO DSO not found");
            }
        }
        return res;
    }
    
    protected static void getDisplaySOFromTree(List<IDisplaySO> matchList, DefaultMutableTreeNode searchNode, 
            IKeyedSearchObject<? extends ISearchResult<?>, ? extends ISearchResultObject> searchObject) {
        ITypedObject<DisplaySOTreeItemType> userObj = (ITypedObject<DisplaySOTreeItemType>)searchNode.getUserObject();
        if (!DisplaySOTreeItemType.TEMPLATE.equals(userObj.getType())) {
            Enumeration chl = searchNode.children();
            while(chl.hasMoreElements()) {
                DefaultMutableTreeNode currChild = (DefaultMutableTreeNode)chl.nextElement();
                getDisplaySOFromTree(matchList, currChild, searchObject);
            }
        } else {
            IDisplaySOTemplate currRef = (IDisplaySOTemplate)searchNode.getUserObject();
            if (currRef.matchesSearchObject(searchObject)) {
                matchList.add(currRef.getDisplaySearchObject(searchObject));
            }
        }
    }
    
}
