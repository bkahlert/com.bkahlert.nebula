(function() {
    CKEDITOR.plugins.add("standardtags", {
        init : function(editor) {
            editor.addCommand("h1", {
                exec : function(editor) {
                    var format = {
                        element : 'h1'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("h2", {
                exec : function(editor) {
                    var format = {
                        element : 'h2'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("h3", {
                exec : function(editor) {
                    var format = {
                        element : 'h3'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("h4", {
                exec : function(editor) {
                    var format = {
                        element : 'h4'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("h5", {
                exec : function(editor) {
                    var format = {
                        element : 'h5'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("h6", {
                exec : function(editor) {
                    var format = {
                        element : 'h6'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("pre", {
                exec : function(editor) {
                    var format = {
                        element : 'pre'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
            editor.addCommand("p", {
                exec : function(editor) {
                    var format = {
                        element : 'p'
                    };
                    var style = new CKEDITOR.style(format);
                    style.apply(editor.document);
                }
            });
        }
    });
})();
