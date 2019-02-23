/* IZ 2007-02-06 { SC3

} */

var script;
script = ~script;

~start = {
	// need to use pathMatch to get to this user's home directory.
	var found;
	if ((found = "~/scwork/run.rtf".pathMatch).size > 0) {
		Document.open(found.first);
	}{
		Warn("\n\n\n\n\nSorry, I could not locate the file: \"~/scwork/run.rtf\"")
	};
	{ script.stopped }.defer(0.5);
};

