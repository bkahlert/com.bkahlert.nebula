/* jshint undef: true, unused: true */
/* global joint */
/* global console */

var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.nebula = com.bkahlert.nebula || {};
com.bkahlert.jointjs = com.bkahlert.jointjs || {};
(function ($) {
    $.extend(com.bkahlert.jointjs, {

        graph: null,
        paper: null,

        initEnabled: true,

		start: function () {
			$("html").addClass("ready");
			var $window = $(window);
			
			com.bkahlert.jointjs.graph = new joint.dia.Graph();
			com.bkahlert.jointjs.paper = new joint.dia.Paper({
				el: $('body'),
				width: $window.width(),
				height: $window.height(),
				model: com.bkahlert.jointjs.graph
			});
			
			com.bkahlert.jointjs.activateLinkCreationCapability(com.bkahlert.jointjs.graph, com.bkahlert.jointjs.paper);
			com.bkahlert.jointjs.activateLinkTextChangeCapability();
			
			com.bkahlert.jointjs.createNode('sua://test', { position: { x: 10, y: 100 }, title: 'my box', content: '<ul><li>jkjk</li></ul>' });
			com.bkahlert.jointjs.createNode('sua://test2', { title: 'my box233333' });
			var linkid = com.bkahlert.jointjs.createLink(null, { id: 'sua://test' }, { id: 'sua://test2' });
			com.bkahlert.jointjs.setText(linkid, 0, 'my_label');
			com.bkahlert.jointjs.setText('sua://test2', 'content', 'XN dskjd sdkds dskdsdjks dskj ');
			console.log(com.bkahlert.jointjs.getText('sua://test2', 'title'));
			
			var internal = /[?&]internal=true/.test(location.href);
			if (!internal) {
				com.bkahlert.jointjs.openDemo();
			} else {
				com.bkahlert.jointjs.openDemo();
			}
		},

		load: function (json) {
			com.bkahlert.jointjs.graph.clear();
			com.bkahlert.jointjs.graph.fromJSON(JSON.parse(json));
			if (typeof window.loaded === 'function') { window.loaded(json); }
			return json;
		},

		save: function () {
			var json = JSON.stringify(com.bkahlert.jointjs.graph);
			if (typeof window.save === 'function') { window.save(json); }
			return json;
		},

		layout: function () {
			var graph = com.bkahlert.jointjs.graph;
			joint.layout.DirectedGraph.layout(graph, {
				setLinkVertices: false
			});
		},

        onresize: function () {
            if (com.bkahlert.jointjs.paper) {
                var $window = $(window);
                com.bkahlert.jointjs.paper.setDimensions($window.width(), $window.height());
            }
        },

       openDemo: function () {
		$('<div class="buttons"></div>').appendTo('body').css({
			position: 'absolute',
			top: 0,
			right: 0
		})
		.append($('<button>Load</a>').prop('disabled', true).click(function () {
			com.bkahlert.jointjs.load(window.saved);
		}))
		.append($('<button>Save</a>').click(function () {
			$('.buttons > :first-child').prop('disabled', false);
			window.saved = com.bkahlert.jointjs.save();
		}))
		.append($('<button>Add Node</a>').click(function () {
			com.bkahlert.jointjs.createNode();
		}))
		.append($('<button>Add Link</a>').click(function () {
			com.bkahlert.jointjs.createLink();
		}))
		.append($('<button>Layout</a>').click(function () {
			com.bkahlert.jointjs.layout();
		}));
		},
		
		createNode: function(id, attrs) {
			var config = { id: id };
			_.extend(config, attrs);
			
			var rect = new joint.shapes.html.Element(config);
			com.bkahlert.jointjs.graph.addCell(rect);
			return rect.id;
		},
		
		createLink: function(id, source, target) {
			var config = {
				source: source ? source : { x: 10, y: 10 },
				target: target ? target : { x: 100, y: 10 },
				labels: [
					{ position: 0.5, attrs: { text: { text: '' } } }
				]
			};
			if(id) _.extend(config, { id: id });
			var link = new joint.dia.Link(config).attr({
				'.marker-source': { d: 'M-3.5,0a3.5,3.5 0 1,0 7,0a3.5,3.5 0 1,0 -7,0 z' },
				'.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' }
			}).set('smooth', true);
			com.bkahlert.jointjs.graph.addCell(link);
			return link.id;
		},
		
		removeCell: function(id) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			if(cell) {
				cell.remove();
				return true;
			} else {
				return false;
			}
		},
		
		activateLinkCreationCapability: function(graph, paper) {
			var shiftKey = false;
           $(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey});
			
			paper.on('cell:pointerdblclick', 
				function(cellView, evt, x, y) {
					var bounds = { x: cellView.model.attributes.position.x, y: cellView.model.attributes.position.y, w: cellView.model.attributes.size.width, h: cellView.model.attributes.size.height };
					var id = cellView.model.id;
					
					var source = { id: id };
					var target = { x: bounds.x+bounds.w+100, y: y };
					
					if(shiftKey) {
						target = source;
						source = { x: bounds.x-100, y: y };
					}
					
					com.bkahlert.jointjs.createLink(null, source, target);
				}
			);
		},
		
		activateLinkTextChangeCapability: function() {
			$(document).on('mouseenter', '.link[model-id]', function() {
				com.bkahlert.jointjs.showTextChangePopup($(this).attr('model-id'));
			}).on('mouseleave', '.link[model-id]', function() {
				com.bkahlert.jointjs.hideTextChangePopup($(this).attr('model-id'));
			});
		},
		
		showTextChangePopup: function(id) {
			$('.popover').remove(); // TODO check for link deletions an remove popover
				
			var $el = $('[model-id=' + id + ']');
			$el.popover({
				trigger: 'manual',
				container: 'body',
				title: '',
				html: true,
				content: function() {
					return $('\
						<form class="form-inline" role="form">\
							<div class="form-group">\
								<label class="sr-only" for="linkTitle">Title</label>\
								<input type="text" class="form-control input-sm" id="linkTitle" placeholder="Title" value="' + com.bkahlert.jointjs.getText(id, 0) + '">\
							</div>\
						</form>\
						').submit(function() {
							com.bkahlert.jointjs.hideAndApplyTextChangePopup($el.attr('model-id'));
							return false;
						});
				}
			}).popover('show');
			$('#linkTitle').focus();
		},
		
		hideTextChangePopup: function(id) {
			if($('#linkTitle').length > 0) {
				var $el = $('[model-id=' + id + ']');		
				$el.popover('destroy');
			}
		},
		
		hideAndApplyTextChangePopup: function(id) {
			if($('#linkTitle').length > 0) {
				var $el = $('[model-id=' + id + ']');
				var title = $('#linkTitle').val();
				$el.popover('destroy');
				com.bkahlert.jointjs.setText(id, 0, title);
			}
		},
		
		setText: function(id, index, text) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			
			if(cell instanceof joint.dia.Link) {
				cell.label(index, { attrs: { text: { text: text } }});
				if (typeof window.__linkTitleChanged === 'function') { window.__linkTitleChanged(id, text); }
			} else {
				cell.set(index, text);
			}
		},
		
		getText: function(id, index) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			if(cell instanceof joint.dia.Link) {
				return cell.get('labels')[index].attrs.text.text;
			} else {
				return cell.get(index);
			}
		},
		
		setColor: function(id, rgb) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			cell.set('color', rgb);
		},
		
		setBackgroundColor: function(id, rgb) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			cell.set('background-color', rgb);
		},
		
		setBorderColor: function(id, rgb) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			cell.set('border-color', rgb);
		}
    });
})(jQuery);

