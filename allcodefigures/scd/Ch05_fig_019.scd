Figure 5.19
(
// a function that calculates the square of the mean of any numbers
var sq_mean_all;
sq_mean_all = { | ... numbers | // using ellipsis and | | argument form
	(numbers.sum / numbers.size).squared;
};
// calculate the square of the mean of [1, 3, 5, -7]:
sq_mean_all.(1, 3, 5, -7); // short form: omit message 'value'
)

