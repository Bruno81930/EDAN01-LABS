import org.jacop.constraints.Not;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.XlteqC;
import org.jacop.constraints.XgteqC;
import org.jacop.constraints.XeqC;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

/**
 * Implements Split Search using strategy 1 .
 *
 */

public class SS  {

    boolean trace = false;

    /**
     * Store used in search
     */
    Store store;

    /**
     * Defines varibales to be printed when solution is found
     */
    IntVar[] variablesToReport;

    /**
     * It represents current depth of store used in search.
     */
    int depth = 0;

    int totalCount = 0;

    int wrongDecisions = 0;
    /**
     * It represents the cost value of currently best solution for FloatVar cost.
     */
    public int costValue = IntDomain.MaxInt;

    /**
     * It represents the cost variable.
     */
    public IntVar costVariable = null;

    public SS(Store s) {
	store = s;
    }


    /**
     * This function is called recursively to assign variables one by one.
     */
    public boolean label(IntVar[] vars) {

    	if (trace) {
    	    for (int i = 0; i < vars.length; i++)
    		System.out.print (vars[i] + " ");
    	    System.out.println ();
    	}

    	ChoicePoint choice = null;
      totalCount++;
    	boolean consistent;

    	// Instead of imposing constraint just restrict bounds
    	// -1 since costValue is the cost of last solution
    	if (costVariable != null) {
    	    try {
    		if (costVariable.min() <= costValue - 1)
    		    costVariable.domain.in(store.level, costVariable, costVariable.min(), costValue - 1);
    		else
    		    return false;
    	    } catch (FailException f) {
    		return false;
    	    }
    	}

    	consistent = store.consistency();

    	if (!consistent) {
    	    // Failed leaf of the search tree
    	    return false;
    	} else { // consistent

    	    if (vars.length == 0) {
        		// solution found; no more variables to label

        		// update cost if minimization
        		if (costVariable != null)
        		    costValue = costVariable.min();

        		reportSolution();

        		return costVariable == null; // true is satisfiability search and false if minimization
        	}

   	    choice = new ChoicePoint(vars);

  	    levelUp();

  	    store.impose(choice.getConstraint());

  	    // choice point imposed.

  	    consistent = label(choice.getSearchVariables());

        if (consistent) {
      		levelDown();
      		return true;
  	    } else {

      		restoreLevel();

      		store.impose(new Not(choice.getConstraint()));

      		// negated choice point imposed.

      		consistent = label(vars);

      		levelDown();

      		if (consistent) {
      		    return true;
      		} else {
      		    return false;
      		}
	       }
	      }
      }

    void levelDown() {
    	store.removeLevel(depth);
    	store.setLevel(--depth);
    }

    void levelUp() {
    	store.setLevel(++depth);
    }

    void restoreLevel() {
    	store.removeLevel(depth);
    	store.setLevel(store.level);
      wrongDecisions++;
    }

    public void reportSolution() {
	if (costVariable != null)
	    System.out.println ("Cost is " + costVariable);

	for (int i = 0; i < variablesToReport.length; i++)
	    System.out.print (variablesToReport[i] + " ");
	System.out.println ("\n---------------");
    }

    public void reportTotalNode() {
      System.out.println("Total # Nodes is " + totalCount);
    }

    public void reportWrongDecisions() {
      System.out.println("Wrong Decisions is " + wrongDecisions);
    }

    public void setVariablesToReport(IntVar[] v) {
	variablesToReport = v;
    }

    public void setCostVariable(IntVar v) {
	costVariable = v;
    }

    public class ChoicePoint {

    	IntVar var;
    	IntVar[] searchVariables;
    	int value;
      int option = 0;

    	public ChoicePoint (IntVar[] v, String type, int strategy) {
          option = strategy;
    	    var = selectVariable(v, type);
    	    value = selectValue(var);

    	}

      public ChoicePoint (IntVar[] v, int strategy) {
          option = strategy;
    	    var = selectVariable(v);
    	    value = selectValue(var);
    	}

      public ChoicePoint (IntVar[] v, String type) {
    	    var = selectVariable(v, type);
    	    value = selectValue(var);
    	}

      public ChoicePoint(IntVar[] v) {
        var = selectVariable(v);
        value = selectValue(var);
      }

    	public IntVar[] getSearchVariables() {
    	    return searchVariables;
    	}

	/**
	 * example variable selection; input order
	 */

  IntVar selectVariable(IntVar[] v, int index) {
    if (v.length != 0) {
      if(v[index].min() == v[index].max()){

        searchVariables = new IntVar[v.length-1];
        for (int i = 0; i < index; i++)
              searchVariables[i] = v[i];

        for (int i = index + 1; i < v.length; i++)
              searchVariables[i-1] = v[i];

        return v[index];
      } else {
        searchVariables = v;

        return v[index];
      }

    }
    else {
      System.err.println("Zero length list of variables for labeling");
      return new IntVar(store);
    }
  }

	IntVar selectVariable(IntVar[] v) {
	    return selectVariable(v, 0);
	}

  IntVar selectVariable(IntVar[] v, String type) {
    switch(type){
      case "firstfail":
        return selectVariableFirstFail(v);
      default:
        return selectVariable(v);
    }
  }

  IntVar selectVariableFirstFail(IntVar[] v) {
    int minSize = Integer.MAX_VALUE;
    int minIndex = 0;

    for(int i = 0; i < v.length-1; i++) {
      if (v[i].getSize() < minSize) {
        minSize = v[i].getSize();
        minIndex = i;
      }
    }

    return selectVariable(v, minIndex);
  }

	/**
	 * example value selection; indomain_min
	 */

  int selectValue(IntVar v) {
      switch(option) {
        case 1:
          return (v.max() + v.min())/2;
        case 2:
	       return (int) Math.ceil((double)(v.max() + v.min())/2);
        default:
          return v.min();
        }
	}

	/**
	 * example constraint assigning a selected value
	 */
	public PrimitiveConstraint getConstraint() {
      switch(option) {
        case 1:
	       return new XlteqC(var, value);
        case 2:
          return new XgteqC(var, value);
        default:
          return new XeqC(var, value);
	     }
    }
  }
}
