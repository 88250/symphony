/*
 * Copyright (c) 2012-2015, b3log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview add-article.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.10.6.5, Dec 25, 2015
 */

/**
 * @description Add article function.
 * @static
 */
var AddArticle = {
    editor: undefined,
    rewardEditor: undefined,
    /**
     * @description 发布文章
     * @id [string] 文章 id ，如不为空则表示更新文章否则为添加文章
     * @csrfToken [string] CSRF 令牌
     */
    add: function (id, csrfToken) {
        if (Validate.goValidate({target: $('#addArticleTip'),
            data: [{
                    "type": "string",
                    "max": 256,
                    "msg": Label.articleTitleErrorLabel,
                    "target": $('#articleTitle')
                }, {
                    "type": "editor",
                    "target": this.editor,
                    "max": 1048576,
                    "min": 4,
                    "msg": Label.articleContentErrorLabel
                }, {
                    "type": "tags",
                    "msg": Label.tagsErrorLabel,
                    "target": $('#articleTags')
                }]})) {
            var requestJSONObject = {
                articleTitle: $("#articleTitle").val().replace(/(^\s*)|(\s*$)/g, ""),
                articleContent: this.editor.getValue(),
                articleTags: $("#articleTags").val().replace(/(^\s*)|(\s*$)/g, ""),
                //articleCommentable: $("#articleCommentable").prop("checked"),
                articleCommentable: true,
                articleType: $("input[type='radio'][name='articleType']:checked").val(),
                articleRewardContent: this.rewardEditor.getValue(),
                articleRewardPoint: $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")
            },
            url = "/article", type = "POST";

            if (3 === parseInt(requestJSONObject.articleType)) { // 如果文章是“思绪”
                requestJSONObject.articleContent = window.localStorage.thoughtContent;
            }

            if (id) {
                url = url + "/" + id;
                type = "PUT";
            }

            $.ajax({
                url: url,
                type: type,
                headers: {"csrfToken": csrfToken},
                cache: false,
                data: JSON.stringify(requestJSONObject),
                beforeSend: function () {
                    $(".form button.red").attr("disabled", "disabled").css("opacity", "0.3");
                },
                success: function (result, textStatus) {
                    $(".form button.red").removeAttr("disabled").css("opacity", "1");
                    if (result.sc) {
                        window.location.href = "/member/" + Label.userName;

                        if (window.localStorage) {
                            window.localStorage.articleContent = "";
                            window.localStorage.thoughtContent = "";
                        }
                    } else {
                        $("#addArticleTip").addClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    }
                },
                complete: function () {
                    $(".form button.red").removeAttr("disabled").css("opacity", "1");
                }
            });
        }
    },
    /**
     * @description 初识化发文
     */
    init: function () {
        Util.initCodeMirror();

        // 初始化文章编辑器
        AddArticle.editor = CodeMirror.fromTextArea(document.getElementById("articleContent"), {
            mode: 'markdown',
            dragDrop: false,
            lineWrapping: true,
            extraKeys: {
                "'@'": "autocompleteUserName",
                "Ctrl-/": "autocompleteEmoji",
                "Alt-S": "startAudioRecord",
                "Alt-E": "endAudioRecord",
                "F11": function (cm) {
                    cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                }
            }
        });

        if (window.localStorage && window.localStorage.articleContent && "" === AddArticle.editor.getValue()
                && "" !== window.localStorage.articleContent.replace(/(^\s*)|(\s*$)/g, "")) {
            AddArticle.editor.setValue(window.localStorage.articleContent);
        }

        if (!window.localStorage.thoughtContent) {
            window.localStorage.thoughtContent = "";
        }

        var atIdx = location.href.indexOf("at=");
        if (-1 !== atIdx) {
            var at = AddArticle.editor.getValue();
            AddArticle.editor.setValue("\n\n\n" + at);
            AddArticle.editor.setCursor(CodeMirror.Pos(0, 0));
            AddArticle.editor.focus();
            
            var username = getParameterByName("at");
            $("#articleTitle").val("Hi, " + username);
            
            var tagTitles = "讨论组";
            var tags = getParameterByName("tags");
            if ("" !== tags) {
                tagTitles += "," + tags;
            }
            $("#articleTags").val(tagTitles);
        } else {
            $("#articleTitle").focus();
        }

        AddArticle.editor.on('keydown', function (cm, evt) {
            if (8 === evt.keyCode) { // Backspace
                var cursor = cm.getCursor();
                var token = cm.getTokenAt(cursor);

                if (" " !== token.string) {
                    return;
                }

                // delete the whole username
                var preCursor = CodeMirror.Pos(cursor.line, cursor.ch - 1);
                token = cm.getTokenAt(preCursor);
                if (Util.startsWith(token.string, "@")) {
                    cm.replaceRange("", CodeMirror.Pos(cursor.line, token.start),
                            CodeMirror.Pos(cursor.line, token.end));
                }
            }
        });

        var thoughtTime = '';
        AddArticle.editor.on('changes', function (cm, changes) {
            if (cm.getValue().replace(/(^\s*)|(\s*$)/g, "") !== "") {
                $(".form .green").show();
            } else {
                $(".form .green").hide();
            }

            if (window.localStorage) {
                window.localStorage.articleContent = cm.getValue();
            }

            if (!window.localStorage.thoughtContent) {
                window.localStorage.thoughtContent = '';
            }

            if (thoughtTime === '') {
                thoughtTime = (new Date()).getTime();
            }

            var change = "",
                    unitSep = String.fromCharCode(31), // Unit Separator (单元分隔符)
                    time = (new Date()).getTime() - thoughtTime;
            // console.log(changes[0])

            switch (changes[0].origin) {
                case "+delete":
                    change = String.fromCharCode(24) + unitSep + time // cancel
                            + unitSep + changes[0].from.ch + '-' + changes[0].from.line
                            + unitSep + changes[0].to.ch + '-' + changes[0].to.line
                            + String.fromCharCode(30);  // Record Separator (记录分隔符)
                    break;
                case "*compose":
                case "+input":
                default:

                    for (var i = 0; i < changes[0].text.length; i++) {
                        if (i === changes[0].text.length - 1) {
                            change += changes[0].text[i];
                        } else {
                            change += changes[0].text[i] + String.fromCharCode(10); // New Line
                        }
                    }
                    for (var j = 0; j < changes[0].removed.length; j++) {
                        if (j === 0) {
                            change += String.fromCharCode(29); // group separator
                            break;
                        }
                    }
                    change += unitSep + time
                            + unitSep + changes[0].from.ch + '-' + changes[0].from.line
                            + unitSep + changes[0].to.ch + '-' + changes[0].to.line
                            + String.fromCharCode(30);  // Record Separator (记录分隔符)
                    break;
            }

            window.localStorage.thoughtContent += change;
        });

        $("#articleTitle, #articleTags, #articleRewardPoint").keypress(function (event) {
            if (13 === event.keyCode) {
                AddArticle.add();
            }
        });

        $("#preview").dialog({
            "modal": true,
            "hideFooter": true
        });

        // 初始化打赏区编辑器
        var readOnly = false;
        if (0 < $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")) {
            readOnly = 'nocursor';
        }
        AddArticle.rewardEditor = CodeMirror.fromTextArea(document.getElementById("articleRewardContent"), {
            mode: 'markdown',
            dragDrop: false,
            lineWrapping: true,
            readOnly: readOnly,
            extraKeys: {
                "'@'": "autocompleteUserName",
                "Ctrl-/": "autocompleteEmoji",
                "F11": function (cm) {
                    cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                }
            }
        });

        AddArticle.rewardEditor.on('keydown', function (cm, evt) {
            if (8 === evt.keyCode) { // Backspace
                var cursor = cm.getCursor();
                var token = cm.getTokenAt(cursor);

                if (" " !== token.string) {
                    return;
                }

                // delete the whole username
                var preCursor = CodeMirror.Pos(cursor.line, cursor.ch - 1);
                token = cm.getTokenAt(preCursor);
                if (Util.startsWith(token.string, "@")) {
                    cm.replaceRange("", CodeMirror.Pos(cursor.line, token.start),
                            CodeMirror.Pos(cursor.line, token.end));
                }
            }
        });

        $("#articleRewardContent").next().height(100);
    },
    /**
     * @description 预览文章
     */
    preview: function () {
        var it = this;
        $.ajax({
            url: "/markdown",
            type: "POST",
            cache: false,
            data: {
                markdownText: it.editor.getValue()
            },
            success: function (result, textStatus) {
                $("#preview").dialog("open");
                $("#preview").html(result.html);
                hljs.initHighlighting.called = false;
                hljs.initHighlighting();
            }
        });
    },
    /**
     * @description 显示简要语法
     */
    grammar: function () {
        var $grammar = $(".grammar"),
                $codemirror = $(".CodeMirror:first");
        if ($("#articleTitle").width() < 500) {
            // for mobile
            $grammar.toggle();
            return;
        }
        if ($codemirror.width() > 900) {
            $grammar.show();
            $codemirror.width('75%');
        } else {
            $grammar.hide();
            $codemirror.width('100%');
        }
    }
};

AddArticle.init();

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
    
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
