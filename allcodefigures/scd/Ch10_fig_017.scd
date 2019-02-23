Figure 10.17

(
~action = { arg fileName; "Selected file is '%'\n".postf( fileName )};
~sResp  = JavaObject( "de.sciss.swingosc.ActionResponder", nil, ~fileChooser.id );
~cResp  = OSCpathResponder( g.addr, [ '/action', ~fileChooser.id ], {
    fork { var file, fileName;
        file     = ~fileChooser.getSelectedFile__;
        fileName = file.getAbsolutePath_;
        file.destroy;
        ~action.value( fileName.asString );
    };
}).add;
)

