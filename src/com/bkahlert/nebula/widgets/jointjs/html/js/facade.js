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
				linkView: joint.shapes.LinkView,
				async: true
			});
			com.bkahlert.nebula.jointjs.setTitle(null);
			
			var myAdjustVertices = _.partial(adjustVertices, com.bkahlert.nebula.jointjs.graph);
			com.bkahlert.nebula.jointjs.graph.on('add remove change:source change:target', myAdjustVertices);
			com.bkahlert.nebula.jointjs.paper.on('cell:pointerup', myAdjustVertices);
			
			com.bkahlert.nebula.jointjs.activateZoomControls();
			com.bkahlert.nebula.jointjs.activatePanCapability(com.bkahlert.nebula.jointjs.paper);
			com.bkahlert.nebula.jointjs.activateLinkCreationCapability(com.bkahlert.nebula.jointjs.graph, com.bkahlert.nebula.jointjs.paper);
			com.bkahlert.nebula.jointjs.activateLinkAbandonCapability(com.bkahlert.nebula.jointjs.graph, com.bkahlert.nebula.jointjs.paper);
			com.bkahlert.nebula.jointjs.activateElementTools();
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
			.append($('<button>Load JSON</a>').click(function () {
				var json = window.prompt("Please enter the JSON to be loaded", "{\"cells\":[{\"type\":\"html.Element\",\"position\":{\"x\":-292,\"y\":119},\"size\":{\"width\":251,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775281\",\"title\":\"Entwurfsentscheidung (3 properties)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Dokumentation von ~ ist besonder wichtig f\u00FCr Top-Down-Lernen (erste Hypothesen ben\u00F6tigen besonders a...<\/div><\/div>\",\"content\":\"\",\"z\":0,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(198, 198, 57)\",\"border-color\":\"rgb(168, 168, 49)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-258,\"y\":166},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774919\",\"title\":\"Architektonisch<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":1,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 151, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 128, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775281\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774919\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775281|apiua:\/\/code\/-9223372036854774919\",\"smooth\":true,\"permanent\":true,\"z\":6,\"vertices\":[{\"x\":-177.25,\"y\":192}],\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775514\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775515|apiua:\/\/code\/-9223372036854775514\",\"smooth\":true,\"permanent\":true,\"z\":7,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-184,\"y\":324},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775514\",\"title\":\"Metafunktionen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Analyse:Sinn & Zweck von Metafunktionen\u00A0unklar, obwohl an mehreren Stellen der Versuch, diese zu erk...<\/div><\/div>\",\"content\":\"\",\"z\":8,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 110, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 93, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774919\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774919|apiua:\/\/code\/-9223372036854775515\",\"smooth\":true,\"permanent\":true,\"z\":8,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-183,\"y\":384},\"size\":{\"width\":211,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775579\",\"title\":\"Generische Programmierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Grund: ErweiterbarkeitDas global interface mit seinen global interface functions ist eine SeqAn zugr...<\/div><\/div>\",\"content\":\"\",\"z\":11,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 122, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 103, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-257,\"y\":500},\"size\":{\"width\":117,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774918\",\"title\":\"Sprachlich<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":11,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-182,\"y\":441},\"size\":{\"width\":193,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775412\",\"title\":\"Template-Spezialisierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Grund: PolymorphieTemplate-Spezialisierung:String<TValue, TSpec>Spec ist nicht nur atomarString<Dna>...<\/div><\/div>\",\"content\":\"\",\"z\":12,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 133, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 113, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775515|apiua:\/\/code\/-9223372036854775579\",\"smooth\":true,\"permanent\":true,\"z\":12,\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775412\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775515|apiua:\/\/code\/-9223372036854775412\",\"smooth\":true,\"permanent\":true,\"z\":13,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-224,\"y\":555},\"size\":{\"width\":112,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775611\",\"title\":\"Shortcuts<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Werden verst\u00E4ndlicher mit\u00A0existierendem\u00A0Fehlendes BenennungschemaDesign-Feature von\u00A0citep{GogolDori...<\/div><\/div>\",\"content\":\"\",\"z\":13,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 182, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 155, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775281\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775281|apiua:\/\/code\/-9223372036854774918\",\"smooth\":true,\"permanent\":true,\"z\":14,\"vertices\":[{\"x\":-183,\"y\":339}],\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774918|apiua:\/\/code\/-9223372036854775611\",\"smooth\":true,\"permanent\":true,\"z\":14,\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-224,\"y\":611},\"size\":{\"width\":237,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774848\",\"title\":\"Dom\u00E4nen-spezifische Benennung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Quelle SeqAn-BuchVerschiedene Dom\u00E4ne werden vermischt:z.B. technisches Vokabular: Allocfachlich:- bi...<\/div><\/div>\",\"content\":\"\",\"z\":15,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774918|apiua:\/\/code\/-9223372036854774848\",\"smooth\":true,\"permanent\":true,\"z\":16,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":340,\"y\":432},\"size\":{\"width\":176,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775448\",\"title\":\"Benennungsprobleme<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">1 Funktion - zwei Namen (infix substring),\u00A0substring vs. infix\u00A0,\u00A02 Funktionen - ein Name (globale Fu...<\/div><\/div>\",\"content\":\"\",\"z\":17,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 189, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 161, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-224,\"y\":264},\"size\":{\"width\":222,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775515\",\"title\":\"Templatemetaprogrammierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">siehe auch\u00A0Metafunktionen\u00A0Template Programming ist ein \u201Cselten\u201D gebrauchter bzw. unwissentlich gebra...<\/div><\/div>\",\"content\":\"\",\"z\":19,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 122, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 103, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-228,\"y\":669},\"size\":{\"width\":210,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774838\",\"title\":\"Datenstrukturmodifikationen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie erlaube ich API-Anwendern die Modifikation von Datenstrukturen.Bei SeqAn:Zwei Eigenschaften:Dire...<\/div><\/div>\",\"content\":\"\",\"z\":20,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(182, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(155, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774838\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774918|apiua:\/\/code\/-9223372036854774838\",\"smooth\":true,\"permanent\":true,\"z\":21,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":344,\"y\":719},\"size\":{\"width\":146,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775117\",\"title\":\"Syntaxprobleme<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":22,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 195, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 165, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":304,\"y\":78},\"size\":{\"width\":103,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774915\",\"title\":\"Struktur<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":24,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 186, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 158, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775448\",\"smooth\":true,\"permanent\":true,\"z\":30,\"vertices\":[{\"x\":391.75,\"y\":269}],\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775117\",\"smooth\":true,\"permanent\":true,\"z\":31,\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":341,\"y\":169},\"size\":{\"width\":214,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775237\",\"title\":\"funktionsbezogene Probleme<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":32,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 183, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 156, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775237\",\"smooth\":true,\"permanent\":true,\"z\":35,\"vertices\":[{\"x\":401.75,\"y\":137.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":375,\"y\":210},\"size\":{\"width\":245,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775280\",\"title\":\"Fehlende Funktionskategorisierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie sind Funktionen kategorisiert?Funktionen sind nicht kategorisiert.Die Zugeh\u00F6rigkeit mancher Funk...<\/div><\/div>\",\"content\":\"\",\"z\":36,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 181, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 154, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erh\u00F6ht Eintrittwahrscheinlichkeit\\n3 (3)\\nDie Verwendung ...\"}}}],\"id\":\"apiua:\/\/relation\/bc471l5bdmgjsbp92ljc2372aoil9olr\",\"smooth\":true,\"z\":36,\"vertices\":[{\"x\":210,\"y\":311}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/bnukj71m5n1lejph7g3vgng83cv6qbeo\",\"smooth\":true,\"z\":37,\"customClasses\":[],\"vertices\":[{\"x\":705,\"y\":256}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\\nWenn man nicht ...\"}}}],\"id\":\"apiua:\/\/relation\/koak8ole0n4vbudg45f8kar682o3e9hd\",\"smooth\":true,\"z\":38,\"customClasses\":[],\"vertices\":[{\"x\":671,\"y\":263}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/imei5mn1tgdebfrfhrbcsuqb0q2q1kfd\",\"smooth\":true,\"z\":39,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775280\",\"smooth\":true,\"permanent\":true,\"z\":40,\"vertices\":[{\"x\":472.75,\"y\":203.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n5 (5)\"}}}],\"id\":\"apiua:\/\/relation\/sl1hrvpiabeq61pfihufniil0q8bo7kn\",\"smooth\":true,\"z\":40,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":251},\"size\":{\"width\":246,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775544\",\"title\":\"Identifikation relevanter Funktionen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Welche Funktionen stehen mir zur Verf\u00FCgung?Anf\u00E4nger beschreiben, dass Sie anf\u00E4nglich nicht wissen, w...<\/div><\/div>\",\"content\":\"\",\"z\":41,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 182, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 155, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/el2lliolgockjahk0e0jrhjglq3n8avi\",\"smooth\":true,\"z\":41,\"vertices\":[{\"x\":204.20724445993318,\"y\":291.85727335897457}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775544\",\"smooth\":true,\"permanent\":true,\"z\":44,\"vertices\":[{\"x\":472.5,\"y\":224}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":295},\"size\":{\"width\":230,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775279\",\"title\":\"Funktionszweckunerkennbarkeit<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Macht die Funktion jetzt X oder Y?oder: 2 Funktionen brauchen auch 2 NamenDas Verhalten \/ der Zweck ...<\/div><\/div>\",\"content\":\"\",\"z\":45,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 183, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 156, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775279\",\"smooth\":true,\"permanent\":true,\"z\":48,\"vertices\":[{\"x\":468.5,\"y\":246}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":375,\"y\":336},\"size\":{\"width\":162,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775405\",\"title\":\"Funktionsgebrauch<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie verwende ich diese Funktion?Wenn man wei\u00DF, welche Funktion hilft aber nicht, wie diese anzuwende...<\/div><\/div>\",\"content\":\"\",\"z\":49,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 184, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 157, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775405\",\"smooth\":true,\"permanent\":true,\"z\":50,\"vertices\":[{\"x\":452,\"y\":266.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":373,\"y\":484},\"size\":{\"width\":186,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774860\",\"title\":\"Synonyme \/ Redundanz<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Shortcuts erzeugen Redundanzen und nervenZ.B. String<Dna> und DnaStringVon\u00A0GM: \\\"It&apos;s just stupid\u201D,\u00A0\u201C...<\/div><\/div>\",\"content\":\"\",\"z\":50,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 186, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 158, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":383},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775264\",\"title\":\"Funktionsweise<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Was macht die Funktion (intern)?Die Funktionsweise - also was tut die Funktion - einer Funktion ist ...<\/div><\/div>\",\"content\":\"\",\"z\":51,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 185, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 158, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774860\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854774860\",\"smooth\":true,\"permanent\":true,\"z\":51,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775264\",\"smooth\":true,\"permanent\":true,\"z\":52,\"vertices\":[{\"x\":446.5,\"y\":290}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":532},\"size\":{\"width\":185,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774861\",\"title\":\"Abstraktionssuggestion<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der Name einer Sache suggeriert eine Abstraktion, die es nicht gibt.Beispiel: Peptide<\/div><\/div>\",\"content\":\"\",\"z\":52,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 187, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 159, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775623\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n5 (5)\"}}}],\"id\":\"apiua:\/\/relation\/ssghh5e2s6hrn5vrmj0b8ihofu81njf4\",\"smooth\":true,\"z\":52,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774861\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854774861\",\"smooth\":true,\"permanent\":true,\"z\":53,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775533\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/vv4l57ksh9tu3acqikdrqtler65aeqjm\",\"smooth\":true,\"z\":53,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":373,\"y\":580},\"size\":{\"width\":231,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775567\",\"title\":\"Ununterscheidbarkeit von Typen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">\u00C4hnliche Typen wie String, StringSet und array (z.B. SuffixArray) k\u00F6nnen nicht unterschieden werden....<\/div><\/div>\",\"content\":\"\",\"z\":54,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 188)\",\"border-color\":\"rgb(49, 168, 160)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775567\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854775567\",\"smooth\":true,\"permanent\":true,\"z\":54,\"vertices\":[{\"x\":458.25,\"y\":520}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774860\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n4 (4)\"}}}],\"id\":\"apiua:\/\/relation\/cu94r9kfkleo2sauj0fgt1hdsuh1dc3q\",\"smooth\":true,\"z\":54,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":371,\"y\":622},\"size\":{\"width\":179,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775623\",\"title\":\"Technisch vs. Fachlich<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Kuchenbecker:St\u00F6rt deutlich, dass ein biologischer Term mit einem informatischen Term gemischt wird ...<\/div><\/div>\",\"content\":\"\",\"z\":55,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 189, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 161, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775533\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/5pkeab04gpfffj2e2td9ic6i1lp4qta5\",\"smooth\":true,\"z\":55,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775623\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854775623\",\"smooth\":true,\"permanent\":true,\"z\":56,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775567\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/9huj9eb6vefpubjje9jn9vu75qdp9n92\",\"smooth\":true,\"z\":56,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":368,\"y\":668},\"size\":{\"width\":182,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775533\",\"title\":\"Benennungskonsistenz<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wenn schon Shortcuts, dann bitte auch konsistent<\/div><\/div>\",\"content\":\"\",\"z\":57,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 191, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 163, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775533\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854775533\",\"smooth\":true,\"permanent\":true,\"z\":57,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774861\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/lhn440v7jt0ls5eke544jerkic74tr7c\",\"smooth\":true,\"z\":57,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775215\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/3eo08gokhoeu4k9283l2u8t1lb8e6s8q\",\"smooth\":true,\"z\":62,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":480,\"y\":-131},\"size\":{\"width\":150,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774830\",\"title\":\"Libraryerwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Erwartung geweckt durch SeqAn-Beschreibung als LibraryDetails siehe\u00A0Framework vs. Library\u00A0<\/div><\/div>\",\"content\":\"\",\"z\":65,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 134, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 114, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775413\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\\nreine Vermutung...\"}}}],\"id\":\"apiua:\/\/relation\/thq387lsd79dmprfs03flmkdtu23uh4b\",\"smooth\":true,\"z\":65,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-223,\"y\":217},\"size\":{\"width\":178,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775215\",\"title\":\"Framework vs. Library<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAn war urspr\u00FCnglich als Framework konzipiert.Marker:Ausspruch:\u00A0\u201Cin SeqAn l\u00F6sen\/programmieren\u201D, da...<\/div><\/div>\",\"content\":\"\",\"z\":67,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 157, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 133, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774919\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775215\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774919|apiua:\/\/code\/-9223372036854775215\",\"smooth\":true,\"permanent\":true,\"z\":67,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":341,\"y\":123},\"size\":{\"width\":203,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775413\",\"title\":\"Sprachentit\u00E4tstypen \/ LETs<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Dem Anwender unbekannte LET \/ Konstrukte f\u00FChren zu Verst\u00E4ndnis- u. folglich zu Anwendungsschwierigke...<\/div><\/div>\",\"content\":\"\",\"z\":71,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 177, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 151, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775413\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775413\",\"smooth\":true,\"permanent\":true,\"z\":71,\"vertices\":[{\"x\":399,\"y\":114.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":217,\"y\":-456},\"size\":{\"width\":187,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774893\",\"title\":\"Anwender (7 properties)<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":75,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 57)\",\"border-color\":\"rgb(49, 168, 49)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-83,\"y\":-398},\"size\":{\"width\":273,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775596\",\"title\":\"T\u00E4tigkeitsbereich (Informatik ... Biologie)<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":82,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(117, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(100, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775596\",\"smooth\":true,\"permanent\":true,\"z\":83,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775494\",\"smooth\":true,\"permanent\":true,\"z\":83,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-84,\"y\":-310},\"size\":{\"width\":342,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775494\",\"title\":\"Paradigmatische Pr\u00E4gung (C++, Java, C++-Anf\u00E4nger)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">In welcher Sprache f\u00FChlt sich der Anwender zu Hause?C++Anwender hat einen C++-Hintergrund, was auf E...<\/div><\/div>\",\"content\":\"\",\"z\":84,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(97, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(83, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-86,\"y\":-267},\"size\":{\"width\":113,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775600\",\"title\":\"Arbeitsstil<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Verwendung von Personas citep{Pruitt:2003ki} nach cite{Stylos:2007jb} sehr gut geeignet f\u00FCr API-Us...<\/div><\/div>\",\"content\":\"\",\"z\":84,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(77, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(66, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775600\",\"smooth\":true,\"permanent\":true,\"z\":85,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-86,\"y\":-220},\"size\":{\"width\":322,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774892\",\"title\":\"Library-Vorerfahrung (Boost, cityhash, Biopython)<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":86,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774892\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854774892\",\"smooth\":true,\"permanent\":true,\"z\":87,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-84,\"y\":-354},\"size\":{\"width\":440,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775446\",\"title\":\"SeqAn-Anwendungskompetenz (wiedergebend, einfach, fortgeschritten)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie f\u00E4hig und sicher ist der Anwender im Gebrauch von SeqAn?Wiedergabe (Tutorials) (~ Bloom 1-2)Einf...<\/div><\/div>\",\"content\":\"\",\"z\":88,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 77, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 66, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775446\",\"smooth\":true,\"permanent\":true,\"z\":89,\"vertices\":[{\"x\":501.06439598216417,\"y\":-341.9263487714805}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-86,\"y\":-174},\"size\":{\"width\":162,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774938\",\"title\":\"SeqAn-Einsatzform<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie wird technisch SeqAn eingesetzt?Ausdifferenzierung angesichts des Grounding zu interpretativ.<\/div><\/div>\",\"content\":\"\",\"z\":90,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 97, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 83, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774938\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854774938\",\"smooth\":true,\"permanent\":true,\"z\":92,\"vertices\":[{\"x\":152.75,\"y\":-301}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":410,\"y\":-400},\"size\":{\"width\":116,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775599\",\"title\":\"Motivation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Gr\u00FCnde sich mit SeqAn auseinanderzusetzen oder damit weiterhin zu arbeiten<\/div><\/div>\",\"content\":\"\",\"z\":93,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 117)\",\"border-color\":\"rgb(49, 168, 100)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-48,\"y\":-127},\"size\":{\"width\":110,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775587\",\"title\":\"Helferlein<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Entwickler lernen SeqAn und sehen als Einsatzzweck die Entwicklung von persistenten Tools, die insbe...<\/div><\/div>\",\"content\":\"\",\"z\":93,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 92, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 79, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775599\",\"smooth\":true,\"permanent\":true,\"z\":94,\"vertices\":[{\"x\":389.25,\"y\":-414}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774938\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775587\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774938|apiua:\/\/code\/-9223372036854775587\",\"smooth\":true,\"permanent\":true,\"z\":94,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":77,\"y\":-128},\"size\":{\"width\":295,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775598\",\"title\":\"Langj\u00E4hrige stabile Anwendungsentwicklung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAn wird seit mehreren Jahren verwendet, um regelm\u00E4\u00DFig verwendete Anwendungen zu entwickeln. Dabei...<\/div><\/div>\",\"content\":\"\",\"z\":95,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 102, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 87, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774938\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775598\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774938|apiua:\/\/code\/-9223372036854775598\",\"smooth\":true,\"permanent\":true,\"z\":96,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":-354},\"size\":{\"width\":199,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775407\",\"title\":\"Kennenlernen \/ Evaluation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Eng verwandt mit\u00A0Helferlein\u00A0Anwender wollen SeqAn zun\u00E4chst einmal kennenlernen \/ machen nicht den Ei...<\/div><\/div>\",\"content\":\"\",\"z\":97,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 119, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 101, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775407\",\"smooth\":true,\"permanent\":true,\"z\":98,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":478,\"y\":-308},\"size\":{\"width\":263,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775547\",\"title\":\"Suche nach professioneller Alternative<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der Anwender hat eine eigenen Sequenzanalyse-Tools entwickelt und testet, ob SeqAn ihm dabei helfen ...<\/div><\/div>\",\"content\":\"\",\"z\":99,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 102, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 86, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775547\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775407|apiua:\/\/code\/-9223372036854775547\",\"smooth\":true,\"permanent\":true,\"z\":100,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":753,\"y\":-307},\"size\":{\"width\":159,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775406\",\"title\":\"Genome Assembly<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Genome assembly refers to the process of taking a large number of short and putting them back togeth...<\/div><\/div>\",\"content\":\"\",\"z\":101,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 113, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 96, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775406\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775407|apiua:\/\/code\/-9223372036854775406\",\"smooth\":true,\"permanent\":true,\"z\":102,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":483,\"y\":-220},\"size\":{\"width\":122,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775297\",\"title\":\"M\u00E4chtigkeit<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Ziel von Gogol: Erweiterbarkeit (die ja dann zur M\u00E4chtigkeit f\u00FChrt)Anwender loben \/ stellen heraus d...<\/div><\/div>\",\"content\":\"\",\"z\":109,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 163, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 139, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775297\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775297\",\"smooth\":true,\"permanent\":true,\"z\":110,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":649,\"y\":-132},\"size\":{\"width\":181,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775568\",\"title\":\"M\u00E4chtigkeitserwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Anf\u00E4nger loben den Funktionsumfang von SeqAn. Mit SeqAn w\u00FCrde man viel gekapselte Funktionalit\u00E4t erh...<\/div><\/div>\",\"content\":\"\",\"z\":113,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 140, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 119, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775568\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775568\",\"smooth\":true,\"permanent\":true,\"z\":114,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1097,\"y\":-131},\"size\":{\"width\":181,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775298\",\"title\":\"Performanceerwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Anwender (insb. Anf\u00E4nger) glauben von SeqAn, es sei effizient - schnell.Das kann als Motivation dien...<\/div><\/div>\",\"content\":\"\",\"z\":115,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 151)\",\"border-color\":\"rgb(49, 168, 129)\",\"unimportant\":false,\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775298\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775298\",\"smooth\":true,\"permanent\":true,\"z\":116,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":845,\"y\":-132},\"size\":{\"width\":238,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775311\",\"title\":\"Benutzerfreundlichkeitserwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">~ wird mehr oder minder explizit als eine Erwartung formuliert.- bei\u00A0d3b2 - learningStyle\u00A0zum Beispi...<\/div><\/div>\",\"content\":\"\",\"z\":117,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 145, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 124, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775311\",\"smooth\":true,\"permanent\":true,\"z\":118,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":618,\"y\":-221},\"size\":{\"width\":128,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775552\",\"title\":\"Performance<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">\u201Cvergleichbares sucht man lange\u201D\u00A0 60ba - similiarSystems\\\"extremely efficient\u201D \u00A0 7c79 - workStepUnit\u201C...<\/div><\/div>\",\"content\":\"\",\"z\":119,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 181)\",\"border-color\":\"rgb(49, 168, 153)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775552\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775552\",\"smooth\":true,\"permanent\":true,\"z\":120,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854774830\",\"smooth\":true,\"permanent\":true,\"z\":121,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":719,\"y\":83},\"size\":{\"width\":272,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774828\",\"title\":\"Produktbedingte Erwartungskonformit\u00E4t<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">NICHT GRILL, eigentlich Eigenschaft f\u00FCr alle Probleme.Separat gegliedert, um Theorie zu vereinfachen...<\/div><\/div>\",\"content\":\"\",\"z\":121,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 139, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 118, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":-176},\"size\":{\"width\":206,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774827\",\"title\":\"Produktbedingte Erwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Erwartung, die durch Produkt \/ -beschreibung beim Anwender entsteht<\/div><\/div>\",\"content\":\"\",\"z\":122,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 143, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 121, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854774830\",\"smooth\":true,\"permanent\":true,\"z\":123,\"vertices\":[{\"x\":554,\"y\":-139.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nBiopython\"}}}],\"id\":\"apiua:\/\/relation\/89dgrcs586tk7vsvfknsv034l0jodsvc\",\"smooth\":true,\"z\":123,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775568\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854775568\",\"smooth\":true,\"permanent\":true,\"z\":124,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775080\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n2 (2)\\nOOP-Hintergrund\"}}}],\"id\":\"apiua:\/\/relation\/17r5q0so7e80u9sndci61rcegjt90u38\",\"smooth\":true,\"z\":124,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775298\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854775298\",\"smooth\":true,\"permanent\":true,\"z\":125,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775080\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/ohu1p0tojsaf6mi9o43pog3est14ofdu\",\"smooth\":true,\"z\":125,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854775311\",\"smooth\":true,\"permanent\":true,\"z\":126,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":-263},\"size\":{\"width\":210,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774826\",\"title\":\"Produktspezifisches Feature<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Tats\u00E4chliches Feature, dass die weitere Verwendung des Produkts f\u00F6rdert \/ motiviert<\/div><\/div>\",\"content\":\"\",\"z\":126,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 166, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 141, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nLangj\u00E4hriger sp...\"}}}],\"id\":\"apiua:\/\/relation\/i2junc0k8gvb7lhp122eq980klf4nj5u\",\"smooth\":true,\"z\":126,\"vertices\":[{\"x\":11.5,\"y\":-31}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854774827\",\"smooth\":true,\"permanent\":true,\"z\":127,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774826\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775297\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774826|apiua:\/\/code\/-9223372036854775297\",\"smooth\":true,\"permanent\":true,\"z\":127,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/5hjekqu0olhfh532huq55dg67mfbk4n8\",\"smooth\":true,\"z\":127,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774826\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775552\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774826|apiua:\/\/code\/-9223372036854775552\",\"smooth\":true,\"permanent\":true,\"z\":128,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erwartet\\n3 (3)\\nOOP-Hintergrund\"}}}],\"id\":\"apiua:\/\/relation\/v0qaiccev68u2gokimdftv97schqa61f\",\"smooth\":true,\"z\":128,\"vertices\":[{\"x\":292,\"y\":-15.5}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774826\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854774826\",\"smooth\":true,\"permanent\":true,\"z\":129,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775552\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verschmerzt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/6fd9s09lp94p05bje6l3g3fks8knme26\",\"smooth\":true,\"z\":129,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774828\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774828|apiua:\/\/code\/-9223372036854775633\",\"smooth\":true,\"permanent\":true,\"z\":130,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nGilt f\u00FCr pragma...\"}}}],\"id\":\"apiua:\/\/relation\/f2kms2c44b3tudk2om8nm257kqn3a6vj\",\"smooth\":true,\"z\":130,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":748,\"y\":126},\"size\":{\"width\":162,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774829\",\"title\":\"Projektorganisation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAn ist organisiert in Form von Core, Extras und Sandbox.Erstellung innerhalb von Framework NUR mi...<\/div><\/div>\",\"content\":\"\",\"z\":131,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 92, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 79, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774828\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774829\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774828|apiua:\/\/code\/-9223372036854774829\",\"smooth\":true,\"permanent\":true,\"z\":131,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774828\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775080\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774828|apiua:\/\/code\/-9223372036854775080\",\"smooth\":true,\"permanent\":true,\"z\":131,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":721,\"y\":333},\"size\":{\"width\":102,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774914\",\"title\":\"Laufzeit<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":131,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 186, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 158, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erwartet\\n3 (3)\\nOOP-Hintergrund\"}}}],\"id\":\"apiua:\/\/relation\/qrsbkamd2lm7nc87iugv1j8an2ha3ss1\",\"smooth\":true,\"z\":131,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":749,\"y\":171},\"size\":{\"width\":197,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775080\",\"title\":\"Inkonsistenzen bzgl. OOP<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":132,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 116, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 98, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":747,\"y\":213},\"size\":{\"width\":192,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775633\",\"title\":\"Inkonsistenzen bzgl. STL<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAns API entspricht nicht den Erwartungen der Anwender. Sie bem\u00E4ngeln, dass die API nicht wie die ...<\/div><\/div>\",\"content\":\"\",\"z\":132,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 139)\",\"border-color\":\"rgb(49, 168, 118)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":743,\"y\":380},\"size\":{\"width\":203,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775100\",\"title\":\"Compiler-Fehlermeldungen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die Anwender verstehen Compiler-Meldungen nicht.Viele Gruppendiskussionsteilnehmer stimmen zu.\u00A0a few...<\/div><\/div>\",\"content\":\"\",\"z\":132,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 192, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 163, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erlaubt Wahrnehmung von\\n10 (10)\\nWenn der Anwend...\"}}}],\"id\":\"apiua:\/\/relation\/m7c927ilp2ds6skudfo0627amlugghi1\",\"smooth\":true,\"z\":132,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774914\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775100\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774914|apiua:\/\/code\/-9223372036854775100\",\"smooth\":true,\"permanent\":true,\"z\":133,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nGilt f\u00FCr pragma...\"}}}],\"id\":\"apiua:\/\/relation\/en4pribolkkg7egnm7rdh065mq41qj5a\",\"smooth\":true,\"z\":133,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":742,\"y\":423},\"size\":{\"width\":193,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775615\",\"title\":\"Versagensverschleppung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">~\u00A0beschreibt, dass ein Versagen erst viel sp\u00E4ter sichtbar wird, als der Anwender erwarten w\u00FCrde. Die...<\/div><\/div>\",\"content\":\"\",\"z\":134,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 180, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 153, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/8t0fhejti77827o490prf08j3soaroa0\",\"smooth\":true,\"z\":134,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774914\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775615\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774914|apiua:\/\/code\/-9223372036854775615\",\"smooth\":true,\"permanent\":true,\"z\":135,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774829\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/tptpd7is3kujht2d08o3vdpbde7ub3o9\",\"smooth\":true,\"z\":135,\"customClasses\":[\"important\"],\"unimportant\":true,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nWiedergabe\"}}}],\"id\":\"apiua:\/\/relation\/5pj50p0qslojnbv3ut1vpulj731ekvpa\",\"smooth\":true,\"z\":136,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nExploration (Bl...\"}}}],\"id\":\"apiua:\/\/relation\/pthets4oehu6afuocrlgs3rgm7mgtfih\",\"smooth\":true,\"z\":137,\"vertices\":[{\"x\":30.05084381871116,\"y\":-23.525630888577552}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verursacht\\n3 (3)\\nDie jetztige Fo...\"}}}],\"id\":\"apiua:\/\/relation\/7fu79qaas2p0m5qkp2a9160agfiotm6j\",\"smooth\":true,\"z\":138,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":719,\"y\":513},\"size\":{\"width\":185,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775396\",\"title\":\"Werkzeugunterst\u00FCtzung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">NICHT VON GRILL (= BEITRAG)Alles zum Thema Integration von SeqAn in die IDE des Anwenderseb6d - pers...<\/div><\/div>\",\"content\":\"\",\"z\":152,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 139, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 118, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":740,\"y\":560},\"size\":{\"width\":230,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775148\",\"title\":\"Fehlende Autovervollst\u00E4ndigung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die fehlende Autovervollst\u00E4ndigung wird bem\u00E4ngelt.Auch wenn andere Strategie verwendet wird, sagen d...<\/div><\/div>\",\"content\":\"\",\"z\":153,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 116, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 98, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/ova0mpol7s2st1b8er93f946lld7funa\",\"smooth\":true,\"z\":153,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775396\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775148\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775396|apiua:\/\/code\/-9223372036854775148\",\"smooth\":true,\"permanent\":true,\"z\":154,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775396\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erfordert\\n2 (2)\\nOOP-Anwender (i...\"}}}],\"id\":\"apiua:\/\/relation\/m2hron3i1dh2f4m4epabucu6enicf5jb\",\"smooth\":true,\"z\":154,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":28,\"y\":973},\"size\":{\"width\":267,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775404\",\"title\":\"Dokumentation (Verbessert, Confusing)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die Online-Dokumentaton ist schwer zu finden.<\/div><\/div>\",\"content\":\"\",\"z\":155,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 163, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 138, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775577\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verst\u00E4rkt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/3shsibj2mp5kbknqqbcsg0fksuej8l4l\",\"smooth\":true,\"z\":155,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/jn27d5k8d0bc8fjfvdu5hgju0i8bk74b\",\"smooth\":true,\"z\":156,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verringert Work-Step Unit\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1866845070\",\"smooth\":true,\"z\":157,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775587\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verwendet\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1946163892\",\"smooth\":true,\"z\":158,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775567\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-274351899\",\"smooth\":true,\"z\":159,\"vertices\":[{\"x\":476.7629310255273,\"y\":512.4322140064568}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":64,\"y\":1021},\"size\":{\"width\":206,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775572\",\"title\":\"Fehlender Gesamt\u00FCberblick<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Das Lernen von SeqAn erfordert Orientierungshilfen (z.B. \u00DCberblick), die aktuell nicht ausreichen.Be...<\/div><\/div>\",\"content\":\"\",\"z\":160,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 161, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 137, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-607855653\",\"smooth\":true,\"z\":160,\"vertices\":[{\"x\":432.02290869492225,\"y\":267.4569864098241}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775572\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775572\",\"smooth\":true,\"permanent\":true,\"z\":161,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1365749788\",\"smooth\":true,\"z\":161,\"vertices\":[{\"x\":308.14729506347203,\"y\":-27.301053433197048}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":64,\"y\":1068},\"size\":{\"width\":228,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775581\",\"title\":\"Fehlende Anwendungsbeispiele<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">In den Online-Quellen gibt es (viel) zu wenig Beispiele. Das erschwert das Verst\u00E4ndnis und die Arbei...<\/div><\/div>\",\"content\":\"\",\"z\":162,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 153, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 130, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775581\",\"smooth\":true,\"permanent\":true,\"z\":162,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774838\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n18 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1600091982\",\"smooth\":true,\"z\":162,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":63,\"y\":1116},\"size\":{\"width\":299,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775577\",\"title\":\"Fehlende Dokumentation der R\u00FCckgabetypen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der R\u00FCckgabetyp von Funktionen wird sehr h\u00E4ufig nicht dokumentiert.Sehr h\u00E4ufig handelt es sich um R\u00FC...<\/div><\/div>\",\"content\":\"\",\"z\":163,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 157, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 133, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775577\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775577\",\"smooth\":true,\"permanent\":true,\"z\":163,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verschlechtert\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1074652199\",\"smooth\":true,\"z\":163,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":62,\"y\":1163},\"size\":{\"width\":206,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775504\",\"title\":\"Suchfunktion (Umst\u00E4ndlich)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die Suche wird als\u00A0umst\u00E4ndlich beschrieben.<\/div><\/div>\",\"content\":\"\",\"z\":164,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 165, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 140, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775572\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erwartet\\n3 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-906600510\",\"smooth\":true,\"z\":164,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775504\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775504\",\"smooth\":true,\"permanent\":true,\"z\":165,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775514\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2031358344\",\"smooth\":true,\"z\":165,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":63,\"y\":1211},\"size\":{\"width\":105,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775271\",\"title\":\"Tutorials<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Assignments werden als Hilfreich beschrieben.Hier sind nur noch die \u00FCberarbeiteten Tutorials erfasst...<\/div><\/div>\",\"content\":\"\",\"z\":166,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 168, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 143, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verhindert\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1659680609\",\"smooth\":true,\"z\":166,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775271\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775271\",\"smooth\":true,\"permanent\":true,\"z\":167,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1246635787\",\"smooth\":true,\"z\":167,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2081713548\",\"smooth\":true,\"z\":168,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/957294190\",\"smooth\":true,\"z\":169,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-915611634\",\"smooth\":true,\"z\":170,\"vertices\":[{\"x\":471.97709130507775,\"y\":265.5430135901759}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1870562677\",\"smooth\":true,\"z\":171,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1405,\"y\":41},\"size\":{\"width\":117,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775441\",\"title\":\"Emotionen<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":172,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(139, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(118, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775314\",\"smooth\":true,\"permanent\":true,\"z\":188,\"vertices\":[{\"x\":1491.5,\"y\":152.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1423,\"y\":286},\"size\":{\"width\":119,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775115\",\"title\":\"Sarkasmus<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":189,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(145, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(123, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775115\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775115\",\"smooth\":true,\"permanent\":true,\"z\":190,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775623\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n4 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1493842278\",\"smooth\":true,\"z\":190,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775115\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1833319428\",\"smooth\":true,\"z\":190,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1426,\"y\":89},\"size\":{\"width\":116,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775447\",\"title\":\"Sympathie<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der Entwickler mag SeqAn - einfach so.<\/div><\/div>\",\"content\":\"\",\"z\":191,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(87, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(74, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n6 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/2115050082\",\"smooth\":true,\"z\":191,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1421,\"y\":-282},\"size\":{\"width\":128,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774824\",\"title\":\"Erlernbarkeit<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie leicht kann man SeqAn erlernen?<\/div><\/div>\",\"content\":\"\",\"z\":191,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 57, 124, 0.27450980392156865)\",\"border-color\":\"rgba(168, 49, 105, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775447\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775447\",\"smooth\":true,\"permanent\":true,\"z\":192,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/883167155\",\"smooth\":true,\"z\":192,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1500398331\",\"smooth\":true,\"z\":192,\"vertices\":[{\"x\":1017.25,\"y\":-304}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1426,\"y\":133},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775134\",\"title\":\"Verunsicherung<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":193,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(133, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(113, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"dennoch\\n2 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/151131261\",\"smooth\":true,\"z\":193,\"vertices\":[{\"x\":1224.5,\"y\":-193}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775134\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775134\",\"smooth\":true,\"permanent\":true,\"z\":194,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775568\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"motiviert\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/740930886\",\"smooth\":true,\"z\":194,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1425,\"y\":183},\"size\":{\"width\":214,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775440\",\"title\":\"Unverst\u00E4ndnis \/ Genervt sein<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Anwender k\u00F6nnen wenig Verst\u00E4ndnis daf\u00FCr aufbringen, dass SeqAn so komplex und schwierig zu lernen is...<\/div><\/div>\",\"content\":\"\",\"z\":195,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(98, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(84, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775440\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1068432899\",\"smooth\":true,\"z\":195,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775440\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775440\",\"smooth\":true,\"permanent\":true,\"z\":195,\"vertices\":[{\"x\":1497.75,\"y\":126}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-609412344\",\"smooth\":true,\"z\":195,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775440\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n4 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-663948029\",\"smooth\":true,\"z\":196,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1424,\"y\":236},\"size\":{\"width\":191,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775314\",\"title\":\"Frustration \/ Resignation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">liegt erst ab folgender Schwelle vor:Gef\u00FChl tritt mehrfach einGef\u00FChl kann nur mit erheblichen Aufwan...<\/div><\/div>\",\"content\":\"\",\"z\":196,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(110, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(93, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1599453106\",\"smooth\":true,\"z\":196,\"customClasses\":[\"proposed\"],\"vertices\":[{\"x\":810.5,\"y\":-304}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2128967051\",\"smooth\":true,\"z\":197,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"macht m\u00F6glich\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-163244303\",\"smooth\":true,\"z\":197,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"f\u00FChrt dennoch zu\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1766831944\",\"smooth\":true,\"z\":197,\"vertices\":[{\"x\":1018.7847449962236,\"y\":-323.9410269995446}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n2 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/246968158\",\"smooth\":true,\"z\":198,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/407687608\",\"smooth\":true,\"z\":199,\"customClasses\":[\"proposed\"],\"vertices\":[{\"x\":769.25,\"y\":-326}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-213750483\",\"smooth\":true,\"z\":200,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-455427786\",\"smooth\":true,\"z\":201,\"customClasses\":[\"proposed\"],\"vertices\":[{\"x\":1502.25,\"y\":-9}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1443182131\",\"smooth\":true,\"z\":202,\"vertices\":[{\"x\":1218.9666113994517,\"y\":-212.21930307257065}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2120514294\",\"smooth\":true,\"z\":203,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775298\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1164483283\",\"smooth\":true,\"z\":204,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/774422231\",\"smooth\":true,\"z\":205,\"vertices\":[{\"x\":1522.2057883477896,\"y\":-10.329101733588308}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775504\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1015718930\",\"smooth\":true,\"z\":206,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-886008515\",\"smooth\":true,\"z\":207,\"vertices\":[{\"x\":811.5659402032697,\"y\":-323.971574086262}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1886168737\",\"smooth\":true,\"z\":208,\"vertices\":[{\"x\":770.8653825924407,\"y\":-345.9346567334389}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}}],\"title\":\"GT\",\"data\":{\"origin\":null},\"zoom\":0.6400000000000001,\"pan\":{\"x\":145.5408749999998,\"y\":110.40262499999989}}");
				if(json != null) {
				    com.bkahlert.nebula.jointjs.load(json);
				}
			}))
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
			.append($('<button>Get Links</button>').click(function () {
				console.log('getLinks();', com.bkahlert.nebula.jointjs.getLinks());
				console.log('getLinks(\’debugCustomClass\’);', com.bkahlert.nebula.jointjs.getLinks('debugCustomClass'));
				console.log('getLinks(\’unimportant\’);', com.bkahlert.nebula.jointjs.getLinks('unimportant'));
				console.log('graph.getLinks();', com.bkahlert.nebula.jointjs.graph.getLinks());
				console.log('graph.getLinks(\’debugCustomClass\’);', com.bkahlert.nebula.jointjs.graph.getLinks('debugCustomClass'));
				console.log('graph.getLinks(\’unimportant\’);', com.bkahlert.nebula.jointjs.graph.getLinks('unimportant'));
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
			com.bkahlert.nebula.jointjs.paper.setPan(100, 200);
			window.setTimeout(function() {
				com.bkahlert.nebula.jointjs.setPosition(c, 500, 500);
			}, 500);
			window.setTimeout(function() {
				com.bkahlert.nebula.jointjs.load("{\"cells\":[{\"type\":\"html.Element\",\"position\":{\"x\":-292,\"y\":119},\"size\":{\"width\":251,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775281\",\"title\":\"Entwurfsentscheidung (3 properties)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Dokumentation von ~ ist besonder wichtig f\u00FCr Top-Down-Lernen (erste Hypothesen ben\u00F6tigen besonders a...<\/div><\/div>\",\"content\":\"\",\"z\":0,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(198, 198, 57)\",\"border-color\":\"rgb(168, 168, 49)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-258,\"y\":166},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774919\",\"title\":\"Architektonisch<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":1,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 151, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 128, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775281\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774919\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775281|apiua:\/\/code\/-9223372036854774919\",\"smooth\":true,\"permanent\":true,\"z\":6,\"vertices\":[{\"x\":-177.25,\"y\":192}],\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775514\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775515|apiua:\/\/code\/-9223372036854775514\",\"smooth\":true,\"permanent\":true,\"z\":7,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-184,\"y\":324},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775514\",\"title\":\"Metafunktionen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Analyse:Sinn & Zweck von Metafunktionen\u00A0unklar, obwohl an mehreren Stellen der Versuch, diese zu erk...<\/div><\/div>\",\"content\":\"\",\"z\":8,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 110, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 93, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774919\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774919|apiua:\/\/code\/-9223372036854775515\",\"smooth\":true,\"permanent\":true,\"z\":8,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-183,\"y\":384},\"size\":{\"width\":211,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775579\",\"title\":\"Generische Programmierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Grund: ErweiterbarkeitDas global interface mit seinen global interface functions ist eine SeqAn zugr...<\/div><\/div>\",\"content\":\"\",\"z\":11,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 122, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 103, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-257,\"y\":500},\"size\":{\"width\":117,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774918\",\"title\":\"Sprachlich<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":11,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-182,\"y\":441},\"size\":{\"width\":193,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775412\",\"title\":\"Template-Spezialisierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Grund: PolymorphieTemplate-Spezialisierung:String<TValue, TSpec>Spec ist nicht nur atomarString<Dna>...<\/div><\/div>\",\"content\":\"\",\"z\":12,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 133, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 113, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775515|apiua:\/\/code\/-9223372036854775579\",\"smooth\":true,\"permanent\":true,\"z\":12,\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775412\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775515|apiua:\/\/code\/-9223372036854775412\",\"smooth\":true,\"permanent\":true,\"z\":13,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-224,\"y\":555},\"size\":{\"width\":112,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775611\",\"title\":\"Shortcuts<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Werden verst\u00E4ndlicher mit\u00A0existierendem\u00A0Fehlendes BenennungschemaDesign-Feature von\u00A0citep{GogolDori...<\/div><\/div>\",\"content\":\"\",\"z\":13,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 182, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 155, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775281\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775281|apiua:\/\/code\/-9223372036854774918\",\"smooth\":true,\"permanent\":true,\"z\":14,\"vertices\":[{\"x\":-183,\"y\":339}],\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774918|apiua:\/\/code\/-9223372036854775611\",\"smooth\":true,\"permanent\":true,\"z\":14,\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-224,\"y\":611},\"size\":{\"width\":237,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774848\",\"title\":\"Dom\u00E4nen-spezifische Benennung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Quelle SeqAn-BuchVerschiedene Dom\u00E4ne werden vermischt:z.B. technisches Vokabular: Allocfachlich:- bi...<\/div><\/div>\",\"content\":\"\",\"z\":15,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774918|apiua:\/\/code\/-9223372036854774848\",\"smooth\":true,\"permanent\":true,\"z\":16,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":340,\"y\":432},\"size\":{\"width\":176,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775448\",\"title\":\"Benennungsprobleme<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">1 Funktion - zwei Namen (infix substring),\u00A0substring vs. infix\u00A0,\u00A02 Funktionen - ein Name (globale Fu...<\/div><\/div>\",\"content\":\"\",\"z\":17,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 189, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 161, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-224,\"y\":264},\"size\":{\"width\":222,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775515\",\"title\":\"Templatemetaprogrammierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">siehe auch\u00A0Metafunktionen\u00A0Template Programming ist ein \u201Cselten\u201D gebrauchter bzw. unwissentlich gebra...<\/div><\/div>\",\"content\":\"\",\"z\":19,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 122, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 103, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-228,\"y\":669},\"size\":{\"width\":210,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774838\",\"title\":\"Datenstrukturmodifikationen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie erlaube ich API-Anwendern die Modifikation von Datenstrukturen.Bei SeqAn:Zwei Eigenschaften:Dire...<\/div><\/div>\",\"content\":\"\",\"z\":20,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(182, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(155, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774918\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774838\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774918|apiua:\/\/code\/-9223372036854774838\",\"smooth\":true,\"permanent\":true,\"z\":21,\"unimportant\":false,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":344,\"y\":719},\"size\":{\"width\":146,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775117\",\"title\":\"Syntaxprobleme<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":22,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 195, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 165, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":304,\"y\":78},\"size\":{\"width\":103,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774915\",\"title\":\"Struktur<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":24,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 186, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 158, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775448\",\"smooth\":true,\"permanent\":true,\"z\":30,\"vertices\":[{\"x\":391.75,\"y\":269}],\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775117\",\"smooth\":true,\"permanent\":true,\"z\":31,\"unimportant\":true,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":341,\"y\":169},\"size\":{\"width\":214,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775237\",\"title\":\"funktionsbezogene Probleme<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":32,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 183, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 156, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775237\",\"smooth\":true,\"permanent\":true,\"z\":35,\"vertices\":[{\"x\":401.75,\"y\":137.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":375,\"y\":210},\"size\":{\"width\":245,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775280\",\"title\":\"Fehlende Funktionskategorisierung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie sind Funktionen kategorisiert?Funktionen sind nicht kategorisiert.Die Zugeh\u00F6rigkeit mancher Funk...<\/div><\/div>\",\"content\":\"\",\"z\":36,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 181, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 154, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erh\u00F6ht Eintrittwahrscheinlichkeit\\n3 (3)\\nDie Verwendung ...\"}}}],\"id\":\"apiua:\/\/relation\/bc471l5bdmgjsbp92ljc2372aoil9olr\",\"smooth\":true,\"z\":36,\"vertices\":[{\"x\":210,\"y\":311}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/bnukj71m5n1lejph7g3vgng83cv6qbeo\",\"smooth\":true,\"z\":37,\"customClasses\":[],\"vertices\":[{\"x\":705,\"y\":256}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\\nWenn man nicht ...\"}}}],\"id\":\"apiua:\/\/relation\/koak8ole0n4vbudg45f8kar682o3e9hd\",\"smooth\":true,\"z\":38,\"customClasses\":[],\"vertices\":[{\"x\":671,\"y\":263}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/imei5mn1tgdebfrfhrbcsuqb0q2q1kfd\",\"smooth\":true,\"z\":39,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775280\",\"smooth\":true,\"permanent\":true,\"z\":40,\"vertices\":[{\"x\":472.75,\"y\":203.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n5 (5)\"}}}],\"id\":\"apiua:\/\/relation\/sl1hrvpiabeq61pfihufniil0q8bo7kn\",\"smooth\":true,\"z\":40,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":251},\"size\":{\"width\":246,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775544\",\"title\":\"Identifikation relevanter Funktionen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Welche Funktionen stehen mir zur Verf\u00FCgung?Anf\u00E4nger beschreiben, dass Sie anf\u00E4nglich nicht wissen, w...<\/div><\/div>\",\"content\":\"\",\"z\":41,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 182, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 155, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/el2lliolgockjahk0e0jrhjglq3n8avi\",\"smooth\":true,\"z\":41,\"vertices\":[{\"x\":204.20724445993318,\"y\":291.85727335897457}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775544\",\"smooth\":true,\"permanent\":true,\"z\":44,\"vertices\":[{\"x\":472.5,\"y\":224}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":295},\"size\":{\"width\":230,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775279\",\"title\":\"Funktionszweckunerkennbarkeit<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Macht die Funktion jetzt X oder Y?oder: 2 Funktionen brauchen auch 2 NamenDas Verhalten \/ der Zweck ...<\/div><\/div>\",\"content\":\"\",\"z\":45,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 183, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 156, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775279\",\"smooth\":true,\"permanent\":true,\"z\":48,\"vertices\":[{\"x\":468.5,\"y\":246}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":375,\"y\":336},\"size\":{\"width\":162,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775405\",\"title\":\"Funktionsgebrauch<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie verwende ich diese Funktion?Wenn man wei\u00DF, welche Funktion hilft aber nicht, wie diese anzuwende...<\/div><\/div>\",\"content\":\"\",\"z\":49,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 184, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 157, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775405\",\"smooth\":true,\"permanent\":true,\"z\":50,\"vertices\":[{\"x\":452,\"y\":266.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":373,\"y\":484},\"size\":{\"width\":186,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774860\",\"title\":\"Synonyme \/ Redundanz<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Shortcuts erzeugen Redundanzen und nervenZ.B. String<Dna> und DnaStringVon\u00A0GM: \\\"It&apos;s just stupid\u201D,\u00A0\u201C...<\/div><\/div>\",\"content\":\"\",\"z\":50,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 186, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 158, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":383},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775264\",\"title\":\"Funktionsweise<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Was macht die Funktion (intern)?Die Funktionsweise - also was tut die Funktion - einer Funktion ist ...<\/div><\/div>\",\"content\":\"\",\"z\":51,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 185, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 158, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774860\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854774860\",\"smooth\":true,\"permanent\":true,\"z\":51,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775237|apiua:\/\/code\/-9223372036854775264\",\"smooth\":true,\"permanent\":true,\"z\":52,\"vertices\":[{\"x\":446.5,\"y\":290}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":374,\"y\":532},\"size\":{\"width\":185,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774861\",\"title\":\"Abstraktionssuggestion<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der Name einer Sache suggeriert eine Abstraktion, die es nicht gibt.Beispiel: Peptide<\/div><\/div>\",\"content\":\"\",\"z\":52,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 187, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 159, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775623\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n5 (5)\"}}}],\"id\":\"apiua:\/\/relation\/ssghh5e2s6hrn5vrmj0b8ihofu81njf4\",\"smooth\":true,\"z\":52,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774861\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854774861\",\"smooth\":true,\"permanent\":true,\"z\":53,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775533\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/vv4l57ksh9tu3acqikdrqtler65aeqjm\",\"smooth\":true,\"z\":53,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":373,\"y\":580},\"size\":{\"width\":231,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775567\",\"title\":\"Ununterscheidbarkeit von Typen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">\u00C4hnliche Typen wie String, StringSet und array (z.B. SuffixArray) k\u00F6nnen nicht unterschieden werden....<\/div><\/div>\",\"content\":\"\",\"z\":54,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 188)\",\"border-color\":\"rgb(49, 168, 160)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775567\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854775567\",\"smooth\":true,\"permanent\":true,\"z\":54,\"vertices\":[{\"x\":458.25,\"y\":520}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774860\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n4 (4)\"}}}],\"id\":\"apiua:\/\/relation\/cu94r9kfkleo2sauj0fgt1hdsuh1dc3q\",\"smooth\":true,\"z\":54,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":371,\"y\":622},\"size\":{\"width\":179,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775623\",\"title\":\"Technisch vs. Fachlich<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Kuchenbecker:St\u00F6rt deutlich, dass ein biologischer Term mit einem informatischen Term gemischt wird ...<\/div><\/div>\",\"content\":\"\",\"z\":55,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 189, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 161, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775533\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/5pkeab04gpfffj2e2td9ic6i1lp4qta5\",\"smooth\":true,\"z\":55,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775623\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854775623\",\"smooth\":true,\"permanent\":true,\"z\":56,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775567\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/9huj9eb6vefpubjje9jn9vu75qdp9n92\",\"smooth\":true,\"z\":56,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":368,\"y\":668},\"size\":{\"width\":182,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775533\",\"title\":\"Benennungskonsistenz<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wenn schon Shortcuts, dann bitte auch konsistent<\/div><\/div>\",\"content\":\"\",\"z\":57,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 191, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 163, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775533\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775448|apiua:\/\/code\/-9223372036854775533\",\"smooth\":true,\"permanent\":true,\"z\":57,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775611\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774861\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/lhn440v7jt0ls5eke544jerkic74tr7c\",\"smooth\":true,\"z\":57,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775215\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/3eo08gokhoeu4k9283l2u8t1lb8e6s8q\",\"smooth\":true,\"z\":62,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":480,\"y\":-131},\"size\":{\"width\":150,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774830\",\"title\":\"Libraryerwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Erwartung geweckt durch SeqAn-Beschreibung als LibraryDetails siehe\u00A0Framework vs. Library\u00A0<\/div><\/div>\",\"content\":\"\",\"z\":65,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 134, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 114, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775413\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\\nreine Vermutung...\"}}}],\"id\":\"apiua:\/\/relation\/thq387lsd79dmprfs03flmkdtu23uh4b\",\"smooth\":true,\"z\":65,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-223,\"y\":217},\"size\":{\"width\":178,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775215\",\"title\":\"Framework vs. Library<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAn war urspr\u00FCnglich als Framework konzipiert.Marker:Ausspruch:\u00A0\u201Cin SeqAn l\u00F6sen\/programmieren\u201D, da...<\/div><\/div>\",\"content\":\"\",\"z\":67,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 157, 57, 0.27450980392156865)\",\"border-color\":\"rgba(168, 133, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774919\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775215\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774919|apiua:\/\/code\/-9223372036854775215\",\"smooth\":true,\"permanent\":true,\"z\":67,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":341,\"y\":123},\"size\":{\"width\":203,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775413\",\"title\":\"Sprachentit\u00E4tstypen \/ LETs<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Dem Anwender unbekannte LET \/ Konstrukte f\u00FChren zu Verst\u00E4ndnis- u. folglich zu Anwendungsschwierigke...<\/div><\/div>\",\"content\":\"\",\"z\":71,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 177, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 151, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774915\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775413\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774915|apiua:\/\/code\/-9223372036854775413\",\"smooth\":true,\"permanent\":true,\"z\":71,\"vertices\":[{\"x\":399,\"y\":114.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":217,\"y\":-456},\"size\":{\"width\":187,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774893\",\"title\":\"Anwender (7 properties)<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":75,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 57)\",\"border-color\":\"rgb(49, 168, 49)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-83,\"y\":-398},\"size\":{\"width\":273,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775596\",\"title\":\"T\u00E4tigkeitsbereich (Informatik ... Biologie)<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":82,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(117, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(100, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775596\",\"smooth\":true,\"permanent\":true,\"z\":83,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775494\",\"smooth\":true,\"permanent\":true,\"z\":83,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-84,\"y\":-310},\"size\":{\"width\":342,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775494\",\"title\":\"Paradigmatische Pr\u00E4gung (C++, Java, C++-Anf\u00E4nger)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">In welcher Sprache f\u00FChlt sich der Anwender zu Hause?C++Anwender hat einen C++-Hintergrund, was auf E...<\/div><\/div>\",\"content\":\"\",\"z\":84,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(97, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(83, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-86,\"y\":-267},\"size\":{\"width\":113,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775600\",\"title\":\"Arbeitsstil<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Verwendung von Personas citep{Pruitt:2003ki} nach cite{Stylos:2007jb} sehr gut geeignet f\u00FCr API-Us...<\/div><\/div>\",\"content\":\"\",\"z\":84,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(77, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(66, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775600\",\"smooth\":true,\"permanent\":true,\"z\":85,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-86,\"y\":-220},\"size\":{\"width\":322,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774892\",\"title\":\"Library-Vorerfahrung (Boost, cityhash, Biopython)<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":86,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 49, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774892\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854774892\",\"smooth\":true,\"permanent\":true,\"z\":87,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-84,\"y\":-354},\"size\":{\"width\":440,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775446\",\"title\":\"SeqAn-Anwendungskompetenz (wiedergebend, einfach, fortgeschritten)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie f\u00E4hig und sicher ist der Anwender im Gebrauch von SeqAn?Wiedergabe (Tutorials) (~ Bloom 1-2)Einf...<\/div><\/div>\",\"content\":\"\",\"z\":88,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 77, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 66, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775446\",\"smooth\":true,\"permanent\":true,\"z\":89,\"vertices\":[{\"x\":501.06439598216417,\"y\":-341.9263487714805}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":-86,\"y\":-174},\"size\":{\"width\":162,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774938\",\"title\":\"SeqAn-Einsatzform<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie wird technisch SeqAn eingesetzt?Ausdifferenzierung angesichts des Grounding zu interpretativ.<\/div><\/div>\",\"content\":\"\",\"z\":90,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 97, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 83, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774938\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854774938\",\"smooth\":true,\"permanent\":true,\"z\":92,\"vertices\":[{\"x\":152.75,\"y\":-301}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":410,\"y\":-400},\"size\":{\"width\":116,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775599\",\"title\":\"Motivation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Gr\u00FCnde sich mit SeqAn auseinanderzusetzen oder damit weiterhin zu arbeiten<\/div><\/div>\",\"content\":\"\",\"z\":93,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 117)\",\"border-color\":\"rgb(49, 168, 100)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":-48,\"y\":-127},\"size\":{\"width\":110,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775587\",\"title\":\"Helferlein<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Entwickler lernen SeqAn und sehen als Einsatzzweck die Entwicklung von persistenten Tools, die insbe...<\/div><\/div>\",\"content\":\"\",\"z\":93,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 92, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 79, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774893\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is property of\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774893|apiua:\/\/code\/-9223372036854775599\",\"smooth\":true,\"permanent\":true,\"z\":94,\"vertices\":[{\"x\":389.25,\"y\":-414}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774938\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775587\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774938|apiua:\/\/code\/-9223372036854775587\",\"smooth\":true,\"permanent\":true,\"z\":94,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":77,\"y\":-128},\"size\":{\"width\":295,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775598\",\"title\":\"Langj\u00E4hrige stabile Anwendungsentwicklung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAn wird seit mehreren Jahren verwendet, um regelm\u00E4\u00DFig verwendete Anwendungen zu entwickeln. Dabei...<\/div><\/div>\",\"content\":\"\",\"z\":95,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 102, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 87, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774938\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775598\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774938|apiua:\/\/code\/-9223372036854775598\",\"smooth\":true,\"permanent\":true,\"z\":96,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":-354},\"size\":{\"width\":199,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775407\",\"title\":\"Kennenlernen \/ Evaluation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Eng verwandt mit\u00A0Helferlein\u00A0Anwender wollen SeqAn zun\u00E4chst einmal kennenlernen \/ machen nicht den Ei...<\/div><\/div>\",\"content\":\"\",\"z\":97,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 119, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 101, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775407\",\"smooth\":true,\"permanent\":true,\"z\":98,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":478,\"y\":-308},\"size\":{\"width\":263,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775547\",\"title\":\"Suche nach professioneller Alternative<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der Anwender hat eine eigenen Sequenzanalyse-Tools entwickelt und testet, ob SeqAn ihm dabei helfen ...<\/div><\/div>\",\"content\":\"\",\"z\":99,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 102, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 86, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775547\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775407|apiua:\/\/code\/-9223372036854775547\",\"smooth\":true,\"permanent\":true,\"z\":100,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":753,\"y\":-307},\"size\":{\"width\":159,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775406\",\"title\":\"Genome Assembly<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Genome assembly refers to the process of taking a large number of short and putting them back togeth...<\/div><\/div>\",\"content\":\"\",\"z\":101,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 113, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 96, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775406\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775407|apiua:\/\/code\/-9223372036854775406\",\"smooth\":true,\"permanent\":true,\"z\":102,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":483,\"y\":-220},\"size\":{\"width\":122,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775297\",\"title\":\"M\u00E4chtigkeit<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Ziel von Gogol: Erweiterbarkeit (die ja dann zur M\u00E4chtigkeit f\u00FChrt)Anwender loben \/ stellen heraus d...<\/div><\/div>\",\"content\":\"\",\"z\":109,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 163, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 139, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775297\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775297\",\"smooth\":true,\"permanent\":true,\"z\":110,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":649,\"y\":-132},\"size\":{\"width\":181,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775568\",\"title\":\"M\u00E4chtigkeitserwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Anf\u00E4nger loben den Funktionsumfang von SeqAn. Mit SeqAn w\u00FCrde man viel gekapselte Funktionalit\u00E4t erh...<\/div><\/div>\",\"content\":\"\",\"z\":113,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 140, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 119, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775568\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775568\",\"smooth\":true,\"permanent\":true,\"z\":114,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1097,\"y\":-131},\"size\":{\"width\":181,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775298\",\"title\":\"Performanceerwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Anwender (insb. Anf\u00E4nger) glauben von SeqAn, es sei effizient - schnell.Das kann als Motivation dien...<\/div><\/div>\",\"content\":\"\",\"z\":115,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 151)\",\"border-color\":\"rgb(49, 168, 129)\",\"unimportant\":false,\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775298\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775298\",\"smooth\":true,\"permanent\":true,\"z\":116,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":845,\"y\":-132},\"size\":{\"width\":238,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775311\",\"title\":\"Benutzerfreundlichkeitserwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">~ wird mehr oder minder explizit als eine Erwartung formuliert.- bei\u00A0d3b2 - learningStyle\u00A0zum Beispi...<\/div><\/div>\",\"content\":\"\",\"z\":117,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 145, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 124, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775311\",\"smooth\":true,\"permanent\":true,\"z\":118,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":618,\"y\":-221},\"size\":{\"width\":128,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775552\",\"title\":\"Performance<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">\u201Cvergleichbares sucht man lange\u201D\u00A0 60ba - similiarSystems\\\"extremely efficient\u201D \u00A0 7c79 - workStepUnit\u201C...<\/div><\/div>\",\"content\":\"\",\"z\":119,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 181)\",\"border-color\":\"rgb(49, 168, 153)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775552\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854775552\",\"smooth\":true,\"permanent\":true,\"z\":120,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854774830\",\"smooth\":true,\"permanent\":true,\"z\":121,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":719,\"y\":83},\"size\":{\"width\":272,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774828\",\"title\":\"Produktbedingte Erwartungskonformit\u00E4t<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">NICHT GRILL, eigentlich Eigenschaft f\u00FCr alle Probleme.Separat gegliedert, um Theorie zu vereinfachen...<\/div><\/div>\",\"content\":\"\",\"z\":121,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 139, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 118, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":-176},\"size\":{\"width\":206,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774827\",\"title\":\"Produktbedingte Erwartung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Erwartung, die durch Produkt \/ -beschreibung beim Anwender entsteht<\/div><\/div>\",\"content\":\"\",\"z\":122,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 143, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 121, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854774830\",\"smooth\":true,\"permanent\":true,\"z\":123,\"vertices\":[{\"x\":554,\"y\":-139.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nBiopython\"}}}],\"id\":\"apiua:\/\/relation\/89dgrcs586tk7vsvfknsv034l0jodsvc\",\"smooth\":true,\"z\":123,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775568\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854775568\",\"smooth\":true,\"permanent\":true,\"z\":124,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775080\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n2 (2)\\nOOP-Hintergrund\"}}}],\"id\":\"apiua:\/\/relation\/17r5q0so7e80u9sndci61rcegjt90u38\",\"smooth\":true,\"z\":124,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775298\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854775298\",\"smooth\":true,\"permanent\":true,\"z\":125,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775080\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (3)\"}}}],\"id\":\"apiua:\/\/relation\/ohu1p0tojsaf6mi9o43pog3est14ofdu\",\"smooth\":true,\"z\":125,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774827|apiua:\/\/code\/-9223372036854775311\",\"smooth\":true,\"permanent\":true,\"z\":126,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":-263},\"size\":{\"width\":210,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774826\",\"title\":\"Produktspezifisches Feature<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Tats\u00E4chliches Feature, dass die weitere Verwendung des Produkts f\u00F6rdert \/ motiviert<\/div><\/div>\",\"content\":\"\",\"z\":126,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 166, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 141, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nLangj\u00E4hriger sp...\"}}}],\"id\":\"apiua:\/\/relation\/i2junc0k8gvb7lhp122eq980klf4nj5u\",\"smooth\":true,\"z\":126,\"vertices\":[{\"x\":11.5,\"y\":-31}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774827\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854774827\",\"smooth\":true,\"permanent\":true,\"z\":127,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774826\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775297\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774826|apiua:\/\/code\/-9223372036854775297\",\"smooth\":true,\"permanent\":true,\"z\":127,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/5hjekqu0olhfh532huq55dg67mfbk4n8\",\"smooth\":true,\"z\":127,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774826\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775552\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774826|apiua:\/\/code\/-9223372036854775552\",\"smooth\":true,\"permanent\":true,\"z\":128,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erwartet\\n3 (3)\\nOOP-Hintergrund\"}}}],\"id\":\"apiua:\/\/relation\/v0qaiccev68u2gokimdftv97schqa61f\",\"smooth\":true,\"z\":128,\"vertices\":[{\"x\":292,\"y\":-15.5}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775599\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774826\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775599|apiua:\/\/code\/-9223372036854774826\",\"smooth\":true,\"permanent\":true,\"z\":129,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775552\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verschmerzt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/6fd9s09lp94p05bje6l3g3fks8knme26\",\"smooth\":true,\"z\":129,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774828\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774828|apiua:\/\/code\/-9223372036854775633\",\"smooth\":true,\"permanent\":true,\"z\":130,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nGilt f\u00FCr pragma...\"}}}],\"id\":\"apiua:\/\/relation\/f2kms2c44b3tudk2om8nm257kqn3a6vj\",\"smooth\":true,\"z\":130,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":748,\"y\":126},\"size\":{\"width\":162,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774829\",\"title\":\"Projektorganisation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAn ist organisiert in Form von Core, Extras und Sandbox.Erstellung innerhalb von Framework NUR mi...<\/div><\/div>\",\"content\":\"\",\"z\":131,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 92, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 79, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774828\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774829\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774828|apiua:\/\/code\/-9223372036854774829\",\"smooth\":true,\"permanent\":true,\"z\":131,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774828\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775080\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774828|apiua:\/\/code\/-9223372036854775080\",\"smooth\":true,\"permanent\":true,\"z\":131,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":721,\"y\":333},\"size\":{\"width\":102,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774914\",\"title\":\"Laufzeit<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":131,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 186, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 158, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775280\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erwartet\\n3 (3)\\nOOP-Hintergrund\"}}}],\"id\":\"apiua:\/\/relation\/qrsbkamd2lm7nc87iugv1j8an2ha3ss1\",\"smooth\":true,\"z\":131,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":749,\"y\":171},\"size\":{\"width\":197,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775080\",\"title\":\"Inkonsistenzen bzgl. OOP<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":132,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 116, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 98, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":747,\"y\":213},\"size\":{\"width\":192,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775633\",\"title\":\"Inkonsistenzen bzgl. STL<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">SeqAns API entspricht nicht den Erwartungen der Anwender. Sie bem\u00E4ngeln, dass die API nicht wie die ...<\/div><\/div>\",\"content\":\"\",\"z\":132,\"customClasses\":[],\"color\":\"rgb(255, 255, 255)\",\"background-color\":\"rgb(57, 198, 139)\",\"border-color\":\"rgb(49, 168, 118)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":743,\"y\":380},\"size\":{\"width\":203,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775100\",\"title\":\"Compiler-Fehlermeldungen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die Anwender verstehen Compiler-Meldungen nicht.Viele Gruppendiskussionsteilnehmer stimmen zu.\u00A0a few...<\/div><\/div>\",\"content\":\"\",\"z\":132,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 192, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 163, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erlaubt Wahrnehmung von\\n10 (10)\\nWenn der Anwend...\"}}}],\"id\":\"apiua:\/\/relation\/m7c927ilp2ds6skudfo0627amlugghi1\",\"smooth\":true,\"z\":132,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774914\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775100\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774914|apiua:\/\/code\/-9223372036854775100\",\"smooth\":true,\"permanent\":true,\"z\":133,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nGilt f\u00FCr pragma...\"}}}],\"id\":\"apiua:\/\/relation\/en4pribolkkg7egnm7rdh065mq41qj5a\",\"smooth\":true,\"z\":133,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":742,\"y\":423},\"size\":{\"width\":193,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775615\",\"title\":\"Versagensverschleppung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">~\u00A0beschreibt, dass ein Versagen erst viel sp\u00E4ter sichtbar wird, als der Anwender erwarten w\u00FCrde. Die...<\/div><\/div>\",\"content\":\"\",\"z\":134,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 180, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 153, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/8t0fhejti77827o490prf08j3soaroa0\",\"smooth\":true,\"z\":134,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774914\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775615\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854774914|apiua:\/\/code\/-9223372036854775615\",\"smooth\":true,\"permanent\":true,\"z\":135,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774830\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774829\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/tptpd7is3kujht2d08o3vdpbde7ub3o9\",\"smooth\":true,\"z\":135,\"customClasses\":[\"important\"],\"unimportant\":true,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nWiedergabe\"}}}],\"id\":\"apiua:\/\/relation\/5pj50p0qslojnbv3ut1vpulj731ekvpa\",\"smooth\":true,\"z\":136,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (1)\\nExploration (Bl...\"}}}],\"id\":\"apiua:\/\/relation\/pthets4oehu6afuocrlgs3rgm7mgtfih\",\"smooth\":true,\"z\":137,\"vertices\":[{\"x\":30.05084381871116,\"y\":-23.525630888577552}],\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775579\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verursacht\\n3 (3)\\nDie jetztige Fo...\"}}}],\"id\":\"apiua:\/\/relation\/7fu79qaas2p0m5qkp2a9160agfiotm6j\",\"smooth\":true,\"z\":138,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":719,\"y\":513},\"size\":{\"width\":185,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775396\",\"title\":\"Werkzeugunterst\u00FCtzung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">NICHT VON GRILL (= BEITRAG)Alles zum Thema Integration von SeqAn in die IDE des Anwenderseb6d - pers...<\/div><\/div>\",\"content\":\"\",\"z\":152,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 139, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 118, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":740,\"y\":560},\"size\":{\"width\":230,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775148\",\"title\":\"Fehlende Autovervollst\u00E4ndigung<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die fehlende Autovervollst\u00E4ndigung wird bem\u00E4ngelt.Auch wenn andere Strategie verwendet wird, sagen d...<\/div><\/div>\",\"content\":\"\",\"z\":153,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 116, 198, 0.27450980392156865)\",\"border-color\":\"rgba(49, 98, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/ova0mpol7s2st1b8er93f946lld7funa\",\"smooth\":true,\"z\":153,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775396\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775148\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775396|apiua:\/\/code\/-9223372036854775148\",\"smooth\":true,\"permanent\":true,\"z\":154,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775396\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erfordert\\n2 (2)\\nOOP-Anwender (i...\"}}}],\"id\":\"apiua:\/\/relation\/m2hron3i1dh2f4m4epabucu6enicf5jb\",\"smooth\":true,\"z\":154,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":28,\"y\":973},\"size\":{\"width\":267,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775404\",\"title\":\"Dokumentation (Verbessert, Confusing)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die Online-Dokumentaton ist schwer zu finden.<\/div><\/div>\",\"content\":\"\",\"z\":155,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 163, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 138, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775577\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775279\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verst\u00E4rkt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/3shsibj2mp5kbknqqbcsg0fksuej8l4l\",\"smooth\":true,\"z\":155,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (1)\"}}}],\"id\":\"apiua:\/\/relation\/jn27d5k8d0bc8fjfvdu5hgju0i8bk74b\",\"smooth\":true,\"z\":156,\"customClasses\":[],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verringert Work-Step Unit\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1866845070\",\"smooth\":true,\"z\":157,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775587\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verwendet\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1946163892\",\"smooth\":true,\"z\":158,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775567\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-274351899\",\"smooth\":true,\"z\":159,\"vertices\":[{\"x\":476.7629310255273,\"y\":512.4322140064568}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":64,\"y\":1021},\"size\":{\"width\":206,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775572\",\"title\":\"Fehlender Gesamt\u00FCberblick<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Das Lernen von SeqAn erfordert Orientierungshilfen (z.B. \u00DCberblick), die aktuell nicht ausreichen.Be...<\/div><\/div>\",\"content\":\"\",\"z\":160,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 161, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 137, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-607855653\",\"smooth\":true,\"z\":160,\"vertices\":[{\"x\":432.02290869492225,\"y\":267.4569864098241}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775572\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775572\",\"smooth\":true,\"permanent\":true,\"z\":161,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1365749788\",\"smooth\":true,\"z\":161,\"vertices\":[{\"x\":308.14729506347203,\"y\":-27.301053433197048}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":64,\"y\":1068},\"size\":{\"width\":228,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775581\",\"title\":\"Fehlende Anwendungsbeispiele<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">In den Online-Quellen gibt es (viel) zu wenig Beispiele. Das erschwert das Verst\u00E4ndnis und die Arbei...<\/div><\/div>\",\"content\":\"\",\"z\":162,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 153, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 130, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775581\",\"smooth\":true,\"permanent\":true,\"z\":162,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774838\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n18 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1600091982\",\"smooth\":true,\"z\":162,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":63,\"y\":1116},\"size\":{\"width\":299,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775577\",\"title\":\"Fehlende Dokumentation der R\u00FCckgabetypen<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der R\u00FCckgabetyp von Funktionen wird sehr h\u00E4ufig nicht dokumentiert.Sehr h\u00E4ufig handelt es sich um R\u00FC...<\/div><\/div>\",\"content\":\"\",\"z\":163,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 157, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 133, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775577\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775577\",\"smooth\":true,\"permanent\":true,\"z\":163,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verschlechtert\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1074652199\",\"smooth\":true,\"z\":163,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":62,\"y\":1163},\"size\":{\"width\":206,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775504\",\"title\":\"Suchfunktion (Umst\u00E4ndlich)<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Die Suche wird als\u00A0umst\u00E4ndlich beschrieben.<\/div><\/div>\",\"content\":\"\",\"z\":164,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 165, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 140, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775494\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775572\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"erwartet\\n3 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-906600510\",\"smooth\":true,\"z\":164,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775504\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775504\",\"smooth\":true,\"permanent\":true,\"z\":165,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775514\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n3 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2031358344\",\"smooth\":true,\"z\":165,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":63,\"y\":1211},\"size\":{\"width\":105,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775271\",\"title\":\"Tutorials<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Assignments werden als Hilfreich beschrieben.Hier sind nur noch die \u00FCberarbeiteten Tutorials erfasst...<\/div><\/div>\",\"content\":\"\",\"z\":166,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(57, 198, 168, 0.27450980392156865)\",\"border-color\":\"rgba(49, 168, 143, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"verhindert\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1659680609\",\"smooth\":true,\"z\":166,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775404\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775271\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775404|apiua:\/\/code\/-9223372036854775271\",\"smooth\":true,\"permanent\":true,\"z\":167,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775515\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1246635787\",\"smooth\":true,\"z\":167,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2081713548\",\"smooth\":true,\"z\":168,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775544\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/957294190\",\"smooth\":true,\"z\":169,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775237\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-915611634\",\"smooth\":true,\"z\":170,\"vertices\":[{\"x\":471.97709130507775,\"y\":265.5430135901759}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774848\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1870562677\",\"smooth\":true,\"z\":171,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1405,\"y\":41},\"size\":{\"width\":117,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775441\",\"title\":\"Emotionen<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":172,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(139, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(118, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775314\",\"smooth\":true,\"permanent\":true,\"z\":188,\"vertices\":[{\"x\":1491.5,\"y\":152.5}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1423,\"y\":286},\"size\":{\"width\":119,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775115\",\"title\":\"Sarkasmus<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":189,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(145, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(123, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775115\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775115\",\"smooth\":true,\"permanent\":true,\"z\":190,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775623\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n4 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1493842278\",\"smooth\":true,\"z\":190,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775115\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1833319428\",\"smooth\":true,\"z\":190,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1426,\"y\":89},\"size\":{\"width\":116,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775447\",\"title\":\"Sympathie<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Der Entwickler mag SeqAn - einfach so.<\/div><\/div>\",\"content\":\"\",\"z\":191,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(87, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(74, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775405\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n6 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/2115050082\",\"smooth\":true,\"z\":191,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1421,\"y\":-282},\"size\":{\"width\":128,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854774824\",\"title\":\"Erlernbarkeit<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Wie leicht kann man SeqAn erlernen?<\/div><\/div>\",\"content\":\"\",\"z\":191,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(198, 57, 124, 0.27450980392156865)\",\"border-color\":\"rgba(168, 49, 105, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775447\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775447\",\"smooth\":true,\"permanent\":true,\"z\":192,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/883167155\",\"smooth\":true,\"z\":192,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1500398331\",\"smooth\":true,\"z\":192,\"vertices\":[{\"x\":1017.25,\"y\":-304}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1426,\"y\":133},\"size\":{\"width\":142,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775134\",\"title\":\"Verunsicherung<div class=\\\"details\\\"><\/div>\",\"content\":\"\",\"z\":193,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(133, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(113, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"dennoch\\n2 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/151131261\",\"smooth\":true,\"z\":193,\"vertices\":[{\"x\":1224.5,\"y\":-193}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775134\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775134\",\"smooth\":true,\"permanent\":true,\"z\":194,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775568\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"motiviert\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/740930886\",\"smooth\":true,\"z\":194,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1425,\"y\":183},\"size\":{\"width\":214,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775440\",\"title\":\"Unverst\u00E4ndnis \/ Genervt sein<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">Anwender k\u00F6nnen wenig Verst\u00E4ndnis daf\u00FCr aufbringen, dass SeqAn so komplex und schwierig zu lernen is...<\/div><\/div>\",\"content\":\"\",\"z\":195,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(98, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(84, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775440\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1068432899\",\"smooth\":true,\"z\":195,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775441\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775440\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"is a\"}}}],\"id\":\"apiua:\/\/code\/-9223372036854775441|apiua:\/\/code\/-9223372036854775440\",\"smooth\":true,\"permanent\":true,\"z\":195,\"vertices\":[{\"x\":1497.75,\"y\":126}],\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-609412344\",\"smooth\":true,\"z\":195,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775448\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775440\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n4 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-663948029\",\"smooth\":true,\"z\":196,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":1424,\"y\":236},\"size\":{\"width\":191,\"height\":28},\"angle\":0,\"id\":\"apiua:\/\/code\/-9223372036854775314\",\"title\":\"Frustration \/ Resignation<div class=\\\"details\\\"><div class=\\\"memo\\\"><img src=\\\"file:\/\/\/var\/folders\/19\/qzlff4ts3xn7qpdywz_6bqgr0000gn\/T\/ImageUtils2299345047035227872.png\\\">liegt erst ab folgender Schwelle vor:Gef\u00FChl tritt mehrfach einGef\u00FChl kann nur mit erheblichen Aufwan...<\/div><\/div>\",\"content\":\"\",\"z\":196,\"customClasses\":[],\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(110, 57, 198, 0.27450980392156865)\",\"border-color\":\"rgba(93, 49, 168, 0.39215686274509803)\",\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n0 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1599453106\",\"smooth\":true,\"z\":196,\"customClasses\":[\"proposed\"],\"vertices\":[{\"x\":810.5,\"y\":-304}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775264\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2128967051\",\"smooth\":true,\"z\":197,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775581\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"macht m\u00F6glich\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-163244303\",\"smooth\":true,\"z\":197,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775407\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"f\u00FChrt dennoch zu\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1766831944\",\"smooth\":true,\"z\":197,\"vertices\":[{\"x\":1018.7847449962236,\"y\":-323.9410269995446}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775633\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n2 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/246968158\",\"smooth\":true,\"z\":198,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/407687608\",\"smooth\":true,\"z\":199,\"customClasses\":[\"proposed\"],\"vertices\":[{\"x\":769.25,\"y\":-326}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775600\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Kontext\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-213750483\",\"smooth\":true,\"z\":200,\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-455427786\",\"smooth\":true,\"z\":201,\"customClasses\":[\"proposed\"],\"vertices\":[{\"x\":1502.25,\"y\":-9}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775311\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"bedingt\\n1 (0)\"}}}],\"id\":\"apiua:\/\/relation\/merged\/1443182131\",\"smooth\":true,\"z\":202,\"vertices\":[{\"x\":1218.9666113994517,\"y\":-212.21930307257065}],\"customClasses\":[\"proposed\"],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775117\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-2120514294\",\"smooth\":true,\"z\":203,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775298\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1164483283\",\"smooth\":true,\"z\":204,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854775314\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/774422231\",\"smooth\":true,\"z\":205,\"vertices\":[{\"x\":1522.2057883477896,\"y\":-10.329101733588308}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775504\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1015718930\",\"smooth\":true,\"z\":206,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775446\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-886008515\",\"smooth\":true,\"z\":207,\"vertices\":[{\"x\":811.5659402032697,\"y\":-323.971574086262}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua:\/\/code\/-9223372036854775596\"},\"target\":{\"id\":\"apiua:\/\/code\/-9223372036854774824\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"\"}}}],\"id\":\"apiua:\/\/relation\/merged\/-1886168737\",\"smooth\":true,\"z\":208,\"vertices\":[{\"x\":770.8653825924407,\"y\":-345.9346567334389}],\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}}],\"title\":\"GT\",\"data\":{\"origin\":null},\"zoom\":0.6400000000000001,\"pan\":{\"x\":145.5408749999998,\"y\":110.40262499999989}}");
			}, 600);
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
		
		getLinks: function(classes) {
			var links = [];
			_.each(com.bkahlert.nebula.jointjs.graph.getLinks(classes), function(link) {
				if(!link.get('permanent')) links.push(link.id);
			});
			return links;
		},
		
		getPermanentLinks: function(classes) {
			var links = [];
			_.each(com.bkahlert.nebula.jointjs.graph.getLinks(classes), function(link) {
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
		
		activateElementTools: function() {
			var hoveredId = null;
			$(document).on('mouseenter', 'svg .element', function() {
				hoveredId = $(this).attr('model-id');
			}).on('mouseleave', '[model-id]', function() {
				hoveredId = null;
			}).on('keydown', function(e) {
				if(hoveredId != null) {
					var cell = com.bkahlert.nebula.jointjs.graph.getCell(hoveredId);
					switch(e.keyCode) {
						case 32: // space
							com.bkahlert.nebula.jointjs.toggleCustomClasses(cell.id, 'unimportant');
							return false;
					}
				}
			});
		},
		
		activateLinkTools: function() {
			var hoveredId = null;
			$(document).on('mouseenter', 'svg .link[model-id]', function() {
				hoveredId = $(this).attr('model-id');
			}).on('mouseleave', '[model-id]', function() {
				hoveredId = null;
			}).on('keydown', function(e) {
				if(hoveredId != null) {
					var cell = com.bkahlert.nebula.jointjs.graph.getCell(hoveredId);
					switch(e.keyCode) {
						case 32: // space
							com.bkahlert.nebula.jointjs.toggleCustomClasses(cell.id, 'unimportant');
							return false;
						case 13: // enter
							if(!cell.get('permanent')) {
								com.bkahlert.nebula.jointjs.showTextChangePopup(hoveredId);
								hoveredId = null;
								return false;
							}
							break;
					}
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
			_.each(com.bkahlert.nebula.jointjs.graph.getCells(), function(cell) {
				if(!_.contains(ids, cell.get('id'))) return;
				
				var customClasses = cell.get('customClasses') || [];
				customClasses = _.union(customClasses, add);
				cell.set('customClasses', customClasses);
			});
		},
		
		toggleCustomClasses: function(ids, classes) {
			if(!_.isArray(classes)) classes = classes ? [classes] : [];
			_.each(com.bkahlert.nebula.jointjs.graph.getCells(), function(cell) {
				if(!_.contains(ids, cell.get('id'))) return;
				
				var customClasses = cell.get('customClasses') || [];
				_.each(classes, function(clazz) {
					if(_.contains(customClasses, clazz)) customClasses = _.without(customClasses, clazz);
					else customClasses = _.union(customClasses, [clazz]);
				});
				cell.set('customClasses', customClasses);
			});
		},
		
		removeCustomClasses: function(ids, remove) {
			if(!_.isArray(remove)) remove = remove ? [remove] : [];
			_.each(com.bkahlert.nebula.jointjs.graph.getCells(), function(cell) {
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
			ids = ids ? ids : [];
			
			if(ids.length == 0) $('html').removeClass('cellFocused');
			else $('html').addClass('cellFocused');
			
			_.each(com.bkahlert.nebula.jointjs.graph.getCells(), function(cell) {
				cell.set('focused', _.contains(ids, cell.get('id')));
			});
			
			// Workaround: Render all elements anew so neighbours of focused cells can also be updated.
			_.each(_.union(com.bkahlert.nebula.jointjs.oldSetFocusIds, ids), function(id) {
				_.each(com.bkahlert.nebula.jointjs.graph.getAdjancedCells(id), function(cell) {
					cell.trigger('change');
				});
			});
			com.bkahlert.nebula.jointjs.oldSetFocusIds = ids;
			
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
		if(_.some(com.bkahlert.nebula.jointjs.graph.getAdjancedCells(this.model), function(cell) { return cell.get('selected'); })) classes.push('selectedNeighbour');
		if(this.model.get('focused')) classes.push('focused');
		if(_.some(com.bkahlert.nebula.jointjs.graph.getAdjancedCells(this.model), function(cell) { return cell.get('focused'); })) classes.push('focusNeighbour');
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


// own functions
joint.dia.Graph.prototype.getCells = function() {
	return _.union(this.getElements(), this.getLinks());
}

joint.dia.Graph.prototype._getLinks = joint.dia.Graph.prototype.getLinks;
joint.dia.Graph.prototype.getLinks = function(classes) {
	var links = this._getLinks.apply(this, arguments);
	if(!_.isArray(classes)) classes = classes ? [classes] : [];
	
	if(classes.length > 0) {
		links = _.filter(links, function(link) {
			var customClasses = link.get('customClasses');
			if(!customClasses) customClasses = [];
			return _.filter(classes, function(clazz) {
				return _.contains(customClasses, clazz);
			}).length > 0;
		});
	}
	
	return links;
}

joint.dia.Graph.prototype.getAdjancedCells = function(cell, depth) {
	if(typeof cell === 'string') cell = this.getCell(cell);
	if(!depth) depth = 1;
	
	var adjancedCells = [];
	if(cell) {
		_.each(com.bkahlert.nebula.jointjs.graph.getConnectedLinks(cell), function(link) { adjancedCells.push(link); });
		
		var source = cell.get('source');
		var target = cell.get('target');
		source = source && source.hasOwnProperty('id') ? this.getCell(source.id) : null;
		target = target && target.hasOwnProperty('id') ? this.getCell(target.id) : null;
		
		if(source) adjancedCells.push(source);
		if(target) adjancedCells.push(target);
	}
	
	depth--;
	
	return adjancedCells;
	
	/* TODO
	else {
		var $this = this;
		_.each(_.clone(adjancedCells), function(adjancedCell) {
			_.each($this.getAdjancedCells(adjancedCell, depth), function(deepAdjancedCell) {
				if(!_.contains(adjancedCells, deepAdjancedCell)) adjancedCells.push(deepAdjancedCell);
			});
		});
		return adjancedCells;
	}
	*/
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
		if(_.some(com.bkahlert.nebula.jointjs.graph.getAdjancedCells(this.model, 2), function(cell) { return cell.get('selected'); })) classNames.push('selectNeighbour');
		if(this.model.get('focused')) classNames.push('focused');
		if(_.some(com.bkahlert.nebula.jointjs.graph.getAdjancedCells(this.model, 2), function(cell) { return cell.get('focused'); })) classNames.push('focusNeighbour');
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
