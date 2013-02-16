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
                $this.timeline('loadJSON', json);
            }, timestart, timeend, callback);
        },

        loadJSON : function(json, callback) {
            $(".timeline").timeline('loadJSON', json);
            if ( typeof callback == "function")
                callback(this);
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
    alert(com.bkahlert.devel.nebula.timeline.getZoomIndex());
    var x = com.bkahlert.devel.nebula.timeline.getCenterVisibleDate();
    com.bkahlert.devel.nebula.timeline.setCenterVisibleDate(x);
    // TODO update methode in TimelineGroupViewer implementieren
    com.bkahlert.devel.nebula.timeline.replace([3, 75], {
        "title" : "XXXXXXXXX",
        "icon" : "file:///var/folders/3m/4p47cq955p142xmsnh0c_9lm0000gn/T/TimelineHelper6874591262470491341.png",
        "image" : "file:///var/folders/3m/4p47cq955p142xmsnh0c_9lm0000gn/T/TimelineHelper6874591262470491341.png",
        "start" : "2011-09-13T02:58:39-05:00",
        "end" : "2011-09-13T03:00:55-05:00",
        "durationEvent" : true,
        "classname" : "DoclogRecord SCROLL _-_timeline-id_-_2,0,75_-_ _-_timeline-id_-_3,75_-_"
    });
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

    if (!internal) {
        /**
         * Code to test Eclipse interaction.
         * Forwards the clicked / hovered events via callbacks.
         *
         */
        //
        function handler(e, callback) {
            var element = document.elementFromPoint(e.pageX, e.pageY);
            if (!element)
                return false;

            var $element = $(element);

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
                            callback(parts[2]);
                        }
                    }
                });
                if (!callbackCalled)
                    callback(null);
            }
            return true;
        }


        $(document).click(function(e) {
            switch (e.which) {
                case 1:
                    return handler(e, timeline_plugin_click_callback);
                    break;
                case 2:
                    return handler(e, timeline_plugin_mclick_callback);
                    break;
                case 3:
                    // handled by contextmenu handler
                    break;
                default:
                // unknown mouse button
            }
            return false;
        });

        $(document).bind("contextmenu", function(e) {
            return handler(e, timeline_plugin_rclick_callback);
        });

        $(document).dblclick(function(e) {
            e.stopPropagation();
            return handler(e, timeline_plugin_dblclick_callback);
        });

        var hoveredId = null;
        $(document).mousemove(function(e) {
            return handler(e, function(id) {
                if (hoveredId == id)
                    return;
                if (hoveredId != null)
                    timeline_plugin_mouseOut_callback(hoveredId);
                if (id != null)
                    timeline_plugin_mouseIn_callback(id);
                hoveredId = id;
            });
        });

        function timeline_plugin_click_callback(id) {
            console.log("click " + id);
        }

        function timeline_plugin_mclick_callback(id) {
            console.log("middle click " + id);
        }

        function timeline_plugin_rclick_callback(id) {
            console.log("right click " + id);
        }

        function timeline_plugin_dblclick_callback(id) {
            console.log("dblclick " + id);
        }

        function timeline_plugin_mouseIn_callback(id) {
            console.log("mouse in " + id);
        }

        function timeline_plugin_mouseOut_callback(id) {
            console.log("mouse out " + id);
        }

    }
});

/**
 * Stops browsers (notices in Safari) to select the underlaying word when right-clicked.
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