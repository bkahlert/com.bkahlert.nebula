// TODO forward exceptions to Eclipse

(function($) {
    var methods = {

        /**
         * Magnifies the zoom steps array by the specified number.
         * This means that the first fields are removed and
         * the last one are duplicated so the resulting array
         * has the same number of elements.
         * <p>
         * e.g. [A, B, C, D] and magnification of 2 results in [C, D, D, D].
         * same input but with magnification of -2 results in [A, A, A, C];
         */
        magnifyZoomSteps : function(zoomSteps, by) {
            if (!zoomSteps)
                return false;
            var magnified = [];
            for (var i = 0; i < zoomSteps.length; i++) {
                var pos = i + by;
                if (pos < 0)
                    pos = 0;
                if (pos > zoomSteps.length - 1)
                    pos = zoomSteps.length - 1;
                magnified[i] = zoomSteps[pos];
            }
            return magnified;
        },

        extendZones : function(zones, options) {
            if (zones == false)
                return [];
            var newZones = new Array(zones.length);
            for (var i = 0, m = zones.length; i < m; i++) {
                newZones[i] = $.extend({}, zones[i], options);
            }
            return newZones;
        },

        /**
         * Creates SIMILE timeline decorators on the base of JSON decorators.
         * <p>
         * The supplied options are applied to each JSON decorator before a SIMILE timeline decorator is created.
         *
         * @return an array of SIMILE timeline decorators
         */
        createDecorators : function(decorators, options) {
            if (!decorators)
                return [];
            var newDecorators = new Array(decorators.length);
            for (var i = 0, m = decorators.length; i < m; i++) {
                newDecorators[i] = new Timeline.SpanHighlightDecorator($.extend({}, decorators[i], options));
            }
            return newDecorators;
        },

        calculateBandWidths : function(totalWidth, bandSettings, minWidth) {
            var restingWidth = totalWidth;
            var minWidth = (totalWidth * minWidth) / 100;
            var numBandsWithoutWidth = 0;
            $.each(bandSettings, function(i) {
                if (bandSettings[i].options.width) {
                    var relativeWidth = (totalWidth * bandSettings[i].options.width) / 100;
                    bandSettings[i].options.width = relativeWidth + "%";
                    restingWidth -= relativeWidth;
                } else {
                    numBandsWithoutWidth++;
                }
            });

            var width = restingWidth / numBandsWithoutWidth;
            if (width < minWidth)
                width = minWidth;
            $.each(bandSettings, function(i) {
                if (!bandSettings[i].options.width) {
                    bandSettings[i].options.width = width + "%";
                }
            });
        },

        addClassOn : function(eventType, className, keepClassUntilQualified) {
            var timeline = $(this).data("timeline");
            $(document).bind(eventType, function(e) {
                var element = document.elementFromPoint(e.pageX, e.pageY);
                if (!element)
                    return;
                    
                /*
                 * do nothing if user is resizing an event
                 */
                if(timeline._resizing) return;

                var $element = $(element);

                var newEventLabel = null;
                var newEventTape = null;
                var newEventIcon = null;
                if ($element.hasClass('timeline-event-tape')) {
                    newEventLabel = $element.prev();
                    newEventTape = $element;
                    newEventIcon = $element.next();
                } else if ($element.hasClass('timeline-event-label')) {
                    newEventLabel = $element;
                    newEventTape = $element.next();
                    newEventIcon = $element.next().next();
                } else if ($element.parent().hasClass('timeline-event-icon')) {
                    newEventLabel = $element.parent().prev().prev();
                    newEventTape = $element.parent().prev();
                    newEventIcon = $element.parent();
                }

                if (keepClassUntilQualified && (newEventLabel == null || newEventTape == null))
                    return;

                $("." + className).removeClass(className);

                if (newEventLabel != null)
                    newEventLabel.addClass(className);
                if (newEventTape != null)
                    newEventTape.addClass(className);
                if (newEventIcon != null)
                    newEventIcon.addClass(className);
            });
        },

        activateHoverClass : function() {
            this.timeline("addClassOn", "mousemove", "hover", false);
        },

        activateFocusClass : function() {
            this.timeline("addClassOn", "click", "focus", true);
        },

        /**
         * Creates a theme from the passed options.
         */
        createTheme : function(options) {
            var theme = Timeline.ClassicTheme.create();

            theme.firstDayOfWeek = options.firstDayOfWeek;
            theme.timeline_start = options.timeline_start;
            theme.timeline_end = options.timeline_end;

            theme.event.instant.icon = options.icon_url;
            theme.event.instant.iconWidth = options.icon_width;
            theme.event.instant.iconHeight = options.icon_height;
            theme.event.instant.impreciseOpacity = options.tape_impreciseOpacity;
            theme.event.duration.impreciseOpacity = options.tape_impreciseOpacity;
            theme.event.track.height = options.track_height;
            theme.event.track.gap = options.tape_gap;
            theme.event.track.offset = options.tape_offset;
            theme.event.tape.height = options.tape_height;
            // the bar above the label
            theme.event.overviewTrack.offset = options.overviewTrack_offset;
            theme.event.overviewTrack.tickHeight = options.overviewTrack_tickHeight;
            theme.event.overviewTrack.height = options.overviewTrack_height;
            theme.event.overviewTrack.gap = options.overviewTrack_gap;
            theme.event.overviewTrack.autoWidthMargin = options.overviewTrack_autoWidthMargin;

            return theme;
        },

        // array of arrays
        setPermanentDecorators : function(permanentDecorators) {
            return this.each(function() {
                $(this).data("permanentDecorators", permanentDecorators);
            });
        },

        // array of arrays
        getPermanentDecorators : function() {
            return $($(this)[0]).data("permanentDecorators");
        },

        /**
         * Applies the decorators to each band
         *
         * @param decorators to apply to each band
         */
        setDecorators : function(decorators) {
            if ( typeof decorators === 'string')
                decorators = $.parseJSON(decorators);
            decorators = decorators || [];
            return this.each(function() {
                $this = $(this);
                var permanentDecorators = $this.timeline("getPermanentDecorators") || [];
                var timeline = $this.data("timeline");
                for (var i = 0, m = timeline.getBandCount(); i < m; i++) {
                    var band = timeline.getBand(i);

                    // remove old decorators
                    for (var j = 0, n = band._decorators.length; j < n; j++) {
                        if (band._decorators[j]._layerDiv != null) {
                            band.removeLayerDiv(band._decorators[j]._layerDiv);
                        }
                    }

                    var permanentDecoratorsForBandI = permanentDecorators[i] || [];

                    // init new decorators
                    // dirty to access private fields but couldn't find any alternative
                    var decorators_ = [];
                    $.each(decorators, function(i, decorator) {
                        var classNames = "timeline-highlight-custom";
                        if (decorator.classname)
                            classNames += " " + decorator.classname;
                        var decorator_ = $.extend({}, decorator);
                        decorator_.cssClass = classNames;
                        decorators_.push(decorator_);
                    });
                    band._decorators = $.merge($.merge([], permanentDecoratorsForBandI), $this.timeline("createDecorators", decorators_, {}));
                    for (var j = 0, n = band._decorators.length; j < n; j++)
                        band._decorators[j].initialize(band, timeline);
                }
                timeline.paint();
            });
        },

        init : function(options, bandOptions) {
            var settings = {
                title : false,
                minStart : false,
                centerStart : false,
                maxStart : false,
                firstDayOfWeek : 1,
                timeline_start : false,
                timeline_end : false,
                icon_url : "no-image-40.png",
                icon_width : 40,
                icon_height : 20,
                track_height : 3,
                track_gap : 2,
                track_offset : 5,
                tape_height : 5,
                tape_impreciseOpacity : 20,

                show_bubble : false,
                show_bubble_field : false,

                hotZones : false,
                permanentDecorators : [],
                decorators : [],
                timeZone : 2,

                // see http://simile-widgets.org/wiki/Timeline_ThemeClass#event
                overviewTrack_offset : 5,
                overviewTrack_tickHeight : 6,
                overviewTrack_height : 2,
                overviewTrack_gap : 1,
                overviewTrack_autoWidthMargin : 5
            };

            var bandSettings = {
                showInTimeBand : true
            };
            // default options for a single band; will be overridden with an array of band settings

            if (options)
                $.extend(settings, options, true);

            // If zoom steps have been have been defined and their unit property is text
            // look for the corresponding variable (e.g. "Timeline.DateTime.SECOND" -> Timeline.DateTime.SECOND)
            if (settings.zoomSteps) {
                for (var i = 0; i < settings.zoomSteps.length; i++) {
                    if ( typeof settings.zoomSteps[i].unit === "string") {
                        settings.zoomSteps[i].unit = SimileAjax.DateTime[settings.zoomSteps[i].unit];
                    }
                }
                if (!settings.zoomIndex) {
                    settings.zoomIndex = 0;
                }
            } else {
                settings.zoomSteps = [{
                    pixelsPerInterval : 66,
                    unit : Timeline.DateTime.HOUR,
                    getShowLabelEveryUnits : 1
                }];
                settings.zoomIndex = 0;
            }

            if (settings.centerStart == null)
                settings.centerStart = false;

            (function() {
                var temp = [];
                $.each(bandOptions, function(i, bandOptions) {
                    temp[i] = $.extend({}, bandSettings, bandOptions, true);
                });
                bandSettings = temp;
            })();
            this.timeline("calculateBandWidths", 88.5, bandSettings, 10);

            /*
             * Use a user defined bubble function or ignore it completely.
             */
            if (settings.show_bubble && settings.show_bubble_field) {
                Timeline.CompactEventPainter.prototype._showBubble = function(x, y, evt) {
                    var fn = window[settings.show_bubble];
                    if ( typeof fn === 'function') {
                        fn(evt[0]["_" + settings.show_bubble_field]);
                    }
                }
            } else {
                Timeline.CompactEventPainter.prototype._showBubble = function() {
                };
            }
            
            this.data("timeZone", settings.timeZone);

            return this.each(function() {
                var $this = $(this);
                var customBandEventSources = [];
                var timeBandEventSource = new Timeline.DefaultEventSource(0);

                var customBands = [];
                $.each(bandSettings, function(i) {
                    customBandEventSources[i] = new Timeline.DefaultEventSource(0);
                    var theme = $this.timeline("createTheme", $.extend({}, settings, bandSettings[i], true));
                    customBands[i] = Timeline.createBandInfo($.extend({
                        eventSource : customBandEventSources[i],
                        theme : theme,
                        timeZone : settings.timeZone,
                        eventPainter : Timeline.CompactEventPainter,
                        eventPainterParams : {// surprisingly the CompactEventPainter does not take all options from the theme but also from the painerParams
                            iconLabelGap : 5,
                            labelRightMargin : 10,
                            trackHeight : settings.track_height,
                            trackOffset : settings.track_offset,

                            iconWidth : theme.event.instant.iconWidth, // These are for per-event custom icons
                            iconHeight : theme.event.instant.iconHeight,

                            stackConcurrentPreciseInstantEvents : {
                                limit : 5,
                                moreMessageTemplate : "%0 More Events",
                                icon : "no-image-80.png", // default icon in stacks
                                iconWidth : 40,
                                iconHeight : 20
                            }
                        },
                        intervalPixels : settings.zoomSteps[settings.zoomIndex].pixelsPerInterval,
                        intervalUnit : settings.zoomSteps[settings.zoomIndex].unit,
                        multiple : settings.zoomSteps[settings.zoomIndex].showLabelEveryUnits
                    }, bandSettings[i].options));
                });

                function createOverviewBand(zoomIndex, zoomSteps) {
                    return Timeline.createBandInfo({
                        width : "7%",
                        intervalPixels : zoomSteps[zoomIndex].pixelsPerInterval,
                        intervalUnit : zoomSteps[zoomIndex].unit,
                        multiple : zoomSteps[zoomIndex].showLabelEveryUnits,
                        zoomIndex : zoomIndex,
                        zoomSteps : zoomSteps,
                        eventSource : timeBandEventSource,
                        theme : timeBandTheme,
                        timeZone : settings.timeZone,
                        layout : 'overview' // original, overview, detailed
                    })
                }

                var timeBandTheme = $this.timeline("createTheme", settings);
                var timeBands = [createOverviewBand(settings.zoomIndex, $this.timeline("magnifyZoomSteps", settings.zoomSteps, 5)), createOverviewBand(settings.zoomIndex, $this.timeline("magnifyZoomSteps", settings.zoomSteps, 16))];

                /*
                 * Synchronize all bands with the first one
                 *
                 * Add filter that ignores very small rendered events
                 * to improve performance
                 */
                var bandInfos = $.merge(customBands, timeBands);
                for (var i = 0, m = bandInfos.length; i < m; i++) {
                    if (i != 0) {
                        bandInfos[i].syncWith = 0;
                        bandInfos[i].highlight = true;
                    }

                    bandInfos[i].eventPainter.setFilterMatcher(function(evt) {
                        var num = i;
                        if (evt._start && evt._end) {
                            var width = Math.round(band.dateToPixelOffset(evt._end) - band.dateToPixelOffset(evt._start));
                            if (width <= 1)
                                return false;
                        }
                        return true;
                    });
                }

                var permanentDecorators = [];
                for (var i = 0; i < bandInfos.length; i++) {
                    var permanentDecoratorsForBandI = $this.timeline("createDecorators", settings.permanentDecorators, {
                        cssClass : 'timeline-highlight-data'
                    });
                    permanentDecorators.push(permanentDecoratorsForBandI);
                }
                $this.timeline("setPermanentDecorators", permanentDecorators);

                // Instantiation
                $this.data('timeline', Timeline.create(this, bandInfos, Timeline.HORIZONTAL));
                $this.data('customBandEventSources', customBandEventSources);
                $this.data('timeBandEventSource', timeBandEventSource);
                $this.timeline("setDecorators", settings.decorators);

                /*
                 * Add custom CSS classes
                 * - custom bands
                 * - last custom band
                 *
                 * Create band labels
                 */
                var bandLabels = $('<div class="band-labels"></div>').prependTo($this);
                for (var i = 0, m = bandSettings.length; i < m; i++) {
                    var band = $this.data('timeline').getBand(i);
                    var bandContainer = $(band._innerDiv).parent(".timeline-band");
                    bandContainer.addClass("timeline-custom-band");
                    if (i == m - 1)
                        bandContainer.addClass("last");

                    var bandLabel = $('<div class="band-label">' + bandSettings[i].options.title + '</div>').appendTo(bandLabels);
                    bandLabel.css({
                        "position" : "absolute",
                        "left" : "0px",
                        "top" : bandContainer.css("top"),
                    });
                    bandLabel.data('bandContainer', bandContainer);
                }

                /*
                 * Add meridian layer (1px centered line)
                 */
                $('<div class="timeline-meridian"></div>').prependTo($this);

                /*
                 * Add zoom controls
                 * if zoomSteps and a valid index are set.
                 * // TODO codeInstance soll memo nicht in instance speichern
                 *
                 * // TODO icons
                 */
                if (settings.zoomSteps.length > 0) {
                    $('<div class="timeline-zoom"></div>').prependTo($this).zoomControl({
                        min : 0,
                        max : settings.zoomSteps.length - 1,
                        value : settings.zoomIndex,
                        change : function(event, ui) {
                            var newZoomIndex = ui.value;
                            if ($this.data("zoomIndex") != newZoomIndex) {
                                $this.timeline("setZoomIndex", newZoomIndex);
                            }
                        },
                        slide : function(event, ui) {
                            var newZoomIndex = ui.value;
                            if ($this.data("zoomIndex") != newZoomIndex) {
                                $this.timeline("setZoomIndex", newZoomIndex);
                            }
                        }
                    });
                    $this.data("zoomIndex", settings.zoomIndex);
                    $this.data("minZoomIndex", 0);
                    $this.data("maxZoomIndex", settings.zoomSteps.length - 1);
                }

                // Init Position
                if (settings.minStart != false)
                    $this.timeline("setMinVisibleDate", settings.minStart);
                else if (settings.centerStart != false)
                    $this.timeline("setCenterVisibleDate", settings.centerStart);
                else if (settings.maxStart != false)
                    $this.timeline("setMaxVisibleDate", settings.maxStart);
                else
                    $this.timeline("setCenterVisibleDate", new Date());

                /*
                 * Meta Information
                 */
                var meta = $('<div class="timeline-meta"></div>').appendTo($this.parent());

                /* Title Label */
                if (settings.title) {
                    $('<div class="title"></div>').appendTo(meta).html(settings.title);
                }

                /* Date Label + Save in Data field */
                function getFormattedCenterVisibleDate(band) {
                    var timeZone = $this.data("timeZone");
                    var iso8601 = formatDate(convertTimeZone(band.getCenterVisibleDate(), timeZone), timeZone);
                    $this.data('centerVisibleDate', iso8601);
                    return iso8601;
                }

                var currentDate = $('<div class="current-date"></div>').appendTo(meta).html(getFormattedCenterVisibleDate($this.data('timeline').getBand(0)) + "");
                $this.data('timeline').getBand(0).addOnScrollListener(function(band) {
                    currentDate.html(getFormattedCenterVisibleDate(band));
                });

                /* Add a hover class to the label AND its corresponding tape */
                $this.timeline("activateHoverClass");

                /* Add a focus class to the label AND its corresponding tape */
                $this.timeline("activateFocusClass");

                /* Copyright */
                $('<div class="copyright"></div>').appendTo(meta).html('<a href="http://nebula.devel.bkahlert.com/" target="_blank">Eclipse Integration &copy; Björn Kahlert, FU Berlin</a> | <a href="http://code.google.com/p/simile-widgets/" target="_blank">Timeline &copy; SIMILE</a>');

                /*
                 * Resize
                 */
                var resizeTimerID = null;
                jQuery(window).resize(function(event) {
                    // resize events bubble; we only want window resize
                    if(event.target != window) return;
                    
                    var timeline = $this.data('timeline');
                    var date = timeline.getBand(0).getCenterVisibleDate();
                    if (resizeTimerID == null) {
                        resizeTimerID = window.setTimeout(function() {
                            resizeTimerID = null;
                            timeline.layout();
                            timeline.getBand(0).setCenterVisibleDate(date);

                            jQuery.each(jQuery('.band-labels .band-label'), function() {
                                $label = jQuery(this);
                                $container = $label.data('bandContainer');
                                $label.css('top', $container.css('top'));
                            });
                        }, 150);
                    }
                });
            });
        },
        setMinVisibleDate : function(date) {
            if ( typeof date !== 'object')
                date = Timeline.DateTime.parseGregorianDateTime(date);
            return this.each(function() {
                $(this).data('timeline').getBand(0).setMinVisibleDate(date);
            });
        },
        setCenterVisibleDate : function(date) {
            if ( typeof date !== 'object')
                date = Timeline.DateTime.parseGregorianDateTime(date);
            return this.each(function() {
                $(this).data('timeline').getBand(0).setCenterVisibleDate(date);
            });
        },
        getCenterVisibleDate : function() {
            var centerVisibleDates = [];
            this.each(function() {
                centerVisibleDates.push($(this).data('centerVisibleDate'));
            });
            return centerVisibleDates;
        },
        setMaxVisibleDate : function(date) {
            if ( typeof date !== 'object')
                date = Timeline.DateTime.parseGregorianDateTime(date);
            return this.each(function() {
                $(this).data('timeline').getBand(0).setMaxVisibleDate(date);
            });
        },
        
        /**
         * Returns the date a given tape element starts or ends.
         * 
         * @param tapeElement the element of interest
         * @param getStart true if you want to get the start date; false if you want to get the end date
         * @return the start/end date
         */
        getTapeDate : function(tapeElement, getStart) {
            var bandDiv = $(tapeElement).parents(".timeline-band");
            if(bandDiv.length != 1) return null;
            else bandDiv = bandDiv[0];

            var band = null;
            var bands = $(this).data('timeline')._bands;
            $.each(bands, function(i) {
                if(bands[i]._div == bandDiv) band = bands[i];
            });
            
            if(band == null) return null;
            
            var pixels = parseInt(tapeElement.css("left"));
            if(!getStart) pixels += parseInt(tapeElement.css("width"));
            return band._ether.pixelOffsetToDate(band._viewOffset+pixels);
        },
        
        /**
         * Returns the start date of a given tape element.
         * 
         * @param tapeElement the element of interest
         * @return the start date
         */
        getTapeStartDate : function(tapeElement) {
            return $(this).timeline("getTapeDate", tapeElement, true);
        },
        
        /**
         * Returns the end date of a given tape element.
         * 
         * @param tapeElement the element of interest
         * @return the end date
         */
        getTapeEndDate : function(tapeElement) {
            return $(this).timeline("getTapeDate", tapeElement, false);
        },

        /**
         * Zooms in to the specified level.
         * Checks if the index bounds are not exceeded.
         * @param increment
         * @param x the x coordinate on the affected band where to zoom in [optional]
         * @param y the y coordinate on the affected band where to zoom in [optional]
         */
        setZoomIndex : function(newZoomIndex, x, y) {
            if (newZoomIndex < this.data("minZoomIndex"))
                newZoomIndex = this.data("minZoomIndex");
            if (newZoomIndex > this.data("maxZoomIndex"))
                newZoomIndex = this.data("maxZoomIndex");
            this.data("zoomIndex", newZoomIndex);
            this.children(".timeline-zoom").zoomControl("setValue", newZoomIndex);

            return this.each(function() {
                $timeline = $(this);
                var timeline = $timeline.data("timeline");
                if (!x) {
                    var offset = parseInt($(timeline._bands[0]._div).css("left")) * -1;
                    var center = $(document).width() / 2;
                    x = offset + center;
                }
                if (!y) {
                    y = 0;
                }
                timeline.zoomTo(newZoomIndex, x, y);
            });
        },

        /**
         * Returns the current zoom index.
         */
        getZoomIndex : function() {
            return this.data("zoomIndex");
        },

        /**
         * Zooms in by increment steps
         * @param increment
         * @param x the x coordinate on the affected band where to zoom in [optional]
         * @param y the y coordinate on the affected band where to zoom in [optional]
         */
        zoomBy : function(increment, x, y) {
            $(this).timeline("setZoomIndex", this.data("zoomIndex") + increment, x, y);
        },

        loadJSON : function(json) {
            if ( typeof json === 'string')
                json = $.parseJSON(json);

            var bandInformation = $(this).timeline('splitBandInformation', json);
            var bandOptions = [];
            $.each(bandInformation, function(i, bandInformation) {
                bandOptions[i] = bandInformation || {};
            });

            return this.each(function() {
                if (json.options) {
                    // re-create timeline if options were passed
                    $(this).timeline(json.options, bandOptions);
                }

                var timeBandInformation = {
                    "options" : json.options,
                    "events" : []
                };
                var customBandEventSources = $(this).data('customBandEventSources');
                // TODO handle the case that no enough or to many bandInformation objects exist
                $.each(customBandEventSources, function(i) {
                    customBandEventSources[i].clear();
                    if (bandInformation[i]) {
                        customBandEventSources[i].loadJSON(bandInformation[i], "");

                        // add information to the timebands
                        if (bandInformation[i].options.showInOverviewBands) {
                            $.each(bandInformation[i].events, function() {
                                timeBandInformation.events.push(this);
                            });
                        }
                    }
                });

                var timeBandEventSource = $(this).data('timeBandEventSource');
                timeBandEventSource.clear();
                timeBandEventSource.loadJSON(timeBandInformation, "");

                $this.data('customBandInformations', bandInformation);
            });

        },
        /**
         * Replaces a element at pos by the json object
         * and reloads the affected elements.
         */
        replace : function(pos, json) {
            if ( typeof json === 'string')
                json = $.parseJSON(json);

            return this.each(function() {
                var customBandInformations = $this.data('customBandInformations');
                if (pos && pos.length && pos.length >= 1 && customBandInformations.length >= pos[0] + 1) {
                    var bandNumber = pos[0];
                    var customBandInformation = customBandInformations[bandNumber];
                    if (pos.length >= 2 && customBandInformation.events.length >= pos[1] + 1) {
                        var eventNumber = pos[1];
                        var newCustomBandInformation = {
                            options : customBandInformation.options,
                            events : []
                        }
                        for (var i = 0; i < customBandInformation.events.length; i++) {
                            if (i != eventNumber)
                                newCustomBandInformation.events.push(customBandInformation.events[i]);
                            else
                                newCustomBandInformation.events.push(json);
                        }

                        $this.data('customBandInformations')[bandNumber] = newCustomBandInformation;

                        $this.data('customBandEventSources')[bandNumber].clear();
                        $this.data('customBandEventSources')[bandNumber].loadJSON(newCustomBandInformation, "");
                    }
                }
            });
        },

        splitBandInformation : function(json) {
            if (!json.bands)
                window.alert("Band information missing");
            var bandInformation = [];
            $.each(json.bands, function(i, band) {
                bandInformation[i] = {
                    "options" : $.extend({}, json.options || {}, band.options || {}),
                    "events" : band.events
                }
            });
            return bandInformation;
        }
    };

    $.fn.timeline = function(method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if ( typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.timeline');
        }
    };
})(jQuery);

