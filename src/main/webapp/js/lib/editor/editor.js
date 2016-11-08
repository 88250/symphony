(function (global) {
    var listRE = /^(\s*)([*+-]|(\d+)\.)([\w+(\s+\w+)]|[\s*])/,
            emptyListRE = /^(\s*)([*+-]|(\d+)\.)(\s*)$/,
            unorderedBullets = '*+-';

    var inListState = function (cm, pos) {
        return cm.getStateAfter(pos.line).list || null;
    };

    var inListOrNot = function (cm) {
        var pos = cm.getCursor();
        return inListState(cm, pos);
    };

    CodeMirror.commands.shiftTabAndIndentContinueMarkdownList = function (cm) {
        var inList = inListOrNot(cm);

        if (inList !== null) {
            cm.execCommand('insertTab');
            return;
        }

        cm.execCommand('indentLess');
    };

    CodeMirror.commands.tabAndIndentContinueMarkdownList = function (cm) {
        var inList = inListOrNot(cm);

        if (inList !== null) {
            cm.execCommand('insertTab');
            return;
        }

        cm.execCommand('indentMore');
    };

    CodeMirror.commands.newlineAndIndentContinueMarkdownList = function (cm) {
        var pos, tok, match, emptyMatch, inList;

        pos = cm.getCursor();
        tok = cm.getTokenAt(pos);
        emptyMatch = cm.getLine(pos.line).match(emptyListRE);
        inList = inListState(cm, pos);

        if (!inList && emptyMatch) {
            cm.replaceRange("", {line: pos.line, ch: tok.start}, {line: pos.line, ch: tok.end});
            cm.execCommand('delLineLeft');
            cm.execCommand('newlineAndIndent');
            return;
        }

        if (!inList || !(match = cm.getLine(pos.line).match(listRE))) {
            cm.execCommand('newlineAndIndent');
            return;
        }

        var indent = match[1], after = " ";
        var bullet = unorderedBullets.indexOf(match[2]) >= 0
                ? match[2]
                : (parseInt(match[3], 10) + 1) + '.';

        cm.replaceSelection('\n' + indent + bullet + after, 'end');
    };

    CodeMirror.defineMode("xml", function (config, parserConfig) {
        var indentUnit = config.indentUnit;
        var multilineTagIndentFactor = parserConfig.multilineTagIndentFactor || 1;
        var multilineTagIndentPastTag = parserConfig.multilineTagIndentPastTag || true;

        var Kludges = parserConfig.htmlMode ? {
            autoSelfClosers: {'area': true, 'base': true, 'br': true, 'col': true, 'command': true,
                'embed': true, 'frame': true, 'hr': true, 'img': true, 'input': true,
                'keygen': true, 'link': true, 'meta': true, 'param': true, 'source': true,
                'track': true, 'wbr': true},
            implicitlyClosed: {'dd': true, 'li': true, 'optgroup': true, 'option': true, 'p': true,
                'rp': true, 'rt': true, 'tbody': true, 'td': true, 'tfoot': true,
                'th': true, 'tr': true},
            contextGrabbers: {
                'dd': {'dd': true, 'dt': true},
                'dt': {'dd': true, 'dt': true},
                'li': {'li': true},
                'option': {'option': true, 'optgroup': true},
                'optgroup': {'optgroup': true},
                'p': {'address': true, 'article': true, 'aside': true, 'blockquote': true, 'dir': true,
                    'div': true, 'dl': true, 'fieldset': true, 'footer': true, 'form': true,
                    'h1': true, 'h2': true, 'h3': true, 'h4': true, 'h5': true, 'h6': true,
                    'header': true, 'hgroup': true, 'hr': true, 'menu': true, 'nav': true, 'ol': true,
                    'p': true, 'pre': true, 'section': true, 'table': true, 'ul': true},
                'rp': {'rp': true, 'rt': true},
                'rt': {'rp': true, 'rt': true},
                'tbody': {'tbody': true, 'tfoot': true},
                'td': {'td': true, 'th': true},
                'tfoot': {'tbody': true},
                'th': {'td': true, 'th': true},
                'thead': {'tbody': true, 'tfoot': true},
                'tr': {'tr': true}
            },
            doNotIndent: {"pre": true},
            allowUnquoted: true,
            allowMissing: true
        } : {
            autoSelfClosers: {},
            implicitlyClosed: {},
            contextGrabbers: {},
            doNotIndent: {},
            allowUnquoted: false,
            allowMissing: false
        };
        var alignCDATA = parserConfig.alignCDATA;

        // Return variables for tokenizers
        var tagName, type;

        function inText(stream, state) {
            function chain(parser) {
                state.tokenize = parser;
                return parser(stream, state);
            }

            var ch = stream.next();
            if (ch == "<") {
                if (stream.eat("!")) {
                    if (stream.eat("[")) {
                        if (stream.match("CDATA["))
                            return chain(inBlock("atom", "]]>"));
                        else
                            return null;
                    } else if (stream.match("--")) {
                        return chain(inBlock("comment", "-->"));
                    } else if (stream.match("DOCTYPE", true, true)) {
                        stream.eatWhile(/[\w\._\-]/);
                        return chain(doctype(1));
                    } else {
                        return null;
                    }
                } else if (stream.eat("?")) {
                    stream.eatWhile(/[\w\._\-]/);
                    state.tokenize = inBlock("meta", "?>");
                    return "meta";
                } else {
                    var isClose = stream.eat("/");
                    tagName = "";
                    var c;
                    while ((c = stream.eat(/[^\s\u00a0=<>\"\'\/?]/)))
                        tagName += c;
                    if (!tagName)
                        return "error";
                    type = isClose ? "closeTag" : "openTag";
                    state.tokenize = inTag;
                    return "tag";
                }
            } else if (ch == "&") {
                var ok;
                if (stream.eat("#")) {
                    if (stream.eat("x")) {
                        ok = stream.eatWhile(/[a-fA-F\d]/) && stream.eat(";");
                    } else {
                        ok = stream.eatWhile(/[\d]/) && stream.eat(";");
                    }
                } else {
                    ok = stream.eatWhile(/[\w\.\-:]/) && stream.eat(";");
                }
                return ok ? "atom" : "error";
            } else {
                stream.eatWhile(/[^&<]/);
                return null;
            }
        }

        function inTag(stream, state) {
            var ch = stream.next();
            if (ch == ">" || (ch == "/" && stream.eat(">"))) {
                state.tokenize = inText;
                type = ch == ">" ? "endTag" : "selfcloseTag";
                return "tag";
            } else if (ch == "=") {
                type = "equals";
                return null;
            } else if (ch == "<") {
                return "error";
            } else if (/[\'\"]/.test(ch)) {
                state.tokenize = inAttribute(ch);
                state.stringStartCol = stream.column();
                return state.tokenize(stream, state);
            } else {
                stream.eatWhile(/[^\s\u00a0=<>\"\']/);
                return "word";
            }
        }

        function inAttribute(quote) {
            var closure = function (stream, state) {
                while (!stream.eol()) {
                    if (stream.next() == quote) {
                        state.tokenize = inTag;
                        break;
                    }
                }
                return "string";
            };
            closure.isInAttribute = true;
            return closure;
        }

        function inBlock(style, terminator) {
            return function (stream, state) {
                while (!stream.eol()) {
                    if (stream.match(terminator)) {
                        state.tokenize = inText;
                        break;
                    }
                    stream.next();
                }
                return style;
            };
        }
        function doctype(depth) {
            return function (stream, state) {
                var ch;
                while ((ch = stream.next()) != null) {
                    if (ch == "<") {
                        state.tokenize = doctype(depth + 1);
                        return state.tokenize(stream, state);
                    } else if (ch == ">") {
                        if (depth == 1) {
                            state.tokenize = inText;
                            break;
                        } else {
                            state.tokenize = doctype(depth - 1);
                            return state.tokenize(stream, state);
                        }
                    }
                }
                return "meta";
            };
        }

        var curState, curStream, setStyle;
        function pass() {
            for (var i = arguments.length - 1; i >= 0; i--)
                curState.cc.push(arguments[i]);
        }
        function cont() {
            pass.apply(null, arguments);
            return true;
        }

        function pushContext(tagName, startOfLine) {
            var noIndent = Kludges.doNotIndent.hasOwnProperty(tagName) || (curState.context && curState.context.noIndent);
            curState.context = {
                prev: curState.context,
                tagName: tagName,
                indent: curState.indented,
                startOfLine: startOfLine,
                noIndent: noIndent
            };
        }
        function popContext() {
            if (curState.context)
                curState.context = curState.context.prev;
        }

        function element(type) {
            if (type == "openTag") {
                curState.tagName = tagName;
                curState.tagStart = curStream.column();
                return cont(attributes, endtag(curState.startOfLine));
            } else if (type == "closeTag") {
                var err = false;
                if (curState.context) {
                    if (curState.context.tagName != tagName) {
                        if (Kludges.implicitlyClosed.hasOwnProperty(curState.context.tagName.toLowerCase())) {
                            popContext();
                        }
                        err = !curState.context || curState.context.tagName != tagName;
                    }
                } else {
                    err = true;
                }
                if (err)
                    setStyle = "error";
                return cont(endclosetag(err));
            }
            return cont();
        }
        function endtag(startOfLine) {
            return function (type) {
                var tagName = curState.tagName;
                curState.tagName = curState.tagStart = null;
                if (type == "selfcloseTag" ||
                        (type == "endTag" && Kludges.autoSelfClosers.hasOwnProperty(tagName.toLowerCase()))) {
                    maybePopContext(tagName.toLowerCase());
                    return cont();
                }
                if (type == "endTag") {
                    maybePopContext(tagName.toLowerCase());
                    pushContext(tagName, startOfLine);
                    return cont();
                }
                return cont();
            };
        }
        function endclosetag(err) {
            return function (type) {
                if (err)
                    setStyle = "error";
                if (type == "endTag") {
                    popContext();
                    return cont();
                }
                setStyle = "error";
                return cont(arguments.callee);
            };
        }
        function maybePopContext(nextTagName) {
            var parentTagName;
            while (true) {
                if (!curState.context) {
                    return;
                }
                parentTagName = curState.context.tagName.toLowerCase();
                if (!Kludges.contextGrabbers.hasOwnProperty(parentTagName) ||
                        !Kludges.contextGrabbers[parentTagName].hasOwnProperty(nextTagName)) {
                    return;
                }
                popContext();
            }
        }

        function attributes(type) {
            if (type == "word") {
                setStyle = "attribute";
                return cont(attribute, attributes);
            }
            if (type == "endTag" || type == "selfcloseTag")
                return pass();
            setStyle = "error";
            return cont(attributes);
        }
        function attribute(type) {
            if (type == "equals")
                return cont(attvalue, attributes);
            if (!Kludges.allowMissing)
                setStyle = "error";
            else if (type == "word")
                setStyle = "attribute";
            return (type == "endTag" || type == "selfcloseTag") ? pass() : cont();
        }
        function attvalue(type) {
            if (type == "string")
                return cont(attvaluemaybe);
            if (type == "word" && Kludges.allowUnquoted) {
                setStyle = "string";
                return cont();
            }
            setStyle = "error";
            return (type == "endTag" || type == "selfCloseTag") ? pass() : cont();
        }
        function attvaluemaybe(type) {
            if (type == "string")
                return cont(attvaluemaybe);
            else
                return pass();
        }

        return {
            startState: function () {
                return {tokenize: inText, cc: [], indented: 0, startOfLine: true, tagName: null, tagStart: null, context: null};
            },
            token: function (stream, state) {
                if (!state.tagName && stream.sol()) {
                    state.startOfLine = true;
                    state.indented = stream.indentation();
                }
                if (stream.eatSpace())
                    return null;

                setStyle = type = tagName = null;
                var style = state.tokenize(stream, state);
                state.type = type;
                if ((style || type) && style != "comment") {
                    curState = state;
                    curStream = stream;
                    while (true) {
                        var comb = state.cc.pop() || element;
                        if (comb(type || style))
                            break;
                    }
                }
                state.startOfLine = false;
                return setStyle || style;
            },
            indent: function (state, textAfter, fullLine) {
                var context = state.context;
                // Indent multi-line strings (e.g. css).
                if (state.tokenize.isInAttribute) {
                    return state.stringStartCol + 1;
                }
                if ((state.tokenize != inTag && state.tokenize != inText) ||
                        context && context.noIndent)
                    return fullLine ? fullLine.match(/^(\s*)/)[0].length : 0;
                // Indent the starts of attribute names.
                if (state.tagName) {
                    if (multilineTagIndentPastTag)
                        return state.tagStart + state.tagName.length + 2;
                    else
                        return state.tagStart + indentUnit * multilineTagIndentFactor;
                }
                if (alignCDATA && /<!\[CDATA\[/.test(textAfter))
                    return 0;
                if (context && /^<\//.test(textAfter))
                    context = context.prev;
                while (context && !context.startOfLine)
                    context = context.prev;
                if (context)
                    return context.indent + indentUnit;
                else
                    return 0;
            },
            electricChars: "/",
            blockCommentStart: "<!--",
            blockCommentEnd: "-->",
            configuration: parserConfig.htmlMode ? "html" : "xml",
            helperType: parserConfig.htmlMode ? "html" : "xml"
        };
    });

    CodeMirror.defineMIME("text/xml", "xml");
    CodeMirror.defineMIME("application/xml", "xml");
    if (!CodeMirror.mimeModes.hasOwnProperty("text/html"))
        CodeMirror.defineMIME("text/html", {name: "xml", htmlMode: true});

    CodeMirror.defineMode("markdown", function (cmCfg, modeCfg) {

        var htmlFound = CodeMirror.modes.hasOwnProperty("xml");
        var htmlMode = CodeMirror.getMode(cmCfg, htmlFound ? {name: "xml", htmlMode: true} : "text/plain");
        var aliases = {
            html: "htmlmixed",
            js: "javascript",
            json: "application/json",
            c: "text/x-csrc",
            "c++": "text/x-c++src",
            java: "text/x-java",
            csharp: "text/x-csharp",
            "c#": "text/x-csharp",
            scala: "text/x-scala"
        };

        var getMode = (function () {
            var i, modes = {}, mimes = {}, mime;

            var list = [];
            for (var m in CodeMirror.modes)
                if (CodeMirror.modes.propertyIsEnumerable(m))
                    list.push(m);
            for (i = 0; i < list.length; i++) {
                modes[list[i]] = list[i];
            }
            var mimesList = [];
            for (var m in CodeMirror.mimeModes)
                if (CodeMirror.mimeModes.propertyIsEnumerable(m))
                    mimesList.push({mime: m, mode: CodeMirror.mimeModes[m]});
            for (i = 0; i < mimesList.length; i++) {
                mime = mimesList[i].mime;
                mimes[mime] = mimesList[i].mime;
            }

            for (var a in aliases) {
                if (aliases[a] in modes || aliases[a] in mimes)
                    modes[a] = aliases[a];
            }

            return function (lang) {
                return modes[lang] ? CodeMirror.getMode(cmCfg, modes[lang]) : null;
            };
        }());

        // Should underscores in words open/close em/strong?
        if (modeCfg.underscoresBreakWords === undefined)
            modeCfg.underscoresBreakWords = true;

        // Turn on fenced code blocks? ("```" to start/end)
        if (modeCfg.fencedCodeBlocks === undefined)
            modeCfg.fencedCodeBlocks = false;

        // Turn on task lists? ("- [ ] " and "- [x] ")
        if (modeCfg.taskLists === undefined)
            modeCfg.taskLists = false;

        var codeDepth = 0;

        var header = 'header'
                , code = 'comment'
                , quote1 = 'atom'
                , quote2 = 'number'
                , list1 = 'variable-2'
                , list2 = 'variable-3'
                , list3 = 'keyword'
                , hr = 'hr'
                , image = 'tag'
                , linkinline = 'link'
                , linkemail = 'link'
                , linktext = 'link'
                , linkhref = 'string'
                , em = 'em'
                , strong = 'strong'
                , strike = 'strike';

        var hrRE = /^([*\-=_])(?:\s*\1){4,}\s*$/
                , ulRE = /^[*\-+]\s+/
                , olRE = /^[0-9]+\.\s+/
                , taskListRE = /^\[(x| )\](?=\s)/ // Must follow ulRE or olRE
                , headerRE = /^(?:\={1,}|-{1,})$/
                , textRE = /^[^!\[\]*_~\\<>` "'(]+/;

        function switchInline(stream, state, f) {
            state.f = state.inline = f;
            return f(stream, state);
        }

        function switchBlock(stream, state, f) {
            state.f = state.block = f;
            return f(stream, state);
        }


        // Blocks

        function blankLine(state) {
            // Reset linkTitle state
            state.linkTitle = false;
            // Reset EM state
            state.em = false;
            // Reset STRONG state
            state.strong = false;
            // Reset STRIKE state
            state.strike = false;

            // Reset state.quote
            state.quote = 0;
            if (!htmlFound && state.f == htmlBlock) {
                state.f = inlineNormal;
                state.block = blockNormal;
            }
            // Reset state.trailingSpace
            state.trailingSpace = 0;
            state.trailingSpaceNewLine = false;
            // Mark this line as blank
            state.thisLineHasContent = false;
            return null;
        }

        function blockNormal(stream, state) {

            var prevLineIsList = (state.list !== false);
            if (state.list !== false && state.indentationDiff >= 0) { // Continued list
                if (state.indentationDiff < 4) { // Only adjust indentation if *not* a code block
                    state.indentation -= state.indentationDiff;
                }
                state.list = null;
            } else if (state.list !== false && state.indentation > 0) {
                state.list = null;
                state.listDepth = Math.floor(state.indentation / 4);
            } else if (state.list !== false) { // No longer a list
                state.list = false;
                state.listDepth = 0;
            }

            if (state.indentationDiff >= 4) {
                state.indentation -= 4;
                stream.skipToEnd();
                return code;
            } else if (stream.eatSpace()) {
                return null;
            } else if (stream.peek() === '#' || (state.prevLineHasContent && stream.match(headerRE))) {
                state.header = true;
            } else if (stream.eat('>')) {
                state.indentation++;
                state.quote = 1;
                stream.eatSpace();
                while (stream.eat('>')) {
                    stream.eatSpace();
                    state.quote++;
                }
            } else if (stream.peek() === '[') {
                return switchInline(stream, state, footnoteLink);
            } else if (stream.match(hrRE, true)) {
                return hr;
            } else if ((!state.prevLineHasContent || prevLineIsList) && (stream.match(ulRE, true) || stream.match(olRE, true))) {
                state.indentation += 4;
                state.list = true;
                state.listDepth++;
                if (modeCfg.taskLists && stream.match(taskListRE, false)) {
                    state.taskList = true;
                }
            } else if (modeCfg.fencedCodeBlocks && stream.match(/^```([\w+#]*)/, true)) {
                // try switching mode
                state.localMode = getMode(RegExp.$1);
                if (state.localMode)
                    state.localState = state.localMode.startState();
                switchBlock(stream, state, local);
                return code;
            }

            return switchInline(stream, state, state.inline);
        }

        function htmlBlock(stream, state) {
            var style = htmlMode.token(stream, state.htmlState);
            if (htmlFound && style === 'tag' && state.htmlState.type !== 'openTag' && !state.htmlState.context) {
                state.f = inlineNormal;
                state.block = blockNormal;
            }
            if (state.md_inside && stream.current().indexOf(">") != -1) {
                state.f = inlineNormal;
                state.block = blockNormal;
                state.htmlState.context = undefined;
            }
            return style;
        }

        function local(stream, state) {
            if (stream.sol() && stream.match(/^```/, true)) {
                state.localMode = state.localState = null;
                state.f = inlineNormal;
                state.block = blockNormal;
                return code;
            } else if (state.localMode) {
                return state.localMode.token(stream, state.localState);
            } else {
                stream.skipToEnd();
                return code;
            }
        }

        // Inline
        function getType(state) {
            var styles = [];

            if (state.taskOpen) {
                return "meta";
            }
            if (state.taskClosed) {
                return "property";
            }

            if (state.strong) {
                styles.push(strong);
            }
            if (state.strike) {
                styles.push(strike);
            }
            if (state.em) {
                styles.push(em);
            }

            if (state.linkText) {
                styles.push(linktext);
            }

            if (state.code) {
                styles.push(code);
            }

            if (state.header) {
                styles.push(header);
            }
            if (state.quote) {
                styles.push(state.quote % 2 ? quote1 : quote2);
            }
            if (state.list !== false) {
                var listMod = (state.listDepth - 1) % 3;
                if (!listMod) {
                    styles.push(list1);
                } else if (listMod === 1) {
                    styles.push(list2);
                } else {
                    styles.push(list3);
                }
            }

            if (state.trailingSpaceNewLine) {
                styles.push("trailing-space-new-line");
            } else if (state.trailingSpace) {
                styles.push("trailing-space-" + (state.trailingSpace % 2 ? "a" : "b"));
            }

            return styles.length ? styles.join(' ') : null;
        }

        function handleText(stream, state) {
            if (stream.match(textRE, true)) {
                return getType(state);
            }
            return undefined;
        }

        function inlineNormal(stream, state) {
            var style = state.text(stream, state);
            if (typeof style !== 'undefined')
                return style;

            if (state.list) { // List marker (*, +, -, 1., etc)
                state.list = null;
                return getType(state);
            }

            if (state.taskList) {
                var taskOpen = stream.match(taskListRE, true)[1] !== "x";
                if (taskOpen)
                    state.taskOpen = true;
                else
                    state.taskClosed = true;
                state.taskList = false;
                return getType(state);
            }

            state.taskOpen = false;
            state.taskClosed = false;

            var ch = stream.next();

            if (ch === '\\') {
                stream.next();
                return getType(state);
            }

            // Matches link titles present on next line
            if (state.linkTitle) {
                state.linkTitle = false;
                var matchCh = ch;
                if (ch === '(') {
                    matchCh = ')';
                }
                matchCh = (matchCh + '').replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
                var regex = '^\\s*(?:[^' + matchCh + '\\\\]+|\\\\\\\\|\\\\.)' + matchCh;
                if (stream.match(new RegExp(regex), true)) {
                    return linkhref;
                }
            }

            // If this block is changed, it may need to be updated in GFM mode
            if (ch === '`') {
                var t = getType(state);
                var before = stream.pos;
                stream.eatWhile('`');
                var difference = 1 + stream.pos - before;
                if (!state.code) {
                    codeDepth = difference;
                    state.code = true;
                    return getType(state);
                } else {
                    if (difference === codeDepth) { // Must be exact
                        state.code = false;
                        return t;
                    }
                    return getType(state);
                }
            } else if (state.code) {
                return getType(state);
            }

            if (ch === '!' && stream.match(/\[[^\]]*\] ?(?:\(|\[)/, false)) {
                stream.match(/\[[^\]]*\]/);
                state.inline = state.f = linkHref;
                return image;
            }

            if (ch === '[' && stream.match(/.*\](\(| ?\[)/, false)) {
                state.linkText = true;
                return getType(state);
            }

            if (ch === ']' && state.linkText) {
                var type = getType(state);
                state.linkText = false;
                state.inline = state.f = linkHref;
                return type;
            }

            if (ch === '<' && stream.match(/^(https?|ftps?):\/\/(?:[^\\>]|\\.)+>/, false)) {
                return switchInline(stream, state, inlineElement(linkinline, '>'));
            }

            if (ch === '<' && stream.match(/^[^> \\]+@(?:[^\\>]|\\.)+>/, false)) {
                return switchInline(stream, state, inlineElement(linkemail, '>'));
            }

            if (ch === '<' && stream.match(/^\w/, false)) {
                if (stream.string.indexOf(">") != -1) {
                    var atts = stream.string.substring(1, stream.string.indexOf(">"));
                    if (/markdown\s*=\s*('|"){0,1}1('|"){0,1}/.test(atts)) {
                        state.md_inside = true;
                    }
                }
                stream.backUp(1);
                return switchBlock(stream, state, htmlBlock);
            }

            if (ch === '<' && stream.match(/^\/\w*?>/)) {
                state.md_inside = false;
                return "tag";
            }

            var ignoreUnderscore = false;
            if (!modeCfg.underscoresBreakWords) {
                if (ch === '_' && stream.peek() !== '_' && stream.match(/(\w)/, false)) {
                    var prevPos = stream.pos - 2;
                    if (prevPos >= 0) {
                        var prevCh = stream.string.charAt(prevPos);
                        if (prevCh !== '_' && prevCh.match(/(\w)/, false)) {
                            ignoreUnderscore = true;
                        }
                    }
                }
            }
            var t = getType(state);
            if (ch === '*' || (ch === '_' && !ignoreUnderscore)) {
                if (state.strong === ch && stream.eat(ch) && stream.peek(ch)) { // Remove STRONG
                    state.strong = false;
                    return t;
                } else if (!state.strong && stream.eat(ch) && stream.peek(ch)) { // Add STRONG
                    state.strong = ch;
                    return getType(state);
                } else if (state.em === ch) { // Remove EM
                    state.em = false;
                    return t;
                } else if (!state.em) { // Add EM
                    state.em = ch;
                    return getType(state);
                }
            } else if (ch === '~') {
                if (state.strike === ch && stream.eat(ch)) { // Remove SRTIKE
                    state.strike = false;
                    return t;
                } else if (!state.strike && stream.eat(ch)) { // Add STRIKE
                    state.strike = ch;
                    return getType(state);
                }
            } else if (ch === ' ') {
                if (stream.eat('*') || stream.eat('_')) { // Probably surrounded by spaces
                    if (stream.peek() === ' ') { // Surrounded by spaces, ignore
                        return getType(state);
                    } else { // Not surrounded by spaces, back up pointer
                        stream.backUp(1);
                    }
                }
            }

            if (ch === ' ') {
                if (stream.match(/ +$/, false)) {
                    state.trailingSpace++;
                } else if (state.trailingSpace) {
                    state.trailingSpaceNewLine = true;
                }
            }

            return getType(state);
        }

        function linkHref(stream, state) {
            // Check if space, and return NULL if so (to avoid marking the space)
            if (stream.eatSpace()) {
                return null;
            }
            var ch = stream.next();
            if (ch === '(' || ch === '[') {
                return switchInline(stream, state, inlineElement(linkhref, ch === '(' ? ')' : ']'));
            }
            return 'error';
        }

        function footnoteLink(stream, state) {
            if (stream.match(/^[^\]]*\]:/, true)) {
                state.f = footnoteUrl;
                return linktext;
            }
            return switchInline(stream, state, inlineNormal);
        }

        function footnoteUrl(stream, state) {
            // Check if space, and return NULL if so (to avoid marking the space)
            if (stream.eatSpace()) {
                return null;
            }
            // Match URL
            stream.match(/^[^\s]+/, true);
            // Check for link title
            if (stream.peek() === undefined) { // End of line, set flag to check next line
                state.linkTitle = true;
            } else { // More content on line, check if link title
                stream.match(/^(?:\s+(?:"(?:[^"\\]|\\\\|\\.)+"|'(?:[^'\\]|\\\\|\\.)+'|\((?:[^)\\]|\\\\|\\.)+\)))?/, true);
            }
            state.f = state.inline = inlineNormal;
            return linkhref;
        }

        var savedInlineRE = [];
        function inlineRE(endChar) {
            if (!savedInlineRE[endChar]) {
                // Escape endChar for RegExp (taken from http://stackoverflow.com/a/494122/526741)
                endChar = (endChar + '').replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
                // Match any non-endChar, escaped character, as well as the closing
                // endChar.
                savedInlineRE[endChar] = new RegExp('^(?:[^\\\\]|\\\\.)*?(' + endChar + ')');
            }
            return savedInlineRE[endChar];
        }

        function inlineElement(type, endChar, next) {
            next = next || inlineNormal;
            return function (stream, state) {
                stream.match(inlineRE(endChar));
                state.inline = state.f = next;
                return type;
            };
        }

        return {
            startState: function () {
                return {
                    f: blockNormal,
                    prevLineHasContent: false,
                    thisLineHasContent: false,
                    block: blockNormal,
                    htmlState: CodeMirror.startState(htmlMode),
                    indentation: 0,
                    inline: inlineNormal,
                    text: handleText,
                    linkText: false,
                    linkTitle: false,
                    em: false,
                    strong: false,
                    strike: false,
                    header: false,
                    taskList: false,
                    list: false,
                    listDepth: 0,
                    quote: 0,
                    trailingSpace: 0,
                    trailingSpaceNewLine: false
                };
            },
            copyState: function (s) {
                return {
                    f: s.f,
                    prevLineHasContent: s.prevLineHasContent,
                    thisLineHasContent: s.thisLineHasContent,
                    block: s.block,
                    htmlState: CodeMirror.copyState(htmlMode, s.htmlState),
                    indentation: s.indentation,
                    localMode: s.localMode,
                    localState: s.localMode ? CodeMirror.copyState(s.localMode, s.localState) : null,
                    inline: s.inline,
                    text: s.text,
                    linkTitle: s.linkTitle,
                    em: s.em,
                    strong: s.strong,
                    strike: s.strike,
                    header: s.header,
                    taskList: s.taskList,
                    list: s.list,
                    listDepth: s.listDepth,
                    quote: s.quote,
                    trailingSpace: s.trailingSpace,
                    trailingSpaceNewLine: s.trailingSpaceNewLine,
                    md_inside: s.md_inside
                };
            },
            token: function (stream, state) {
                if (stream.sol()) {
                    if (stream.match(/^\s*$/, true)) {
                        state.prevLineHasContent = false;
                        return blankLine(state);
                    } else {
                        state.prevLineHasContent = state.thisLineHasContent;
                        state.thisLineHasContent = true;
                    }

                    // Reset state.header
                    state.header = false;

                    // Reset state.taskList
                    state.taskList = false;

                    // Reset state.code
                    state.code = false;

                    // Reset state.trailingSpace
                    state.trailingSpace = 0;
                    state.trailingSpaceNewLine = false;

                    state.f = state.block;
                    var indentation = stream.match(/^\s*/, true)[0].replace(/\t/g, '    ').length;
                    var difference = Math.floor((indentation - state.indentation) / 4) * 4;
                    if (difference > 4)
                        difference = 4;
                    var adjustedIndentation = state.indentation + difference;
                    state.indentationDiff = adjustedIndentation - state.indentation;
                    state.indentation = adjustedIndentation;
                    if (indentation > 0)
                        return null;
                }
                return state.f(stream, state);
            },
            blankLine: blankLine,
            getType: getType
        };

    }, "xml");

    CodeMirror.defineMIME("text/x-markdown", "markdown");


    var isMac = /Mac/.test(navigator.platform);

    var shortcuts = {
        'Cmd-B': toggleBold,
        'Cmd-I': toggleItalic,
        'Cmd-K': drawLink,
        "Cmd-E": toggleBlockquote,
        'Shift-Cmd-L': toggleOrderedList,
        'Cmd-L': toggleUnOrderedList,
        'Cmd-D': togglePreview,
        'Shift-Cmd-A': toggleFullScreen
    };


    /**
     * Fix shortcut. Mac use Command, others use Ctrl.
     */
    function fixShortcut(name) {
        if (isMac) {
            name = name.replace('Ctrl', 'Cmd');
        } else {
            name = name.replace('Cmd', 'Ctrl');
        }
        return name;
    }


    /**
     * Create icon element for toolbar.
     */
    function createIcon(name, options) {
        options = options || {};
        if (name === 'image') {
            return $(options.html)[0];
        }
        var el = document.createElement('a');

        var shortcut = options.shortcut || shortcuts[name];
        if (shortcut) {
            shortcut = fixShortcut(shortcut);
            el.title = shortcut;
            el.title = el.title.replace('Cmd', '⌘');
            if (isMac) {
                el.title = el.title.replace('Alt', '⌥');
            }
        }

        el.className = options.className || 'icon-' + name;
        return el;
    }

    function createSep() {
        el = document.createElement('i');
        el.className = 'separator';
        el.innerHTML = '|';
        return el;
    }


    /**
     * The state of CodeMirror at the given position.
     */
    function getState(cm, pos) {
        pos = pos || cm.getCursor('start');
        var stat = cm.getTokenAt(pos);
        if (!stat.type)
            return {};

        var types = stat.type.split(' ');

        var ret = {}, data, text;
        for (var i = 0; i < types.length; i++) {
            data = types[i];
            if (data === 'strong') {
                ret.bold = true;
            } else if (data === 'variable-2') {
                text = cm.getLine(pos.line);
                if (/^\s*\d+\.\s/.test(text)) {
                    ret['ordered-list'] = true;
                } else {
                    ret['unordered-list'] = true;
                }
            } else if (data === 'atom') {
                ret.quote = true;
            } else if (data === 'em') {
                ret.italic = true;
            }
        }
        return ret;
    }


    /**
     * Toggle full screen of the editor.
     */
    function toggleFullScreen(editor) {
        var cm = editor.codemirror,
        wrap = editor.codemirror.getWrapperElement();
        
        if ('icon-fullscreen' === editor.toolbar.fullscreen.className) {
            editor.toolbar.fullscreen.className = 'icon-contract';
            editor.toolbar.preview.style.display = 'none';
            if (editor.toolbar.preview.className.indexOf('active') > -1) {
                editor.toolbar.preview.click();
            }

            $(editor.element.parentElement).css({
                'position': 'fixed',
                'top': '0',
                'z-index': '90',
                'left': '0',
                'right': '0'
            });

            cm.state.fullScreenRestore = {scrollTop: window.pageYOffset, scrollLeft: window.pageXOffset,
                                          width: wrap.style.width, height: wrap.style.height};
            wrap.style.width = "50%";
            wrap.style.height = ($(window).height() - $('.editor-toolbar').outerHeight()) + 'px';
            cm.refresh();
            
            $.ajax({
                url: editor.options.htmlURL,
                type: "POST",
                cache: false,
                data: {
                    markdownText: cm.getValue()
                },
                success: function (result, textStatus) {
                    $(editor.element.parentElement).prepend('<div class="CodeMirror-preview content-reset" style="height:' 
                + ($(window).height() - $('.editor-toolbar').outerHeight()) + 'px">' + result.html + '</div>');
                    hljs.initHighlighting.called = false;
                    hljs.initHighlighting();
                }
            });

            return false;
        }

        editor.toolbar.fullscreen.className = 'icon-fullscreen';
        editor.toolbar.preview.style.display = 'inline';
        $(editor.element.parentElement).css({
            'position': 'inherit'
        });

        $(editor.element.parentElement).find('.CodeMirror-preview').remove();    

        var info = cm.state.fullScreenRestore;
        wrap.style.width = info.width; 
        wrap.style.height = info.height;
        cm.refresh();
    }


    /**
     * Action for toggling bold.
     */
    function toggleBold(editor) {
        var cm = editor.codemirror;
        var stat = getState(cm);

        var text;
        var start = '**';
        var end = '**';

        var startPoint = cm.getCursor('start');
        var endPoint = cm.getCursor('end');
        if (stat.bold) {
            text = cm.getLine(startPoint.line);
            start = text.slice(0, startPoint.ch);
            end = text.slice(startPoint.ch);

            start = start.replace(/^(.*)?(\*|\_){2}(\S+.*)?$/, '$1$3');
            end = end.replace(/^(.*\S+)?(\*|\_){2}(\s+.*)?$/, '$1$3');
            startPoint.ch -= 2;
            endPoint.ch -= 2;
            cm.replaceRange(start + end, CodeMirror.Pos(startPoint.line, 0), CodeMirror.Pos(startPoint.line, text.length));
        } else {
            text = cm.getSelection();
            cm.replaceSelection(start + text + end);

            startPoint.ch += 2;
            endPoint.ch += 2;
        }
        cm.setSelection(startPoint, endPoint);
        cm.focus();
    }


    /**
     * Action for toggling italic.
     */
    function toggleItalic(editor) {
        var cm = editor.codemirror;
        var stat = getState(cm);

        var text;
        var start = '*';
        var end = '*';

        var startPoint = cm.getCursor('start');
        var endPoint = cm.getCursor('end');
        if (stat.italic) {
            text = cm.getLine(startPoint.line);
            start = text.slice(0, startPoint.ch);
            end = text.slice(startPoint.ch);

            start = start.replace(/^(.*)?(\*|\_)(\S+.*)?$/, '$1$3');
            end = end.replace(/^(.*\S+)?(\*|\_)(\s+.*)?$/, '$1$3');
            startPoint.ch -= 1;
            endPoint.ch -= 1;
            cm.replaceRange(start + end, CodeMirror.Pos(startPoint.line, 0), CodeMirror.Pos(startPoint.line, text.length));
        } else {
            text = cm.getSelection();
            cm.replaceSelection(start + text + end);

            startPoint.ch += 1;
            endPoint.ch += 1;
        }
        cm.setSelection(startPoint, endPoint);
        cm.focus();
    }


    /**
     * Action for toggling blockquote.
     */
    function toggleBlockquote(editor) {
        var cm = editor.codemirror;
        _toggleLine(cm, 'quote');
    }


    /**
     * Action for toggling ul.
     */
    function toggleUnOrderedList(editor) {
        var cm = editor.codemirror;
        _toggleLine(cm, 'unordered-list');
    }


    /**
     * Action for toggling ol.
     */
    function toggleOrderedList(editor) {
        var cm = editor.codemirror;
        _toggleLine(cm, 'ordered-list');
    }


    /**
     * Action for drawing a link.
     */
    function drawLink(editor) {
        var cm = editor.codemirror;
        var stat = getState(cm);
        _replaceSelection(cm, stat.link, '[', '](http://)');
    }


    /**
     * Action for drawing an img.
     */
    function drawImage(editor) {
        var cm = editor.codemirror;
        var stat = getState(cm);
        _replaceSelection(cm, stat.image, '![', '](http://)');
    }


    /**
     * Undo action.
     */
    function undo(editor) {
        var cm = editor.codemirror;
        cm.undo();
        cm.focus();
    }


    /**
     * Redo action.
     */
    function redo(editor) {
        var cm = editor.codemirror;
        cm.redo();
        cm.focus();
    }

    /**
     * Preview action.
     */
    function togglePreview(editor) {
        var toolbar = editor.toolbar.preview;
        var cm = editor.codemirror;
        var wrapper = cm.getWrapperElement();
        var preview = wrapper.lastChild;
        if (!/editor-preview/.test(preview.className)) {
            preview = document.createElement('div');
            preview.className = 'editor-preview content-reset';
            wrapper.appendChild(preview);
        }
        if (/editor-preview-active/.test(preview.className)) {
            preview.className = preview.className.replace(
                    /\s*editor-preview-active\s*/g, ''
                    );
            toolbar.className = toolbar.className.replace(/\s*active\s*/g, '');
        } else {
            /* When the preview button is clicked for the first time,
             * give some time for the transition from editor.css to fire and the view to slide from right to left,
             * instead of just appearing.
             */
            setTimeout(function () {
                preview.className += ' editor-preview-active'
            }, 1);
            toolbar.className += ' active';
        }
        var text = cm.getValue();

        $.ajax({
            url: editor.options.htmlURL,
            type: "POST",
            cache: false,
            data: {
                markdownText: text
            },
            success: function (result, textStatus) {
                preview.innerHTML = result.html;
                hljs.initHighlighting.called = false;
                hljs.initHighlighting();
            }
        });
    }

    function _replaceSelection(cm, active, start, end) {
        var text;
        var startPoint = cm.getCursor('start');
        var endPoint = cm.getCursor('end');
        if (active) {
            text = cm.getLine(startPoint.line);
            start = text.slice(0, startPoint.ch);
            end = text.slice(startPoint.ch);
            cm.replaceRange(start + end, CodeMirror.Pos(startPoint.line, 0), CodeMirror.Pos(startPoint.line, text.length));
        } else {
            text = cm.getSelection();
            cm.replaceSelection(start + text + end);

            startPoint.ch += start.length;
            endPoint.ch += start.length;
        }
        cm.setSelection(startPoint, endPoint);
        cm.focus();
    }


    function _toggleLine(cm, name) {
        var stat = getState(cm);
        var startPoint = cm.getCursor('start');
        var endPoint = cm.getCursor('end');
        var repl = {
            quote: /^(\s*)\>\s+/,
            'unordered-list': /^(\s*)(\*|\-|\+)\s+/,
            'ordered-list': /^(\s*)\d+\.\s+/
        };
        var map = {
            quote: '> ',
            'unordered-list': '* ',
            'ordered-list': '1. '
        };
        for (var i = startPoint.line; i <= endPoint.line; i++) {
            (function (i) {
                var text = cm.getLine(i);
                if (stat[name]) {
                    text = text.replace(repl[name], '$1');
                } else {
                    text = map[name] + text;
                }
                cm.replaceRange(text, CodeMirror.Pos(i, 0), CodeMirror.Pos(i, cm.getLine(i).length));
            })(i);
        }
        cm.focus();
    }


    /* The right word count in respect for CJK. */
    function wordCount(data) {
        var pattern = /[a-zA-Z0-9_\u0392-\u03c9]+|[\u4E00-\u9FFF\u3400-\u4dbf\uf900-\ufaff\u3040-\u309f\uac00-\ud7af]+/g;
        var m = data.match(pattern);
        var count = 0;
        if (m === null)
            return count;
        for (var i = 0; i < m.length; i++) {
            if (m[i].charCodeAt(0) >= 0x4E00) {
                count += m[i].length;
            } else {
                count += 1;
            }
        }
        return count;
    }

    var toolbar = [
        {name: 'bold', action: toggleBold},
        {name: 'italic', action: toggleItalic},
        '|',
        {name: 'quote', action: toggleBlockquote},
        {name: 'unordered-list', action: toggleUnOrderedList},
        {name: 'ordered-list', action: toggleOrderedList},
        '|',
        {name: 'link', action: drawLink},
        {name: 'image', action: drawImage},
        '|',
        {name: 'redo', action: redo},
        {name: 'undo', action: undo},
        '|',
        //{name: 'info', action: 'http://lab.lepture.com/editor/markdown'},
        {name: 'preview', action: togglePreview},
        {name: 'fullscreen', action: toggleFullScreen}
    ];

    /**
     * Interface of Editor.
     */
    function Editor(options) {
        options = options || {};

        if (options.element) {
            this.element = options.element;
        }

        options.toolbar = options.toolbar || Editor.toolbar;
        // you can customize toolbar with object
        // [{name: 'bold', shortcut: 'Ctrl-B', className: 'icon-bold'}]

        if (!options.hasOwnProperty('status')) {
            options.status = ['lines', 'words', 'cursor'];
        }

        this.options = options;

        // If user has passed an element, it should auto rendered
        if (this.element) {
            this.render();
        }
    }

    /**
     * Default toolbar elements.
     */
    Editor.toolbar = toolbar;

    /**
     * Default markdown render.
     */
    Editor.markdown = function (text) {
        if (window.marked) {
            // use marked as markdown parser
            return marked(text);
        }
    };

    /**
     * Render editor to the given element.
     */
    Editor.prototype.render = function (el) {
        if (!el) {
            el = this.element || document.getElementsByTagName('textarea')[0];
        }

        if (this._rendered && this._rendered === el) {
            // Already rendered.
            return;
        }

        this.element = el;
        var options = this.options;

        var self = this;
        var keyMaps = {};

        for (var key in shortcuts) {
            (function (key) {
                keyMaps[fixShortcut(key)] = function (cm) {
                    shortcuts[key](self);
                };
            })(key);
        }

        keyMaps["Enter"] = "newlineAndIndentContinueMarkdownList";
        keyMaps['Tab'] = 'tabAndIndentContinueMarkdownList';
        keyMaps['Shift-Tab'] = 'shiftTabAndIndentContinueMarkdownList';

        $.extend(keyMaps, options.extraKeys);
        this.codemirror = CodeMirror.fromTextArea(el, {
            mode: 'markdown',
            indentWithTabs: true,
            lineNumbers: false,
            autofocus: options.autofocus,
            lineWrapping: options.lineWrapping,
            extraKeys: keyMaps,
            inputStyle: 'textarea'
        });

        if (options.toolbar !== false) {
            this.createToolbar();
        }
        if (options.status !== false) {
            this.createStatusbar();
        }

        this._rendered = this.element;
    };

    Editor.prototype.createToolbar = function (items) {
        items = items || this.options.toolbar;

        if (!items || items.length === 0) {
            return;
        }

        var bar = document.createElement('div');
        bar.className = 'editor-toolbar';

        var self = this;

        var el;
        self.toolbar = {};

        for (var i = 0; i < items.length; i++) {
            (function (item) {
                var el;
                if (item.name) {
                    el = createIcon(item.name, item);
                } else if (item === '|') {
                    el = createSep();
                } else {
                    el = createIcon(item);
                }

                // bind events, special for info

                if (!item.action) {
                    for (var j = 0, max = Editor.toolbar.length; j < max; j++) {
                        if (Editor.toolbar[j].name === item.name) {
                            item.action = Editor.toolbar[j].action;
                            break;
                        }
                    }
                }
                if (item.action) {
                    if (typeof item.action === 'function' && item.name !== 'image') {
                        el.onclick = function (e) {
                            item.action(self);
                        };
                    } else if (typeof item.action === 'string') {
                        el.href = item.action;
                        el.target = '_blank';
                    }
                }
                self.toolbar[item.name || item] = el;
                bar.appendChild(el);
            })(items[i]);
        }

        var cm = this.codemirror;
        cm.on('cursorActivity', function () {
            var stat = getState(cm);

            for (var key in self.toolbar) {
                (function (key) {
                    var el = self.toolbar[key];
                    if (stat[key]) {
                        if (el.className.indexOf('active') === -1) {
                            el.className += ' active';
                        }
                    } else {
                        el.className = el.className.replace(/\s*active\s*/g, '');
                    }
                })(key);
            }
        });

        var cmWrapper = cm.getWrapperElement();
        cmWrapper.parentNode.insertBefore(bar, cmWrapper);
        return bar;
    };

    Editor.prototype.createStatusbar = function (status) {
        status = status || this.options.status;

        if (!status || status.length === 0)
            return;

        var bar = document.createElement('div');
        bar.className = 'editor-statusbar';

        var pos, cm = this.codemirror;
        for (var i = 0; i < status.length; i++) {
            (function (name) {
                var el = document.createElement('span');
                el.className = name;
                if (name === 'words') {
                    el.innerHTML = '0';
                    cm.on('update', function () {
                        el.innerHTML = wordCount(cm.getValue());
                    });
                } else if (name === 'lines') {
                    el.innerHTML = '0';
                    cm.on('update', function () {
                        el.innerHTML = cm.lineCount();
                    });
                } else if (name === 'cursor') {
                    el.innerHTML = '0:0';
                    cm.on('cursorActivity', function () {
                        pos = cm.getCursor();
                        el.innerHTML = pos.line + ':' + pos.ch;
                    });
                }
                bar.appendChild(el);
            })(status[i]);
        }
        var cmWrapper = this.codemirror.getWrapperElement();
        cmWrapper.parentNode.insertBefore(bar, cmWrapper.nextSibling);
        return bar;
    };

    /**
     * Get or set the text content.
     */
    Editor.prototype.value = function (val) {
        if (val) {
            this.codemirror.getDoc().setValue(val);
            return this;
        } else {
            return this.codemirror.getValue();
        }
    };


    /**
     * Bind static methods for exports.
     */
    Editor.toggleBold = toggleBold;
    Editor.toggleItalic = toggleItalic;
    Editor.toggleBlockquote = toggleBlockquote;
    Editor.toggleUnOrderedList = toggleUnOrderedList;
    Editor.toggleOrderedList = toggleOrderedList;
    Editor.drawLink = drawLink;
    Editor.drawImage = drawImage;
    Editor.undo = undo;
    Editor.redo = redo;
    Editor.togglePreview = togglePreview;
    Editor.toggleFullScreen = toggleFullScreen;

    /**
     * Bind instance methods for exports.
     */
    Editor.prototype.toggleBold = function () {
        toggleBold(this);
    };
    Editor.prototype.toggleItalic = function () {
        toggleItalic(this);
    };
    Editor.prototype.toggleBlockquote = function () {
        toggleBlockquote(this);
    };
    Editor.prototype.toggleUnOrderedList = function () {
        toggleUnOrderedList(this);
    };
    Editor.prototype.toggleOrderedList = function () {
        toggleOrderedList(this);
    };
    Editor.prototype.drawLink = function () {
        drawLink(this);
    };
    Editor.prototype.drawImage = function () {
        drawImage(this);
    };
    Editor.prototype.undo = function () {
        undo(this);
    };
    Editor.prototype.redo = function () {
        redo(this);
    };
    Editor.prototype.togglePreview = function () {
        togglePreview(this);
    };
    Editor.prototype.toggleFullScreen = function () {
        toggleFullScreen(this);
    };

    global.Editor = Editor;
})(this);