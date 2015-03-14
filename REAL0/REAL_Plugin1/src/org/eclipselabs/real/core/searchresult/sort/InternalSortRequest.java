package org.eclipselabs.real.core.searchresult.sort;

public abstract class InternalSortRequest implements IInternalSortRequest {

    //private static final Logger log = LogManager.getLogger(InternalSortRequest.class);
    protected SortingType type;
    protected SortingApplicability sortApplicability;
    protected String name;
    
    public InternalSortRequest(SortingType sortType) {
        type = sortType;
    }
    
    public InternalSortRequest(SortingType sortType, String aName) {
        type = sortType;
        name = aName;
    }
    
    public InternalSortRequest(SortingType sortType, SortingApplicability appl, String aName) {
        type = sortType;
        sortApplicability = appl;
        name = aName;
    }
    
    protected boolean sortAvailable() {
        return (getSortApplicability() != null) && (getSortApplicability().getScope() >= SortingApplicability.ALL.getScope());
    }
    
    @Override
    public SortingType getType() {
        return type;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SortingApplicability getSortApplicability() {
        return sortApplicability;
    }

    @Override
    public void setSortApplicability(SortingApplicability sortApplicability) {
        this.sortApplicability = sortApplicability;
    }

    @Override
    public InternalSortRequest clone()  throws CloneNotSupportedException {
        return (InternalSortRequest)super.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        InternalSortRequest other = (InternalSortRequest) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InternalSortRequest [name=" + name + ", type=" + type + ", sortApplicability=" + sortApplicability + "]";
    }


}
