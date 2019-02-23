Figure 17.28 

(
var w, keys, steps, octaves;

w = Window.new.name="Custom keyboard: 7 steps per octave";
steps = 7;
octaves= 2;
// seven steps per octave;
a=Array.fill(7, { |i| (1/7)*(i) })+1;
b= a++(a*2);

c=Synth(\default, [\amp, 0]);

keys=Array.fill(steps*octaves,{ |i|

Button(w, Rect(20+(i*22),20,20,50))
.states_([
if(i.mod(steps)==0,{
[i.asString, Color.black, Color.red]},{
[i.asString, Color.black, Color.yellow]});

])
.action_({ arg butt;
c.set(\freq,b[i]*220, \amp, 0.25)
});
});

w.front;
)
//////////////////////////////////////////////////////


