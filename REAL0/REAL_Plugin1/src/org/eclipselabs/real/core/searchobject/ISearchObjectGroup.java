package org.eclipselabs.real.core.searchobject;

import java.util.List;

/**
 * This interface is just an abstraction of Path. A path consists
 * of a sequence of objects of the same class (not necessarily String)
 * The object of ISearchObjectGroup can return a String representation
 * of the group by getting toString() from every object in the path
 * The name path was quite overused so the name "ISearchObjectGroup" was used.
 * It is actually a group but groups can be nested therefore the group
 * is a sequence of group names (The names are not necessarily Strings)
 * 
 * @author Vadim Korkin
 *
 * @param <T> the type of group element
 */
public interface ISearchObjectGroup<T> extends Cloneable {

    /**
     * In order to get a String representation of path a delimiter
     * has to be used. This method returns the default delimiter.
     * @return the default delimiter
     */
    public String getDefaultDelimiter();
    
    /**
     * Sets the new default delimiter
     * @param newDelim the new delimiter for this object
     */
    public void setDefaultDelimiter(String newDelim);
    
    /**
     * Adds a new element to this group.
     * @param elem the element to add
     */
    public void addGroupElement(T elem);
    
    /**
     * Adds a new element to this group to the specified position.
     * @param elem the element to add
     */
    public void addGroupElement(int pos, T elem);
    
    /**
     * Adds new elements to this group.
     * @param elem the list of elements to add
     */
    public void addGroupElements(List<T> elem);
    
    /**
     * Adds new elements to this group to the specified position.
     * @param elem the list of elements to add
     */
    public void addGroupElements(int pos, List<T> elem);
    
    /**
     * Returns the element in the position pos
     * @param pos the position in the group
     * @return the element in the position pos
     */
    public T get(int pos);
    
    /**
     * Removes the last element from the group 
     * @return true if successful false otherwise
     */
    public boolean removeLastGroupElement();
    
    /**
     * Removes the element in the position elemNumber
     * from the group
     * @param elemNumber the position to remove
     * @return true if successful false otherwise
     */
    public boolean removeGroupElement(int elemNumber);
    
    /**
     * Returns the number of elements in the group. If the group
     * @return the number of elements in the group
     */
    public int getElementCount();
    
    /**
     * Returns an independent object that contains all the same group elements
     * as this group but only from position 0 till lastCount (inclusive)
     * @param lastCount the last element position to include
     * @return a new group with elements 0 - lastCount
     */
    public ISearchObjectGroup<T> getSubGroup(int lastCount);
    
    /**
     * Returns an independent object that contains all the same group elements
     * as this group but only from position firstCount till lastCount (inclusive)
     * @param firstCount the first element position to include
     * @param lastCount the last element position to include
     * @return a new group with elements firstCount - lastCount
     */
    public ISearchObjectGroup<T> getSubGroup(int firstCount, int lastCount);
    
    /**
     * Returns a list of independent objects in which all groups are present
     * from containing only the first element till containing all elements.
     * The sorting in the list is not guaranteed.
     * For example:
     * initial group: AAA.BBB.CCC
     * returns:
     * AAA
     * AAA.BBB
     * AAA.BBB.CCC
     * @return a list of groups containing from 1 till all elements
     */
    public List<ISearchObjectGroup<T>> getParentGroups();
    
    /**
     * Returns the reference to the group elements list
     * @return The reference to the group elements list
     */
    public List<T> getGroupElements();
    
    /**
     * Returns true if this group starts with the supplied value.
     * For example:
     * This Group:   AAA.BBB.CCC
     * smallerGroup: AAA.BBB
     * return: true
     * 
     * This Group:   AAA.BBB.CCC
     * smallerGroup: AAA.BBBQ
     * return: false
     * @param smallerGroup
     * @return true if this group starts with the supplied value
     */
    public boolean startsWith(ISearchObjectGroup<T> smallerGroup);
    
    /**
     * Returns the String representation of this group.
     * This is done usually (may be implementation-specific) by calling
     * toString() on the group elements. Because this String representation
     * may be used in regular expressions it is necessary to provide a method
     * to get the String representation with other than default delimiter
     * @param delim the delimiter for the group elements
     * @return the String representation of this group
     */
    public String getString(String delim);
    
    /**
     * Returns the String representation of this group.
     * This is done usually (may be implementation-specific) by calling
     * toString() on the group elements. The default delimiter is used.
     * @return the String representation of this group
     */
    public String getString();
    
    /**
     * Parses the passed string with the default delimiter
     * and sets the group elements to the received values.
     * For example:
     * strGroup: AAA.BBB.CCC
     * default delimiter: .
     * Resulting group elements: AAA BBB CCC
     * @param strGroup the string to parse
     * @return this group
     */
    public ISearchObjectGroup<T> parseAndSet(String strGroup);
    
    /**
     * Parses the passed string with the provided delimiter
     * and sets the group elements to the received values.
     * For example:
     * strGroup: AAA/BBB/CCC
     * delim: /
     * Resulting group elements: AAA BBB CCC
     * @param strGroup the string to parse
     * @param delim the delimiter to use for parsing
     * @return this group
     */
    public ISearchObjectGroup<T> parseAndSet(String strGroup, String delim);
    
    /**
     * Returns a reference to an independent copy of this object
     * @return a reference to an independent copy of this object
     * @throws CloneNotSupportedException this exception has been inherited from {@link}Object.clone()
     */
    public ISearchObjectGroup<T> clone() throws CloneNotSupportedException;
}
