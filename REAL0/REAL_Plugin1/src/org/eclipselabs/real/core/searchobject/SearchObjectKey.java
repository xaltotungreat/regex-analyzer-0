package org.eclipselabs.real.core.searchobject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class SearchObjectKey {

    protected String soName;
    protected ISearchObjectGroup<String> soGroup;
    protected Map<String,String> soTags = new ConcurrentHashMap<String, String>();

    public static class SOKNamePredicate implements Predicate<SearchObjectKey> {

        protected String testName;
        public SOKNamePredicate(String name) {
            testName = name;
        }
        @Override
        public boolean test(SearchObjectKey t) {
            boolean result = false;
            if ((testName != null) && (t != null)) {
                result = testName.equals(t.getSOName());
            } else if ((testName == null) && (t != null) && (t.getSOName() == null)) {
                result = true;
            }
            return result;
        }

    }

    public static class SOKGroupPredicate implements Predicate<SearchObjectKey> {

        protected ISearchObjectGroup<String> testGroup;
        public SOKGroupPredicate(ISearchObjectGroup<String> group) {
            testGroup = group;
        }
        @Override
        public boolean test(SearchObjectKey t) {
            boolean result = false;
            if ((testGroup != null) && (t != null)) {
                result = testGroup.equals(t.getSOGroup());
            } else if ((testGroup == null) && (t != null) && (t.getSOGroup() == null)) {
                result = true;
            }
            return result;
        }

    }

    public SearchObjectKey(String name) {
        soName = name;
    }

    public SearchObjectKey(String name, ISearchObjectGroup<String> group) {
        soName = name;
        soGroup = group;
    }

    public SearchObjectKey(String name, ISearchObjectGroup<String> group, Map<String, String> tags) {
        soName = name;
        soGroup = group;
        if (tags != null) {
            soTags.putAll(tags);
        }
    }

    public SearchObjectKey(SearchObjectKey otherKey) {
        if (otherKey != null) {
            soGroup = otherKey.getSOGroup();
            soName = otherKey.getSOName();
            if (otherKey.getSOTags() != null) {
                soTags.putAll(otherKey.getSOTags());
            }
        }
    }

    public ISearchObjectGroup<String> getSOGroup() {
        return soGroup;
    }
    public void setSOGroup(ISearchObjectGroup<String> soPath) {
        this.soGroup = soPath;
    }
    public String getSOName() {
        return soName;
    }
    public void setSOName(String soName) {
        this.soName = soName;
    }

    public Map<String, String> getSOTags() {
        return new HashMap<String,String>(soTags);
    }

    public void setSOTags(Map<String, String> soTags) {
        this.soTags.clear();
        this.soTags.putAll(soTags);
    }

    @Override
    public String toString() {
        return "SearchObjectKey [soName=" + soName + ", soGroup=" + soGroup + ", soTags=" + soTags + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((soGroup == null) ? 0 : soGroup.hashCode());
        result = prime * result + ((soName == null) ? 0 : soName.hashCode());
        result = prime * result + ((soTags == null) ? 0 : soTags.hashCode());
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
        SearchObjectKey other = (SearchObjectKey) obj;
        if (soGroup == null) {
            if (other.soGroup != null)
                return false;
        } else if (!soGroup.equals(other.soGroup))
            return false;
        if (soName == null) {
            if (other.soName != null)
                return false;
        } else if (!soName.equals(other.soName))
            return false;
        if (soTags == null) {
            if (other.soTags != null)
                return false;
        } else if (!soTags.equals(other.soTags))
            return false;
        return true;
    }

}
