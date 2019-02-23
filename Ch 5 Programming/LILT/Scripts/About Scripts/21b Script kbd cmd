/* IZ 2007-01-27 { SC3

On the main pane of the window of a Script: 
m : toggle control by MIDI and off
alt-m: Learn MIDI - create new bindings from incoming MIDI messages
escape: Stop learning MIDI
M : read MIDI bindings from file
alt-M write MIDI bindings to file
x : Remove MIDI array: Deactivate and remove current MIDI bindings
esc (escape): 
					escapeKey, { MIDIHandler.stopLearning },
					$., { MIDIHandler.handler.skipNextLearningItem },
					$e, { this.edit },
					$g, { this.showControlGui },	// show control gui part
					$h, { this.hideControlGui },	// hide control gui part
					// return key: Load the currently selected snapshot
	 				$\r, { this.loadSelectedSnapshot; },
					enterKey, { this.takeSnapshot },
					$ , { this.toggle; },	// space key: stop if running / start if stopped
					$k, { this.kill; }, 	// free synth immediately
					$R, { this.resetParams }, // reset param values to defaults 
					$l, { this.makeLinksGui },	 // pop up window listing existing links
					$s, { this.showSnapshots(snapshotListView) },
					$S, { this.hideSnapshots(snapshotListView) },
					{ 	// thisMethod.report(char, mod, unicode, key);
						view.defaultKeyDownAction(char, mod, unicode, key) }
				);
			};
			snapshotListView.keyDownAction = { | view, char, mod, unicode, key |
				switch (unicode,
					127, {	// backspace: delete snapshot 
						if (snapshots.selection.notNil) {
							this.removeSnapshot(view.value);
							thisMethod.report(this, "removed snapshot");
						}
					},
					3, {	// enter: create snapshot 
		//				if (snapshots.selection.notNil) {
							this.takeSnapshot(snapshots.selection);
							thisMethod.report(this, "took a snapshot");
		//				}
					},
					13, { 	// return: load snapshot
						if (snapshots.selection.notNil) {
							this.loadSnapshot(snapshots.selection);
							thisMethod.report(this, "activated snapshot");
						}
					},
					32, {   // space: toggle script on/off
						this.toggle;
					},
					27, {   // escape: close snapshots view
						this.hideSnapshots(snapshotListView);
					},
					115, {   // s: also close snapshots view
						this.hideSnapshots(snapshotListView);
					},
					83, {   // S: also close snapshots view
						this.hideSnapshots(snapshotListView);
					},
					101, {   // e: select name edit view
						snapshotNameEditView.focus(true);
					},
					{ view.defaultKeyDownAction(char, mod, unicode, key) }
				)
			};
			snapshotNameEditView.keyDownAction = { | view, char, mod, unicode, key |
				switch (unicode,
					3, {	// enter: change selections name and return to list view
							view.defaultKeyDownAction(char, mod, unicode, key);
							snapshotListView.focus(true);
					},
					13, { 	// return: change selections name and return to list view
						view.defaultKeyDownAction(char, mod, unicode, key);
						snapshotListView.focus(true);
					},
					27, { 	// escape: undo editing and return to list view
						view.valueAction = if (snapshots.selection.isNil) { ""
						}{
							snapshots.names[snapshotListView.value].postln
						};
						snapshotListView.focus(true);
					},
					{ view.defaultKeyDownAction(char, mod, unicode, key) }
				)
			};
			.keyDownAction_({ | view, char, modifiers, unicode, keycode |
				switch ( char,
	//				$u, { this.removeOutput },		// unlink - NOT YET DONE!
					$e, { this.edit },				// open script file for editing
					$g, { this.showControlGui },	// show control gui part
					$h, { this.hideControlGui },	// hide control gui part
					$o, { this.class.openDialog },	// open new script file
					$R, { this.load },				// reload edited script
	// 				$m, { this.bind2MIDIFromUser }, // make controllable by next incoming MIDI
					$\r, { this.toggle; },	// return key: stop if running / start if stopped
					$ , { this.toggle; },	// space key: stop if running / start if stopped
					$k, { this.kill; },				// free synth immediately
					$r, { this.resetParams },
					{ view.defaultKeyDownAction(char, modifiers, unicode, keycode); }
				);
			});
			readersView.keyDownAction = { | me, char, mod, uni, key |
				var sel = readers.selection;
				if (sel.notNil) {
					switch (char,
						// return: start script of reader parameter
						$\r, { sel.script.start;
							if (mod > 256) { this.start }
						},
						// escape: stop script of reader parameter
						27.asAscii, { sel.script.stop;
							if (mod > 256) { this.stop }
						},
						// s: show script of reader parameter
						$s, { sel.script.makeGui },
						// h hide script of reader parameter
						$h, { sel.script.closeGui },
						// backspace: remove reader
						127.asAscii, { this.removeReader(sel) },
						{ me.defaultKeyDownAction(char, mod, uni, key) }
					)
				}{
					"no reader parameter selected".postln;
				}
			};
			writers.addListView(writersView);
			writers.selectAt(0);
	//		writers.onSelect(writersView, _.postln);
			writersView.keyDownAction = { | me, char, mod, uni, key |
				var sel = writers.selection;
				if (sel.notNil) {
					switch (char,
						// return: start script of writer parameter
						$\r, { sel[0].script.start;
							// on shift, also start the writer
							if (mod > 256) { this.start }
						},
						// escape: stop script of writer parameter
						27.asAscii, { sel[0].script.stop;
							// on shift, also stop the writer
							if (mod > 256) { this.stop }
						},
						// s: show script of writer parameter
						$s, { sel[0].script.makeGui },
						// h hide script of writer parameter
						$h, { sel[0].script.closeGui },
						// backspace: remove writer
						127.asAscii, { sel[0].removeReader(sel[1]) },
						{ me.defaultKeyDownAction(char, mod, uni, key) }
					)
				}{
					"no writer parameter selected".postln;
				}
			};

} */