/*
 * The paint method does not correctly color the tape.
 * Simply copying the original sources(!) from
 * http://code.google.com/searchframe#Lhpgg-dZ4PI/timeline/trunk/src/webapp/api/scripts/compact-painter.js&q=CompactEventPainter%20package:simile-widgets%5C.googlecode%5C.com
 * makes it work.
 */
Timeline.CompactEventPainter.prototype.paintPreciseDurationEvent = function(evt, metrics, theme, highlightIndex) {
    var commonData = {
        tooltip : evt.getProperty("tooltip") || evt.getText()
    };

    var tapeData = {
        start : evt.getStart(),
        end : evt.getEnd(),
        color : evt.getColor() || evt.getTextColor(),
        isInstant : false
    };

    var iconData = {
        url : evt.getIcon()
    };
    if (iconData.url == null) {
        iconData = null;
    } else {
        iconData.width = evt.getProperty("iconWidth") || metrics.customIconWidth;
        iconData.height = evt.getProperty("iconHeight") || metrics.customIconHeight;
    }

    var labelData = {
        text : evt.getText(),
        color : evt.getTextColor() || evt.getColor(),
        className : evt.getClassName()
    };

    var result = this.paintTapeIconLabel(evt.getLatestStart(), commonData, tapeData, iconData, labelData, metrics, theme, highlightIndex);

    var self = this;
    var clickHandler = iconData != null ? function(elmt, domEvt, target) {
        return self._onClickInstantEvent(result.iconElmtData.elmt, domEvt, evt);
    } : function(elmt, domEvt, target) {
        return self._onClickInstantEvent(result.labelElmtData.elmt, domEvt, evt);
    };
    
    /*
     * NEW
     */
    if(typeof evt.getClassName() === "string" && $.inArray("resizable", evt.getClassName().split(" ")) != -1) {
        $(result.tapeElmtData.elmt).resizable({
            handles: "e, w",
            start: function() {
                self._timeline._resizing = true;
            },
            stop: function() {
                self._timeline._resizing = false;
            }
        });
    }

    SimileAjax.DOM.registerEvent(result.labelElmtData.elmt, "mousedown", clickHandler);
    SimileAjax.DOM.registerEvent(result.tapeElmtData.elmt, "mousedown", clickHandler);

    if (iconData != null) {
        SimileAjax.DOM.registerEvent(result.iconElmtData.elmt, "mousedown", clickHandler);
        this._eventIdToElmt[evt.getID()] = result.iconElmtData.elmt;
    } else {
        this._eventIdToElmt[evt.getID()] = result.labelElmtData.elmt;
    }
};

