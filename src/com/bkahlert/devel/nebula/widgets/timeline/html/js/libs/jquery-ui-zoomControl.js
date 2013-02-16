(function($) {
    var methods = {
        init : function(options) {
            return this.each(function() {
                $this = $(this);
                $this.addClass("zoomControl");

                var settings = {
                    orientation : "vertical",
                    range : $this.data("range") || "min",
                    min : $this.data("min") || 0,
                    max : $this.data("max") || 100,
                    value : $this.data("value") || 50
                };
                $.extend(settings, options || {});

                $slider = $("<div class='innerSlider'></div>");

                $("<button class='zoomIn'></button>").appendTo($this).button({
                    icons : {
                        primary : "ui-icon-zoomin"
                    },
                    text : false
                }).click(function(e) {
                    $slider.slider("value", $slider.slider("value") + 1);
                });

                $("<button class='zoomOut'></button>").appendTo($this).button({
                    icons : {
                        primary : "ui-icon-zoomout"
                    },
                    text : false
                }).click(function(e) {
                    $slider.slider("value", $slider.slider("value") - 1);
                });

                /*
                 * Reverse the value for all callbacks
                 */
                if (settings.max && settings.value)
                    settings.value = settings.max - settings.value;
                $.each(["create", "start", "slide", "change", "stop"], function() {
                    if (settings[this]) {
                        var oldCallback = settings[this];
                        settings[this] = function(event, ui) {
                            ui.value = $slider.slider("option", "max") - ui.value;
                            oldCallback(event, ui);
                        }
                    }
                });
                $slider.appendTo(this).slider(settings);

                $this.zoomControl("registerKeyboardBindings");
            });
        },

        registerKeyboardBindings : function() {
            return this.each(function(i, wrapper) {
                var $this = $(this);
                $(document).keydown(function(event) {
                    switch(event.which) {
                        /* normal keyboard, arrow up */
                        case 38:
                        // TODO buggy
                        /* normal keyboard, + */
                        case 171:
                        /* normal keyboard, numpad + */
                        case 107:
                        /* laptop keyboard, + */
                        case 187:
                            $this.zoomControl("zoomIn");
                            event.preventDefault();
                            event.stopPropagation();
                            break;
                        /* normal keyboard, arrow down */
                        case 40:
                        // TODO buggy
                        /* normal keyboard, - */
                        case 173:
                        /* normal keyboard, numpad - */
                        case 109:
                        /* laptop keyboard, - */
                        case 189:
                            $this.zoomControl("zoomOut");
                            event.preventDefault();
                            event.stopPropagation();
                            break;
                    }
                });
            });
        },

        zoomIn : function() {
            return this.each(function(i, wrapper) {
                var $this = $(this);
                $this.children("button.zoomIn").click();
            });
        },

        zoomOut : function() {
            return this.each(function(i, wrapper) {
                var $this = $(this);
                $this.children("button.zoomOut").click();
            });
        },

        /**
         * Sets the sliders value to the given one. Calling this function triggers events.
         * @param {Object} value
         */
        setValue : function(value) {
            return this.each(function() {
                $(this).children(".innerSlider").each(function(i, slider) {
                    var $slider = $(slider);
                    var max = $slider.slider("option", "max");
                    $slider.slider("value", max - value);
                });
            });
        },

        /**
         * Returns the value of the *first* matches control.
         */
        getValue : function() {
            var sliders = $(this).children(".innerSlider");
            var max = sliders.slider("option", "max");
            return max - sliders.slider("value");
        }
    };

    $.fn.zoomControl = function(method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if ( typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.zoomControl');
        }
    };
})(jQuery);
