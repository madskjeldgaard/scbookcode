Figure 11.8
// NetAddr and OSCresponder example
n = NetAddr("localhost", 57120); 
r = OSCresponder(n, '/good/news', { arg time, resp, msg; [time, msg].postln }).add;

n.sendMsg("/good/news", "you", "not you");

r.remove;
n.disconnect;

