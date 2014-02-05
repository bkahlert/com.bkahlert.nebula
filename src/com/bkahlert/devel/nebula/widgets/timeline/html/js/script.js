var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.devel = com.bkahlert.devel || {};
com.bkahlert.devel.nebula = com.bkahlert.devel.nebula || {};
com.bkahlert.devel.nebula.timeline = com.bkahlert.devel.nebula.timeline || {};
(function($) {
    /**
     * Façade for the core functionality of the SIMILE Timeline
     */
    $.extend(com.bkahlert.devel.nebula.timeline, {

        animate : function(runnable, timestart, timeend, callback) {
            $(".timeline").animate({
                opacity : 0
            }, timestart, function() {
                var $this = $(this);
                runnable($this);
                if ( typeof callback == "function")
                    callback(this);
                $this.animate({
                    opacity : 1
                }, timeend);
            });
        },

        loadJSONAnimated : function(json, callback, timestart, timeend) {
            timestart = timestart || 300;
            timeend = timeend || 300;
            com.bkahlert.devel.nebula.timeline.animate(function($this) {
                com.bkahlert.devel.nebula.timeline.loadJSON(json, callback);
            }, timestart, timeend, callback);
        },

        loadJSON : function(json, callback) {
            if(typeof(json) === "string") {
                // if string interpret as url and call again with parsed json 
                Timeline.loadJSON(json, function(json, url) {
                    com.bkahlert.devel.nebula.timeline.loadJSON(json, callback);
                });
            } else {
                $(".timeline").timeline('loadJSON', json);
                if(typeof callback == "function") callback(this);
            }
        },

        replace : function(pos, json) {
            $(".timeline").timeline('replace', pos, json);
        },

        setMinVisibleDate : function(date) {
            $(".timeline").timeline('setMinVisibleDate', date);
        },

        setCenterVisibleDate : function(date) {
            $(".timeline").timeline('setCenterVisibleDate', date);
        },

        getCenterVisibleDate : function() {
            return $(".timeline").timeline('getCenterVisibleDate')[0];
        },

        setMaxVisibleDate : function(date) {
            $(".timeline").timeline('setMaxVisibleDate', date);
        },

        setDecorators : function(decorators, callback) {
            $(".timeline").timeline('setDecorators', decorators);
        },

        /**
         * Zooms in to the specified index.
         * @param {Object} newZoomIndex
         */
        setZoomIndex : function(newZoomIndex) {
            $(".timeline").timeline("setZoomIndex", "newZoomIndex");
        },

        getZoomIndex : function() {
            return $(".timeline").timeline("getZoomIndex");
        },

        openDemo : function(data) {
            $("#container").remove();
            $("html").addClass("ready");
            $("head").append($("<link rel='stylesheet' type='text/css' href='file:///Users/bkahlert/Dropbox/Development/Repositories/SUA/de.fu_berlin.imp.seqan.usability_analyzer.timeline/bin/de/fu_berlin/imp/seqan/usability_analyzer/timeline/ui/widgets/style.css' />"));
            $("body").append($('<div style="position: absolute; top: 10px; right: 50px; z-index: 99999;"><input type="button" value="Custom Function" onClick="testFunction();"/></div>'));

            data.options = data.options || {};
            com.bkahlert.devel.nebula.timeline.loadJSONAnimated(data, function() {
            });
        }
    });
})(jQuery);

function testFunction() {
    com.bkahlert.devel.nebula.timeline.setDecorators([{
                "startLabel": null,
                "endLabel": null,
                "startDate": "2011-09-13T06:23:16.32-05:00",
                "endDate": "2011-09-13T06:25:36.663-05:00"
            }]);
}


jQuery(document).ready(function($) {
    var internal = /[?&]internal=true/.test(location.href);
    var loadDemo = /[?&]loadDemo=true/.test(location.href);
    if (!internal)
        $.getJSON("test.json", function(data, textStatus, jqXHR) {
            $("#cboxWrapper").click(function() {
                $.colorbox.close();
            });
            $("input[name=demo]").click(function() {
                com.bkahlert.devel.nebula.timeline.openDemo(data);
            }).attr("disabled", false);
            $("input[name=immediateDemo]").click(function() {
                window.location.href = window.location.href + "?loadDemo=true";
            }).attr("disabled", false);

            // start demo instantly
            if (loadDemo)
                com.bkahlert.devel.nebula.timeline.openDemo(data);
        });

    if (internal || loadDemo) {
        $("#container").remove();
        $("html").addClass("ready");
        $("head").append($("<link rel='stylesheet' type='text/css' href='file:///Users/bkahlert/Dropbox/Development/Repositories/SUA/de.fu_berlin.imp.seqan.usability_analyzer.timeline/bin/de/fu_berlin/imp/seqan/usability_analyzer/timeline/ui/widgets/style.css' />"));
    }

    startObservation();
    if (!internal) {
        window.timeline_plugin_click_callback = function(id) {
            console.log("click " + id);
        }

        window.timeline_plugin_mclick_callback = function(id) {
            console.log("middle click " + id);
        }

        window.timeline_plugin_rclick_callback = function(id) {
            console.log("right click " + id);
        }

        window.timeline_plugin_dblclick_callback = function(id) {
            console.log("dblclick " + id);
        }

        window.timeline_plugin_resizestart_callback = function(id, custom) {
            console.log("resizestart " + id + "; " + custom[0] + " - " + custom[1]);
        }

        window.timeline_plugin_resizing_callback = function(id, custom) {
            console.log("resizing " + id + "; " + custom[0] + " - " + custom[1]);
        }

        window.timeline_plugin_resizestop_callback = function(id, custom) {
            console.log("resizestop " + id + "; " + custom[0] + " - " + custom[1]);
        }

        window.timeline_plugin_mouseIn_callback = function(id) {
            console.log("mouse in " + id);
        }

        window.timeline_plugin_mouseOut_callback = function(id) {
            console.log("mouse out " + id);
        }
    }
});

