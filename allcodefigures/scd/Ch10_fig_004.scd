Figure 10.4

~node.free; ~node = nil;

(
~model        = Event.new;
~model.amp    = ~amp;
~model.speed  = ~speed;
~model.node   = ~node;
~model.buf    = ~buf;
~model.adjust = { arg mod, key, value, source;
    mod.put( key, value );
    mod.changed( key, value, source )};

UpdateListener.newFor( ~model, { arg upd, mod, value;
    mod.node.set( \amp, value )}, \amp );
UpdateListener.newFor( ~model, { arg upd, mod, value;
    mod.node.set( \speed, value )}, \speed );

~playButton.action = { arg view;
    if( view.value == 1, {
        ~model.node = Synth( \bufPlay, [
            \buf, ~model.buf, \amp, ~model.amp, \speed, ~model.speed ]);
    }, {
        ~model.node.free; ~model.node = nil;
    })};
~ampSlider.action   = { arg view;
    ~model.adjust( \amp, view.value, view )};
~speedSlider.action = { arg view;
    ~model.adjust( \speed, ~speedSpec.map( view.value ), view )};
)

