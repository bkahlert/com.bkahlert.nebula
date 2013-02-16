/**
 * Zooms the band to the specified index
 * @param {Object} newZoomIndex
 */
Timeline.LinearEther.prototype.zoomTo = function(newZoomIndex) {
    var netIntervalChange = 0;
    var currentZoomIndex = this._band._zoomIndex;

    if (newZoomIndex < 0) {
        newZoomIndex = 0;
    }

    if (newZoomIndex >= this._band._zoomSteps.length) {
        newZoomIndex = this._band._zoomSteps.length - 1;
    }

    if (newZoomIndex == currentZoomIndex)
        return null;
    this._band._etherPainter._multiple = this._band._zoomSteps[newZoomIndex].showLabelEveryUnits;

    this._band._zoomIndex = newZoomIndex;
    this._interval = SimileAjax.DateTime.gregorianUnitLengths[this._band._zoomSteps[newZoomIndex].unit];
    this._pixelsPerInterval = this._band._zoomSteps[newZoomIndex].pixelsPerInterval;
    netIntervalChange = this._band._zoomSteps[newZoomIndex].unit - this._band._zoomSteps[currentZoomIndex].unit;

    return netIntervalChange;
};

/**
 * Zooms the hot zone ether to the specified index
 * @param {Object} newZoomIndex
 */
Timeline.HotZoneEther.prototype.zoomTo = function(newZoomIndex) {
    var netIntervalChange = 0;
    var currentZoomIndex = this._band._zoomIndex;

    if (newZoomIndex < 0) {
        newZoomIndex = 0;
    }

    if (newZoomIndex >= this._band._zoomSteps.length) {
        newZoomIndex = this._band._zoomSteps.length - 1;
    }

    if (newZoomIndex == currentZoomIndex)
        return null;
    this._band._etherPainter._multiple = this._band._zoomSteps[newZoomIndex].showLabelEveryUnits;

    this._band._zoomIndex = newZoomIndex;
    this._interval = SimileAjax.DateTime.gregorianUnitLengths[this._band._zoomSteps[newZoomIndex].unit];
    this._pixelsPerInterval = this._band._zoomSteps[newZoomIndex].pixelsPerInterval;
    netIntervalChange = this._band._zoomSteps[newZoomIndex].unit - this._band._zoomSteps[currentZoomIndex].unit;

    return netIntervalChange;
};

/**
 * Zooms the band to the specified index
 * @param {Object} newZoomIndex
 * @param {Object} x
 * @param {Object} y (currently ignored)
 */
Timeline._Band.prototype.zoomTo = function(newZoomIndex, x, y) {
    if (!this._zoomSteps) {
        // zoom disabled
        return;
    }

    // shift the x value by our offset
    x += this._viewOffset;

    var zoomDate = this._ether.pixelOffsetToDate(x);
    var netIntervalChange = this._ether.zoomTo(newZoomIndex);

    if (netIntervalChange == null)
        return;

    this._etherPainter.zoom(netIntervalChange);

    // shift our zoom date to the far left
    this._moveEther(Math.round(-this._ether.dateToPixelOffset(zoomDate)));
    // then shift it back to where the mouse was
    this._moveEther(x);
};

/**
 * Zooms the whole timeline (= all custom bands) to the specified index
 * @param {Object} newZoomIndex
 * @param {Object} x
 * @param {Object} y (currently ignored)
 */
Timeline._Impl.prototype.zoomTo = function(newZoomIndex, x, y) {
    $.each(this._bands, function(i, band) {
        if (!$(band._div).hasClass("timeline-custom-band"))
            return;
        band._disableSyncing = true;
        band.zoomTo(newZoomIndex, x, y);
        band._disableSyncing = false;
    });
    this.paint();
};

/**
 * Stop sync notifications when flag set by Timeline.zoom/zoomTo
 * in order to prevent ping pong requests provoked by sequential (one band after another) zoom operations. 
 */
Timeline._Band.prototype._onChanging_ = Timeline._Band.prototype._onChanging;
Timeline._Band.prototype._onChanging = function() {
    if(this._disableSyncing) return;
    this._onChanging_();
};

/**
 * Zooms the whole timeline (= all custom bands) in/out by one step
 * @param {Object} zoomIn
 * @param {Object} x
 * @param {Object} y (currently ignored)
 */
Timeline._Impl.prototype.zoom = function(zoomIn, x, y) {
    $.each(this._bands, function(i, band) {
        if (!$(band._div).hasClass("timeline-custom-band"))
            return;
        band._disableSyncing = true;
        band.zoom(zoomIn, x, y, null);
        band._disableSyncing = false;
    });
    this.paint();
};

/**
 * Override the scroll to behavior and zooms in instead.
 */
