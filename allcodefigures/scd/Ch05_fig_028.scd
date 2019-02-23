Figure 5.28
(	
/* a function that recursively prints all folders and files
   found in a path and its subfolders */
{ | path |
	// store function here for use inside the if's {}:
	var thisFunc = thisFunction;
	format("====== now exploring: %", path).postln;
	// for all items in the path:
	path.pathMatch do: { | p | 
		// if the item is a folder, run this function on its contents
		// otherwise print the file found
		if (p.last == $/) { thisFunc.(p ++ "*") }{ p.postln }
	}
}.("*") // run function on home path of SuperCollider
)


