Figure 5.13
(
var alpha, beta, gamma;
gamma = alpha;	// storing variable alpha in gamma only stores nil 
alpha = 10; 	// store 10 in alpha ...
gamma.postln;	// but the value of gamma remains unchanged
alpha = beta;	// so one cannot use gamma as 'joker'
beta = 20;		// to switch between variables alpha and beta.
gamma.postln;	// gamma is still nil.
)


