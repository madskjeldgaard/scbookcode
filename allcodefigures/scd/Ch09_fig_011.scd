
figure 9.11

(
	var window, listViews, model, updater;
	
	model = (	left: [ "SinOsc", "Saw", "LFSaw", "WhiteNoise", "PinkNoise", "BrownNoise", "Osc" ], 
				right: []);
	
	window = SCWindow("list view drag & drop", Rect(200,200, 255, 100)).front;
	window.view.decorator_(FlowLayout(window.view.bounds));
	
	listViews = [\left, \right].collect{|it|
		SCListView(window, Rect(10,10,120,70))
			.items_(model[it])
			.canReceiveDragHandler_{ SCView.currentDrag.isKindOf(Symbol) or: SCView.currentDrag.isKindOf(String)}
			.receiveDragHandler_{|v| 
				var index;
				index = model[it].indexOfEqual(SCView.currentDrag);
				if(index.notNil){
					v.value_(index);
				}{
					model[it] = model[it].add(SCView.currentDrag);
					model.changed(it);
					v.value_(v.items.size-1);
				}
			}
			.beginDragAction_{|v| v.item}
			.action_({|view|
				[view.value, view.item].postln;
			})
			.onClose_({model.removeDependant(updater)});
	};
	
	updater = {|theChanger, what, moreArgs| 
			switch(what, 
				\left, {listViews[0].items_(model[\left])},
				\right, {listViews[1].items_(model[\right])}
			)
			};
	model.addDependant(updater);
)
