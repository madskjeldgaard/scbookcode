/* (IZ 2005-09-25) {
	Add to Function the ability to play a UGEn-function with multiple arguments
	passed to the UGen-functions arguments. 
	Needed for using aFunction.play(argName1, ~argValue1, argName2, ~argValue2 ...)
	in Scripts
*/

+ Function {

	playArgs { arg target, outbus = 0, fadeTime=0.02, addAction=\addToHead, args;
		var def, synth, server, bytes, synthMsg;
		target = target.asTarget;
		server = target.server;
		if(server.serverRunning.not) {
			("server '" ++ server.name ++ "' not running.").warn; ^nil
		};
		def = this.asSynthDef(fadeTime:fadeTime);
		synth = Synth.basicNew(def.name,server);
		bytes = def.asBytes;
		synthMsg = synth.newMsg(target, [\i_out, outbus, \out, outbus] ++ args, addAction);
		if(bytes.size > 8192) {
			def.load(server, synthMsg);
		} {
			server.sendMsg("/d_recv", bytes, synthMsg)
		};
		^synth
	}
}