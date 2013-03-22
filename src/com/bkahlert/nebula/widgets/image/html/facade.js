var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.nebula = com.bkahlert.nebula || {};
com.bkahlert.nebula.image = com.bkahlert.nebula.image || {};
(function($) {
    $.extend(com.bkahlert.nebula.image, {

        initEnabled : true,

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
            com.bkahlert.nebula.image.load("http://wallpapersinbox.files.wordpress.com/2011/07/abstract-3d-wallpaper-22.jpg");
        }
    });
})(jQuery);

$(window).resize(com.bkahlert.nebula.image.onresize);
$(document).ready(com.bkahlert.nebula.image.start);

function testFunction() {
    var test = true;
    var offset = null;
    editor.addCommand("test", {
        exec : function(editor) {
            if (test) {
                offset = $(".cke_editable").saveSelection();
                console.log(offset);
                console.log("locked");
            } else {
                $(".cke_editable").restoreSelection(offset);
            }
            test = !test;
        }
    });
}