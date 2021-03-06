/* jshint undef: true, unused: true */
/* global joint */
/* global console */

// http://stackoverflow.com/questions/901115/how-can-i-get-query-string-values-in-javascript
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function addClassNames(classNames, add) {
	if(!_.isArray(add)) add = add ? [add] : [];
	return _.union(classNames.split(/ +/), _.toArray(add)).join(' ');
}

function removeClassNames(classNames, remove) {
	if(!_.isArray(remove)) remove = remove ? [remove] : [];
	return _.filter(classNames.split(/ +/), function(className) { return !_.contains(remove, className); }).join(' ');
}

if('aa bbb xxxx' != addClassNames('aa bbb', 'xxxx')) throw "Assertion Error";
if('aa bbb xxxx yyyyy' != addClassNames('aa   bbb', ['xxxx', 'yyyyy', 'aa'])) throw "Assertion Error";
if('a bb ccc' != removeClassNames('a bb   ccc dddd', 'dddd')) throw "Assertion Error";
if('bb dddd' != removeClassNames('a bb ccc  dddd', ['a', 'ccc', 'xxxxx'])) throw "Assertion Error";



var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.nebula = com.bkahlert.nebula || {};
com.bkahlert.nebula.jointjs = com.bkahlert.nebula.jointjs || {};
(function ($) {

    $.extend(com.bkahlert.nebula.jointjs, {

        graph: null,
        paper: null,

        initEnabled: true,

		start: function () {
			$("html").addClass("ready");
			var $window = $(window);
			
			com.bkahlert.nebula.jointjs.graph = new joint.dia.Graph();
			com.bkahlert.nebula.jointjs.paper = new joint.dia.Paper({
				el: $('.jointjs'),
				width: $window.width(),
				height: $window.height(),
				gridSize: 1,
				model: com.bkahlert.nebula.jointjs.graph,
				elementView: joint.shapes.html.ElementView,
				linkView: joint.shapes.LinkView
			});
			com.bkahlert.nebula.jointjs.setTitle(null);
			
			var myAdjustVertices = _.partial(adjustVertices, com.bkahlert.nebula.jointjs.graph);
			com.bkahlert.nebula.jointjs.graph.on('add remove change:source change:target', myAdjustVertices);
			com.bkahlert.nebula.jointjs.paper.on('cell:pointerup', myAdjustVertices);
			
			com.bkahlert.nebula.jointjs.activateZoomControls();
			com.bkahlert.nebula.jointjs.activatePanCapability(com.bkahlert.nebula.jointjs.paper);
			com.bkahlert.nebula.jointjs.activateLinkCreationCapability(com.bkahlert.nebula.jointjs.graph, com.bkahlert.nebula.jointjs.paper);
			com.bkahlert.nebula.jointjs.activateLinkAbandonCapability(com.bkahlert.nebula.jointjs.graph, com.bkahlert.nebula.jointjs.paper);
			com.bkahlert.nebula.jointjs.activateLinkTools();
			com.bkahlert.nebula.jointjs.activateSelections();
			com.bkahlert.nebula.jointjs.activateEventForwarding();
			com.bkahlert.nebula.jointjs.activateModificationNotification();
			
			var internal = /[?&]internal=true/.test(location.href);
			if (!internal) {
				com.bkahlert.nebula.jointjs.openDemo();
				com.bkahlert.nebula.jointjs.setTitle('Demo');
			} else {
			}
		},

		load: function (json) {
			com.bkahlert.nebula.jointjs.graph.clear();
			if(typeof json === 'string') json = JSON.parse(json);
			
			var title = null;
			if(json.title) {
				title = json.title;
				delete json.title;
			}
			
			com.bkahlert.nebula.jointjs.data  = null;
			if(json.data) {
				com.bkahlert.nebula.jointjs.data = json.data;
				delete json.data;
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
				com.bkahlert.nebula.jointjs.graph.fromJSON(json);
			} catch(e) {
			}
			com.bkahlert.nebula.jointjs.setTitle(title);
			com.bkahlert.nebula.jointjs.paper.setZoom(zoom);
			com.bkahlert.nebula.jointjs.paper.setPan(pan.x, pan.y);
			
			var json = com.bkahlert.nebula.jointjs.serialize()
			if (typeof window.loaded === 'function') { window.loaded(json); }
			return json;
		},

		save: function () {
			var json = com.bkahlert.nebula.jointjs.serialize();
			if (typeof window.save === 'function') { window.save(json); }
			return json;
		},
		
		serialize: function () {
			var json = JSON.parse(JSON.stringify(com.bkahlert.nebula.jointjs.graph));

			// remove temporary states
			_.each(json.cells, function(cell) {
				delete cell.highlighted;
				delete cell.selected;
				delete cell.focused;
			});
			
			json.title = com.bkahlert.nebula.jointjs.getTitle();
			json.data = com.bkahlert.nebula.jointjs.data || {};
			json.zoom = com.bkahlert.nebula.jointjs.paper.getZoom();
			var pan = com.bkahlert.nebula.jointjs.paper.getPan();
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
			com.bkahlert.nebula.jointjs.graph.trigger('change:title');
		},
		
		getData: function(key) {
			if(com.bkahlert.nebula.jointjs.data && com.bkahlert.nebula.jointjs.data[key]) {
				return com.bkahlert.nebula.jointjs.data[key];
			}
			return null;
		},
		
		setData: function(key, value) {
			if(!com.bkahlert.nebula.jointjs.data) {
				com.bkahlert.nebula.jointjs.data = {};
			}
			com.bkahlert.nebula.jointjs.data[key] = value;
		},

		autoLayout: function () {
			var oldBounds = com.bkahlert.nebula.jointjs.getBoundingBox();
			var graph = com.bkahlert.nebula.jointjs.graph;
			joint.layout.DirectedGraph.layout(graph, {
				setLinkVertices: false,
				nodeSep: 150,
				edgeSep: 150,
				rankSep: 50,
				rankDir: 'LR'
			});
			var newBounds = com.bkahlert.nebula.jointjs.getBoundingBox();
			var zoom = com.bkahlert.nebula.jointjs.paper.getZoom();
			var shift = [
				oldBounds[0]+(oldBounds[2]-newBounds[2])/2,
				oldBounds[1]+(oldBounds[3]-newBounds[3])/2];
			com.bkahlert.nebula.jointjs.shiftBy(shift[0], shift[1]);
		},

		openDemo: function () {
			window.__modified = function(html) {
				console.log('modified: ' + html.length);
			}
		
			$('<div class="buttons" style="z-index: 9999999"></div>').appendTo('body').css({
				position: 'absolute',
				top: 0,
				right: 0,
				opacity: 0.5
			})
			.append($('<button>Load</a>').prop('disabled', true).click(function () {
				com.bkahlert.nebula.jointjs.load(window.saved);
			}))
			.append($('<button>Save</a>').click(function () {
				$('.buttons > :first-child').prop('disabled', false);
				window.saved = com.bkahlert.nebula.jointjs.save();
				console.log(window.saved);
			}))
			.append($('<button>Add Node</button>').click(function () {
				com.bkahlert.nebula.jointjs.createNode();
			}))
			.append($('<button>Add Link</button>').click(function () {
				com.bkahlert.nebula.jointjs.createLink();
			}))
			.append($('<button>Layout</button>').click(function () {
				com.bkahlert.nebula.jointjs.autoLayout();
			}))
			.append($('<button>Zoom In</button>').click(function () {
				com.bkahlert.nebula.jointjs.paper.zoomIn();
			}))
			.append($('<button>Zoom Out</button>').click(function () {
				com.bkahlert.nebula.jointjs.paper.zoomOut();
			}))
			.append($('<button>Get Pan</button>').click(function () {
				console.log(com.bkahlert.nebula.jointjs.paper.getPan());
			}))
			.append($('<button>Set Pan</button>').click(function () {
				com.bkahlert.nebula.jointjs.paper.setPan(100, 100);
			}))
			.append($('<button>Shift By(20, 20)</button>').click(function () {
				com.bkahlert.nebula.jointjs.shiftBy(20, 20);
			}))
			.append($('<button>Bounding Box</button>').click(function () {
				var bounds = com.bkahlert.nebula.jointjs.getBoundingBox();
				var pan = com.bkahlert.nebula.jointjs.paper.getPan();
				var zoom = com.bkahlert.nebula.jointjs.paper.getZoom();
				var render = {
					left: (bounds[0]+pan[0])*zoom,
					top: (bounds[1]+pan[1])*zoom,
					width: bounds[2]*zoom,
					height: bounds[3]*zoom
				 }
				$('.bounding-box').remove();
				$('<div class="bounding-box"></div>').css({ position: 'absolute', border: '1px solid #f00', left: render.left, top: render.top, width: render.width, height: render.height }).prependTo('body').delay(500).fadeOut();
			}))
			.append($('<button>Center Fit</button>').click(function () {
				com.bkahlert.nebula.jointjs.fitOnScreen();
			}))
			.append($('<button>Log Nodes/Links</button>').click(function () {
				console.log(com.bkahlert.nebula.jointjs.getNodes());
				console.log(com.bkahlert.nebula.jointjs.getLinks());
				console.log(com.bkahlert.nebula.jointjs.getPermanentLinks());
			}))
			.append($('<button>Enable</button>').click(function () {
				com.bkahlert.nebula.jointjs.setEnabled(true);
			}))
			.append($('<button>Disable</button>').click(function () {
				com.bkahlert.nebula.jointjs.setEnabled(false);
			}))
			.append($('<button>Highlight</button>').click(function () {
				com.bkahlert.nebula.jointjs.highlight(['apiua://test', linkid]);
			}))
			.append($('<button>De-Highlight</button>').click(function () {
				com.bkahlert.nebula.jointjs.highlight();
			}))
			.append($('<button>Get Selection</button>').click(function () {
				console.log(com.bkahlert.nebula.jointjs.getSelection());
			}))
			.append($('<button>Get Focus</button>').click(function () {
				console.log(com.bkahlert.nebula.jointjs.getFocus());
			}))
			.append($('<button>Custom Class</button>').click(function () {
				com.bkahlert.nebula.jointjs.addCustomClasses(['apiua://test', linkid, linkid2], 'debugCustomClass');
				window.setTimeout(function() { com.bkahlert.nebula.jointjs.removeCustomClasses(['apiua://test', linkid, linkid2], 'debugCustomClass'); }, 2000);
			}))
			.append($('<button>Set Data</button>').click(function () {
				com.bkahlert.nebula.jointjs.setData('test', Math.random().toString(36).substring(7));
			}))
			.append($('<button>Get Data</button>').click(function () {
				alert(com.bkahlert.nebula.jointjs.getData('test'));
			}))
			.append($('<button>Custom</button>').click(function () {
				com.bkahlert.nebula.jointjs.load({"cells":[{"type":"html.Element","position":{"x":-81.5,"y":-192},"size":{"width":245,"height":28},"angle":0,"id":"apiua://code/-9223372036854775280","title":"Fehlende Funktionskategorisierung","content":"","z":0,"customClasses":[],"color":"rgb(0, 0, 0)","background-color":"rgba(57, 200, 123, 0.27450980392156865)","border-color":"rgba(47, 171, 104, 0.39215686274509803)","attrs":{}},{"type":"html.Element","position":{"x":248.5,"y":-192},"size":{"width":230,"height":28},"angle":0,"id":"apiua://code/-9223372036854775279","title":"Funktionszweckunerkennbarkeit","content":"","z":1,"customClasses":[],"color":"rgb(0, 0, 0)","background-color":"rgba(200, 202, 54, 0.27450980392156865)","attrs":{}},{"type":"html.Element","position":{"x":-303.5,"y":-103},"size":{"width":145,"height":28},"angle":0,"id":"apiua://code/-9223372036854775579","title":"Global Interface","content":"","z":2,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":-54,"y":-14},"size":{"width":190,"height":28},"angle":0,"id":"apiua://code/-9223372036854775277","title":"2 Funktionen = 2 Namen","content":"","z":3,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":-68.5,"y":164},"size":{"width":219,"height":28},"angle":0,"id":"apiua://code/-9223372036854775633","title":"Inkonsistenzen bzgl. STD/STL","content":"","z":4,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":-536.5,"y":-103},"size":{"width":183,"height":28},"angle":0,"id":"apiua://code/-9223372036854775515","title":"Template Programming","content":"","z":5,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":-108.5,"y":342},"size":{"width":299,"height":28},"angle":0,"id":"apiua://code/-9223372036854775577","title":"Fehlende Dokumentation der Rückgabetypen","content":"","z":6,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":240.5,"y":-14},"size":{"width":246,"height":28},"angle":0,"id":"apiua://code/-9223372036854775544","title":"Identifikation relevanter Funktionen","content":"","z":7,"customClasses":[],"attrs":{}},{"type":"link","source":{"id":"apiua://code/-9223372036854775579"},"target":{"id":"apiua://code/-9223372036854775280"},"labels":[{"position":0.5,"attrs":{"text":{"text":"erhöht Eintrittwahrscheinlichkeit\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/bc471l5bdmgjsbp92ljc2372aoil9olr","smooth":true,"z":8,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775280"},"target":{"id":"apiua://code/-9223372036854775279"},"labels":[{"position":0.5,"attrs":{"text":{"text":"bedingt2222\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/koak8ole0n4vbudg45f8kar682o3e9hd","smooth":true,"z":9,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775577"},"target":{"id":"apiua://code/-9223372036854775279"},"labels":[{"position":0.5,"attrs":{"text":{"text":"verstärkt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/3shsibj2mp5kbknqqbcsg0fksuej8l4l","smooth":true,"z":10,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775515"},"target":{"id":"apiua://code/-9223372036854775579"},"labels":[{"position":0.5,"attrs":{"text":{"text":"begingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/q7fgboeh34qflo84sa5bvlr3asiglv7s","smooth":true,"z":11,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775579"},"target":{"id":"apiua://code/-9223372036854775277"},"labels":[{"position":0.5,"attrs":{"text":{"text":"begingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/c95pfuvinhva0vaeqmbk4p48kpcb37im","smooth":true,"z":12,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775579"},"target":{"id":"apiua://code/-9223372036854775633"},"labels":[{"position":0.5,"attrs":{"text":{"text":"verursacht\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/7fu79qaas2p0m5qkp2a9160agfiotm6j","smooth":true,"z":13,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"html.Element","position":{"x":536.5,"y":-14},"size":{"width":345,"height":28},"angle":0,"id":"apiua://code/-9223372036854775455","title":"Programmentwicklung (Unmöglich, Langsam, Schnell)","content":"","z":14,"attrs":{}},{"type":"link","source":{"id":"apiua://code/-9223372036854775280"},"target":{"id":"apiua://code/-9223372036854775544"},"labels":[{"position":0.5,"attrs":{"text":{"text":"begingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/bnukj71m5n1lejph7g3vgng83cv6qbeo","smooth":true,"z":15,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775544"},"target":{"id":"apiua://code/-9223372036854775455"},"labels":[{"position":0.5,"attrs":{"text":{"text":"bedingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/s8n6bibe20mp3s27aej9vi1pdffmlnhl","smooth":true,"z":16,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}}],"title":"Fehlende Funktionskategorisierung","data":{"origin":"apiua://code/-9223372036854775280"},"zoom":1,"pan":{"x":0,"y":0}});
			}))
			.append($('<button>Custom2</button>').click(function () {
				com.bkahlert.nebula.jointjs.load({"cells":[{"type":"html.Element","position":{"x":205,"y":-89},"size":{"width":245,"height":28},"angle":0,"id":"apiua://code/-9223372036854775280","title":"Fehlende Funktionskategorisierung","content":"","z":0,"customClasses":[],"color":"rgb(0, 0, 0)","background-color":"rgba(57, 200, 123, 0.27450980392156865)","border-color":"rgba(47, 171, 104, 0.39215686274509803)","attrs":{}},{"type":"html.Element","position":{"x":527,"y":0},"size":{"width":230,"height":28},"angle":0,"id":"apiua://code/-9223372036854775279","title":"Funktionszweckunerkennbarkeit","content":"","z":1,"customClasses":[],"color":"rgb(0, 0, 0)","attrs":{}},{"type":"html.Element","position":{"x":-17,"y":-89},"size":{"width":145,"height":28},"angle":0,"id":"apiua://code/-9223372036854775579","title":"Global Interface","content":"","z":2,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":232.5,"y":89},"size":{"width":190,"height":28},"angle":0,"id":"apiua://code/-9223372036854775277","title":"2 Funktionen = 2 Namen","content":"","z":3,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":218,"y":-267},"size":{"width":219,"height":28},"angle":0,"id":"apiua://code/-9223372036854775633","title":"Inkonsistenzen bzgl. STD/STL","content":"","z":4,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":-331,"y":-89},"size":{"width":183,"height":28},"angle":0,"id":"apiua://code/-9223372036854775515","title":"Template Programming","content":"","z":5,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":178,"y":267},"size":{"width":299,"height":28},"angle":0,"id":"apiua://code/-9223372036854775577","title":"Fehlende Dokumentation der Rückgabetypen","content":"","z":6,"customClasses":[],"attrs":{}},{"type":"html.Element","position":{"x":-362.5,"y":89},"size":{"width":246,"height":28},"angle":0,"id":"apiua://code/-9223372036854775544","title":"Identifikation relevanter Funktionen","content":"","z":7,"customClasses":[],"attrs":{}},{"type":"link","source":{"id":"apiua://code/-9223372036854775579"},"target":{"id":"apiua://code/-9223372036854775633"},"labels":[{"position":0.5,"attrs":{"text":{"text":"verursacht\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/7fu79qaas2p0m5qkp2a9160agfiotm6j","smooth":true,"z":8,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775280"},"target":{"id":"apiua://code/-9223372036854775279"},"labels":[{"position":0.5,"attrs":{"text":{"text":"bedingt2222\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/koak8ole0n4vbudg45f8kar682o3e9hd","smooth":true,"z":9,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775577"},"target":{"id":"apiua://code/-9223372036854775279"},"labels":[{"position":0.5,"attrs":{"text":{"text":"verstärkt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/3shsibj2mp5kbknqqbcsg0fksuej8l4l","smooth":true,"z":10,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775579"},"target":{"id":"apiua://code/-9223372036854775280"},"labels":[{"position":0.5,"attrs":{"text":{"text":"erhöht Eintrittwahrscheinlichkeit\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/bc471l5bdmgjsbp92ljc2372aoil9olr","smooth":true,"z":11,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775515"},"target":{"id":"apiua://code/-9223372036854775579"},"labels":[{"position":0.5,"attrs":{"text":{"text":"begingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/q7fgboeh34qflo84sa5bvlr3asiglv7s","smooth":true,"z":12,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775579"},"target":{"id":"apiua://code/-9223372036854775277"},"labels":[{"position":0.5,"attrs":{"text":{"text":"begingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/c95pfuvinhva0vaeqmbk4p48kpcb37im","smooth":true,"z":13,"vertices":[],"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"html.Element","position":{"x":-412,"y":267},"size":{"width":345,"height":28},"angle":0,"id":"apiua://code/-9223372036854775455","title":"Programmentwicklung (Unmöglich, Langsam, Schnell)","content":"","z":14,"attrs":{}},{"type":"link","source":{"id":"apiua://code/-9223372036854775544"},"target":{"id":"apiua://code/-9223372036854775455"},"labels":[{"position":0.5,"attrs":{"text":{"text":"bedingt\nTODO Anzahl direkte und indirekte Groundings anzeigen"}}}],"id":"apiua://relation/s8n6bibe20mp3s27aej9vi1pdffmlnhl","smooth":true,"z":15,"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}},{"type":"link","source":{"id":"apiua://code/-9223372036854775280"},"target":{"id":"apiua://code/-9223372036854775544"},"labels":[{"position":0.5,"attrs":{"text":{"text":""}}}],"id":"apiua://relation/bnukj71m5n1lejph7g3vgng83cv6qbeo","smooth":true,"z":16,"attrs":{".marker-target":{"d":"M 10 0 L 0 5 L 10 10 z"}}}],"title":"Fehlende Funktionskategorisierung","data":{"origin":"apiua://code/-9223372036854775280"},"zoom":1,"pan":{"x":0,"y":0}});
			}));
			
			var a = com.bkahlert.nebula.jointjs.createNode('apiua://test', { position: { x: 100, y: 300 }, title: 'my box', content: '<ul><li>jkjk</li></ul>' });
			var b = com.bkahlert.nebula.jointjs.createNode('apiua://test2', { title: 'my box233333' });
			var linkid = com.bkahlert.nebula.jointjs.createPermanentLink(null, { id: 'apiua://test' }, { id: 'apiua://test2' });
			var c = com.bkahlert.nebula.jointjs.createNode('apiua://test3', { title: 'my box233333', position: { x: 300, y: 300 },  });
			var linkid2 = com.bkahlert.nebula.jointjs.createLink(null, { id: 'apiua://test3' }, { id: 'apiua://test2' });
			com.bkahlert.nebula.jointjs.setText(linkid, 0, 'my_label');
			com.bkahlert.nebula.jointjs.setText('apiua://test2', 'content', 'XN dskjd sdkds dskdsdjks dskj ');
			com.bkahlert.nebula.jointjs.setSize(c, 300, 100);
			
			com.bkahlert.nebula.jointjs.highlight(['apiua://test', 'apiua://test3', linkid]);
			
			console.log(com.bkahlert.nebula.jointjs.getConnectedLinks('apiua://test2'));
			console.log(com.bkahlert.nebula.jointjs.getConnectedPermanentLinks('apiua://test2'));
			window.setTimeout(function() {
				com.bkahlert.nebula.jointjs.setPosition(c, 500, 500);
			}, 1000);
		},
		
		fitOnScreen: function() {
			// (c) http://selbie.wordpress.com/2011/01/23/scale-crop-and-center-an-image-with-correct-aspect-ratio-in-html-and-javascript/
			function ScaleImage(srcwidth, srcheight, targetwidth, targetheight, fLetterBox) {
			    var result = { width: 0, height: 0, fScaleToTargetWidth: true };
			
			    if ((srcwidth <= 0) || (srcheight <= 0) || (targetwidth <= 0) || (targetheight <= 0)) {
			        return result;
			    }
			
			    // scale to the target width
			    var scaleX1 = targetwidth;
			    var scaleY1 = (srcheight * targetwidth) / srcwidth;
			
			    // scale to the target height
			    var scaleX2 = (srcwidth * targetheight) / srcheight;
			    var scaleY2 = targetheight;
			
			    // now figure out which one we should use
			    var fScaleOnWidth = (scaleX2 > targetwidth);
			    if (fScaleOnWidth) {
			        fScaleOnWidth = fLetterBox;
			    }
			    else {
			       fScaleOnWidth = !fLetterBox;
			    }
			
			    if (fScaleOnWidth) {
			        result.width = Math.floor(scaleX1);
			        result.height = Math.floor(scaleY1);
			        result.fScaleToTargetWidth = true;
			    }
			    else {
			        result.width = Math.floor(scaleX2);
			        result.height = Math.floor(scaleY2);
			        result.fScaleToTargetWidth = false;
			    }
			    result.left = Math.floor((targetwidth - result.width) / 2);
			    result.top = Math.floor((targetheight - result.height) / 2);
			
			    return result;
			}
			
			var bounds = com.bkahlert.nebula.jointjs.getBoundingBox();
			
			var width = $(window).width();
			var height = $(window).height();
			var margin = 10;
			var margins = [ margin, margin, margin + $('.title').height(), margin ];
			
			var scaledBounds = ScaleImage(bounds[2], bounds[3], width-margins[1]-margins[3], height-margins[0]-margins[2], true);
			var wRatio = bounds[2]/(width-margins[1]-margins[3]);
			var hRatio = bounds[3]/(height-margins[0]-margins[2]);
			var zoom;
			if(wRatio > hRatio) {
				zoom = 1/wRatio;
			} else {
				zoom = 1/hRatio;
			}
			
			com.bkahlert.nebula.jointjs.paper.setZoom(zoom, [0,0]);

			//com.bkahlert.nebula.jointjs.shiftBy(-bounds[0], -bounds[1]);
			//com.bkahlert.nebula.jointjs.paper.setPan((width-margins[1]-scaledBounds.width)/2/zoom,(height-margins[2]-scaledBounds.height)/2/zoom);
			// ^ shiftBy may hang because of a SVG bug in Safari calling SVGPathElement.getTotalLength()
			// therefor we put it into the pan to not provoke this fucking problem
			com.bkahlert.nebula.jointjs.paper.setPan((width-margins[1]-scaledBounds.width)/2/zoom - bounds[0],(height-margins[2]-scaledBounds.height)/2/zoom - bounds[1]);
		},
		
		shiftBy: function(byX, byY) {
			_.each(com.bkahlert.nebula.jointjs.graph.getElements(), function(element) {
				var pos = element.get('position');
				element.set('position', { x: pos.x+byX, y: pos.y+byY });
			});
			_.each(com.bkahlert.nebula.jointjs.graph.getLinks(), function(link) {
				var vertices = [];
				_.each(link.get('vertices'), function(vertice) {
					vertices.push({ x: vertice.x+byX, y: vertice.y+byY });
				});
				link.set('vertices', vertices);
			});
		},
		
		shiftTo: function(x, y) {
			var bounds = com.bkahlert.nebula.jointjs.getBoundingBox();
			com.bkahlert.nebula.jointjs.shiftBy(x-bounds[0], y-bounds[1]);
		},
		
		// bounding regarding (0,0)
		getBoundingBox: function() {
			var bounds = {
				left: Number.POSITIVE_INFINITY,
				top: Number.POSITIVE_INFINITY,
				right: Number.NEGATIVE_INFINITY,
				bottom: Number.NEGATIVE_INFINITY
			};
			_.each(com.bkahlert.nebula.jointjs.graph.getElements(), function(element) {
				var pos = element.get('position');
				var size = element.get('size');
				
				var offset = {
					left: pos.x,
					top: pos.y,
					right: pos.x+size.width,
					bottom: pos.y+size.height
				};
				 
				if (offset.left < bounds.left)
				bounds.left = offset.left;
				 
				if (offset.top < bounds.top)
				bounds.top = offset.top;
				 
				if (offset.right > bounds.right)
				bounds.right = offset.right;
				 
				if (offset.bottom > bounds.bottom)
				bounds.bottom = offset.bottom;
				
			});
			return [ bounds.left, bounds.top, (bounds.right-bounds.left), (bounds.bottom-bounds.top) ];
		},
		
		createNode: function(id, attrs) {
			var config = { id: id };
			_.extend(config, attrs);
			
			var rect = new joint.shapes.html.Element(config);
			com.bkahlert.nebula.jointjs.graph.addCell(rect);
			return rect.id;
		},
		
		createLink: function(id, source, target) {
			if(!id) id = getParameterByName('linkCreationPrefix') + joint.util.uuid();
			
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
			com.bkahlert.nebula.jointjs.graph.addCell(link);
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
			
			com.bkahlert.nebula.jointjs.graph.addCell(link);
			
			return link.id;			
		},
		
		removeCell: function(id) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			var rt = com.bkahlert.nebula.jointjs.getCell(id);
			if(cell) {
				cell.remove();
				return rt;
			} else {
				return null;
			}
		},
		
		getNodes: function() {
			var nodes = [];
			_.each(com.bkahlert.nebula.jointjs.graph.getElements(), function(element) {
				nodes.push(element.id);
			});
			return nodes;
		},
		
		getLinks: function() {
			var links = [];
			_.each(com.bkahlert.nebula.jointjs.graph.getLinks(), function(link) {
				if(!link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getPermanentLinks: function() {
			var links = [];
			_.each(com.bkahlert.nebula.jointjs.graph.getLinks(), function(link) {
				if(link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getConnectedLinks: function(id) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			var links = [];
			_.each(com.bkahlert.nebula.jointjs.graph.getConnectedLinks(cell, {}), function(link) {
				if(!link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getConnectedPermanentLinks: function(id) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			var links = [];
			if(cell) {
				_.each(com.bkahlert.nebula.jointjs.graph.getConnectedLinks(cell, {}), function(link) {
					if(link.get('permanent')) links.push(link.id);
				});
			}
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
						com.bkahlert.nebula.jointjs.paper.zoomIn();
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
						com.bkahlert.nebula.jointjs.paper.zoomOut();
						event.preventDefault();
						event.stopPropagation();
						break;
				}
			});
			
			$(document).mousemove(function(event) {
		        com.bkahlert.nebula.jointjs.mousePosition = [ event.pageX, event.pageY ];
		    });
		    $(document).blur(function(event) {
		        com.bkahlert.nebula.jointjs.mousePosition = null;
		    });
			
			com.bkahlert.nebula.jointjs.paper.on('blank:pointerdblclick', 
				function(evt, x, y) {
					if(shiftKey) com.bkahlert.nebula.jointjs.paper.zoomOut();
					else com.bkahlert.nebula.jointjs.paper.zoomIn();
				}
			);
        },
        
        activatePanCapability: function(paper) {
        	paper.on('blank:pointerdown', function(e) {
        		if(e.which != 1) return;
        		com.bkahlert.nebula.jointjs.mouseX = e.offsetX;
        		com.bkahlert.nebula.jointjs.mouseY = e.offsetY;
				com.bkahlert.nebula.jointjs.mousePan($(this.viewport).parents('svg'), true);
			});
			paper.on('blank:pointerup', function() {
				com.bkahlert.nebula.jointjs.mousePan($(this.viewport).parents('svg'), false);
			});
			$(document).on('mouseleave', '.jointjs svg', function() {
				com.bkahlert.nebula.jointjs.mousePan(this, false);
			});
        },
        
        // is called if mouse is moved with a down button
        // used to pan
        mousePanTracker: function(e) {
        	var deltaX = e.offsetX - com.bkahlert.nebula.jointjs.mouseX;
        	var deltaY = e.offsetY - com.bkahlert.nebula.jointjs.mouseY;
        	
        	var scale = com.bkahlert.nebula.jointjs.paper.getScale();
        	
        	var translate = com.bkahlert.nebula.jointjs.paper.getTranslate();
        	var newX = translate.tx + deltaX/scale.sx;
        	var newY = translate.ty + deltaY/scale.sy;
			
			com.bkahlert.nebula.jointjs.paper.translate(newX, newY);
        	
        	com.bkahlert.nebula.jointjs.mouseX = e.offsetX;
        	com.bkahlert.nebula.jointjs.mouseY = e.offsetY;
        },
        
        // (de)activates mouse panning
        mousePan: function(svg, activate) {
        	$svg = $(svg);
        	if(activate) {
        		$svg.bind('mousemove', com.bkahlert.nebula.jointjs.mousePanTracker);
        		$svg.attr('class', 'grabbing');
        	} else {
        		$svg.attr('class', '');
        		$svg.unbind('mousemove', com.bkahlert.nebula.jointjs.mousePanTracker);
        	}
        },
		
		activateLinkCreationCapability: function(graph, paper) {
			var shiftKey = false;
			var altKey = false;
			$(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey || e.metaKey; altKey = e.altKey; });
			
			paper.on('cell:pointerdblclick', 
				function(cellView, evt, x, y) {
					if(!altKey) return;
					
					var bounds = { x: cellView.model.attributes.position.x, y: cellView.model.attributes.position.y, w: parseInt(cellView.model.attributes.size.width), h: parseInt(cellView.model.attributes.size.height) };
					var id = cellView.model.id;
					
					var source = { id: id };
					var target = { x: bounds.x+bounds.w+100, y: y };
					
					if(shiftKey) {
						target = source;
						source = { x: bounds.x-100, y: y };
					}
					
					com.bkahlert.nebula.jointjs.createLink(null, source, target);
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
			var hoveredId = null;
			$(document).on('mouseenter', '.link[model-id]:not(.permanent)', function() {
				hoveredId = $(this).attr('model-id');
			}).on('mouseleave', '.link[model-id]', function() {
				hoveredId = null;
			}).on('keydown', function(e) {
				if(hoveredId != null && e.keyCode == 13) {
					com.bkahlert.nebula.jointjs.showTextChangePopup(hoveredId);
					hoveredId = null;
					return false;
				}
			});
		},
		
		showTextChangePopup: function(id) {
			$('.popover').remove();
			
			// we use a filter to not have to deal with escaping (e.g. [model-id=abc-def] does not work because of the hyphen in the selector)
			var $el = $('[model-id]').filter(function() { return $(this).attr('model-id') == id; });
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			if(cell) cell.on('remove', com.bkahlert.nebula.jointjs.hideTextChangePopup);
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
								<input type="text" class="form-control input-sm" id="linkTitle" placeholder="Title" value="' + com.bkahlert.nebula.jointjs.getText(id, 0) + '">\
							</div>\
						</form>\
						').submit(function() {
							com.bkahlert.nebula.jointjs.hideAndApplyTextChangePopup($el.attr('model-id'));
							return false;
						}).on('keydown', function(e) {
							if(e.keyCode == 27) {
								com.bkahlert.nebula.jointjs.hideTextChangePopup($el.attr('model-id'));
							}
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
				com.bkahlert.nebula.jointjs.setText(id, 0, title);
			}
		},
		
		activateSelections: function() {
			var shiftKey = false;
			$(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey || e.metaKey});
		
			com.bkahlert.nebula.jointjs.paper.on('blank:pointerdown', 
				function(cell, evt, x, y) { 
			        com.bkahlert.nebula.jointjs.setSelection(null);
			        com.bkahlert.nebula.jointjs.setFocus(null);
			    }
			);
			
			com.bkahlert.nebula.jointjs.paper.on('cell:pointerdown',
				function(cell, evt, x, y) {
					if(shiftKey
					   || (evt.which == 3 /* right click */
					       && _.contains(com.bkahlert.nebula.jointjs.getSelection(), cell.model.id))) com.bkahlert.nebula.jointjs.addSelection([cell.model.id]);
					else com.bkahlert.nebula.jointjs.setSelection([cell.model.id]);
			        com.bkahlert.nebula.jointjs.setFocus([cell.model.id]);
			    }
			);
			
			joint.shapes.LinkView.prototype._pointerdown = joint.shapes.LinkView.prototype.pointerdown;
			joint.shapes.LinkView.prototype.pointerdown = function(evt, x, y) {
				// only handle pointerdown by LinkView if shift was pressed or a vertex-marker
				if(shiftKey || $(evt.target).parents('.marker-vertices, .link-tools, .marker-arrowheads').length > 0) joint.shapes.LinkView.prototype._pointerdown.apply(this, arguments);
				// otherwise directly forward the event to the paper (so the link can be selected)
				else this.paper.trigger('cell:pointerdown', this, evt, x, y);
			};
		},
		
		activateEventForwarding: function() {
			var shiftKey = false;
			var altKey = false;
			$(document).bind('keyup keydown', function(e){shiftKey = e.shiftKey || e.metaKey; altKey = e.altKey; });
			
			var $d = $(document);
			if (typeof window.__cellHoveredOver === 'function') {
				$d.on('mouseenter', '[model-id]', function() {
					var id = $(this).attr('model-id');
					window.__cellHoveredOver(com.bkahlert.nebula.jointjs.getCell(id));
				});
			}
			if (typeof window.__cellHoveredOut === 'function') {
				$d.on('mouseleave', '[model-id]', function() {
					var id = $(this).attr('model-id');
					window.__cellHoveredOut(com.bkahlert.nebula.jointjs.getCell(id));
				});
			}
			if (typeof window.__cellClicked === 'function') {
				$d.on('click', '[model-id]', function() {
					var id = $(this).attr('model-id');
					window.__cellClicked(com.bkahlert.nebula.jointjs.getCell(id));
				});
			}
			if (typeof window.__cellDoubleClicked === 'function') {
				com.bkahlert.nebula.jointjs.paper.on('cell:pointerdblclick', function(cell, evt, x, y) {
					if(!altKey) window.__cellDoubleClicked(com.bkahlert.nebula.jointjs.getCell(cell.model.id));
				});
			}
		},
		
		activateModificationNotification: function() {
			var shuttingDown = false;
			var lastJson = null;
		
			var modified = function() {
				if (!shuttingDown && typeof window.__modified === 'function') {
					var json = com.bkahlert.nebula.jointjs.serialize();
					if(lastJson != json) {
						lastJson = json;
						window.__modified(json);
					}
				}
			}
			
			var debouncingModified = _.debounce(modified, 1000);
			
			com.bkahlert.nebula.jointjs.graph.on('add change remove change:title', function() {
				modified();
			});
			
			com.bkahlert.nebula.jointjs.paper.on('change:translate change:zoom', function() {
				debouncingModified();
			});
			
			$(window).on('beforeunload', function() {
                modified();
				shuttingDown = true;
            });
        },
        
        getCell: function(id) {
        	var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
        	return cell != null ? JSON.stringify(com.bkahlert.nebula.jointjs.graph.getCell(id).attributes) : null;
        },
		
		setText: function(id, index, text) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			
			if(cell instanceof joint.dia.Link) {
				cell.label(index, { attrs: { text: { text: text } }});
				if (typeof window.__linkTitleChanged === 'function') { window.__linkTitleChanged(id, text); }
			} else {
				cell.set(index, text);
			}
		},
		
		getText: function(id, index) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			if(cell instanceof joint.dia.Link) {
				return cell.get('labels')[index].attrs.text.text;
			} else {
				return cell.get(index);
			}
		},
		
		setColor: function(id, rgb) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			cell.set('color', rgb);
		},
		
		setBackgroundColor: function(id, rgb) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			cell.set('background-color', rgb);
		},
		
		setBorderColor: function(id, rgb) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			cell.set('border-color', rgb);
		},
		
		setPosition: function(id, x, y) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			cell.set('position', { x: x, y: y });
		},
		
		setSize: function(id, width, height) {
			var cell = com.bkahlert.nebula.jointjs.graph.getCell(id);
			cell.set('size', { width: width, height: height });
		},
		
		addCustomClasses: function(ids, add) {
			if(!_.isArray(add)) add = add ? [add] : [];
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				if(!_.contains(ids, cell.get('id'))) return;
				
				var customClasses = cell.get('customClasses') || [];
				customClasses = _.union(customClasses, add);
				cell.set('customClasses', customClasses);
			});
		},
		
		removeCustomClasses: function(ids, remove) {
			if(!_.isArray(remove)) remove = remove ? [remove] : [];
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				if(!_.contains(ids, cell.get('id'))) return;
				
				var customClasses = cell.get('customClasses') || [];
				customClasses = _.difference(customClasses, remove);
				cell.set('customClasses', customClasses);
			});
		},
		
		highlight: function(ids) {
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				cell.set('highlighted', _.contains(ids, cell.get('id')));
			});
		},
		
		addSelection: function(ids) {
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				if(_.contains(ids, cell.get('id'))) cell.set('selected', true);
			});
			if (typeof window.__selectionChanged === 'function') { window.__selectionChanged(com.bkahlert.nebula.jointjs.getSelection()); }
		},
		
		setSelection: function(ids) {
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				cell.set('selected', _.contains(ids, cell.get('id')));
			});
			if (typeof window.__selectionChanged === 'function') { window.__selectionChanged(com.bkahlert.nebula.jointjs.getSelection()); }
		},
		
		getSelection: function() {
			var selection = [];
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				if(cell.get('selected')) selection.push(cell.get('id'));
			});
			return selection;
		},
		
		setFocus: function(ids) {
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				cell.set('focused', _.contains(ids, cell.get('id')));
			});
			if (typeof window.__focusChanged === 'function') { window.__focusChanged(com.bkahlert.nebula.jointjs.getFocus()); }
		},
		
		getFocus: function() {
			var focus = [];
			_.each(_.union(com.bkahlert.nebula.jointjs.graph.getElements(), com.bkahlert.nebula.jointjs.graph.getLinks()), function(cell) {
				if(cell.get('focused')) focus.push(cell.get('id'));
			});
			return focus;
		}
    });
})(jQuery);

