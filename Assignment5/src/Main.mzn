include "globals.mzn";

int: residential = 0;
int: commercial = 1;

int: n;
int: n_residential;
int: n_commercial;

array[0..n] of int: point_distribution;

array[1..n, 1..n] of var 0..1: lots;


array[1..n] of var int: residential_row;
array[1..n] of var int: residential_col;

constraint
	lex2(lots);

constraint
	forall(i in 1..n)(
		count(row(lots, i), 0, residential_row[i]) /\
		count(col(lots, i), 0, residential_col[i])
	);

var int: cost_row = sum([point_distribution[residential_row[i]]| i in 1..n]);
var int: cost_col = sum([point_distribution[residential_col[i]]| i in 1..n]);

var int: residential_building = sum(residential_row);

constraint
	residential_building = n_residential;

var int: cost_sum;

constraint
	cost_sum = cost_row + cost_col;

solve maximize cost_sum;
