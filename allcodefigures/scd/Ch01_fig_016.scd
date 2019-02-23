1.16 Additive Synthesis Example

// Try this first
Array.fill(20, {arg cnt; cnt + 1*110}); // harmonics built on 110

// And a patch
(
{Mix.ar(
	Array.fill(12, 
		{arg count; 
		var harm;
		harm = count + 1 * 110; // remember precedence; count + 1, then * 110
			SinOsc.ar(harm, 
				mul: max([0, 0], SinOsc.kr(count+1/4))
				)*1/(count+1)
		})
)*0.7}.play
)