$(document).ready(com.bkahlert.nebula.jointjs.start);








joint.shapes.LinkView = joint.dia.LinkView.extend({

	className: function() {
		var classes = ['link'];
		if(this.model.get('permanent')) classes.push('permanent');
		if(this.model.get('highlighted')) classes.push('highlighted');
		if(this.model.get('selected')) classes.push('selected');
		if(this.model.get('focused')) classes.push('focused');
		if(this.model.get('customClasses')) _.each(this.model.get('customClasses'), function(customClass) { classes.push(customClass); });
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
    	var classNames = joint.shapes.LinkView.prototype.className.apply(this, arguments);
		$(this.el).attr('class', classNames);
    
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
    }

});


// own function
joint.dia.Graph.prototype.getCells = function() {
	return _.union(this.getElements(), this.getLinks());
}

joint.dia.Paper.prototype._initialize = joint.dia.Paper.prototype.initialize;
joint.dia.Paper.prototype.initialize = function() {
		
		$(document).mousemove(function(event) {
	        this.mousePosition = [ event.pageX, event.pageY ];
	    }.bind(this));
	    
	    $(document).blur(function(event) {
	        this.mousePosition = null;
	    }.bind(this));
	    
	    var oldDims = [ $(window).width(), $(window).height() ];
	    $(window).resize(function() {
			var zoom = this.getZoom();
            var newDims = [ $(window).width(), $(window).height() ];

            var diff = [ newDims[0]-oldDims[0], newDims[1]-oldDims[1] ];
            var panGain = [ diff[0]/2/zoom, diff[1]/2/zoom ];
            
            /*
             * correct panGain ...
             * - to avoid content to go out of viewport border (on contraction)
             * - to allow content to move into viewport (on expansion)
             */
            var margins = this.getContentMargins(oldDims[0], oldDims[1]);
            var collisions = [ margins[0] < 0, margins[1] < 0, margins[2] < 0, margins[3] < 0];
            if(collisions[0] != collisions[2]) {
            	if(collisions[0]) panGain[0] = panGain[0] < 0 ? 0 : 2*panGain[0];
            	if(collisions[2]) panGain[0] = panGain[0] > 0 ? 0 : 2*panGain[0];
            }
            if(collisions[1] != collisions[3]) {
            	if(collisions[1]) panGain[1] = panGain[1] < 0 ? 0 : 2*panGain[1];
            	if(collisions[3]) panGain[1] = panGain[1] > 0 ? 0 : 2*panGain[1];
            }
            
            /*
             * fast resize action can make one content border to jump out of viewport;
             * therefore don't allow the panGain to be larger than the margin of the content to each viewport border
             */
            /* does not work ...
        	if(-panGain[0] < 0 && !collisions[0] && -panGain[0] > margins[0]) {
        		panGain[0] = -margins[0];
        	}
        	if(panGain[0] > 0 && !collisions[2] && panGain[0] > margins[2]) {
        		panGain[0] = margins[2];
        	}
        	if(-panGain[1] < 0 && !collisions[1] && -panGain[1] > margins[1]) {
        		panGain[1] = -margins[1];
        	}
        	if(panGain[3] > 0 && !collisions[3] && panGain[1] > margins[3]) {
        		panGain[1] = margins[3];
        	}
        	*/
            
			var pan = this.getPan();
            this.setPan(pan[0]+panGain[0], pan[1]+panGain[1]);
            
            oldDims = newDims;
	    }.bind(this));
	    
		this._initialize.apply(this, arguments);
		
}

