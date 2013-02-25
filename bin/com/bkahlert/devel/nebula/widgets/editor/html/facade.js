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

        onready : function(e) {
            e.editor.execCommand('maximize');
            e.editor.on('change', function(e) {
                com.bkahlert.devel.nebula.editor.onchange();
            });
            $(window).on('beforeunload', function() {
                com.bkahlert.devel.nebula.editor.onchange();
            });

            var internal = /[?&]internal=true/.test(location.href);

            $("html").addClass("ready");

            if (!internal) {
                window["modified"] = function(content) {
                    console.log("change event: " + content.length);
                };
                com.bkahlert.devel.nebula.editor.openDemo();
            }
        },

        onchange : function() {
            if (com.bkahlert.devel.nebula.editor.isDirty() && window["modified"] && typeof window["modified"])
                window["modified"](com.bkahlert.devel.nebula.editor.getSource());
        },

        isDirty : function() {
            var editor = CKEDITOR.instances.editor1;
            // Checks whether the current editor contents present changes when compared
            // to the contents loaded into the editor at startup
            // http://docs.ckeditor.com/#!/api/CKEDITOR.editor-method-checkDirty
            return editor.checkDirty();
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
            var editor = CKEDITOR.instances.editor1;
            editor.setReadOnly(!isEnabled);
        },
        
        getPrevCaretCharacter : function() {
            var container = $(".cke_editable");
            var selection = container.saveSelection();
            return selection[0] > 0 ? container.text().substring(selection[0]-1, selection[0]) : null;
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

        openDemo : function() {
            $("body").append($('<div style="position: absolute; top: 10px; right: 50px; z-index: 200001;"><input type="button" class="testFunction" value="Custom Function" onClick="testFunction();"/></div>'));

            com.bkahlert.devel.nebula.editor.setSource("<h1><img alt=\"Saturn V carrying Apollo 11\" class=\"right\" src=\"samples/assets/sample.jpg\" /> Apollo 11</h1><p><b>Apollo 11</b> was the spaceflight that landed the first humans, Americans <a href=\"http://en.wikipedia.org/wiki/Neil_Armstrong\" title=\"Neil Armstrong\">Neil Armstrong</a> and <a href=\"http://en.wikipedia.org/wiki/Buzz_Aldrin\" title=\"Buzz Aldrin\">Buzz Aldrin</a>, on the Moon on July 20, 1969, at 20:18 UTC. Armstrong became the first to step onto the lunar surface 6 hours later on July 21 at 02:56 UTC.</p><p>Armstrong spent about <strike>three and a half</strike> two and a half hours outside the spacecraft, Aldrin slightly less; and together they collected 47.5 pounds (21.5&nbsp;kg) of lunar material for return to Earth. A third member of the mission, <a href=\"http://en.wikipedia.org/wiki/Michael_Collins_(astronaut)\" title=\"Michael Collins (astronaut)\">Michael Collins</a>, piloted the <a href=\"http://en.wikipedia.org/wiki/Apollo_Command/Service_Module\" title=\"Apollo Command/Service Module\">command</a> spacecraft alone in lunar orbit until Armstrong and Aldrin returned to it for the trip back to Earth.</p><h2>Broadcasting and <em>quotes</em> <a id=\"quotes\" name=\"quotes\"></a></h2><p>Broadcast on live TV to a world-wide audience, Armstrong stepped onto the lunar surface and described the event as:</p><blockquote> <p>One small step for [a] man, one giant leap for mankind.</p> </blockquote><p>Apollo 11 effectively ended the <a href=\"http://en.wikipedia.org/wiki/Space_Race\" title=\"Space Race\">Space Race</a> and fulfilled a national goal proposed in 1961 by the late U.S. President <a href=\"http://en.wikipedia.org/wiki/John_F._Kennedy\" title=\"John F. Kennedy\">John F. Kennedy</a> in a speech before the United States Congress:</p><blockquote> <p>[...] before this decade is out, of landing a man on the Moon and returning him safely to the Earth.</p> </blockquote><h2>Technical details <a id=\"tech-details\" name=\"tech-details\"></a></h2><table align=\"right\" border=\"1\" bordercolor=\"#ccc\" cellpadding=\"5\" cellspacing=\"0\" style=\"border-collapse:collapse;margin:10px 0 10px 15px;\"> <caption><strong>Mission crew</strong></caption> <thead> <tr> <th scope=\"col\">Position</th> <th scope=\"col\">Astronaut</th> </tr> </thead> <tbody> <tr> <td>Commander</td> <td>Neil A. Armstrong</td> </tr> <tr> <td>Command Module Pilot</td> <td>Michael Collins</td> </tr> <tr> <td>Lunar Module Pilot</td> <td>Edwin &quot;Buzz&quot; E. Aldrin, Jr.</td> </tr> </tbody> </table><p>Launched by a <strong>Saturn V</strong> rocket from <a href=\"http://en.wikipedia.org/wiki/Kennedy_Space_Center\" title=\"Kennedy Space Center\">Kennedy Space Center</a> in Merritt Island, Florida on July 16, Apollo 11 was the fifth manned mission of <a href=\"http://en.wikipedia.org/wiki/NASA\" title=\"NASA\">NASA</a>&#39;s Apollo program. The Apollo spacecraft had three parts:</p><ol> <li><strong>Command Module</strong> with a cabin for the three astronauts which was the only part which landed back on Earth</li> <li><strong>Service Module</strong> which supported the Command Module with propulsion, electrical power, oxygen and water</li> <li><strong>Lunar Module</strong> for landing on the Moon.</li> </ol><p>After being sent to the Moon by the Saturn V&#39;s upper stage, the astronauts separated the spacecraft from it and travelled for three days until they entered into lunar orbit. Armstrong and Aldrin then moved into the Lunar Module and landed in the <a href=\"http://en.wikipedia.org/wiki/Mare_Tranquillitatis\" title=\"Mare Tranquillitatis\">Sea of Tranquility</a>. They stayed a total of about 21 and a half hours on the lunar surface. After lifting off in the upper part of the Lunar Module and rejoining Collins in the Command Module, they returned to Earth and landed in the <a href=\"http://en.wikipedia.org/wiki/Pacific_Ocean\" title=\"Pacific Ocean\">Pacific Ocean</a> on July 24.</p><hr /> <p style=\"text-align: right;\"><small>Source: <a href=\"http://en.wikipedia.org/wiki/Apollo_11\">Wikipedia.org</a></small></p> <h1><img alt=\"Saturn V carrying Apollo 11\" class=\"right\" src=\"samples/assets/sample.jpg\" /> Apollo 11</h1><p><b>Apollo 11</b> was the spaceflight that landed the first humans, Americans <a href=\"http://en.wikipedia.org/wiki/Neil_Armstrong\" title=\"Neil Armstrong\">Neil Armstrong</a> and <a href=\"http://en.wikipedia.org/wiki/Buzz_Aldrin\" title=\"Buzz Aldrin\">Buzz Aldrin</a>, on the Moon on July 20, 1969, at 20:18 UTC. Armstrong became the first to step onto the lunar surface 6 hours later on July 21 at 02:56 UTC.</p><p>Armstrong spent about <strike>three and a half</strike> two and a half hours outside the spacecraft, Aldrin slightly less; and together they collected 47.5 pounds (21.5&nbsp;kg) of lunar material for return to Earth. A third member of the mission, <a href=\"http://en.wikipedia.org/wiki/Michael_Collins_(astronaut)\" title=\"Michael Collins (astronaut)\">Michael Collins</a>, piloted the <a href=\"http://en.wikipedia.org/wiki/Apollo_Command/Service_Module\" title=\"Apollo Command/Service Module\">command</a> spacecraft alone in lunar orbit until Armstrong and Aldrin returned to it for the trip back to Earth.</p><h2>Broadcasting and <em>quotes</em> <a id=\"quotes\" name=\"quotes\"></a></h2><p>Broadcast on live TV to a world-wide audience, Armstrong stepped onto the lunar surface and described the event as:</p><blockquote> <p>One small step for [a] man, one giant leap for mankind.</p> </blockquote><p>Apollo 11 effectively ended the <a href=\"http://en.wikipedia.org/wiki/Space_Race\" title=\"Space Race\">Space Race</a> and fulfilled a national goal proposed in 1961 by the late U.S. President <a href=\"http://en.wikipedia.org/wiki/John_F._Kennedy\" title=\"John F. Kennedy\">John F. Kennedy</a> in a speech before the United States Congress:</p><blockquote> <p>[...] before this decade is out, of landing a man on the Moon and returning him safely to the Earth.</p> </blockquote><h2>Technical details <a id=\"tech-details\" name=\"tech-details\"></a></h2><table align=\"right\" border=\"1\" bordercolor=\"#ccc\" cellpadding=\"5\" cellspacing=\"0\" style=\"border-collapse:collapse;margin:10px 0 10px 15px;\"> <caption><strong>Mission crew</strong></caption> <thead> <tr> <th scope=\"col\">Position</th> <th scope=\"col\">Astronaut</th> </tr> </thead> <tbody> <tr> <td>Commander</td> <td>Neil A. Armstrong</td> </tr> <tr> <td>Command Module Pilot</td> <td>Michael Collins</td> </tr> <tr> <td>Lunar Module Pilot</td> <td>Edwin &quot;Buzz&quot; E. Aldrin, Jr.</td> </tr> </tbody> </table><p>Launched by a <strong>Saturn V</strong> rocket from <a href=\"http://en.wikipedia.org/wiki/Kennedy_Space_Center\" title=\"Kennedy Space Center\">Kennedy Space Center</a> in Merritt Island, Florida on July 16, Apollo 11 was the fifth manned mission of <a href=\"http://en.wikipedia.org/wiki/NASA\" title=\"NASA\">NASA</a>&#39;s Apollo program. The Apollo spacecraft had three parts:</p><ol> <li><strong>Command Module</strong> with a cabin for the three astronauts which was the only part which landed back on Earth</li> <li><strong>Service Module</strong> which supported the Command Module with propulsion, electrical power, oxygen and water</li> <li><strong>Lunar Module</strong> for landing on the Moon.</li> </ol><p>After being sent to the Moon by the Saturn V&#39;s upper stage, the astronauts separated the spacecraft from it and travelled for three days until they entered into lunar orbit. Armstrong and Aldrin then moved into the Lunar Module and landed in the <a href=\"http://en.wikipedia.org/wiki/Mare_Tranquillitatis\" title=\"Mare Tranquillitatis\">Sea of Tranquility</a>. They stayed a total of about 21 and a half hours on the lunar surface. After lifting off in the upper part of the Lunar Module and rejoining Collins in the Command Module, they returned to Earth and landed in the <a href=\"http://en.wikipedia.org/wiki/Pacific_Ocean\" title=\"Pacific Ocean\">Pacific Ocean</a> on July 24.</p><hr /> <p style=\"text-align: right;\"><small>Source: <a href=\"http://en.wikipedia.org/wiki/Apollo_11\">Wikipedia.org</a></small></p>", false, function() {

            });
        }
    });
})(jQuery);

$(document).ready(function() {
    CKEDITOR.replace('editor1', {
        on : {
            'instanceReady' : function(evt) {
                com.bkahlert.devel.nebula.editor.onready(evt);
            }
        }
    });
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