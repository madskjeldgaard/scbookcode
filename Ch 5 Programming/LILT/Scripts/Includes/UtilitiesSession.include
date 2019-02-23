// Load all items in folder "Utilities" in a session with that name

var utilities;

if (ScriptBrowser.all['Utilities'].notNil) {
	utilities = Session('Utilities');
	utilities.addScriptGroup('Little Tools');
	(ScriptBrowser.getPath.dirname ++ "/Utilities/*").pathMatch do: Script.load(_);
};
