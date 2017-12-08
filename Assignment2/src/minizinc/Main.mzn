include "globals.mzn";

int: graph_size;
int: start;
int: n_dests;
set of int: dests = 1..n_dests;
array[dests] of int: dest;
int: n_edges;
set of int: edges = 1..n_edges;
array[edges] of int: from;
array[edges] of int: to;
array[edges] of int: cost;

array[1..n_dests, 1..graph_size] of var set of int: Pset;
array[1..n_dests, 1..graph_size] of var int: P;
array[1..n_dests, 1..graph_size] of var 0..100: C;
array[1..n_dests, 1..graph_size, 1..graph_size] of var 0..max(cost): D;


constraint
	forall(destination in 1..n_dests, node in 1..graph_size)(
		Pset[destination, node] =
			{to[transition] | transition in 1..n_edges where (from[transition] == node)}
			union
			{from[transition] | transition in 1..n_edges where (to[transition] == node /\ to[transition] != dest[destination])}
			union
			{start | transition in 1..1 where (node != start)}
			union
			{node | transition in 1..1 where (node != start /\ node != dest[destination])}
	);

constraint
	forall(destination in 1..n_dests, node in 1..graph_size)(
		P[destination, node] in Pset[destination, node]
	);

constraint
	forall(destination in 1..n_dests, edges in 1..n_edges)(
		D[destination, from[edges], to[edges]] = cost[edges]
		/\
		D[destination, to[edges], from[edges]] = cost[edges]
);

constraint
	forall(destination in 1..n_dests)(
		subcircuit([P[destination, node] | node in 1..graph_size])
	);

constraint
	forall(destination in 1..n_dests, node in 1..graph_size)(
		C[destination, node] = D[destination, node, P[destination, node]]
	);

set of int: nodes = 1..graph_size;
array[nodes] of var 0..100: R;

constraint
	forall(node in 1..graph_size)(
		if n_dests == 2 then
			if P[1, node] = P[2, node]
			then R[node] = C[1, node]
			else R[node] = 0 endif
		else
			R[node] = 0
		endif
	);


array[dests] of var int: Cost;

constraint forall(destination in 1..n_dests) (
	Cost[destination] = sum(node in 1..graph_size)(C[destination, node])
);

var 0..100: CostWithReduction;

constraint
	CostWithReduction = sum(c in Cost) (c) - sum(r in R) (r);

solve minimize CostWithReduction;
%output[show(CostWithReduction)];
%constraint
	%forall(node in 1..graph_size)(trace(show(R[node])));
