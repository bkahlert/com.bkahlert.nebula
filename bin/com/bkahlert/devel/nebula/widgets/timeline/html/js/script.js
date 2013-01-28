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
		
		animate: function (runnable, timestart, timeend, callback) {
			$(".timeline").animate({opacity:0}, timestart, function() {
				var $this = $(this);
				runnable($this);
				if(typeof callback == "function") callback(this);
				$this.animate({opacity:1}, timeend);
			});
		},
		
		loadJSON: function (json, callback) {
			com.bkahlert.devel.nebula.timeline.animate(function($this) {
				$this.timeline('loadJSON', json);
			}, 300, 300, callback);
		},
		
		setMinVisibleDate: function(date) {
			$(".timeline").timeline('setMinVisibleDate', date);
		},
		
		setCenterVisibleDate: function(date) {
			$(".timeline").timeline('setCenterVisibleDate', date);
		},
		
		setMaxVisibleDate: function(date) {
			$(".timeline").timeline('setMaxVisibleDate', date);
		},
		
		applyDecorators: function(decorators, callback) {
			$(".timeline").timeline('applyDecorators', decorators);
		},
			
		openDemo: function(data) {
			$("#container").remove();
			if(data.options = data.options || {});
//TODO			data.options.show_bubble = "openColorbox";
			data.options.show_bubble_field = "icon";
			com.bkahlert.devel.nebula.timeline.loadJSON(data, function() {
				com.bkahlert.devel.nebula.timeline.applyDecorators([{ startDate: "2011-09-13T13:08:05+02:00", endDate: "2011-09-13T13:18:28+02:00" }]);
			});
		}
		
	});
})(jQuery);

jQuery(document).ready(function($) {
	var internal = /[?&]internal=true/.test(location.href);
	var loadDemo = /[?&]loadDemo=true/.test(location.href);
	if(!internal) $.getJSON("test.json", function(data, textStatus, jqXHR) {
		$("#cboxWrapper").click(function() { $.colorbox.close(); });
		$("input[name=demo]").click(function() { com.bkahlert.devel.nebula.timeline.openDemo(data); }).attr("disabled",false);
		$("input[name=immediateDemo]").click(function() { window.location.href = window.location.href + "?loadDemo=true"; }).attr("disabled",false);
		
		// start demo instantly
		if(loadDemo) com.bkahlert.devel.nebula.timeline.openDemo(data);
	});
	
	if(internal || loadDemo) {
		$("#container").remove();
		$("head").append($("<link rel='stylesheet' type='text/css' href='file:///Users/bkahlert/Dropbox/Development/Repositories/SUA/de.fu_berlin.imp.seqan.usability_analyzer.timeline/bin/de/fu_berlin/imp/seqan/usability_analyzer/timeline/ui/widgets/style.css' />"));
	}
});

function openColorbox(icon) {
	$.colorbox({
		html: '<img src="' + icon + '">',
		width: '70%',
		height: '90%',
		speed: 200
	});
}


















Timeline.CompactEventPainter.prototype.paintPreciseDurationEvent = function(evt, metrics, theme, highlightIndex) {
    var commonData = {
        tooltip: evt.getProperty("tooltip") || evt.getText()
    };

    var tapeData = {
        start:          evt.getStart(),
        end:            evt.getEnd(),
        color:          evt.getColor() || evt.getTextColor(),
        isInstant:      false
    };

    var iconData = {
        url: evt.getIcon()
    };
    if (iconData.url == null) {
        iconData = null;
    } else {
        iconData.width = evt.getProperty("iconWidth") || metrics.customIconWidth;
        iconData.height = evt.getProperty("iconHeight") || metrics.customIconHeight;
    }

    var labelData = {
        text:       evt.getText(),
        color:      evt.getTextColor() || evt.getColor(),
        className:  evt.getClassName()
    };

    var result = this.paintTapeIconLabel(
        evt.getLatestStart(),
        commonData,
        tapeData,
        iconData,
        labelData,
        metrics,
        theme,
        highlightIndex
    );

    var self = this;
    var clickHandler = iconData != null ?
        function(elmt, domEvt, target) {
            return self._onClickInstantEvent(result.iconElmtData.elmt, domEvt, evt);
        } :
        function(elmt, domEvt, target) {
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