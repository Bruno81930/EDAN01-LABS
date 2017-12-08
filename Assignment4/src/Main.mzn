include "globals.mzn";
include "jacop.mzn";

int: del_add; % delay/clock cycles for add
int: del_mul; % delay/clock cycles for mul

int: number_add;
int: number_mul;

int: n; % number of operations

set of int: last; % last operations (added by 1 since minizinc starts from 1)
set of int: add;
set of int: mul;

array[1..n] of set of int: dependencies; % operation number dependent on operation index

array[1..n] of var 0..100: start_time;
array[1..n] of var 1..2: duration;

% Fix for set: how to iterate over set from dependencies? S_i + D_i <= S_j
constraint
	forall(operation in 1..n)(
		forall(node in dependencies[operation]) (
		start_time[operation] + duration[operation]
			<= start_time[node]
	));


% Set duration based on operation type
constraint
	forall(mul_val in mul)(
			duration[mul_val] = del_mul
	);

constraint
	forall(add_val in add)(
			duration[add_val] = del_add
	);

array[1..card(add)] of var 0..100: add_start;

constraint
	add_start = [start_time[i] | i in 1..n where i in add];

array[1..card(mul)] of var 0..100: mul_start;
constraint
	mul_start = [start_time[i] | i in 1..n where i in mul];

array[1..card(add)] of var del_add..del_add: d_add;
array[1..card(mul)] of var del_mul..del_mul: d_mul;

array[1..card(add)] of var 1..number_add: r_add;
array[1..card(mul)] of var 1..number_mul: r_mul;

array[1..n] of var int: r = r_add ++ r_mul;

constraint
	cumulative(add_start, d_add, r_add, number_add);


constraint
	cumulative(mul_start, d_mul, r_mul, number_mul);

var 0..100: cost;

constraint
	cost = max([start_time[l] + duration[l] | l in last]);

solve :: seq_search ([
        int_search(start_time, smallest_max, indomain_min, complete),
        int_search(r, input_order, indomain_min, complete)])
    minimize cost;
output[show(cost)];