$(window).resize(com.bkahlert.jointjs.onresize);
$(document).ready(com.bkahlert.jointjs.start);



/**
 * Custom node based on HTML
 *
 * Original sources: http://jointjs.com/tutorial/html-elements
 */

// Create a custom element.
// ------------------------

joint.shapes.html = {};
joint.shapes.html.Element = joint.shapes.basic.Rect.extend({
	defaults: joint.util.deepSupplement({
		type: 'html.Element',
		position: { x: 10, y: 10 },
		size: { width: 130, height: 30 },
		attrs: {
			rect: { stroke: 'none', 'fill-opacity': 0 }
		}
	}, joint.shapes.basic.Rect.prototype.defaults)
});

// Create a custom view for that element that displays an HTML div above it.
// -------------------------------------------------------------------------

joint.shapes.html.ElementView = joint.dia.ElementView.extend({

    template: '\
		<div class="html-element">\
			<button class="delete hide">x</button>\
			<h1>fdfd</h1>\
			<div class="content"></div>\
		</div>',

    initialize: function() {
        _.bindAll(this, 'updateBox');
        joint.dia.ElementView.prototype.initialize.apply(this, arguments);

        this.$box = $(_.template(this.template)());
        this.$box.find('.delete').on('click', _.bind(this.model.remove, this.model));
        // Update the box position whenever the underlying model changes.
        this.model.on('change', this.updateBox, this);
        // Remove the box when the model gets removed from the graph.
        this.model.on('remove', this.removeBox, this);

        this.updateBox();
    },
    render: function() {
        joint.dia.ElementView.prototype.render.apply(this, arguments);
        this.paper.$el.prepend(this.$box);
        this.updateBox();
        return this;
    },
    updateBox: function() {
		// Set the position and dimension of the box so that it covers the JointJS element.
		var bbox = this.model.getBBox();
		// Example of updating the HTML with a data stored in the cell model.
		this.$box.find('h1').html(this.model.get('title'));
		this.$box.find('.content').html(this.model.get('content'));
		var color = this.model.get('color');
		this.$box.css('color', color ? color : 'inherit');
		var backgroundColor = this.model.get('background-color');
		this.$box.css('background-color', backgroundColor ? backgroundColor : 'auto');
		var borderColor = this.model.get('border-color');
		this.$box.css('border-color', borderColor ? borderColor : 'auto');
		this.$box.css({ width: bbox.width, height: bbox.height, left: bbox.x, top: bbox.y, transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)' });
    },
    removeBox: function(evt) {
        this.$box.remove();
    }
});