Figure 5.23
Server.default.boot // boot default server before running example
(
var buffer;
buffer = Buffer.read(path: "sounds/a11wlk01.wav",
	action: { | buffer |
		format("loaded % at: %", buffer, Main.elapsedTime).postln;
	});
format("Reached this after 'Buffer.read' at: %", Main.elapsedTime).postln;
buffer;
)

