function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
            return pair[1];
        }
    }
    return (false);
}

CKEDITOR.editorConfig = function(config) {

    // WARNING: Do not load plugin clipboard. For some reason the backspace key does not work realiably anymore.
    // WARNING: Plugin maxheight makes the bottom breadcrumb bar only partly visible.

    // %REMOVE_START%
    // The configuration options below are needed when running CKEditor from source files.
    config.plugins = 'dialogui,dialog,a11yhelp,dialogadvtab,basicstyles,blockquote,button,panelbutton,panel,floatpanel,colorbutton,colordialog,templates,menu,contextmenu,div,resize,toolbar,elementspath,list,indent,enterkey,entities,popup,filebrowser,find,floatingspace,listblock,richcombo,font,fakeobjects,forms,format,htmlwriter,horizontalrule,image,justify,link,liststyle,magicline,maximize,newpage,pagebreak,pastetext,pastefromword,preview,print,removeformat,save,selectall,showblocks,showborders,sourcearea,specialchar,menubutton,scayt,stylescombo,tab,table,tabletools,undo,wsc,symbol,divarea';
    // original: config.plugins = 'dialogui,dialog,a11yhelp,dialogadvtab,basicstyles,blockquote,clipboard,button,panelbutton,panel,floatpanel,colorbutton,colordialog,templates,menu,contextmenu,div,resize,toolbar,elementspath,list,indent,enterkey,entities,popup,filebrowser,find,floatingspace,listblock,richcombo,font,fakeobjects,forms,format,htmlwriter,horizontalrule,image,justify,link,liststyle,magicline,maximize,newpage,pagebreak,pastetext,pastefromword,preview,print,removeformat,save,selectall,showblocks,showborders,sourcearea,specialchar,menubutton,scayt,stylescombo,tab,table,tabletools,undo,wsc,symbol,maxheight,divarea';
    // very reduced: config.plugins = 'dialogui,dialog,a11yhelp,dialogadvtab,basicstyles,blockquote,button,panelbutton,panel,floatpanel,colorbutton,colordialog,menu,contextmenu,toolbar,elementspath,list,indent,enterkey,entities,popup,filebrowser,find,floatingspace,listblock,richcombo,font,fakeobjects,format,htmlwriter,horizontalrule,image,justify,link,liststyle,magicline,maximize,pagebreak,removeformat,selectall,showblocks,showborders,sourcearea,specialchar,menubutton,scayt,stylescombo,tab,table,tabletools,undo,wsc,divarea';
    config.skin = 'moono';
    // %REMOVE_END%

    config.extraPlugins = 'onchange,codemirror,syntaxhighlight,standardtags';
    config.minimumChangeMilliseconds = 50;

    config.format_tags = 'h1;h2;h3;h4;h5;h6;p;pre';
    config.startupFocus = true;
    config.resize_enabled = false;

    var toolbarSet = getQueryVariable("toolbarSet");
    if (!toolbarSet)
        toolbarSet = "default";

    if (toolbarSet == "none") {
 //       config.startupFocus = false;
 //       config.readOnly = true;
        config.removePlugins = 'toolbar,elementspath';
    } else if (toolbarSet == "terminal") {
        config.toolbar = [{
            name : 'terminal',
            items : ['HorizontalRule', 'Styles', 'RemoveFormat', 'ShowBlocks', '-', 'Syntaxhighlight']
        }];
    } else {
        config.toolbar = [{
            name : 'justify',
            items : ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock']
        }, {
            name : 'styles',
            items : ['Styles', 'Format', 'RemoveFormat', 'ShowBlocks', '-', 'Syntaxhighlight']
        }, (!/[?&]internal=true/.test(location.href) ? {
            name : 'document',
            items : ['Source']
        } : '/'), '/', {
            name : 'editing',
            items : ['Find']
        }, {
            name : 'paragraph',
            items : ['NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote']
        }, {
            name : 'history',
            items : ['Undo', 'Redo']
        }, {
            name : 'links',
            items : ['Link', 'Anchor']
        }, {
            name : 'insert',
            items : ['Table', 'HorizontalRule']
        }, {
            name : 'colors',
            items : ['TextColor', 'BGColor']
        }];
    }

    config.keystrokes = [[CKEDITOR.ALT + 121/*F10*/, 'toolbarFocus'], [CKEDITOR.ALT + 122/*F11*/, 'elementsPathFocus'], [CKEDITOR.SHIFT + 121/*F10*/, 'contextMenu'], [CKEDITOR.CTRL + 90/*Z*/, 'undo'], [CKEDITOR.CTRL + 89/*Y*/, 'redo'], [CKEDITOR.CTRL + CKEDITOR.SHIFT + 90/*Z*/, 'redo'], [CKEDITOR.CTRL + 76/*L*/, 'link'], [CKEDITOR.CTRL + 66/*B*/, 'bold'], [CKEDITOR.CTRL + 73/*I*/, 'italic'], [CKEDITOR.CTRL + 85/*U*/, 'underline'], [CKEDITOR.ALT + 109/*-*/, 'toolbarCollapse']];
    config.keystrokes.push([CKEDITOR.CTRL + 49/*1*/, 'h1']);
    config.keystrokes.push([CKEDITOR.CTRL + 50/*2*/, 'h2']);
    config.keystrokes.push([CKEDITOR.CTRL + 51/*3*/, 'h3']);
    config.keystrokes.push([CKEDITOR.CTRL + 52/*4*/, 'h4']);
    config.keystrokes.push([CKEDITOR.CTRL + 53/*5*/, 'h5']);
    config.keystrokes.push([CKEDITOR.CTRL + 54/*6*/, 'h6']);
    config.keystrokes.push([CKEDITOR.CTRL + 55/*7*/, 'pre']);
    config.keystrokes.push([CKEDITOR.CTRL + 56/*8*/, 'pre']);
    config.keystrokes.push([CKEDITOR.CTRL + 57/*9*/, 'pre']);
    config.keystrokes.push([CKEDITOR.CTRL + 48/*0*/, 'p']);

    config.codemirror = {

        // Set this to the theme you wish to use (codemirror themes)
        theme : 'eclipse',

        // Whether or not you want to show line numbers
        lineNumbers : true,

        // Whether or not you want to use line wrapping
        lineWrapping : true,

        // Whether or not you want to highlight matching braces
        matchBrackets : true,

        // Whether or not you want tags to automatically close themselves
        autoCloseTags : false,

        // Whether or not to enable search tools, CTRL+F (Find), CTRL+SHIFT+F (Replace), CTRL+SHIFT+R (Replace All), CTRL+G (Find Next), CTRL+SHIFT+G (Find Previous)
        enableSearchTools : true,

        // Whether or not you wish to enable code folding (requires 'lineNumbers' to be set to 'true')
        enableCodeFolding : true,

        // Whether or not to enable code formatting
        enableCodeFormatting : true,

        // Whether or not to automatically format code should be done every time the source view is opened
        autoFormatOnStart : true,

        // Whether or not to automatically format code which has just been uncommented
        autoFormatOnUncomment : true,

        // Whether or not to highlight the currently active line
        highlightActiveLine : true,

        // Whether or not to highlight all matches of current word/selection
        highlightMatches : true,

        // Whether or not to display tabs
        showTabs : false,

        // Whether or not to show the format button on the toolbar
        showFormatButton : true,

        // Whether or not to show the comment button on the toolbar
        showCommentButton : true,

        // Whether or not to show the uncomment button on the toolbar
        showUncommentButton : true
    };

    // Define changes to default configuration here. For example:
    // config.language = 'fr';
    // config.uiColor = '#AADC6E';
};
