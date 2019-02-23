  Figure  17.11 

(
var stepsperoctave=3;
Array.fill(stepsperoctave, {arg i; 2.pow(i/stepsperoctave)});
)

//Using a function to calculate the value at a chosen degree
(
f = {|degree, steps|
2.pow(degree/steps)
};
)
// 	degree 0
f.(0, 3);
// 	degree 1
f.(1, 3);
// 	degree 2
f.(2, 3);

//The function is modified to multiply the value by a root frequency in Hertz
(
f = {|degree, steps, root=440|
2.pow(degree/steps)*root
};
)
//	12 notes per octave, degrees 0,1 and 12
f.(0,12)
f.(1, 12)
f.(12, 12)

//	14 notes per octave, degrees 0,1, 12 and 14
f.(0,14)
f.(1, 14)
f.(12, 14)
f.(14,14)
//////////////////////////////////////////////////////


