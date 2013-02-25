/**
 * Copyright Björn Kahlert, 2013
 */

(function($) {
    $.fn.extend({
        /**
         * Returns the number of characters between the
         * first element of the current jQuery elements
         * and the given node.
         *
         * e.g. <p>Hello <b>World</b>!</p>;
         * $("p").getCharactersBetween($("b")[0]) returns 7.
         *
         * @param node DOMElement
         */
        getCharactersBetween : function(element) {
            var root = this[0];
            var distance = 0;
            while (element != root) {
                while (element.previousSibling) {
                    element = element.previousSibling;
                    distance += $(element).text().length;
                }
                element = element.parentNode;
            }
            return distance;
        },

        /**
         * Returns the number of characters of the
         * selection start and end relative to the
         * first element of the current jQuery elements.
         *
         * e.g. <p>Hello <b>World</b>!</p>;
         * $("p").getSelectionOffsets(range) whereas range is the selected b-tag returns [7,12].
         */
        getSelectionOffsets : function(range) {
            var startNode = range.startContainer.$ ? range.startContainer.$ : range.startContainer;
            var startOffsetRelative = range.startOffset;
            var endNode = range.endContainer.$ ? range.endContainer.$ : range.endContainer;
            var endOffsetRelative = range.endOffset;
            return [
                this.getCharactersBetween(startNode) + startOffsetRelative,
                this.getCharactersBetween(endNode) + endOffsetRelative
            ];
        },

        /**
         * Returns the element that hold the offset'th element starting
         * relative to the first element of the current jQuery elements.
         *
         * e.g. <p>Hello <b>World</b>!</p>;
         * $("p").getElementFromOffset(7) returns { element: [b-tag], offset: 0 }.
         * $("p").getElementFromOffset(9) returns { element: [b-tag], offset: 2 }.
         *
         * @param {Object} offset
         */
        getElementFromOffset : function(offset) {
            function innermostChild(element) {
                while(element.firstChild) {
                    element = element.firstChild;
                }
                return element;
            }
            
            var rs = null;
            var node = this[0];
            var readTextLength = 0;
            while (rs === null) {
                var $children = $(node).contents();
                if ($children.length > 0) {
                    $children.each(function(i, child) {
                        $child = $(child);
                        var currentTextLength = $child.text().length;
                        if (readTextLength + currentTextLength < offset) {
                            readTextLength += currentTextLength;
                        } else {
                            node = child;
                            return false;
                        }
                    });
                } else {
                    var currentTextLength = $(node).text().length;
                    if (readTextLength <= offset && readTextLength + currentTextLength >= offset) {
                        rs = [
                            innermostChild(node),
                            offset - readTextLength
                        ];
                    } else {
                        alert("error");
                        rs = false;
                    }
                }
            }
            return rs;
        },

        /**
         * Returns the range that can be used to select everything
         * that lies between selectionOffsets.startOffset and selectionOffsets.endOffset
         * relative to the first element of the current jQuery elements.
         *
         * @param {Object} selectionOffsets
         */
        getRangeFromSelectionOffsets : function(selectionOffsets) {
            var start = this.getElementFromOffset(selectionOffsets[0]);
            var end = this.getElementFromOffset(selectionOffsets[1]);
            var range = document.createRange();
            range.setStart(start[0], start[1]);
            range.setEnd(end[0], end[1]);
            return range;
        },
        
        /**
         * Returns a selection object that can be used to restore a selection
         * based on the selected offsets.<br/>
         * This means, if the selection comprises characters 10 to 20, the restored selection
         * again comprises characters 10 to 20 ignoring all changes between the saving and the
         * restoration of the selection.
         */
        saveSelection : function() {
            var range;
            if ( typeof window.getSelection != "undefined") {
                range = window.getSelection().getRangeAt(0);
            } else if ( typeof document.selection != "undefined" && document.selection.type != "Control") {
                range = document.selection.createRange();
            } else {
                return null;
            }
            return this.getSelectionOffsets(range);
        },

        /**
         * Restores a formerly saved selection.
         */
        restoreSelection : function(selectionOffsets) {
            var range = this.getRangeFromSelectionOffsets(selectionOffsets);
            var selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
        }
    });
})(jQuery);
