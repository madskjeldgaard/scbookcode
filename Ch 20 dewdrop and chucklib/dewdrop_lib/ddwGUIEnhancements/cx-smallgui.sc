
+ Object {
	smallGui { arg  ... args;
		if(this.guiClass.findRespondingMethodFor(\smallGui).notNil,{
			^this.guiClass.new(this).performList(\smallGui,args);
		},{
			^Tile(this,args.first.asPageLayout)
		});
	}

	isView { ^false }
}


+ ObjectGui {
	smallGui { arg lay, bounds ... args;
		var layout;
		layout=this.guify(lay,bounds, small:true);
		layout.flow({ arg layout;
			this.view = layout;
			this.writeName(layout);
			this.performList(\smallGuiBody,[layout] ++ args);
		},bounds).background_(this.background);
		//if you created it, front it
		if(lay.isNil,{ layout.resizeToFit.front });
	}
}


+ ObjectGui {
	guiNoLabel { arg lay, bounds ... args;
		var layout;
		layout=this.guify(lay,bounds);
		layout.flow({ arg layout;
			view = layout;
			this.performList(\guiBody,[layout] ++ args);
		},bounds).background_(this.background);
		//if you created it, front it
		if(lay.isNil,{ layout.resizeToFit.front });
	}
}

+ Object {
	guiNoLabel { arg  ... args; 
		^this.guiClass.new(this).performList(\guiNoLabel, args);
	}
}


+ GUI {
	*skins { ^skins }
	*setSkin { |name|
		skin = skins[name];
		scheme.font.new(*GUI.skin.fontSpecs).setDefault;
	}
}
