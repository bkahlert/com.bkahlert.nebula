var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.nebula = com.bkahlert.nebula || {};
com.bkahlert.jointjs = com.bkahlert.jointjs || {};
(function($) {
    $.extend(com.bkahlert.jointjs, {
    
    	graph : null,
    	paper : null,

        initEnabled : true,

        start : function(image) {
            $("html").addClass("ready");
            $window = $(window);
        
            com.bkahlert.jointjs.graph = new joint.dia.Graph;
			com.bkahlert.jointjs.paper = new joint.dia.Paper({
			    el: $('body'),
			    width: $window.width(),
			    height: $window.height(),
			    model: com.bkahlert.jointjs.graph
			});
			
            var internal = /[?&]internal=true/.test(location.href);
            if (!internal) {
                com.bkahlert.jointjs.openDemo();
            } else {
            	com.bkahlert.jointjs.openDemo();
            }
        },
        
        load : function(json) {
            com.bkahlert.jointjs.graph.fromJSON(JSON.parse(json));
            if(typeof window['loaded'] === 'function') window['loaded'](json);
            return json;
        },
        
        save : function() {
        	var json = JSON.stringify(com.bkahlert.jointjs.graph);
        	if(typeof window['save'] === 'function') window['save'](json);
        	return json;
        },
        
        layout : function() {
			var graph = com.bkahlert.jointjs.graph;
        	joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });
		},
        
        onresize : function() {
            if(com.bkahlert.jointjs.paper) {
            	$window = $(window);
            	com.bkahlert.jointjs.paper.setDimensions($window.width(), $window.height());
            }
        },

        openDemo : function() {
        	var rect = new joint.shapes.basic.Rect({
        		id: 'uri://im/an/uri',
			    position: { x: 100, y: 30 },
			    size: { width: 100, height: 30 },
			    attrs: { rect: { fill: 'blue' }, text: { text: 'my box', fill: 'white' } }
			});
			
			var rect2 = rect.clone();
			rect2.translate(300);
			
			var link = new joint.dia.Link({
			    source: { id: rect.id },
			    target: { id: rect2.id }
			});
			
			com.bkahlert.jointjs.graph.addCells([rect, rect2, link]);
			
			$('<div class="buttons"></div>').appendTo('body').css({ position: 'absolute', top: 0, right: 0 })
				.append($('<button>Load</a>').prop('disabled', true).click(function() {
					com.bkahlert.jointjs.load(window.saved);
				}))
				.append($('<button>Save</a>').click(function() {
					com.bkahlert.jointjs.save();
				}))
				.append($('<button>Add Node</a>').click(function() {
					var rect = new joint.shapes.basic.Rect({
					    position: { x: 10, y: 10 },
					    size: { width: 100, height: 30 },
					    attrs: { rect: { fill: 'blue' }, text: { text: 'new box', fill: 'yellow' } }
					});
					
					var link = new joint.dia.Link({
					    source: { id: rect2.id },
					    target: { id: rect.id }
					});
					
					com.bkahlert.jointjs.graph.addCells([rect, link]);
				}))
				.append($('<button>Layout</a>').click(function() {
					com.bkahlert.jointjs.layout();
				}))
				.append($('<button>Log</a>').click(function() {
					alert(jQuery.fn.sortElements);
				}));

		}
    });
})(jQuery);

$(window).resize(com.bkahlert.jointjs.onresize);
$(document).ready(com.bkahlert.jointjs.start);