
figure 9.20

(
var k;
k= KeyCodeResponder.new;
//  shift control p
k.register(   35  ,   true, false, false, true, {
	CocoaDialog.getPaths({|paths|
		Document.current.selectedString_(paths[0].asCompileString);
	});
});
Document.globalKeyDownAction_(k)
)

