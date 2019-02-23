Figure 5.18
(
// a function that calculates the square of the mean of two numbers
var sq_mean;
sq_mean = { arg a, b; 	// arguments a, b defined in arg statement form
	(a + b / 2).squared;
};
// calculate the square of the mean of 3 and 1:
sq_mean.value(3, 1);
)