Timeline._Band.prototype._onMouseDown = function(innerFrame, evt, target) {
    this.closeBubble();
    
    this._dragging = true;
    this._dragX = evt.clientX;
    this._dragY = evt.clientY;
};

/*
 * This method overrides the original one since it has one bug.
 * It ignores the color, if a class name was specified.
 * Now the color is only reset to the default value if nothing was specified.
 */
Timeline.OverviewEventPainter.prototype.paintDurationEvent = function(evt, metrics, theme, highlightIndex) {
    var latestStartDate = evt.getLatestStart();
    var earliestEndDate = evt.getEarliestEnd();

    var latestStartPixel = Math.round(this._band.dateToPixelOffset(latestStartDate));
    var earliestEndPixel = Math.round(this._band.dateToPixelOffset(earliestEndDate));

    var tapeTrack = 0;
    for (; tapeTrack < this._tracks.length; tapeTrack++) {
        if (earliestEndPixel < this._tracks[tapeTrack]) {
            break;
        }
    }
    this._tracks[tapeTrack] = earliestEndPixel;

    var color = evt.getColor(), klassName = evt.getClassName();
    if (klassName != null && color != null) {
        color = color != null ? color : theme.event.duration.color;
    }

    var tapeElmtData = this._paintEventTape(evt, tapeTrack, latestStartPixel, earliestEndPixel, color, 100, metrics, theme, klassName);

    this._createHighlightDiv(highlightIndex, tapeElmtData, theme);
};

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
        band._disableSyncing = true;
        band.zoomTo(newZoomIndex, x, y);
        band._disableSyncing = false;
    });
    /*
     * notify listeners *after* all bands have been zoomed
     */
    this._bands[0]._onChanging();
    this.paint();
};

