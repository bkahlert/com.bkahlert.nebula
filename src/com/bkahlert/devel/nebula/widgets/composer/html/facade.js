var com = com || {};
com.bkahlert = com.bkahlert || {};
com.bkahlert.devel = com.bkahlert.devel || {};
com.bkahlert.devel.nebula = com.bkahlert.devel.nebula || {};
com.bkahlert.devel.nebula.editor = com.bkahlert.devel.nebula.editor || {};
(function($) {
    /**
     * Façade for the core functionality of the CKEditor
     */
    $.extend(com.bkahlert.devel.nebula.editor, {

        initEnabled : true,

        onready : function(e) {
            com.bkahlert.devel.nebula.editor.setEnabled(com.bkahlert.devel.nebula.editor.initEnabled);
            e.editor.execCommand('maximize');

            $("html").addClass("ready");

            e.editor.on('change', function(e) {
                com.bkahlert.devel.nebula.editor.onchange();
            });

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
                com.bkahlert.devel.nebula.editor.onchange();
            });

            var internal = /[?&]internal=true/.test(location.href);

            if (!internal) {
                window["modified"] = function(content) {
                    console.log("change event: " + content.length);
                };
                com.bkahlert.devel.nebula.editor.openDemo(e.editor);
            }
        },

        onchange : function() {
            if (window["modified"] && typeof window["modified"]) {
                window["modified"](com.bkahlert.devel.nebula.editor.getSource());
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
                return;

            // Set editor contents (replace current contents).
            // http://docs.ckeditor.com/#!/api/CKEDITOR.editor-method-setData
            if (restoreSelection) {
                com.bkahlert.devel.nebula.editor.saveSelection();
                editor.setData(html);
                try {
                    com.bkahlert.devel.nebula.editor.restoreSelection();
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
            // TODO disabling and then enabling does not enable
            return;

            if ($("html").hasClass("ready")) {
                var editor = CKEDITOR.instances.editor1;
                editor.setReadOnly(!isEnabled);
            } else {
                com.bkahlert.devel.nebula.editor.initEnabled = isEnabled;
            }
        },

        getPrevCaretCharacter : function() {
            var container = $(".cke_editable");
            var selection = container.saveSelection();
            return selection[0] > 0 ? container.text().substring(selection[0] - 1, selection[0]) : null;
        },

        savedSelection : null,

        saveSelection : function() {
            com.bkahlert.devel.nebula.editor.savedSelection = $(".cke_editable").saveSelection();
            return com.bkahlert.devel.nebula.editor.savedSelection;
        },

        restoreSelection : function() {
            if (com.bkahlert.devel.nebula.editor.savedSelection) {
                $(".cke_editable").restoreSelection(com.bkahlert.devel.nebula.editor.savedSelection);
            }
        },

        openDemo : function(editor) {
            $("body").append($('<div style="position: absolute; top: 10px; right: 50px; z-index: 200001;"><input type="button" class="testFunction" value="Custom Function" onClick="testFunction();"/></div>'));

            $.get('test.html', function(data) {
                com.bkahlert.devel.nebula.editor.setSource(data, false, function() {
                    // callback

                });
            });

            window["mouseenter"] = function(html) {
                console.log("enter " + html);
            }
            window["mouseleave"] = function(html) {
                console.log("leave " + html);
            }
        }
    });
})(jQuery);

CKEDITOR.replace('editor1', {
    on : {
        'instanceReady' : function(evt) {
            com.bkahlert.devel.nebula.editor.onready(evt);
        }
    }
});

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