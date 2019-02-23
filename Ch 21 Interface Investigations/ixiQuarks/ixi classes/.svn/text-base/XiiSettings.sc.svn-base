XiiSettings {	
	
	var settingsDict;
	
	*new { 
		^super.new.initXiiSettings;
	}
		
	initXiiSettings {
		var file;
		if(Object.readArchive("preferences/presets.ixi").isNil, {
			settingsDict = IdentityDictionary.new;
			"ixi-NOTE: NO WORRIES! 'presets.ixi' gets created when you store a preset".postln;
		}, {
			settingsDict = Object.readArchive("preferences/presets.ixi")
		});
	}
	
	storeSetting { arg settingName;
		var setting, file;
		"*********** STORE PRESET ******************".postln;
		setting = List.new; // not using dict because of name
		XQ.globalWidgetList.do({arg widget, i;
			if(widget.xiigui.isNil, { // if widget does not have a GUI abstraction
				setting.add([widget.asString.replace("a ",\), widget.getState]);
			}, {
				setting.add([widget.asString.replace("a ",\), widget.xiigui.getState]);
			});
		});
		settingsDict.add(settingName.asSymbol -> setting);
		settingsDict.writeArchive("preferences/presets.ixi");
	}	
	
	getSetting { arg name;
		var setting;
		setting = settingsDict.at(name.asSymbol);
		
	}
	
	getSettingsList {
		^settingsDict.keys.asArray;
	}
	
	loadSetting {arg name;
		var setting;
		"*********** LOAD PRESET ******************".postln;
		this.clearixiQuarks; // turn all quarks off and empty the screen
		setting = settingsDict.at(name.asSymbol);
		XQ.globalWidgetList = List.new;
		setting.do({arg widget, i;
			var channels, effectCodeString; 
			channels = widget[1][0];
			effectCodeString = widget[0]++".new(Server.default,"++channels++","++widget[1].asCompileString++")";
			XQ.globalWidgetList.add( effectCodeString.interpret );
		});
	}
	
	clearixiQuarks {
		XQ.globalWidgetList.do({arg widget; // close all active windows
			if(widget.xiigui.isNil, { // if widget does not have a GUI abstraction
				widget.win.close;			
			}, {
				widget.xiigui.win.close;
			});
		});
	}
	
	removeSetting {arg settingName;
		settingsDict.removeAt(settingName.asSymbol);
		settingsDict.writeArchive("preferences/presets.ixi");
	}

}

