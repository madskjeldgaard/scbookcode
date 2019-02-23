/* (IZ 2005-09-17) { 
The start-stop button of this script triggers the "magic" kill-all-servers unix command. 
It does the same thing as the "K" (Kill-button) on the default Server gui window. 
Useful if the server GUIs get stuck because of faulty OSC communication in case of a previous crash.

~script.routine message is used to introduce a wait of 0.5 seconds before returning the
 script to stopped state. 
} */

~script.routine {
	"killall -9 scsynth".unixCmd;
	Server.quitAll;
	0.5.wait; // change button back to stopped after 1/2 second
};