joint.dia.Paper.prototype.getZoom = function() {
	return this.getScale().sx;
}

joint.dia.Paper.prototype.setZoom = function(val, coordinates) {
	var center = coordinates || this.mousePosition || [ $(document).width(), $(document).height() ];
	var zoom = this.getZoom();
	var pan = this.getPan();

	var translated = [ center[0]*zoom - pan[0], center[1]*zoom - pan[1] ];
	var newTranslated = [ center[0]*val - pan[0], center[1]*val - pan[1] ];
	
	var shift = [ translated[0]-newTranslated[0], translated[1]-newTranslated[1] ];
	this.scale(val);
	this.setPan(pan[0]+shift[0], pan[1]+shift[1]);
}
		
joint.dia.Paper.prototype.zoomIn = function(val) {
	this.setZoom(this.getZoom()*1.25);
}

joint.dia.Paper.prototype.zoomOut = function(val) {
	this.setZoom(this.getZoom()*0.8);
}

joint.dia.Paper.prototype.getPan = function() {
	var translate = this.getTranslate();
	return [translate.tx, translate.ty];
}

joint.dia.Paper.prototype.setPan = function(x, y) {
	this.translate(x, y);
}

// returns an array that tells you the distance of the content's left, top, right and bottom border to the unscaled viewport (= no zoom applied)
joint.dia.Paper.prototype.getContentMargins = function(width, height) {
	var zoom = this.getZoom();
    var scaledViewport = [
    	(width || $(window).width())/zoom,
    	(height || $(window).height())/zoom
    	];
    
    var bounds = com.bkahlert.nebula.jointjs.getBoundingBox();
	var pan = this.getPan();

    return [
    	bounds[0]+pan[0],
    	bounds[1]+pan[1],
    	scaledViewport[0]-(bounds[0]+bounds[2]+pan[0]),
    	scaledViewport[1]-(bounds[1]+bounds[3]+pan[1])
    	];
}

