/* IZ 2007-02-10 { SC3
Trying to enhance the MIDI Monitor, adding the ability to edit a name for each responder.
} */

var script, window, responders, listener, responderProto;

script = ~script;
responders = ListModel([], makeNames: { |l| l.list.collect { |i|
	[i.displayString, i] }}
);
responderProto = (
	name: "--",
	displayString: { | envir |
		envir use: { ~name ++ ":" ++ ~responder.displayString }
	}
);

~start = {
	var decorator, responderView, responderListView;
	var responderNameView, responderFunctionView;
	if (window.isNil) {
		window = SCWindow("MIDI Monitor", Rect(300, 300, 350, 450).fromTop);
		window.onClose = { window = nil; script.stop; };
		window.view.decorator = decorator = FlowLayout(window.view.bounds, 3@3, 2@2);
		SCButton(window, Rect(0, 0, 110, 20))
			.states_([["Stop Listening"], ["Start Listening"]])
			.action_({ | me | MIDIListener.perform([\startListening, \stopListening][me.value])});
		SCButton(window, Rect(0, 0, 110, 20))
			.states_([["Rescan MIDI Ports"]])
			.action_({ MIDIListener.initMIDI(false) });
		SCButton(window, Rect(0, 0, decorator.rest[0], 20))
			.states_([["Start Posting Input"], ["Stop Posting Input"]])
			.action_({ | me | MIDIListener.verbose = me.value > 0 });
		decorator.nextLine;
		responderView = SCStaticText(window, Rect(0, 0, decorator.rest[0], 20))
			.background_(Color(0.9, 0.2, 0.2, 0.3))
			.string_(" waiting for MIDI input! ");
		decorator.nextLine;
		SCStaticText(window, Rect(0, 0, 130, 20)).string = "Edit responder's name:";
		responderNameView = SCTextField(window, Rect(0, 0, decorator.rest[0], 20))
			.action_({ | me |
				if (responders.selection.notNil) {
					responders.selection.name = me.string;
					responders.updateNames;
				};
			});
		responders.onSelect(responderNameView, { | resp | responderNameView.string = resp.name });
		decorator.nextLine;
		responders.onSelect(responderView, { | resp, name | responderView.string = name });
		SCStaticText(window, Rect(0, 0, 200, 20)).string = "Responder's function:";
		SCButton(window, Rect(0, 0, decorator.rest[0], 20))
			.states_([["Edit Function"]])
			.action_({ | me |
				var selection, doc; // works also with multiple edit docs open! ?
				selection = responders.selection;
				if (responders.selection.notNil) {
					doc = Document(
						"Editing function of: " ++ selection.displayString ++ " (close to update)",
						if (selection.responder.function.isNil) {
							"{ | port, chan, key, val | [port, chan, key, val].postln }"
						}{
							selection.responder.function.def.sourceCode.asString
						}
					).onClose_({ | me |
						selection.responder.function = me.string.interpret;
						// TODO: better update technique needed
						responders.selectItem(selection);
					})
				}
			});
		decorator.nextLine;
		responderFunctionView = SCTextField(window, Rect(0, 0, decorator.rest[0], 60))
			.action_({ | me |
				if (responders.selection.notNil) {
					responders.selection.name = me.string;
					responders.updateNames;
				};
			});
		responders.onSelect(responderFunctionView, { | resp |
			responderFunctionView.string = if (resp.responder.function.isNil) {
				" (no function defined) "
			}{
				resp.responder.function.def.sourceCode.asString
			}
		});
		decorator.nextLine;
		responderListView = SCListView(window, Rect(0, 0, *decorator.rest));
		responderListView.resize = 4;
		responders.addListView(responderListView);
		MIDIListener.init;
		MIDIListener.startListening;
		MIDIListener addDependant: listener = { |  who, responder  |
			if (responder.isKindOf(MIDIResponder)) {
				responders.add(().proto_(responderProto) make: {
					~responder = responder;
				});
				responders.selectLast;
			}
		};
	};
	window.front;
};

~stop = {
	MIDIListener.stopListening;
	MIDIListener removeDependant: listener;
	if (window.notNil) { window.close };
};