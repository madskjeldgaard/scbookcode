Figure 10.15

(
var mouseFunc = { arg view, x, y, modifiers, buttonNumber, clickCount;
    var b, nx, ny, dx, dy, lnP1, lnP2, linePos, lineLenSq,
        proj, dist, sum = 0, minDist = inf, newValue;
    
    b  = view.bounds;
    nx = x / b.width;
    ny = y / b.height;
    ~mySlider.projections = Array( ~mySlider.lines.size );
    // look up the line segment which is closest to the mouse
    ~mySlider.lines.do({ arg pair, i;
        #lnP1, lnP2 = pair;
        dx          = lnP2.x - lnP1.x;
        dy          = lnP2.y - lnP1.y;
        lineLenSq   = (dx*dx) + (dy*dy);
        dist        = (((nx - lnP1.x) * dx) + ((ny - lnP1.y) * dy)) / lineLenSq;
        proj        = (lnP1.x + (dist * dx)) @ (lnP1.y + (dist * dy));
        if( lnP1.x != lnP2.x, {
            linePos = (proj.x - lnP1.x) / dx;
        }, {
            linePos = (proj.y - lnP1.y) / dy;
        });
        if( linePos < 0, {
            proj = lnP1;
        }, { if( linePos > 1, {
            proj = lnP2;
        })});
        ~mySlider.projections.add( proj );
        dist = proj.dist( nx @ ny );
        if( dist < minDist, {
            newValue = sum + (linePos.clip( 0, 1 ) * ~mySlider.lineLens[i]);
            minDist  = dist;
        });
        sum = sum + ~mySlider.lineLens[ i ];
    });
    ~mySlider.pressed = true;
    ~mySlider.adjustPos( newValue );
};
~mySlider.view.mouseDownAction = mouseFunc;
~mySlider.view.mouseMoveAction = mouseFunc;
~mySlider.view.mouseUpAction   = { arg view; ~mySlider.pressed = false; view.refresh };
~mySlider.view.action          = { arg view;
    ~model.adjust( \speed, ~speedSpec.map( ~mySlider.pos ), ~mySlider )};
)

