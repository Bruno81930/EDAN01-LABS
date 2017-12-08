import org.jacop.constraints.*;
import org.jacop.core.*;
import org.jacop.search.*;

public class Main {

  public static void main(String[] args) {

    //TestCase 1
    int n = 9;
    int prefs[][] = {
      { 1, 3 }, { 1, 5 }, { 1, 8 }, { 2, 5 }, { 2, 9 }, { 3, 4 }, { 3, 5 }, { 4, 1 }, { 4, 5 },
      { 5, 6 }, { 5, 1 }, { 6, 1 }, { 6, 9 }, { 7, 3 }, { 7, 8 }, { 8, 9 }, { 8, 7 }
    };
    System.out.println("TEST CASE 1");
    Main.photo(n, prefs, 1);
    Main.photo(n, prefs, 2);

    //TestCase 2
    n = 11;
    int prefs2[][] = {
          {1,3},{1,5},{2,5},{2,8}, {2,9}, {3,4}, {3,5}, {4,1},
          {4,5}, {4,6}, {5,1}, {6,1}, {6,9},{7,3}, {7,5}, {8,9}, {8,7}, {8,10},
          {9, 11}, {10, 11}
    };

    System.out.println("TEST CASE 2");
    Main.photo(n, prefs2, 1);
    Main.photo(n, prefs2, 2);

    //TestCase3
    n = 15;
    int prefs3[][] = {
          {1,3}, {1,5}, {2,5},{2,8}, {2,9}, {3,4}, {3,5}, {4,1},
          {4,15}, {4,13}, {5,1}, {6,10}, {6,9}, {7,3}, {7,5}, {8,9}, {8,7}, {8,14},
          {9, 13}, {10, 11}
        };

    System.out.println("TEST CASE 3");
    Main.photo(n, prefs3, 1);
    Main.photo(n, prefs3, 2);
  }

  public static void photo(int n, int[][] prefs, int diff) {
    Store store = new Store();

    IntVar[] ids = new IntVar[n];
    for (int i = 0; i < n; i++) {
      ids[i] = new IntVar(store, "seating_ " + i, 1, n);
    }

    store.impose(new Alldifferent(ids));

    IntVar[] distance = new IntVar[prefs.length];

	  IntVar[] costs = new IntVar[prefs.length];
    for(int i = 0; i< prefs.length; ++i){
  	  costs[i] = new IntVar(store, 0, 1);
  	  distance[i] = new IntVar(store, 1, n-1);
      store.impose(new Distance(ids[prefs[i][0]-1], ids[prefs[i][1]-1], distance[i]));
	    store.impose(new Reified(new XlteqC(distance[i], diff), costs[i]));
    }

	IntVar cost = new IntVar(store, "cost", 0, prefs.length);
  IntVar negCost = new IntVar(store, "score", -prefs.length, 0);
  store.impose(new XmulCeqZ(cost, -1, negCost));

  IntVar[] vars = new IntVar[n+1+prefs.length+prefs.length];
  for (int i = 0; i < n; i++) {
    vars[i] = ids[i];
  }
  vars[n] = cost;
  for (int i = n+1; i < n+1+prefs.length; i++) {
    vars[i] = distance[i-n-1];
  }

  for (int i = n+1+prefs.length; i < n+1+prefs.length+prefs.length; i++) {
    vars[i] = costs[i-n-1-prefs.length];
  }

	store.impose(new SumInt(store, costs, "==", cost));
    Search<IntVar> search = new DepthFirstSearch<IntVar>();
    SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(vars,
                                                          null,
                                                          new IndomainMax<IntVar>());

    //search.setSolutionListener(new PrintOutListener<IntVar>());

    boolean result = search.labeling(store, select, negCost);
    System.out.println("Cost: " + cost);
    if(result) {
      System.out.println("\n*** Yes");
      System.out.println("Solution: " + java.util.Arrays.asList(ids));
    } else
      System.out.println("\n*** No");
    }

}
