Figure 10.10

// ~win = JSCWindow.new;
[ "/local", 1000, "[", "/new", "de.sciss.swingosc.Frame", "panel", "[", "/new", "java.awt.Rectangle", 128, 64, 400, 400, "]", 0, "]" ]
[ "/local", "ac1000", "[", "/new", "de.sciss.swingosc.WindowResponder", 
1000, "]", 1001, "[", "/method", 1000, "getContentPane", "]" ]]
[ "/local", "key1001", "[", "/new", "de.sciss.swingosc.KeyResponder", 1001,
"]" ]
[ "/local", "cmp1001", "[", "/new", "de.sciss.swingosc.ComponentResponder",
1001, "]" ]]
		
// ~win.front;
[ "/set", 1000, "visible", 1 ]
[ "/method", 1000, "toFront" ]]

// ~playButton = JSCButton( ~win, Rect( 4, 4, 40, 56 ));
[ "/local", 1002, "[", "/new", "de.sciss.gui.MultiStateButton", "]", 
"ac1002", "[", "/new", "de.sciss.swingosc.ActionResponder", 1002, "[",
"/array", "selectedIndex", "lastModifiers", "]", "]" ]
[ "/set", 1002, "bounds", "[", "/new", "java.awt.Rectangle", 1, 1, 46, 62,
"]", "font", "[", "/ref", "font", "]" ]
[ "/local", "dnd1002", "[", "/new", 
"de.sciss.swingosc.DummyTransferHandler", 1002, 2, "]" ]
[ "/local", "key1002", "[", "/new", "de.sciss.swingosc.KeyResponder", 1002,
"]" ]
[ "/local", "cmp1002", "[", "/new", "de.sciss.swingosc.ComponentResponder",
1002, "]" ]]
[ "/method", 1001, "add", "[", "/ref", 1002, "]" ]
[ "/method", 1001, "revalidate" ],
[ "/method", 1001, "repaint" ]]

// ~playButton.states = [[ "Play", Color.white, Color.green( 0.4 )],
//                       [ "Stop", Color.white, Color.red ]];
[ "/method", 1002, "removeAllItems" ]
[ "/method", 1002, "addItem", "Play", "[", "/new", "java.awt.Color", 1.0, 
1.0, 1.0, 1.0, "]", "[", "/new", "java.awt.Color", 0.0, 0.4, 0.0, 1.0,
"]" ]
[ "/method", 1002, "addItem", "Stop", "[", "/new", "java.awt.Color", 1.0, 
1.0, 1.0, 1.0, "]", "[", "/new", "java.awt.Color", 1.0, 0.0, 0.0, 1.0,
"]" ]]

