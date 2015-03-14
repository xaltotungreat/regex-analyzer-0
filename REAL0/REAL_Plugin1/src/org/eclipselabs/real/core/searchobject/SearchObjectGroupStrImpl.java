package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SearchObjectGroupStrImpl implements ISearchObjectGroup<String> {

    private String defaultDelimiter = DEFAULT_DELIMITER;
    protected List<String> pathElements = new ArrayList<>();

    public SearchObjectGroupStrImpl() {
    }

    public SearchObjectGroupStrImpl(String rootElem) {
        parseAndSet(rootElem);
    }

    public SearchObjectGroupStrImpl(String rootElem, String delim) {
        parseAndSet(rootElem, delim);
    }

    @Override
    public String getDefaultDelimiter() {
        return defaultDelimiter;
    }

    @Override
    public void setDefaultDelimiter(String newDelim) {
        defaultDelimiter = newDelim;
    }

    @Override
    public void addGroupElement(String elem) {
        if (elem != null) {
            pathElements.add(elem);
        }
    }

    @Override
    public void addGroupElement(int pos, String elem) {
        if ((pos >= 0) && (pos < pathElements.size()) && (elem != null)) {
            pathElements.add(pos, elem);
        }
    }

    @Override
    public void addGroupElements(List<String> elem) {
        if (elem != null) {
            pathElements.addAll(elem);
        }
    }

    @Override
    public void addGroupElements(int pos, List<String> elem) {
        if ((pos >= 0) && (pos < pathElements.size()) && (elem != null)) {
            pathElements.addAll(pos, elem);
        }
    }

    @Override
    public boolean removeLastGroupElement() {
        boolean result = false;
        if (!pathElements.isEmpty()) {
            pathElements.remove(pathElements.size() - 1);
            result = true;
        }
        return result;
    }

    @Override
    public boolean removeGroupElement(int elemNumber) {
        boolean result = false;
        if ((elemNumber >= 0) && (pathElements.size() > elemNumber)) {
            pathElements.remove(elemNumber);
            result = true;
        }
        return result;
    }

    @Override
    public int getElementCount() {
        return pathElements.size();
    }

    @Override
    public List<ISearchObjectGroup<String>> getParentGroups() {
        List<ISearchObjectGroup<String>> result = new ArrayList<>();
        if (!pathElements.isEmpty()) {
            for (int i = 0; i < pathElements.size(); i++) {
                ISearchObjectGroup<String> newPath = getSubGroup(i);
                result.add(newPath);
            }
        }
        return result;
    }

    @Override
    public ISearchObjectGroup<String> getSubGroup(int lastCount) {
        return getSubGroup(0, lastCount);
    }

    @Override
    public ISearchObjectGroup<String> getSubGroup(int firstCount, int lastCount) {
        ISearchObjectGroup<String> result = new SearchObjectGroupStrImpl();
        if ((firstCount >= 0) && (firstCount < pathElements.size())
                && (lastCount >= 0) && (lastCount < pathElements.size())
                && (firstCount <= lastCount)) {
            for (int i = firstCount; i <= lastCount; i++) {
                result.addGroupElement(pathElements.get(i));
            }
        }
        return result;
    }

    @Override
    public List<String> getGroupElements() {
        return pathElements;
    }

    @Override
    public String getString(String delim) {
        StringBuilder sb = new StringBuilder();
        if ((!pathElements.isEmpty()) && (delim != null)) {
            sb.append(pathElements.get(0));
            for (int i = 1; i < pathElements.size(); i++) {
                sb.append(delim + pathElements.get(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String getString() {
        return getString(defaultDelimiter);
    }

    @Override
    public ISearchObjectGroup<String> parseAndSet(String strGroup) {
        return parseAndSet(strGroup, defaultDelimiter);
    }

    @Override
    public ISearchObjectGroup<String> parseAndSet(String strGroup, String delim) {
        if ((strGroup != null) && (delim != null)) {
            String[] tokens = strGroup.split(Pattern.quote(delim));
            if ((tokens != null) && (tokens.length > 0)) {
                pathElements.clear();
                for (String str : tokens) {
                    pathElements.add(str);
                }
            }
        }
        return this;
    }

    protected void setPathElements(List<String> newElemList) {
        pathElements = newElemList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pathElements == null) ? 0 : pathElements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchObjectGroupStrImpl other = (SearchObjectGroupStrImpl) obj;
        if (pathElements == null) {
            if (other.pathElements != null)
                return false;
        } else if (!pathElements.equals(other.pathElements))
            return false;
        return true;
    }

    @Override
    public ISearchObjectGroup<String> clone() throws CloneNotSupportedException {
        SearchObjectGroupStrImpl cloneObj = (SearchObjectGroupStrImpl)super.clone();
        List<String> pathElem = new ArrayList<>();
        if (cloneObj.pathElements != null) {
            pathElem.addAll(cloneObj.pathElements);
            cloneObj.setPathElements(pathElem);
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public boolean startsWith(ISearchObjectGroup<String> smallerGroup) {
        boolean result = true;
        if ((smallerGroup == null) || (smallerGroup.getElementCount() > getElementCount())) {
            result = false;
        } else {
            for (int i = 0; i < pathElements.size(); i++) {
                String currelem = pathElements.get(i);
                if (!currelem.equals(smallerGroup.get(i))) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String get(int pos) {
        String result = null;
        if ((pos >= 0) && (pos < pathElements.size())) {
            result = pathElements.get(pos);
        }
        return result;
    }


}
