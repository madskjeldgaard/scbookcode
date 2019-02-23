
figure 9.10

(
var window, listView, updater, changeItemGui, model;

model = [\item1, \item2, \item3];

window = SCWindow("double click example", Rect(200,200,180, 320)).front;
listView = SCListView(window, Rect(4,4,172, 310))
				.items_(model)
				.mouseDownAction_({|view, x, y, modifiers, buttonNumber, clickCount|
					if(clickCount == 2){
						changeItemGui.value(view)
					}
				})
				.onClose_({model.removeDependant(updater)});

changeItemGui = {|view|
	var win;
	win = SCWindow("chnage item", Rect(200,250, 150, 30)).front;
	SCTextField(win, Rect(4,4, 142, 20))
		.string_(view.item.asString)
		.action_({|v|
			model[view.value] = v.string.asSymbol;
			model.changed(\value);
			win.close;
		})
		.focus;
};

updater = {|theChanger, what, moreArgs|
	listView.items_(theChanger)
};
model.addDependant(updater);

)

