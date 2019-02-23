JSCSoundFileChooser : JSCView {
    var <path;       // String : current file selection (or nil)
    var <directory;  // String : currently visible directory
    var chResp;      // OSCpathResponder for ChangeResponder

    path_ { arg value;
        path = value;
        server.sendMsg( '/set', this.id, \selectedPath, value );
    }

    directory_ { arg value;
        directory = value;
        server.sendMsg( '/set', this.id, \currentDirectoryPath, value );
    }

    prInitView {
        chResp = OSCpathResponder( server.addr, [ '/change', this.id ], {
            arg time, resp, msg; var oldPath = path;
            path        = if( msg[ 4 ] !== '', { msg[ 4 ].asString });
            directory   = msg[ 6 ].asString;
            if( oldPath != path, {{ this.doAction }.defer });
        }).add;
        ^this.prSCViewNew([[ '/local', this.id, '[', '/new', "SoundFileChooser", ']', "ch" ++ this.id, '[', '/new', "de.sciss.swingosc.ChangeResponder", this.id, '[', '/array', \selectedPath, \currentDirectoryPath, ']', ']' ]]);
    }

    prClose {
        chResp.remove;
        ^super.prClose([[ '/method', "ch" ++ this.id, \remove ],
                        [ '/free', "ch" ++ this.id ]]);
    }
}
