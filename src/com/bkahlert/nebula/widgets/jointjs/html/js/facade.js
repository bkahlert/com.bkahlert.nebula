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
				el: $('.jointjs'),
				width: $window.width(),
				height: $window.height(),
				gridSize: 1,
				model: com.bkahlert.jointjs.graph,
				elementView: joint.shapes.html.ElementView,
				linkView: joint.shapes.LinkView
			});
			com.bkahlert.jointjs.setTitle(null);
			
			com.bkahlert.jointjs.activateZoomControls();
			com.bkahlert.jointjs.activatePanCapability(com.bkahlert.jointjs.paper);
			com.bkahlert.jointjs.activateLinkCreationCapability(com.bkahlert.jointjs.graph, com.bkahlert.jointjs.paper);
			com.bkahlert.jointjs.activateLinkAbandonCapability(com.bkahlert.jointjs.graph, com.bkahlert.jointjs.paper);
			com.bkahlert.jointjs.activateLinkTools();
			com.bkahlert.jointjs.activateSelections();
			
			var internal = /[?&]internal=true/.test(location.href);
			if (!internal) {
				com.bkahlert.jointjs.openDemo();
				com.bkahlert.jointjs.setTitle('Demo');
			} else {
			}
		},

		load: function (json) {
			com.bkahlert.jointjs.graph.clear();
			if(typeof json === 'string') json = JSON.parse(json);
			
			var title = null;
			if(json.title) {
				title = json.title;
				delete json.title;
			}
			
			var zoom = 1;
			if(json.zoom) {
				zoom = json.zoom;
				delete json.zoom;
			}
			
			var pan = { "x": 0, "y": 0 };
			if(json.pan) {
				pan = json.pan;
				delete json.pan;
			}
			
			/**
			 * JointJS stops loading links if one has an invalid source or target
			 * Source and target are replaced with a random position in this case.
			 */
			function sanityCheck(json, cell, prop) {
				if(!cell) {
					json.cells = $(json.cells).filter(function() {
						return sanityCheck(json, this, "source") && sanityCheck(json, this, "target");
					});
				} else {
					if(cell.type == "link" && (prop == "source" || prop == "target")) {
						var id = cell[prop].id;
						if(id && $(json.cells).filter(function() { return this.type != "link" && this.id == id; }).length == 0) {
							if(cell.permanent) {
								return false;
							} else {
								cell[prop] = {
									x: Math.random()*300,
									y: Math.random()*300
								}
								cell["abandoned-" + prop] = true;
							}
						}
					}
					return true;
				}
			}
			
			sanityCheck(json);
			
			try {
				com.bkahlert.jointjs.graph.fromJSON(json);
			} catch(e) {
			}
			com.bkahlert.jointjs.setTitle(title);
			com.bkahlert.jointjs.setZoom(zoom);
			com.bkahlert.jointjs.setPan(pan.x, pan.y);
			
			var json = com.bkahlert.jointjs.serialize()
			if (typeof window.loaded === 'function') { window.loaded(json); }
			return json;
		},

		save: function () {
			var json = com.bkahlert.jointjs.serialize();
			if (typeof window.save === 'function') { window.save(json); }
			return json;
		},
		
		serialize: function () {
			var json = JSON.parse(JSON.stringify(com.bkahlert.jointjs.graph));
			json.title = com.bkahlert.jointjs.getTitle();
			json.zoom = com.bkahlert.jointjs.getZoom();
			var pan = com.bkahlert.jointjs.getPan();
			json.pan = { x: pan[0], y: pan[1] };
			return JSON.stringify(json, null, "\t");
		},
		
		setEnabled: function (enabled) {
			if(enabled) {
				$('body').removeClass('disabled');
			} else {
				$('body').addClass('disabled');
			}
		},
		
		getTitle: function() {
			return $('.title').text();
		},
		
		setTitle: function(title) {
			var visible = title && title.trim() != "";
			$('.title').text(title || "").css("display", visible ? "block" : "none");
		},

		autoLayout: function () {
			var graph = com.bkahlert.jointjs.graph;
			joint.layout.DirectedGraph.layout(graph, {
				setLinkVertices: false,
				nodeSep: 0.1,
				edgeSep: 0.1,
				rankSep: 50,
				rankDir: 'TB'
			});
		},

        onresize: function () {
            if (com.bkahlert.jointjs.paper) {
                var $window = $(window);
                com.bkahlert.jointjs.paper.setDimensions($window.width(), $window.height());
            }
        },

		openDemo: function () {
			$('<div class="buttons" style="z-index: 9999999"></div>').appendTo('body').css({
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
				console.log(window.saved);
			}))
			.append($('<button>Add Node</button>').click(function () {
				com.bkahlert.jointjs.createNode();
			}))
			.append($('<button>Add Link</button>').click(function () {
				com.bkahlert.jointjs.createLink();
			}))
			.append($('<button>Layout</button>').click(function () {
				com.bkahlert.jointjs.autoLayout();
			}))
			.append($('<button>Zoom In</button>').click(function () {
				com.bkahlert.jointjs.zoomIn();
			}))
			.append($('<button>Zoom Out</button>').click(function () {
				com.bkahlert.jointjs.zoomOut();
			}))
			.append($('<button>Get Pan</button>').click(function () {
				console.log(com.bkahlert.jointjs.getPan());
			}))
			.append($('<button>Set Pan</button>').click(function () {
				com.bkahlert.jointjs.setPan(100, 100);
			}))
			.append($('<button>Log Nodes/Links</button>').click(function () {
				console.log(com.bkahlert.jointjs.getNodes());
				console.log(com.bkahlert.jointjs.getLinks());
				console.log(com.bkahlert.jointjs.getPermanentLinks());
			}))
			.append($('<button>Enable</button>').click(function () {
				com.bkahlert.jointjs.setEnabled(true);
			}))
			.append($('<button>Disable</button>').click(function () {
				com.bkahlert.jointjs.setEnabled(false);
			}))
			.append($('<button>Custom</button>').click(function () {
				var x = {"cells":[{"type":"html.Element","position":{"x":270,"y":142},"size":{"width":"242","height":"30"},"angle":"0","id":"sua://code/-9223372036854775640","content":"","title":"Offensichtliche Usability-Probleme","z":"0","color":"rgb(0, 0, 0)","background-color":"rgba(255, 102, 102, 0.27450980392156865)","border-color":"rgba(255, 48, 48, 0.39215686274509803)","attrs":{}}],"title":"New Model","zoom":"1","pan":{"x":"0","y":"0"}};
				console.log(x);
				com.bkahlert.jointjs.graph.clear();
				com.bkahlert.jointjs.graph.fromJSON(x);
			}));
			
			var a = com.bkahlert.jointjs.createNode('sua://test', { position: { x: 100, y: 300 }, title: 'my box', content: '<ul><li>jkjk</li></ul>' });
			var b = com.bkahlert.jointjs.createNode('sua://test2', { title: 'my box233333' });
			var linkid = com.bkahlert.jointjs.createPermanentLink(null, { id: 'sua://test' }, { id: 'sua://test2' });
			var c = com.bkahlert.jointjs.createNode('sua://test3', { title: 'my box233333', position: { x: 300, y: 300 },  });
			var linkid2 = com.bkahlert.jointjs.createLink(null, { id: 'sua://test3' }, { id: 'sua://test2' });
			com.bkahlert.jointjs.setText(linkid, 0, 'my_label');
			com.bkahlert.jointjs.setText('sua://test2', 'content', 'XN dskjd sdkds dskdsdjks dskj ');
			com.bkahlert.jointjs.setSize(c, 300, 100);
			console.log(com.bkahlert.jointjs.getConnectedLinks('sua://test2'));
			console.log(com.bkahlert.jointjs.getConnectedPermanentLinks('sua://test2'));
			window.setTimeout(function() {
				com.bkahlert.jointjs.setPosition(c, 500, 500);
			}, 1000);
		},
		
		getZoom: function() {
			return this.paper.getScale().sx;
		},
		
		setZoom: function(val) {
			com.bkahlert.jointjs.paper.scale(val);
		},
		
		zoomIn: function(val) {
			com.bkahlert.jointjs.setZoom(com.bkahlert.jointjs.getZoom()*1.2);
		},
		
		zoomOut: function(val) {
			com.bkahlert.jointjs.setZoom(com.bkahlert.jointjs.getZoom()*0.8);
		},
		
		getPan: function() {
			var translate = com.bkahlert.jointjs.paper.getTranslate();
			return [translate.tx, translate.ty];
		},
		
		setPan: function(x, y) {
			com.bkahlert.jointjs.paper.translate(x, y);
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
				'.marker-source': {  },
				'.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' }
			}).set('smooth', true);
			com.bkahlert.jointjs.graph.addCell(link);
			return link.id;
		},
		
		createPermanentLink: function(id, source, target) {
			var config = {
				className: 'test',
				source: source ? source : { x: 10, y: 10 },
				target: target ? target : { x: 100, y: 10 },
				labels: [
					{ position: 0.5, attrs: { text: { text: '' } } }
				]
			};
			if(id) _.extend(config, { id: id });
			var link = new joint.dia.Link(config).attr({
				'.marker-source': { d: 'M 10 0 L 0 5 L 10 10 z' },
				'.marker-target': { },
				'.connection': { 'stroke-dasharray': '1,4' }
			}).set('smooth', true).set('permanent', true);
			
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
		
		getNodes: function() {
			var nodes = [];
			_.each(com.bkahlert.jointjs.graph.getElements(), function(element) {
				nodes.push(element.id);
			});
			return nodes;
		},
		
		getLinks: function() {
			var links = [];
			_.each(com.bkahlert.jointjs.graph.getLinks(), function(link) {
				if(!link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getPermanentLinks: function() {
			var links = [];
			_.each(com.bkahlert.jointjs.graph.getLinks(), function(link) {
				if(link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getConnectedLinks: function(id) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			var links = [];
			_.each(com.bkahlert.jointjs.graph.getConnectedLinks(cell, {}), function(link) {
				if(!link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getConnectedPermanentLinks: function(id) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			var links = [];
			_.each(com.bkahlert.jointjs.graph.getConnectedLinks(cell, {}), function(link) {
				if(link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		activateZoomControls : function() {
			var shiftKey = false;
			$(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey || e.metaKey});
			
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
						com.bkahlert.jointjs.zoomIn();
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
						com.bkahlert.jointjs.zoomOut();
						event.preventDefault();
						event.stopPropagation();
						break;
				}
			});
			
			com.bkahlert.jointjs.paper.on('blank:pointerdblclick', 
				function(evt, x, y) {
					if(shiftKey) com.bkahlert.jointjs.zoomOut();
					else com.bkahlert.jointjs.zoomIn();
				}
			);
        },
        
        activatePanCapability: function(paper) {
        	paper.on('blank:pointerdown', function(e) {
        		if(e.which != 1) return;
        		com.bkahlert.jointjs.mouseX = e.offsetX;
        		com.bkahlert.jointjs.mouseY = e.offsetY;
				com.bkahlert.jointjs.mousePan($(this.viewport).parents('svg'), true);
			});
			paper.on('blank:pointerup', function() {
				com.bkahlert.jointjs.mousePan($(this.viewport).parents('svg'), false);
			});
			$(document).on('mouseleave', '.jointjs svg', function() {
				com.bkahlert.jointjs.mousePan(this, false);
			});
        },
        
        // is called if mouse is moved with a down button
        // used to pan
        mousePanTracker: function(e) {
        	var deltaX = e.offsetX - com.bkahlert.jointjs.mouseX;
        	var deltaY = e.offsetY - com.bkahlert.jointjs.mouseY;
        	
        	var scale = com.bkahlert.jointjs.paper.getScale();
        	
        	var translate = com.bkahlert.jointjs.paper.getTranslate();
        	var newX = translate.tx + deltaX/scale.sx;
        	var newY = translate.ty + deltaY/scale.sy;
			
			com.bkahlert.jointjs.paper.translate(newX, newY);
        	
        	com.bkahlert.jointjs.mouseX = e.offsetX;
        	com.bkahlert.jointjs.mouseY = e.offsetY;
        },
        
        // (de)activates mouse panning
        mousePan: function(svg, activate) {
        	$svg = $(svg);
        	if(activate) {
        		$svg.bind('mousemove', com.bkahlert.jointjs.mousePanTracker);
        		$svg.attr('class', 'grabbing');
        	} else {
        		$svg.attr('class', '');
        		$svg.unbind('mousemove', com.bkahlert.jointjs.mousePanTracker);
        	}
        },
		
		activateLinkCreationCapability: function(graph, paper) {
			var shiftKey = false;
			$(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey || e.metaKey});
			
			paper.on('cell:pointerdblclick', 
				function(cellView, evt, x, y) {
					var bounds = { x: cellView.model.attributes.position.x, y: cellView.model.attributes.position.y, w: parseInt(cellView.model.attributes.size.width), h: parseInt(cellView.model.attributes.size.height) };
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
		
		activateLinkAbandonCapability: function(graph, paper) {
			var shiftKey = false;
			$(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey || e.metaKey});
			
			graph.on('change:target', 
				function(link, pos) {
					if(link.get('abandoned-source')) {
						link.set('abandoned-source', false);
					}
					if(link.get('abandoned-target')) {
						link.set('abandoned-target', false);
					}
				}
			);
		},
		
		activateLinkTools: function() {
			$(document).on('mouseenter', '.link[model-id]:not(.permanent)', function() {
				com.bkahlert.jointjs.showTextChangePopup($(this).attr('model-id'));
			}).on('mouseleave', '.link[model-id]', function() {
				com.bkahlert.jointjs.hideTextChangePopup($(this).attr('model-id'));
			});
		},
		
		showTextChangePopup: function(id) {
			$('.popover').remove();
			
			// we use a filter to not have to deal with escaping (e.g. [model-id=abc-def] does not work because of the hyphen in the selector)
			var $el = $('[model-id]').filter(function() { return $(this).attr('model-id') == id; });
			com.bkahlert.jointjs.graph.getCell(id).on('remove', com.bkahlert.jointjs.hideTextChangePopup);
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
			if(id.id) {
				// case if called when link was removed while editing
				$('.popover').remove();
			} else if($('#linkTitle').length > 0) {
				// we use a filter to not have to deal with escaping (e.g. [model-id=abc-def] does not work because of the hyphen in the selector)
				var $el = $('[model-id]').filter(function() { return $(this).attr('model-id') == id; });
				$el.popover('destroy');
			}
		},
		
		hideAndApplyTextChangePopup: function(id) {
			if($('#linkTitle').length > 0) {
				// we use a filter to not have to deal with escaping (e.g. [model-id=abc-def] does not work because of the hyphen in the selector)
				var $el = $('[model-id]').filter(function() { return $(this).attr('model-id') == id; });
				var title = $('#linkTitle').val();
				$el.popover('destroy');
				com.bkahlert.jointjs.setText(id, 0, title);
			}
		},
		
		activateSelections: function() {
			var $d = $(document);
			if (typeof window.__cellHoveredOver === 'function') {
				$d.on('mouseenter', '[model-id]', function() {
					window.__cellHoveredOver($(this).attr('model-id'));
				});
			}
			if (typeof window.__cellHoveredOut === 'function') {
				$d.on('mouseleave', '[model-id]', function() {
					window.__cellHoveredOut($(this).attr('model-id'));
				});
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
		},
		
		setPosition: function(id, x, y) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			cell.set('position', { x: x, y: y });
		},
		
		setSize: function(id, width, height) {
			var cell = com.bkahlert.jointjs.graph.getCell(id);
			cell.set('size', { width: width, height: height });
		}
    });
})(jQuery);

$(window).resize(com.bkahlert.jointjs.onresize);
$(document).ready(com.bkahlert.jointjs.start);







joint.shapes.LinkView = joint.dia.LinkView.extend({

	className: function() {
		var classes = ['link'];
		if(this.model.get('permanent')) classes.push('permanent');
		return classes.join(' ');
    },
	
	renderTools: function() {

        if (!this._V.linkTools) return this;

        // Tools are a group of clickable elements that manipulate the whole link.
        // A good example of this is the remove tool that removes the whole link.
        // Tools appear after hovering the link close to the `source` element/point of the link
        // but are offset a bit so that they don't cover the `marker-arrowhead`.

        var $tools = $(this._V.linkTools.node).empty();
        var toolTemplate = _.template(this.model.get('toolMarkup') || this.model.toolMarkup);
        var tool = V(toolTemplate());

        $tools.append(tool.node);

        // Cache the tool node so that the `updateToolsPosition()` can update the tool position quickly.
        this._toolCache = tool;

        return this;
    },
    
    initialize: function() {
        _.bindAll(this, 'updateBox');
        joint.dia.LinkView.prototype.initialize.apply(this, arguments);
        this.model.on('change', this.updateBox, this);
        this.updateBox();
    },
    
    updateBox: function() {
		if(this.model.get('abandoned-source')) {
			$(this.el).attr('abandoned-source', true);
		} else {
			$(this.el).attr('abandoned-source', null);
		}
		
		if(this.model.get('abandoned-target')) {
			$(this.el).attr('abandoned-target', true);
		} else {
			$(this.el).attr('abandoned-target', null);
		}
    },

});


// own function
joint.dia.Paper.prototype.getScale = function() {
	var transformAttr = V(this.viewport).attr('transform') || '';
			
	var scale;
	var scaleMatch = transformAttr.match(/scale\((.*)\)/);
	if (scaleMatch) {
		scale = scaleMatch[1].split(',');
	}
	var sx = (scale && scale[0]) ? parseFloat(scale[0]) : 1;
	var sy = (scale && scale[1]) ? parseFloat(scale[1]) : sx;
	
	return { sx: sx, sy:sy };
}

joint.dia.Paper.prototype.oldScale = joint.dia.Paper.prototype.scale;
joint.dia.Paper.prototype.scale = function(sx, sy, ox, oy) {
	var translate = this.getTranslate();
	this.oldScale(sx, sy, ox, oy);
	var scale = this.getScale();
	$(this.viewport).attr('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + translate.tx + ', ' + translate.ty + ')');
	this.$el.find('.html-view').css('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + translate.tx + 'px, ' + translate.ty + 'px)');
}

joint.dia.Paper.prototype.getTranslate = function() {
	var transformAttr = V(this.viewport).attr('transform') || '';
			
	var translate;
	var translateMatch = transformAttr.match(/translate\((.*)\)/);
	if (translateMatch) {
		translate = translateMatch[1].split(',');
	}
	var tx = (translate && translate[0]) ? parseFloat(translate[0]) : 0;
	var ty = (translate && translate[1]) ? parseFloat(translate[1]) : tx;
	
	return { tx: tx, ty: ty };
}
        
joint.dia.Paper.prototype.translate = function(tx, ty) {
	var scale = com.bkahlert.jointjs.paper.getScale();
	$(this.viewport).attr('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + tx + ', ' + ty + ')');
	this.$el.find('.html-view').css('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + tx + 'px, ' + ty + 'px)');
}




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
			<h1>Title</h1>\
			<div class="content"></div>\
		</div>',

    initialize: function() {
        _.bindAll(this, 'updateBox');
        joint.dia.ElementView.prototype.initialize.apply(this, arguments);

        this.$box = $(_.template(this.template)());
        this.$box.find('.delete').on('click', _.bind(this.model.remove, this.model));
        // Update the box position whenever the underlying model changes.
        this.model.on('change', this.updateBox, this);
		
		// TODO get paper that shows this element instead of using a singleton (for some reason this.paper seems to exists but can't be accessed)
		com.bkahlert.jointjs.paper.on('scale', this.updateBox, this);
		
        // Remove the box when the model gets removed from the graph.
        this.model.on('remove', this.removeBox, this);

        this.updateBox();
    },
    render: function() {
        joint.dia.ElementView.prototype.render.apply(this, arguments);
		
		var $c = this.paper.$el.find('.html-view');
		if($c.length == 0) $c = $('<div class="html-view"></div>').prependTo(this.paper.$el);
        $c.prepend(this.$box);
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
		
		var transform = 'rotate(' + (this.model.get('angle') || 0) + 'deg)';
		this.$box.css({ width: bbox.width, height: bbox.height, left: bbox.x, top: bbox.y, transform: transform });
    },
    removeBox: function(evt) {
        this.$box.remove();
    }
});