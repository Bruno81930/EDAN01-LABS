import org.jacop.constraints.*;
import org.jacop.core.*;
import org.jacop.search.*;

public class Main {
  public static void main(String[] args) {

    // Test Case 1
    int graph_size = 6;
    int start = 1;
    int n_dests = 1;
    int [] dest = {6};
    int n_edges = 7;
    int [] from = {1,1,2,2,3,4,4};
    int [] to = {2,3,3,4,5,5,6};
    int [] cost = {4,2,5,10,3,4,11};
    execute(graph_size, start, n_dests, dest, n_edges, from, to, cost);

    // Test Case 2
    int graph_size_2 = 6;
    int start_2 = 1;
    int n_dests_2 = 2;
    int [] dest_2 = {5, 6};
    int n_edges_2 = 7;
    int [] from_2 = {1,1,2,2,3,4,4};
    int [] to_2 = {2,3,3,4,5,5,6};
    int [] cost_2 = {4,2,5,10,3,4,11};
    execute(graph_size_2, start_2, n_dests_2, dest_2, n_edges_2, from_2, to_2, cost_2);

    // Test Case 3
    int graph_size_3 = 6;
    int start_3 = 1;
    int n_dests_3 = 2;
    int [] dest_3 = {5,6};
    int n_edges_3 = 9;
    int [] from_3 = {1,1,1,2,2,3,3,3,4};
    int [] to_3 = {2,3,4,3,5,4,5,6,6};
    int [] cost_3 = {6,1,5,5,3,5,6,4,2};
    execute(graph_size_3, start_3, n_dests_3, dest_3, n_edges_3, from_3, to_3, cost_3);
  }

  public static void execute(int graph_size, int start, int n_dests, int[] dest, int n_edges, int[] from, int[] to, int[] cost) {

    Store store = new Store();

    // Process Step 1 - Create nDest * List of IntVar P input of Subcircuit

    IntVar[][] P = new IntVar[n_dests][graph_size];
    for(int destination = 0; destination < n_dests; destination++){
      for(int node = 0; node < graph_size; node++) {
        P[destination][node] = new IntVar(store, "dest_" + dest[destination] + "__node_" + (node+1));
      }
    }

    for(int destination = 0; destination < n_dests; destination++){
      for(int node = 0; node < graph_size; node++) {

        if((node+1) != start) {
          P[destination][node].addDom(start, start);
          if((node+1) == dest[destination])
            continue;
          P[destination][node].addDom((node+1), (node+1));
        }

        for(int transition = 0; transition < n_edges; transition++) {
          if(from[transition] == (node+1))
            P[destination][node].addDom(to[transition], to[transition]);
          if(to[transition] == (node+1))
            P[destination][to[transition]-1].addDom(from[transition], from[transition]);
        }
      }
    }

    // Process Step 2 - Impose Subcircuit constraint for each destination in P

    for(int destination = 0; destination < n_dests; destination++)
      store.impose(new Subcircuit(P[destination]));

    // Process Step 3 - Define matrix D (distances)

    int [][][] D = new int[n_dests][graph_size][graph_size];
    for(int destination = 0; destination < n_dests; destination++){
      for(int node = 0; node < graph_size; node++) {

        for(int edge = 0; edge < graph_size; edge++)
          D[destination][node][edge] = 1000;

        D[destination][node][node] = 0;


          if(dest[destination] == (node+1))
            D[destination][node][start-1] = 0;


        for(int transition = 0; transition < n_edges; transition++) {
          if(from[transition] == node+1)
            D[destination][node][to[transition]-1] = cost[transition];
          if(to[transition] == node+1)
            D[destination][node][from[transition]-1] = cost[transition];
        }
      }
    }

    // Process Step 4 - Define List C (costs)

    IntVar [][] C = new IntVar[n_dests][graph_size];

    for(int destination = 0; destination < n_dests; destination++) {
      for(int node = 0; node < graph_size; node++)
        C[destination][node] = new IntVar(store, "cost_dest_" + (dest[destination]) + "node" + (node+1), 0, Integer.MAX_VALUE);
    }

    // Process Step 5 - Impose Element constraint to link P with C using D

    for(int destination = 0; destination < n_dests; destination++) {
        for(int node = 0; node < graph_size; node++) {
          store.impose(Element.choose(P[destination][node], D[destination][node], C[destination][node]));
        }
    }

    // Process Step 6 - Define Cost Reduction

    IntVar [] R = new IntVar[graph_size];
    IntVar [] B = new IntVar[graph_size];

    if(n_dests == 2){
      for(int node = 0; node < graph_size; node++) {
        B[node] = new IntVar(store, "boolean_" + node, 0, 1);
        R[node] = new IntVar(store, "reduction_" + node, 0, Integer.MAX_VALUE);
        store.impose(new Reified(new XeqY(P[0][node], P[1][node]),B[node]));
        store.impose(new IfThenElse(new XeqC(B[node], 1), new XeqY(R[node], C[0][node]), new XeqC(R[node], 0)));
      }
    } else {
      for(int node = 0; node < graph_size; node++) {
        R[node] = new IntVar(store, "reduction_" + node, 0, 0);
      }
    }

    IntVar Reduction = new IntVar(store, "reduction", -Integer.MAX_VALUE, 0);
    int [] negated = new int[graph_size];
    for(int node = 0; node < graph_size; node++)
      negated[node] = -1;
    store.impose(new LinearInt(store, R, negated, "reductions_sum", Reduction));


    // Process Step 7 - Define Cost
    IntVar[] costs = new IntVar[n_dests+1];
    for(int destination = 0; destination < n_dests; destination++) {
      costs[destination] = new IntVar(store, "cost_dest_" + destination, 0, Integer.MAX_VALUE);
    costs[n_dests] = Reduction;

      store.impose(new SumInt(store, C[destination], "cost_destination", costs[destination]));

    }

    IntVar Cost = new IntVar(store, "cost", 0, Integer.MAX_VALUE);
    store.impose(new SumInt(store, costs, "cost_definition", Cost));

    // Process Step 8 - Concatenate decisive variables

    IntVar [] vars = new IntVar[n_dests*graph_size*2+n_dests+1];
    for(int destination = 0; destination < n_dests; destination++) {
      for(int node = 0; node < graph_size; node++) {
        vars[destination*graph_size + node] = P[destination][node];
        vars[graph_size*n_dests+destination*graph_size + node] = C[destination][node];
      }
      vars[graph_size*n_dests*2+destination] = costs[destination];
    }
    vars[graph_size*n_dests*2+n_dests] = Cost;

    Search<IntVar> search = new DepthFirstSearch<IntVar>();
    SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(vars,
                                                          null,
                                                          new IndomainMax<IntVar>());

    search.setSolutionListener(new PrintOutListener<IntVar>());

    boolean result = search.labeling(store, select, Cost);
    System.out.println("Cost: " + Cost);
    if(result) {
      System.out.println("\n*** Yes");
      System.out.println("Solution: " + java.util.Arrays.asList(P[0]));
    } else
      System.out.println("\n*** No");

  }
}
