package org.eclipselabs.real.gui.e4swt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NamedBookmark {

    protected UUID id;
    protected String bookmarkName;
    protected Integer startPos;
    protected Integer endPos;
    protected List<String> stringKeys = new ArrayList<>();
    
    public NamedBookmark(UUID uID, String name, Integer start, Integer end) {
        id = uID;
        bookmarkName = name;
        startPos = start;
        endPos = end;
    }
    
    public NamedBookmark(String name, Integer start, Integer end) {
        id = UUID.randomUUID();
        bookmarkName = name;
        startPos = start;
        endPos = end;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        NamedBookmark other = (NamedBookmark) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public Integer getStartPos() {
        return startPos;
    }

    public void setStartPos(Integer startPos) {
        this.startPos = startPos;
    }

    public Integer getEndPos() {
        return endPos;
    }

    public void setEndPos(Integer endPos) {
        this.endPos = endPos;
    }

    public List<String> getStringKeys() {
        return stringKeys;
    }

    public void setStringKeys(List<String> stringKeys) {
        this.stringKeys = stringKeys;
    }

    @Override
    public String toString() {
        return "NamedBookmark [bookmarkName=" + bookmarkName + ", startPos=" + startPos + ", endPos=" + endPos + ", stringKeys=" + stringKeys + "]";
    }

}
