var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.nebula = com.bkahlert.nebula || {};
com.bkahlert.nebula.image = com.bkahlert.nebula.image || {};
(function($) {
    $.extend(com.bkahlert.nebula.image, {

        initEnabled : true,
        
        maxSize : null,

        start : function(image) {
            $("html").addClass("ready");
            $("#image").load(com.bkahlert.nebula.image.onload);
            var internal = /[?&]internal=true/.test(location.href);
            if (!internal) {
                com.bkahlert.nebula.image.openDemo();
            }
        },
        
        load : function(image) {
            $image = $("#image");
            $image.attr("src", image);
        },
        
        getOriginalSize : function() {
            $image = $("#image");
            var w = parseInt($image.get(0).naturalWidth);
            var h = parseInt($image.get(0).naturalHeight);
            return [w, h];  
        },
        
        getCurrentSize : function() {
            $image = $("#image");
            if($image.length == 0 || $image.attr("src") == "") return;
            
            var w = parseInt($image.css("width"));
            var h = parseInt($image.css("height"));
            return [w, h];  
        },
        
        limitToOriginalSize : function() {
            $("#image").css({ width: "auto", height: "auto", maxWidth: "100%", maxHeight: "100%" });
        },
        
        onload : function() {
            var originalSize = com.bkahlert.nebula.image.getOriginalSize();
            if (originalSize && window["imageLoaded"] && typeof window["imageLoaded"]) {
                window["imageLoaded"](originalSize);
            }
        },
        
        onresize : function() {
            var currentSize = com.bkahlert.nebula.image.getCurrentSize();
            if (currentSize && window["imageResized"] && typeof window["imageResized"]) {
                window["imageResized"](currentSize);
            }
        },

        openDemo : function() {
            com.bkahlert.nebula.image.load("http://www.w3.org/html/logo/downloads/HTML5_Logo_512.png");
            com.bkahlert.nebula.image.limitToOriginalSize();
        }
    });
})(jQuery);

$(window).resize(com.bkahlert.nebula.image.onresize);
$(document).ready(com.bkahlert.nebula.image.start);