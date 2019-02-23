Figure 17.14 

(
var n, buts, synths, ratios, rationames;
w = Window("tonality diamond", Rect(200,500,420,150));
w.view.decorator = FlowLayout(w.view.bounds);

rationames=[
"7/4", "3/2","5/4","1/1",
"7/5","6/5","1/1","8/5",
"7/6","1/1","5/3","4/3",
"1/1","12/7","10/7","8/7"
];

n=rationames.size;

n.do({ |i|
Button(w, Rect(20,20+(i*30),100,30))
.states_([[rationames[i], Color.black, 
if((rationames[i])=="1/1", {Color.red},{Color.yellow})
]
])
.action_({ arg butt;
Synth(\tone2, [\freq, ((rationames[i]).interpret)*440]);

})
});
w.front;
)
//////////////////////////////////////////////////////

