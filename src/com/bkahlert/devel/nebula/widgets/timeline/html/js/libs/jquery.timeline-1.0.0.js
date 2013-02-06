(function($) {
	var methods = {

		extendZones : function(zones, options) {
			if (zones == false)
				return [];
			var newZones = new Array(zones.length);
			for (var i = 0, m = zones.length; i < m; i++) {
				newZones[i] = $.extend({}, zones[i], options);
			}
			return newZones;
		},

		createDecorators : function(decorators, options) {
			if (decorators == false)
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
					bandSettings[i].options.width = relativeWidth;
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
					bandSettings[i].options.width = width;
				}
			});
		},
		
		addClassOn : function(eventType, className, keepClassUntilQualified) {
		    var eventLabel = null;
            var eventTape = null;
            var eventIcon = null;
            $(document).bind(eventType, function(e) {
                var element = document.elementFromPoint(e.pageX, e.pageY);
                if (!element)
                    return;
        
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
                
                if(keepClassUntilQualified && (newEventLabel == null || newEventTape == null)) return;
                
                if(eventLabel != newEventLabel) {
                    if(eventLabel != null) eventLabel.removeClass(className);
                    if(newEventLabel != null) newEventLabel.addClass(className);
                    eventLabel = newEventLabel;
                }
                
                if(eventTape != newEventTape) {
                    if(eventTape != null) eventTape.removeClass(className);
                    if(newEventTape != null) newEventTape.addClass(className);
                    eventTape = newEventTape;
                }
                
                 if(eventIcon != newEventIcon) {
                    if(eventIcon != null) eventIcon.removeClass(className);
                    if(newEventIcon != null) newEventIcon.addClass(className);
                    eventIcon = newEventIcon;
                }
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
			theme.event.tape.height = options.tape_height; // the bar above the label
			theme.event.overviewTrack.offset = options.overviewTrack_offset;
			theme.event.overviewTrack.tickHeight = options.overviewTrack_tickHeight;
			theme.event.overviewTrack.height = options.overviewTrack_height;
			theme.event.overviewTrack.gap = options.overviewTrack_gap;
			theme.event.overviewTrack.autoWidthMargin = options.overviewTrack_autoWidthMargin;

			return theme;
		},

		// array of arrays
		setDefaultDecorators : function(defaultDecorators) {
			return this.each(function() {
				$(this).data("defaultDecorators", defaultDecorators);
			});
		},

		// array of arrays
		getDefaultDecorators : function() {
			return $($(this)[0]).data("defaultDecorators");
		},

		// decorators: array that is applied to each band
		applyDecorators : function(decorators) {
			if ( typeof decorators === 'string')
				decorators = $.parseJSON(decorators);
			decorators = decorators || [];
			return this.each(function() {
				$this = $(this);
				var defaultDecorators = $this.timeline("getDefaultDecorators") || [];
				var timeline = $this.data("timeline");
				for (var i = 0, m = timeline.getBandCount(); i < m; i++) {
					var band = timeline.getBand(i);

					// remove old decorators
					for (var j = 0, n = band._decorators.length; j < n; j++) {
						if (band._decorators[j]._layerDiv != null) {
							band.removeLayerDiv(band._decorators[j]._layerDiv);
						}
					}

					var defaultDecoratorsForBandI = defaultDecorators[i] || [];

					// init new decorators
					// dirty to access private fields but couldn't find any alternative
					band._decorators = $.merge($.merge([], defaultDecoratorsForBandI), $this.timeline("createDecorators", decorators, {
						cssClass : 'timeline-highlight-custom'
					}));
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
                track_gap: 2,
                track_offset: 5,
				tape_height : 5,
				tape_impreciseOpacity : 20,
				
				show_bubble : false,
				show_bubble_field : false,
				
				hotZones : false,
				decorators : false,
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

			(function() {
				var temp = [];
				$.each(bandOptions, function(i, bandOption) {
					temp[i] = $.extend({}, bandSettings, bandOption, true);
				});
				bandSettings = temp;
			})();
			this.timeline("calculateBandWidths", 72, bandSettings, 10);

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
			    Timeline.CompactEventPainter.prototype._showBubble = function() {};
			}

			return this.each(function() {
				var $this = $(this);
				var customBandEventSources = [];
				var timeBandEventSource = new Timeline.DefaultEventSource(0);

				var customBands = [];
				$.each(bandSettings, function(i) {
					customBandEventSources[i] = new Timeline.DefaultEventSource(0);
					var theme = $this.timeline("createTheme", $.extend({}, settings, bandSettings[i], true));
					customBands[i] = Timeline.createBandInfo({
						width : bandSettings[i].options.width + "%",
						intervalUnit : Timeline.DateTime.SECOND,
						multiple : 10,
						intervalPixels : 3,
						eventSource : customBandEventSources[i],
						theme : theme,
						timeZone : settings.timeZone,
						eventPainter : Timeline.CompactEventPainter,
						eventPainterParams : { // surprisingly the CompactEventPainter does not take all options from the theme but also from the painerParams
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
						}
					});
				});

				var timeBandTheme = $this.timeline("createTheme", settings);
				var timeBands = [Timeline.createHotZoneBandInfo({
					zones : $this.timeline("extendZones", settings.hotZones, {
						magnify : 3,
						unit : Timeline.DateTime.MINUTE
					}),
					width : "7%",
					intervalUnit : Timeline.DateTime.MINUTE,
					intervalPixels : 30,
					multiple : 5,
					eventSource : timeBandEventSource,
					theme : timeBandTheme,
					timeZone : settings.timeZone,
					layout : 'overview' // original, overview, detailed
				}), Timeline.createHotZoneBandInfo({
					zones : $this.timeline("extendZones", settings.hotZones, {
						magnify : 4,
						unit : Timeline.DateTime.HOUR
					}),
					width : "7%",
					intervalUnit : Timeline.DateTime.HOUR,
					intervalPixels : 50,
					eventSource : timeBandEventSource,
					theme : timeBandTheme,
					timeZone : settings.timeZone,
					layout : 'overview' // original, overview, detailed
				}), Timeline.createHotZoneBandInfo({
					zones : $this.timeline("extendZones", settings.hotZones, {
						magnify : 5,
						unit : Timeline.DateTime.DAY
					}),
					width : "7%",
					intervalUnit : Timeline.DateTime.DAY,
					intervalPixels : 100,
					eventSource : timeBandEventSource,
					theme : timeBandTheme,
					timeZone : settings.timeZone,
					layout : 'overview' // original, overview, detailed
				}), Timeline.createHotZoneBandInfo({
					zones : $this.timeline("extendZones", settings.hotZones, {
						magnify : 20,
						unit : Timeline.DateTime.MONTH
					}),
					width : "7%",
					intervalUnit : Timeline.DateTime.MONTH,
					intervalPixels : 50,
					eventSource : timeBandEventSource,
					theme : timeBandTheme,
					timeZone : settings.timeZone,
					layout : 'overview' // original, overview, detailed
				})];

				/*
				 * Synchronize all bands with the first one
				 */
				var bandInfos = $.merge(customBands, timeBands);
				for (var i = 1, m = bandInfos.length; i < m; i++) {
					bandInfos[i].syncWith = 0;
					bandInfos[i].highlight = true;
				}

				var defaultDecorators = [];
				for (var i = 0; i < bandInfos.length; i++) {
					var defaultDecoratorsForBandI = $this.timeline("createDecorators", settings.decorators, {
						cssClass : 'timeline-highlight-data'
					});
					defaultDecorators.push(defaultDecoratorsForBandI);
				}
				$this.timeline("setDefaultDecorators", defaultDecorators);

				// Instantiation
				$this.data('timeline', Timeline.create(this, bandInfos, Timeline.HORIZONTAL));
				$this.data('customBandEventSources', customBandEventSources);
				$this.data('timeBandEventSource', timeBandEventSource);
				$this.timeline("applyDecorators");

				/*
				 * Add custom CSS classes
				 * - custom bands
				 * - last custom band
				 *
				 * Create band labels
				 */
				var bandLabels = $('.band-labels').empty();
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

				/* Date Label */
				var currentDate = $('<div class="current-date"></div>').appendTo(meta).html(formatDate(convertTimeZone($this.data('timeline').getBand(0).getCenterVisibleDate(), settings.timeZone), settings.timeZone) + "");
				$this.data('timeline').getBand(0).addOnScrollListener(function(band) {
					currentDate.html(formatDate(convertTimeZone(band.getCenterVisibleDate(), settings.timeZone), settings.timeZone) + "");
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
				jQuery(window).resize(function($) {
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
		setMaxVisibleDate : function(date) {
			if ( typeof date !== 'object')
				date = Timeline.DateTime.parseGregorianDateTime(date);
			return this.each(function() {
				$(this).data('timeline').getBand(0).setMaxVisibleDate(date);
			});
		},
		loadJSON : function(json) {
			if ( typeof json === 'string')
				json = $.parseJSON(json);

			var bandInformation = $(this).timeline('splitBandInformation', json);
			var bandOptions = [];
			$.each(bandInformation, function(i, bandInformation) {
				bandOptions[i] = bandInformation || {};
			});

			return rt = this.each(function() {
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
				customBandEventSources[0].clear();
				customBandEventSources[0].loadJSON(bandInformation[0], "");
				var timeBandEventSource = $(this).data('timeBandEventSource');
				timeBandEventSource.clear();
				timeBandEventSource.loadJSON(timeBandInformation, "");
			});

		},
		update : function(content) {

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

	SimileAjax.DOM.registerEvent(result.labelElmtData.elmt, "mousedown", clickHandler);
	SimileAjax.DOM.registerEvent(result.tapeElmtData.elmt, "mousedown", clickHandler);

	if (iconData != null) {
		SimileAjax.DOM.registerEvent(result.iconElmtData.elmt, "mousedown", clickHandler);
		this._eventIdToElmt[evt.getID()] = result.iconElmtData.elmt;
	} else {
		this._eventIdToElmt[evt.getID()] = result.labelElmtData.elmt;
	}
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

/*
 * We only want the timeline to scroll on double click iff the user did not
 * click on an event.
 */
Timeline._Band.prototype._onDblClick = function(innerFrame, evt, target) {
    var $target = $(target);
    if($target.hasClass("timeline-event-label")
    || $target.hasClass("timeline-event-tape")
    || $target.hasClass("timeline-event-icon")
    || $target.parent().hasClass("timeline-event-icon")) {
        return true;
    }
    
    var coords = SimileAjax.DOM.getEventRelativeCoordinates(evt, innerFrame);
    var distance = coords.x - (this._viewLength / 2 - this._viewOffset);
    this._autoScroll(-distance);
};