// returns an array for each side of the content if it is out of the unscaled viewport
joint.dia.Paper.prototype.getOutOfViewport = function() {
	var margins = joint.dia.Paper.prototype.getContentMargins.apply(this, arguments);
	return [ margins[0] < 0, margins[1] < 0, margins[2] < 0, margins[3] < 0 ];
}

joint.dia.Paper.prototype.getDimensions = function() {
	var $svg = $(this.viewport).parents('svg');
	return { width: $svg.width(), height: $svg.height() };
}

joint.dia.Paper.prototype.getScale = function() {
	if(!this.viewport) return 1.0;
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

joint.dia.Paper.prototype._scale = joint.dia.Paper.prototype.scale;
joint.dia.Paper.prototype.scale = function(sx, sy, ox, oy) {
	var translate = this.getTranslate();
	this._scale(sx, sy, ox, oy);
	var scale = this.getScale();
	$(this.viewport).attr('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + translate.tx + ', ' + translate.ty + ')');
	this.$el.find('.html-view').css('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + translate.tx + 'px, ' + translate.ty + 'px)');
	this.trigger('change:zoom');
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
	var scale = this.getScale();
	$(this.viewport).attr('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + tx + ', ' + ty + ')');
	this.$el.find('.html-view').css('transform', 'scale(' + scale.sx + ', ' + scale.sy + ') translate(' + tx + 'px, ' + ty + 'px)');
	this.trigger('change:translate');
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
		com.bkahlert.nebula.jointjs.paper.on('scale', this.updateBox, this);
		
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
		
		var classNames = joint.shapes.html.ElementView.prototype.className.apply(this, arguments).split(/ +/);
		classNames.push('html-element');
		
		// sets classes
		if(this.model.get('highlighted')) classNames.push('highlighted');
		if(this.model.get('selected')) classNames.push('selected');
		if(this.model.get('focused')) classNames.push('focused');
		if(this.model.get('customClasses')) classNames = _.union(classNames, this.model.get('customClasses'));
		this.$box.attr('class', classNames.join(' '));
		
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
    },
    
    pointermove: function(evt, x, y) {
    	// only executes actions of left mouse button was clicked
    	if(evt.which == 1) joint.dia.ElementView.prototype.pointermove.apply(this, arguments);
    }
});


