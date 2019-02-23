   Figure 17.23 

(
// F. Mogini pattern-based Tuning - 2000.
x=880;

Pbind(
\freq, Pn(
Plazy({
if(x<=150, {x=x*2});
if(x>=2000, {x=x/2});
x=[

x*[1.1428,  1.36, 1.26].choose,
x/[1.1428,  1.36, 1.26].choose

].choose
})
),
\dur, 0.14,
\sustain, 0.8,
\cutoff, Pfunc({ 1.0.rand})
).play;
)
//////////////////////////////////////////////////////


