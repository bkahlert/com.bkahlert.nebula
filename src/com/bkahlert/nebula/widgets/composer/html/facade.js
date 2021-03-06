var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.nebula = com.bkahlert.nebula || {};
com.bkahlert.nebula.editor = com.bkahlert.nebula.editor || {};
(function($) {
    /**
     * Façade for the core functionality of the CKEditor
     */
    $.extend(com.bkahlert.nebula.editor, {

        config: {
        	on: {
		        'instanceReady' : function(evt) {
		        	// pasting already handled by CKEditor - $(evt.editor.container.$).find('.cke_contents').attr('droppable', 'true');
		            com.bkahlert.nebula.editor.onready(evt);
		        },
		        'change': function(evt) {
	                com.bkahlert.nebula.editor.onchange(evt);
	            }
		    },
		    startupFocus: false,
        	readOnly: false
        },
        
        setTitle : function(name) {
        	var c = $(CKEDITOR.instances.editor1.container.$).find('.cke_bottom');
        	c.find('.title').remove();
        	$('<div class="title" style="position: absolute; left: 1em; right: 1em; text-align: right; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; ">' + name + '</div>')
        		.appendTo(c);
        },

        onready : function(e) {
                        
            $(window).resize(function() {
            	e.editor.resize($(window).width(), $(window).height());
            });
            $(window).resize();

            com.bkahlert.nebula.editor.setEnabled(!com.bkahlert.nebula.editor.config.readOnly);
 
            function turnOffTitle(editor) {
                var editable = editor.editable();
                if (editable && editable.isInline()) {
                    editable.changeAttr('aria-label', "");
                    editable.changeAttr('title', "");
                }
            }

            e.editor.on('mode', function(e) {
                turnOffTitle(e.editor);
            });
            turnOffTitle(e.editor);

            $(window).on('beforeunload', function() {
                com.bkahlert.nebula.editor.onchange();
            });

            $("html").addClass("ready");

            var internal = /[?&]internal=true/.test(location.href);

            if (!internal) {
                window["modified"] = function(content) {
                    console.log("change event: " + content.length);
                };
                com.bkahlert.nebula.editor.openDemo(e.editor);
            }
        },

        onchange : function() {
            if (window["modified"] && typeof window["modified"]) {
                window["modified"](com.bkahlert.nebula.editor.getSource());
            }
        },

        isDirty : function() {
            var editor = CKEDITOR.instances.editor1;
            // Checks whether the current editor contents present changes when compared
            // to the contents loaded into the editor at startup
            // http://docs.ckeditor.com/#!/api/CKEDITOR.editor-method-checkDirty
            return editor.checkDirty();
        },

        /**
         * Sets the editor's mode to the specified one.
         *
         * @param {Object} mode if of type boolean, true activates the wysiwyg mode whereas false activates the source mode.
         */
        setMode : function(mode) {
            var editor = CKEDITOR.instances.editor1;
            if ( typeof mode == "boolean")
                mode = mode ? "wysiwyg" : source;
            editor.setMode(mode);
        },

        selectAll : function() {
            var editor = CKEDITOR.instances.editor1;
            // Checks whether the current editor contents present changes when compared
            // to the contents loaded into the editor at startup
            // http://docs.ckeditor.com/#!/api/CKEDITOR.editor-method-checkDirty
            return editor.execCommand("selectAll");
        },

        setSource : function(html, restoreSelection, callback) {
            $('textarea').val(html);
            var editor = CKEDITOR.instances.editor1;
            if (!editor)
                return false;

            // Set editor contents (replace current contents).
            // http://docs.ckeditor.com/#!/api/CKEDITOR.editor-method-setData
            if (restoreSelection) {
                com.bkahlert.nebula.editor.saveSelection();
                editor.setData(html);
                try {
                    com.bkahlert.nebula.editor.restoreSelection();
                } catch(e) {
                    alert(e);
                }
                if ( typeof callback == "function")
                    callback();
            } else {
                editor.setData(html);
                if ( typeof callback == "function")
                    callback();
            }
            return true;
        },

        getSource : function() {
            var editor = CKEDITOR.instances.editor1;

            // Get editor contents
            // http://docs.ckeditor.com/#!/api/CKEDITOR.editor-method-getData
            return editor.getData();
        },

        showSource : function() {
            var editor = CKEDITOR.instances.editor1;
            editor.setMode('source');
        },

        hideSource : function() {
            var editor = CKEDITOR.instances.editor1;
            editor.setMode('wysiwyg');
        },

        setEnabled : function(isEnabled) {
        	com.bkahlert.nebula.editor.config.readOnly = !isEnabled;
        	//com.bkahlert.nebula.editor.config.startupFocus = isEnabled;
            var editor = CKEDITOR.instances.editor1;
            editor.setReadOnly(com.bkahlert.nebula.editor.config.readOnly);
            if(isEnabled) {
            	$(".cke_top, .cke_bottom").show();
            	$(window).resize();
            } else {
            	$(".cke_top, .cke_bottom").hide();
            	$(window).resize();
            }
            return true;
        },

        getPrevCaretCharacter : function() {
            var container = $(".cke_editable");
            try {
            	var selection = container.saveSelection();
            	return selection[0] > 0 ? container.text().substring(selection[0] - 1, selection[0]) : null;
            } catch(e) {
            	return null;
            }
        },

        savedSelection : null,

        saveSelection : function() {
            com.bkahlert.nebula.editor.savedSelection = $(".cke_editable").saveSelection();
            return com.bkahlert.nebula.editor.savedSelection;
        },

        restoreSelection : function() {
            if (com.bkahlert.nebula.editor.savedSelection) {
                $(".cke_editable").restoreSelection(com.bkahlert.nebula.editor.savedSelection);
            }
        },

        openDemo : function(editor) {
            $(".cke_inner").append($('<div style="position: absolute; top: 10px; right: 50px; z-index: 9999999;"><input type="button" class="testFunction" value="Custom Function" onClick="testFunction();"/></div>'));

            $.get('test.html', function(data) {
                com.bkahlert.nebula.editor.setSource(data, false, function() {
                    // callback

                });
            });

            window["mouseenter"] = function(html) {
                console.log("enter " + html);
            }
            window["mouseleave"] = function(html) {
                console.log("leave " + html);
            }
            
            com.bkahlert.nebula.editor.setTitle("I'm a <b>demo</b>");
        }
    });
})(jQuery);

CKEDITOR.replace('editor1', com.bkahlert.nebula.editor.config);

function testFunction() {
	com.bkahlert.nebula.editor.setEnabled(com.bkahlert.nebula.editor.config.readOnly);

/*
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
    /**/
}