Timeline._Band.prototype._onDblClick = function(innerFrame, evt, target) {
    var coords = SimileAjax.DOM.getEventRelativeCoordinates(evt, innerFrame);
    //var distance = coords.x - (this._viewLength / 2 - this._viewOffset);

    //this._autoScroll(-distance);
    this._timeline.zoom(true, coords.x, coords.y, innerFrame);
};

/**
 * Alters the scroll behavior. Horizontal scroll still moves the timeline
 * but vertical scroll zooms in / out.
 */
Timeline._Band.prototype._onMouseScroll = function(innerFrame, evt, target) {
    /**
     * Copied detection logic from jquery.mousewheel.js to extract x and y scroll directions.
     * I didn't want to bind to the mousewheel event since scroll was already detected by
     * timeline. Double handling the event would have been more complicated and error-prone.
     * @param {Object} event
     */
    function prepare(event) {
        var orgEvent = event || window.event, args = [].slice.call(arguments, 1), delta = 0, deltaX = 0, deltaY = 0;
        event = $.event.fix(orgEvent);

        // Old school scrollwheel delta
        if (orgEvent.wheelDelta) {
            delta = orgEvent.wheelDelta / 120;
        }
        if (orgEvent.detail) {
            delta = -orgEvent.detail / 3;
        }

        // New school multidimensional scroll (touchpads) deltas
        deltaY = delta;

        // Gecko
        if (orgEvent.axis !== undefined && orgEvent.axis === orgEvent.HORIZONTAL_AXIS) {
            deltaY = 0;
            deltaX = -1 * delta;
        }

        // Webkit
        if (orgEvent.wheelDeltaY !== undefined) {
            deltaY = orgEvent.wheelDeltaY / 120;
        }
        if (orgEvent.wheelDeltaX !== undefined) {
            deltaX = -1 * orgEvent.wheelDeltaX / 120;
        }

        // Return value
        event.delta = delta;
        event.deltaX = deltaX;
        event.deltaY = deltaY;

        return event;
    }

    var event = prepare(evt);
    var delta = event.delta;
    var deltaX = event.deltaX;
    var deltaY = event.deltaY;
    var now = event.timeStamp;

    if (!this._timeline._lastScrollTime || ((now - this._timeline._lastScrollTime) > 50)) {
        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            var move_amt = 30 * (delta < 0 ? -1 : 1);
            this._moveEther(move_amt);
        } else {
            if (now - this._timeline._lastScrollTime < 1000) {
                event.stopPropagation();
                event.preventDefault();
                return;
            }
            var loc = SimileAjax.DOM.getEventRelativeCoordinates(evt, innerFrame);
            if (delta != 0) {
                var zoomIn = delta > 0;
                this._timeline.zoom(zoomIn, loc.x, loc.y, innerFrame);
            }
            event.stopPropagation();
            event.preventDefault();
        }

        this._timeline._lastScrollTime = now;
    }

    // prevent bubble
    if (evt.stopPropagation) {
        evt.stopPropagation();
    }
    evt.cancelBubble = true;

    // prevent the default action
    if (evt.preventDefault) {
        evt.preventDefault();
    }
    evt.returnValue = false;
};

/**
 * Zooms in to the specified index.
 * @param {Object} newZoomIndex
 */
com.bkahlert.devel.nebula.timeline.zoomTo = function(newZoomIndex) {
    $.each($(".timeline"), function() {
        $timeline = $(this);
        var timeline = $timeline.data("timeline");
        var offset = parseInt($(timeline._bands[0]._div).css("left")) * -1;
        var center = $(document).width() / 2;
        var x = offset + center;
        var y = 0;
        timeline.zoomTo(newZoomIndex, x, y);
    });
}

/**
 * Zooms in by one level.
 */
com.bkahlert.devel.nebula.timeline.zoomIn = function() {
    com.bkahlert.devel.nebula.timeline.zoom(true);
}

/**
 * Zooms out by one level.
 */
com.bkahlert.devel.nebula.timeline.zoomOut = function() {
    com.bkahlert.devel.nebula.timeline.zoom(false);
}

$(document).ready(function() {
    $(".timeline-zoom").zoomControl({
        min : 0,
        max : 58,
        value : 26,
        change : function(event, ui) {
            var newZoomIndex = 58 - ui.value;
            com.bkahlert.devel.nebula.timeline.zoomTo(newZoomIndex);
            console.log("zooming to " + newZoomIndex);
        }
    });
});



Timeline._Band.prototype.setCenterVisibleDate = function(date) {
    if (!this._changing) {
        this._moveEther(Math.round(this._viewLength / 2 - this._ether.dateToPixelOffset(date)));
    }
};

