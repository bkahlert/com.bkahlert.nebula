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
			var minWidth = (totalWidth*minWidth)/100;
			var numBandsWithoutWidth = 0;
			$.each(bandSettings, function(i) {
				if(bandSettings[i].options.width) {
					var relativeWidth = (totalWidth*bandSettings[i].options.width)/100;
					bandSettings[i].options.width = relativeWidth;
					restingWidth -= relativeWidth;
				} else {
					numBandsWithoutWidth++;
				}
			});
			
			var width = restingWidth / numBandsWithoutWidth;
			if(width < minWidth) width = minWidth;
			$.each(bandSettings, function(i) {
				if(!bandSettings[i].options.width) {
					bandSettings[i].options.width = width;
				}
			});
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
				timeline_start : false,
				timeline_end : false,
				icon_url : "no-image-40.png",
				icon_width : 40,
				icon_height : 40,
				track_height : 20,
				tape_height : 5,
				show_bubble : false,
				show_bubble_field : false,
				hotZones : false,
				decorators : false,
				timeZone : 2
			};
			var bandSettings = {
			};
			// default options for a single band; will be overridden with an array of band settings

			if (options)
				$.extend(settings, options);
				
			(function() {
				var temp = [];
				$.each(bandOptions, function(i, bandOption) {
					temp[i] = $.extend({}, bandSettings, bandOption);
				});
				bandSettings = temp;
			})();
			this.timeline("calculateBandWidths", 72, bandSettings, 10);
			
			if (settings.show_bubble && settings.show_bubble_field) {
				Timeline.CompactEventPainter.prototype._showBubble = function(x, y, evt) {
					var fn = window[settings.show_bubble];
					if ( typeof fn === 'function') {
						fn(evt[0]["_" + settings.show_bubble_field]);
					}
				}
			}

			return this.each(function() {
				var $this = $(this);
				var customBandEventSources = [];
				var timeBandEventSource = new Timeline.DefaultEventSource(0);
alert("todo: optionen nach einzelnen bands verschieben")
				var theme = Timeline.ClassicTheme.create();
				theme.firstDayOfWeek = 1;
				theme.timeline_start = settings.timeline_start;
				theme.timeline_end = settings.timeline_end;
				theme.event.instant.icon = settings.icon_url;
				theme.event.instant.iconWidth = settings.icon_width;
				theme.event.instant.iconHeight = settings.icon_height;
				theme.event.track.height = settings.track_height;
				theme.event.tape.height = settings.tape_height;

				var customBands = [];
				$.each(bandSettings, function(i, bandSetting) {
					customBandEventSources[i] = new Timeline.DefaultEventSource(0);
					customBands[i] = Timeline.createBandInfo({
						width : bandSettings[i].options.width + "%",
						intervalUnit : Timeline.DateTime.SECOND,
						multiple : 10,
						intervalPixels : 3,
						eventSource : customBandEventSources[i],
						theme : theme,
						timeZone : settings.timeZone,
						eventPainter : Timeline.CompactEventPainter,
						eventPainterParams : {
							iconLabelGap : 5,
							labelRightMargin : 10,

							iconWidth : 40, // These are for per-event custom icons
							iconHeight : 20,

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
					theme : theme,
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
					theme : theme,
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
					theme : theme,
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
					theme : theme,
					timeZone : settings.timeZone,
					layout : 'overview' // original, overview, detailed
				})];

				/*
				 * Synchronize all bands with the first one
				 */
				var bandInfos = $.merge(customBands, timeBands);
				for(var i=1, m=bandInfos.length; i<m; i++) {
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
				 */
				for (var i = 0, m = bandSettings.length; i < m; i++) {
					var band = $this.data('timeline').getBand(i);
					var bandContainer = $(band._innerDiv).parent(".timeline-band");
					bandContainer.addClass("timeline-custom-band");
					if(i==m-1) bandContainer.addClass("last");
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
					"options": json.options,
					"events": []
				};
				var customBandEventSources = $(this).data('customBandEventSources');
				// TODO handle the case that no enough or to many bandInformation objects exist
				$.each(customBandEventSources, function(i) {
					customBandEventSources[i].clear();
					if(bandInformation[i]) {
						customBandEventSources[i].loadJSON(bandInformation[i], "");
						$.each(bandInformation[i].events, function() { timeBandInformation.events.push(this); });
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
