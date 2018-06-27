package org.eclipselabs.real.core.searchobject.crit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public abstract class AcceptanceCriterionImpl implements IAcceptanceCriterion {

    protected AcceptanceCriterionType type;
    protected String name;
    protected Set<AcceptanceCriterionStage> stages = new HashSet<>();
    protected Boolean accumulating = false;
    protected List<IAcceptanceGuess> guessList = Collections.synchronizedList(new ArrayList<IAcceptanceGuess>());

    public AcceptanceCriterionImpl(AcceptanceCriterionType aType, AcceptanceCriterionStage...st) {
        this(aType, null, st);
    }

    public AcceptanceCriterionImpl(AcceptanceCriterionType aType, String newName, AcceptanceCriterionStage...st) {
        type = aType;
        if (st != null) {
            Collections.addAll(stages, st);
        }
        name = newName;
    }

    @Override
    public List<AcceptanceGuessResult> getGuessResults(String logText, ISearchResult<? extends ISearchResultObject> sr) throws IncorrectPatternException {
        List<AcceptanceGuessResult> results = null;
        if (guessList != null) {
            init(sr);
            results = new ArrayList<>();
            for (IAcceptanceGuess currGuess : guessList) {
                AcceptanceGuessResult res = currGuess.getGuessResult(logText, sr);
                if (res != null) {
                    results.add(res);
                }
            }
        }
        return results;
    }

    @Override
    public AcceptanceCriterionType getType() {
        return type;
    }

    @Override
    public void setType(AcceptanceCriterionType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    @Override
    public Set<AcceptanceCriterionStage> getStages() {
        return stages;
    }

    @Override
    public void setStages(Set<AcceptanceCriterionStage> newStages) {
        stages = newStages;
    }

    @Override
    public boolean isAccumulating() {
        return accumulating;
    }

    @Override
    public List<IAcceptanceGuess> getGuessList() {
        return guessList;
    }

    @Override
    public void setGuessList(List<IAcceptanceGuess> newList) {
        guessList = newList;
    }

    @Override
    public void addGuess(IAcceptanceGuess newGuess) {
        if ((guessList != null) && (newGuess != null)) {
            guessList.add(newGuess);
            newGuess.setAcceptanceCriterion(this);
        }
    }

    @Override
    public void addGuesses(List<IAcceptanceGuess> newGuesses) {
        if ((guessList != null) && (newGuesses != null)) {
            for (IAcceptanceGuess ag : newGuesses) {
                ag.setAcceptanceCriterion(this);
                guessList.add(ag);
            }
        }
    }

    @Override
    public void updateCriterionReferences() {
        guessList.stream().forEach(g -> g.setAcceptanceCriterion(this));
    }

    @Override
    public IAcceptanceCriterion clone() throws CloneNotSupportedException {
        AcceptanceCriterionImpl cloneObj = (AcceptanceCriterionImpl)super.clone();
        if (guessList != null) {
            List<IAcceptanceGuess> clonedGuessList = Collections.synchronizedList(new ArrayList<IAcceptanceGuess>());
            for (IAcceptanceGuess ag : guessList) {
                IAcceptanceGuess clonedGuess = ag.clone();
                clonedGuess.setAcceptanceCriterion(cloneObj);
                clonedGuessList.add(clonedGuess);
            }
            cloneObj.setGuessList(clonedGuessList);
        }
        return cloneObj;
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
        AcceptanceCriterionImpl other = (AcceptanceCriterionImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