// (c) http://www.vfstech.com/?p=79
(function() {jQuery.fn['bounds'] = function () {
	var bounds = {
		left: Number.POSITIVE_INFINITY,
		top: Number.POSITIVE_INFINITY,
		right: Number.NEGATIVE_INFINITY,
		bottom: Number.NEGATIVE_INFINITY,
		width: Number.NaN,
		height: Number.NaN
	};

	this.each(function (i,el) {
		var elQ = $(el);
		var off = elQ.offset();
		off.right = off.left + $(elQ).width();
		off.bottom = off.top + $(elQ).height();

		if (off.left < bounds.left)
		bounds.left = off.left;

		if (off.top < bounds.top)
		bounds.top = off.top;

		if (off.right > bounds.right)
		bounds.right = off.right;

		if (off.bottom > bounds.bottom)
		bounds.bottom = off.bottom;
	});

	bounds.width = bounds.right - bounds.left;
	bounds.height = bounds.bottom - bounds.top;
	return bounds;
}})();


// (c) http://jointjs.com/tutorial/multiple-links-between-elements
function adjustVertices(graph, cell) {

    // If the cell is a view, find its model.
    cell = cell.model || cell;

    if (cell instanceof joint.dia.Element) {

        _.chain(graph.getConnectedLinks(cell)).groupBy(function(link) {
            // the key of the group is the model id of the link's source or target, but not our cell id.
            return _.omit([link.get('source').id, link.get('target').id], cell.id)[0];
        }).each(function(group, key) {
            // If the member of the group has both source and target model adjust vertices.
            if (key !== 'undefined') adjustVertices(graph, _.first(group));
        });

        return;
    }

    // The cell is a link. Let's find its source and target models.
    var srcId = cell.get('source').id || cell.previous('source').id;
    var trgId = cell.get('target').id || cell.previous('target').id;

    // If one of the ends is not a model, the link has no siblings.
    if (!srcId || !trgId) return;

    var siblings = _.filter(graph.getLinks(), function(sibling) {

        var _srcId = sibling.get('source').id;
        var _trgId = sibling.get('target').id;

        return (_srcId === srcId && _trgId === trgId) || (_srcId === trgId && _trgId === srcId);
    });

    switch (siblings.length) {

    case 0:
        // The link was removed and had no siblings.
        break;

    case 1:
        // There is only one link between the source and target. No vertices needed.
        break;

    default:

        // There is more than one siblings. We need to create vertices.

        // First of all we'll find the middle point of the link.
        var srcCenter = graph.getCell(srcId).getBBox().center();
        var trgCenter = graph.getCell(trgId).getBBox().center();
        var midPoint = g.line(srcCenter, trgCenter).midpoint();

        // Then find the angle it forms.
        var theta = srcCenter.theta(trgCenter);

        // This is the maximum distance between links
        var gap = 20;

        _.each(siblings, function(sibling, index) {

            // We want the offset values to be calculated as follows 0, 20, 20, 40, 40, 60, 60 ..
            var offset = gap * Math.ceil(index / 2);

            // Now we need the vertices to be placed at points which are 'offset' pixels distant
            // from the first link and forms a perpendicular angle to it. And as index goes up
            // alternate left and right.
            //
            //  ^  odd indexes 
            //  |
            //  |---->  index 0 line (straight line between a source center and a target center.
            //  |
            //  v  even indexes
            var sign = index % 2 ? 1 : -1;
            var angle = g.toRad(theta + sign * 90);

            // We found the vertex.
            var vertex = g.point.fromPolar(offset, angle, midPoint);

            sibling.set('vertices', [{ x: vertex.x, y: vertex.y }]);
        });
    }
};
