Figure 5.39
(
~make_display = { | counter |
	var window, label, adapter, stagger;
	window = Window(
		"counting to " ++ counter.max_count.asString, 
		Rect(stagger = UniqueID.next % 20 * 20 + 400, stagger, 200, 50)
	);
	label = StaticText(window, window.view.bounds.insetBy(10, 10));
	adapter = { | counter, what, count |
			{ label.string = counter.current_count.asString }.defer
	};
	counter addDependant: adapter;
	/* remove the adapter when window closes to prevent error in 
	   updating non-existent views: */
	window.onClose = { counter removeDependant: adapter };
	window.front
};
)
