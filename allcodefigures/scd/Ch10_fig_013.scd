Figure 10.13

(
~mySlider            = Event.new;
~mySlider.view       = JSCUserView( ~win, Rect( 110, 32, 200, 145 )).focusVisible_( false );
~mySlider.pos        = ~speedSpec.unmap( ~model.speed );
// these will be used in the final draw func:
~mySlider.pressed    = false;
~mySlider.project    = false;
// this will update the slider position and refresh the view:
~mySlider.adjustPos  = { arg slid, pos;
    slid.pos         = pos;
    slid.view.action.value( slid );
    slid.view.refresh };
// these will set the slider track curve and refresh the view:
~mySlider.adjustCurve= { arg slid, curve;
    slid.curve       = curve;
    slid.lines       = curve.slide( 2, 1 ).clump( 2 );
    slid.lineLens    = slid.lines.collect({ arg pair; pair[ 0 ].dist( pair[ 1 ])}).normalizeSum; 
    slid.projections = nil;
    slid.view.refresh };
// a primary draw function just to indicate the view's bounds:
~mySlider.view.drawFunc = { arg view; var b = view.bounds;
    JPen.line( 0 @ 0, b.width @ b.height );
    JPen.line( 0 @ b.height, b.width @ 0 );
    JPen.stroke };
// make the window a little bigger:
~win.setInnerExtent( 370, 180 );
)

