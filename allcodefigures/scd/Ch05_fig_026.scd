Figure 5.26
(
var iterative_factorial;
iterative_factorial = { | n |
	var factorial = 1;	// initialize factorial as factorial of 1
	// calculate factorial n times, updating its value each time
	n do: { | i | factorial = factorial * (i + 1) };
	factorial;	// return the final value of factorial;
};
iterative_factorial.(10).postln;	// 10 factorial: 3628800
)