/**
 * Stop sync notifications when flag set by Timeline.zoom/zoomTo
 * in order to prevent ping pong requests provoked by sequential (one band after another) zoom operations.
 */
Timeline._Band.prototype._onChanging_ = Timeline._Band.prototype._onChanging;
Timeline._Band.prototype._onChanging = function() {
    if (this._disableSyncing)
        return;
    this._onChanging_();
};

/**
 * No double click action if clicked on an event.
 * Override the scroll to behavior and zooms in instead.
 */
Timeline._Band.prototype._onDblClick = function(innerFrame, evt, target) {
    var $target = $(target);
    if ($target.hasClass("timeline-event-label") || $target.hasClass("timeline-event-tape") || $target.hasClass("timeline-event-icon") || $target.parent().hasClass("timeline-event-icon")) {
        return true;
    }

    var coords = SimileAjax.DOM.getEventRelativeCoordinates(evt, innerFrame);
    $(this._timeline._containerDiv).timeline("zoomBy", -1, coords.x, coords.y);
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
            // no zoom on scrolling vertically but real scrolling
            var move_amt = 10 * (delta < 0 ? -1 : 1);
            var eventsDiv = $(this._div).find(".timeline-band-events .timeline-band-layer-inner");
            $(eventsDiv.parent()).css("overflow-y", "hidden");

            var top = parseInt(eventsDiv.css("top"));
            var height = parseInt(eventsDiv.css("height"));
            if (isNaN(top))
                top = 0;
            var newTop = top + move_amt;

            var bandHeight = 0;
            $(eventsDiv.find(".timeline-event-label")).each(function() {
                var $label = $(this);
                var h = parseInt($label.css("top")) + parseInt($label.css("height"));
                if (h > bandHeight)
                    bandHeight = h;
            });
            if (newTop > 0)
                newTop = 0;
            if (newTop < -bandHeight + height)
                newTop = -bandHeight + height;
            console.log(bandHeight);

            eventsDiv.css("top", newTop + "px");
            event.stopPropagation();
            event.preventDefault();
            return;

            if (now - this._timeline._lastScrollTime < 1000) {
                event.stopPropagation();
                event.preventDefault();
                return;
            }
            var loc = SimileAjax.DOM.getEventRelativeCoordinates(evt, innerFrame);
            if (delta != 0) {
                var zoomBy = delta > 0 ? -1 : +1;
                $(this._timeline._containerDiv).timeline("zoomBy", zoomBy, loc.x, loc.y);
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
 * Custom labels (adds among other things a emphasized class to all round dates)
 * @param {Object} date
 * @param {Object} intervalUnit
 */
Timeline.GregorianDateLabeller.prototype.defaultLabelInterval = function(date, intervalUnit) {
    var text;
    var emphasized = false;

    date = SimileAjax.DateTime.removeTimeZoneOffset(date, this._timeZone);

    switch(intervalUnit) {
        case SimileAjax.DateTime.MILLISECOND:
            var ms = date.getUTCMilliseconds();
            if (ms == 0) {
                text = date.getUTCHours() + ":" + date.getUTCMinutes() + ":" + date.getUTCSeconds() + ".0";
                emphasized = true;
            } else {
                text = ms;
            }
            break;
        case SimileAjax.DateTime.SECOND:
            var s = date.getUTCSeconds();
            if (s == 0) {
                text = date.getUTCHours() + ":" + date.getUTCMinutes() + ":00";
                emphasized = true;
            } else {
                text = s;
            }
            break;
        case SimileAjax.DateTime.MINUTE:
            var m = date.getUTCMinutes();
            if (m == 0) {
                text = date.getUTCHours() + ":00";
                emphasized = true;
            } else {
                text = m;
            }
            break;
        case SimileAjax.DateTime.HOUR:
            text = date.getUTCHours() + "hr";
            break;
        case SimileAjax.DateTime.DAY:
            text = Timeline.GregorianDateLabeller.getMonthName(date.getUTCMonth(), this._locale) + " " + date.getUTCDate();
            break;
        case SimileAjax.DateTime.WEEK:
            text = Timeline.GregorianDateLabeller.getMonthName(date.getUTCMonth(), this._locale) + " " + date.getUTCDate();
            break;
        case SimileAjax.DateTime.MONTH:
            var m = date.getUTCMonth();
            if (m != 0) {
                text = Timeline.GregorianDateLabeller.getMonthName(m, this._locale);
                break;
            }// else, fall through
        case SimileAjax.DateTime.YEAR:
        case SimileAjax.DateTime.DECADE:
        case SimileAjax.DateTime.CENTURY:
        case SimileAjax.DateTime.MILLENNIUM:
            var y = date.getUTCFullYear();
            if (y > 0) {
                text = date.getUTCFullYear();
            } else {
                text = (1 - y) + "BC";
            }
            emphasized = (intervalUnit == SimileAjax.DateTime.MONTH) || (intervalUnit == SimileAjax.DateTime.DECADE && y % 100 == 0) || (intervalUnit == SimileAjax.DateTime.CENTURY && y % 1000 == 0);
            break;
        default:
            text = date.toUTCString();
    }
    return {
        text : text,
        emphasized : emphasized
    };
}