/**
 * Catches various events (clicking on an label, resizing a tape) and
 * calls the appropriate listener.
 * Overriding those listeners can be used to forward the events to Eclipse.
 */
function startObservation() {
    function handler(e, data, callback) {
        var $element;

        var custom = [];
        if (e.type == "resizestart" || e.type == "resize" || e.type == "resizestop") {
            // tape
            $element = $(e.target);
            var $timeline = $element.parents(".timeline");
            var timeZone = $timeline.data("timeZone");
            var newStart = null;
            var newEnd = null;
            if (data.position.left != data.originalPosition.left) {
                newStart = formatDate(convertTimeZone($timeline.timeline("getTapeStartDate", $element), timeZone), timeZone);
            }
            if (data.position.left + data.size.width != data.originalPosition.left + data.originalSize.width) {
                newEnd = formatDate(convertTimeZone($timeline.timeline("getTapeEndDate", $element), timeZone), timeZone);
            }
            custom = [newStart, newEnd];
        } else {
            var element = document.elementFromPoint(e.pageX, e.pageY);
            if (!element)
                return false;
            $element = $(element);
        }

        if ($element.parent().hasClass('timeline-event-icon')) {
            $element = $element.parent().prev();
            if (!$element) {
                alert('No previous element found. This is unexpected since icons should always be preceeded by a tape div.');
            }
        }
        if ($element.hasClass('timeline-event-tape')) {
            $element = $element.prev();
            if (!$element) {
                alert('No previous element found. This is unexpected since tapes should always be preceeded by a label div.');
            }
        }

        var callbackCalled = false;
        if ($element.attr('class')) {
            $.each($element.attr('class').split(/\s+/), function(i, className) {
                if (callbackCalled)
                    return false;
                if (className.length > '_-_'.length * 3) {
                    parts = className.split('_-_');
                    if (parts.length == 4) {
                        callbackCalled = true;
                        callback(parts[2], custom);
                    }
                }
            });
            if (!callbackCalled)
                callback(null);
        }
        return true;
    }


    $(document).click(function(e, data) {
        switch (e.which) {
            case 1:
                return window['timeline_plugin_click_callback'] ? handler(e, data, timeline_plugin_click_callback) : false;
                break;
            case 2:
                return window['timeline_plugin_mclick_callback'] ? handler(e, data, timeline_plugin_mclick_callback) : false;
                break;
            case 3:
                // handled by contextmenu handler
                break;
            default:
            // unknown mouse button
        }
        return false;
    });

    $(document).bind("contextmenu", function(e, data) {
        return window['timeline_plugin_rclick_callback'] ? handler(e, data, timeline_plugin_rclick_callback) : false;
    });

    $(document).dblclick(function(e, data) {
        e.stopPropagation();
        return window['timeline_plugin_dblclick_callback'] ? handler(e, data, timeline_plugin_dblclick_callback) : false;
    });

    $(document).bind("resizestart", function(e, data) {
        return window['timeline_plugin_resizestart_callback'] ? handler(e, data, timeline_plugin_resizestart_callback) : false;
    });

    $(document).bind("resize", function(e, data) {
        return window['timeline_plugin_resizing_callback'] ? handler(e, data, timeline_plugin_resizing_callback) : false;
    });

    $(document).bind("resizestop", function(e, data) {
        return window['timeline_plugin_resizestop_callback'] ? handler(e, data, timeline_plugin_resizestop_callback) : false;
    });

    var hoveredId = null;
    $(document).mousemove(function(e, data) {
        return handler(e, data, function(id) {
            if (hoveredId == id)
                return;
            if (hoveredId != null && window['timeline_plugin_mouseOut_callback'])
                timeline_plugin_mouseOut_callback(hoveredId);
            if (id != null && window['timeline_plugin_mouseIn_callback'])
                timeline_plugin_mouseIn_callback(id);
            hoveredId = id;
        });
    });
}

/**
 * Stops browsers (especially Safari) to select the underlaying word when right-clicked.
 */
jQuery(document).ready(function($) {
    $(document).bind("contextmenu", function(e) {
        deselectForTheNext(80, 5);
    });
    var deselectId = null;

    function deselectForTheNext(milliseconds, pauseBetweenIntervals) {
        if (deselectId != null)
            window.clearInterval(deselectId);
        deselectId = window.setInterval(deselectAll, pauseBetweenIntervals);
        repeats = Math.round(milliseconds / pauseBetweenIntervals);
    }

    function deselectAll() {
        if (document.selection) {
            document.selection.empty();
        } else {
            window.getSelection().removeAllRanges();
        }
        //$(".timeline-meta").html("deselected " + repeats +  " " + deselectId);
        repeats--;
        if (repeats < 0) {
            window.clearInterval(deselectId);
            deselectId = null;
        }
    }

});
