Figure 12.4
// evaluate the following lines by double-clicking on the
// first paranthesis when `transient-mark-mode' is enabled
// and pressing C-c C-c
(
x = {
Saw.ar(
XLine.kr(20, [400,403,407], 20),
mul: 0.7
).clump(2).sum
}.play; "scel";
)
// evaluate this line by pressing C-c C-c
x.free; "rocks";


