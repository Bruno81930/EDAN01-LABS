# Lab assignment – Auto Regression Filter

Problem:
The data-flow graph for the auto regression filter is depicted on Figure 4.2. It consists of 16 multiplications
and 12 additions. These operations need to be scheduled on multipliers and adders. Write a program which will optimize the schedule length for different amount of resources as specified in table 4.4. Fill the table with results
obtained from your program, Assume that multiplication takes two clock cycles and addition only one, but write
your program in such a way that you can easily specify otherwise. Make your program data independent, so if
new operation or new operation type is added the program does not have to be changed but only the database of
facts. Note, that this means that the graph can change.

[alt text](rsc/example.png)

[alt text](rsc/results.png)

Hints:
* An efficient model should use Cumulative and Diff2/Diffn constraints. You may need to use special
labeling.
* The example with 1 adder and 3 multipliers may take several minutes to compute.
