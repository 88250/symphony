/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
 * @version 2.14.8.8, Mar 16, 2016
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
                }]})) {
            var requestJSONObject = {
                articleTitle: $("#articleTitle").val().replace(/(^\s*)|(\s*$)/g, ""),
                articleContent: this.editor.getValue(),
                articleTags: $("#articleTags").val().replace(/(^\s*)|(\s*$)/g, ""),
                articleCommentable: true,
                articleType: $("input[type='radio'][name='articleType']:checked").val(),
                articleRewardContent: this.rewardEditor.getValue(),
                articleRewardPoint: $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")
            },
            url = "/article", type = "POST";

            if (3 === parseInt(requestJSONObject.articleType)) { // 如果是“思绪”
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
                error: function (jqXHR, textStatus, errorThrown) {
                    $("#addArticleTip").addClass('error').html('<ul><li>' + errorThrown + '</li></ul>');
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
        $.ua.set(navigator.userAgent);
        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            $('#articleType3').hide();
            AddArticle.editor = Util.initTextarea('articleContent',
                    function (editor) {
                        if (window.localStorage) {
                            window.localStorage.articleContent = editor.getValue();
                        }
                    }
            );
            $('#articleContent').before('<form id="fileUpload" method="POST" enctype="multipart/form-data"><label class="btn">'
                    + Label.uploadLabel + '<input type="file"/></label></form>')
                    .css('margin-top', 0);
        } else {
            Util.initCodeMirror();
            // 初始化文章编辑器
            var addArticleEditor = new Editor({
                element: document.getElementById('articleContent'),
                dragDrop: false,
                lineWrapping: true,
                extraKeys: {
                    "Alt-/": "autocompleteUserName",
                    "Ctrl-/": "autocompleteEmoji",
                    "Alt-S": "startAudioRecord",
                    "Alt-E": "endAudioRecord",
                    "F11": function (cm) {
                        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    },
                    "Esc": function (cm) {
                        cm.setOption("fullScreen", false);
                    }
                },
                toolbar: [
                    {name: 'bold'},
                    {name: 'italic'},
                    '|',
                    {name: 'quote'},
                    {name: 'unordered-list'},
                    {name: 'ordered-list'},
                    '|',
                    {name: 'link'},
                    {name: 'image', html: '<form id="fileUpload" method="POST" enctype="multipart/form-data"><label class="icon-upload"><input type="file"/></label></form>'},
                    '|',
                    {name: 'redo'},
                    {name: 'undo'},
                    '|',
                    {name: 'preview'},
                    {name: 'fullscreen'}
                ],
                status: false
            });
            addArticleEditor.render();

            AddArticle.editor = addArticleEditor.codemirror;
        }

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

            var tagTitles = "小黑屋";
            var tags = getParameterByName("tags");
            if ("" !== tags) {
                tagTitles += "," + tags;
            }
            $("#articleTags").val(tagTitles);
        }

        var title = getParameterByName("title");
        if (title && title.length > 0) {
            $("#articleTitle").val(title);
        }

        if ($("#articleTitle").val().length <= 0) {
            $("#articleTitle").focus();
        }
        if ($.ua.device.type !== 'mobile' || ($.ua.device.vendor !== 'Apple' && $.ua.device.vendor !== 'Nokia')) {
            AddArticle.editor.on('keydown', function (cm, evt) {
                if (8 === evt.keyCode) {
                    var cursor = cm.getCursor();
                    var token = cm.getTokenAt(cursor);

                    // delete the whole emoji
                    var preCursor = CodeMirror.Pos(cursor.line, cursor.ch);
                    token = cm.getTokenAt(preCursor);
                    if (/^:\S+:$/.test(token.string)) {
                        cm.replaceRange("", CodeMirror.Pos(cursor.line, token.start),
                                CodeMirror.Pos(cursor.line, token.end - 1));
                    }
                }
            });

            var thoughtTime = '';
            AddArticle.editor.on('changes', function (cm, changes) {
                if (window.localStorage) {
                    window.localStorage.articleContent = cm.getValue();
                }

                if (!window.localStorage.thoughtContent) {
                    window.localStorage.thoughtContent = '';
                }

                if (thoughtTime === '') {
                    thoughtTime = (new Date()).getTime();
                }

                var cursor = cm.getCursor();
                var token = cm.getTokenAt(cursor);
                if (token.string.indexOf('@') === 0) {
                    cm.showHint({hint: CodeMirror.hint.userName, completeSingle: false});
                    return CodeMirror.Pass;
                }

                var change = "",
                        unitSep = String.fromCharCode(31), // Unit Separator (单元分隔符)
                        time = (new Date()).getTime() - thoughtTime;

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
        }

        $("#articleTitle, #articleTags, #articleRewardPoint").keypress(function (event) {
            if (13 === event.keyCode) {
                AddArticle.add();
            }
        });

        // 初始化打赏区编辑器
        var readOnly = false;
        if (0 < $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")) {
            readOnly = 'nocursor';
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            AddArticle.rewardEditor = Util.initTextarea('articleRewardContent');
            $('#articleRewardContent').prop('readOnly', readOnly);
            if (readOnly === false) {
                $('#articleRewardContent').before('<form id="rewardFileUpload" method="POST" enctype="multipart/form-data"><label class="btn">'
                        + Label.uploadLabel + '<input type="file"/></label></form>')
                        .css('margin-top', 0);
            }
        } else {
            var addArticleRewardEditor = new Editor({
                element: document.getElementById('articleRewardContent'),
                dragDrop: false,
                lineWrapping: true,
                readOnly: readOnly,
                toolbar: [
                    {name: 'bold'},
                    {name: 'italic'},
                    '|',
                    {name: 'quote'},
                    {name: 'unordered-list'},
                    {name: 'ordered-list'},
                    '|',
                    {name: 'link'},
                    {name: 'image', html: '<form id="rewardFileUpload" method="POST" enctype="multipart/form-data"><label class="icon-image"><input type="file"/></label></form>'},
                    '|',
                    {name: 'redo'},
                    {name: 'undo'},
                    '|',
                    {name: 'preview'}
                ],
                extraKeys: {
                    "Alt-/": "autocompleteUserName",
                    "Ctrl-/": "autocompleteEmoji",
                    "F11": function (cm) {
                        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    }
                },
                status: false
            });
            addArticleRewardEditor.render();
            AddArticle.rewardEditor = addArticleRewardEditor.codemirror;

            AddArticle.rewardEditor.on('keydown', function (cm, evt) {
                if (8 === evt.keyCode) {
                    var cursor = cm.getCursor();
                    var token = cm.getTokenAt(cursor);

                    // delete the whole emoji
                    var preCursor = CodeMirror.Pos(cursor.line, cursor.ch);
                    token = cm.getTokenAt(preCursor);
                    if (/^:\S+:$/.test(token.string)) {
                        cm.replaceRange("", CodeMirror.Pos(cursor.line, token.start),
                                CodeMirror.Pos(cursor.line, token.end - 1));
                    }
                }
            });

            AddArticle.rewardEditor.on('changes', function (cm) {
                var cursor = cm.getCursor();
                var token = cm.getTokenAt(cursor);
                if (token.string.indexOf('@') === 0) {
                    cm.showHint({hint: CodeMirror.hint.userName, completeSingle: false});
                    return CodeMirror.Pass;
                }
            });
        }

        $("#articleRewardContent").next().next().height(100);
    },
    /**
     * @description 显示简要语法
     */
    grammar: function () {
        $(".grammar").slideToggle();
    }
};

AddArticle.init();

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);

    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
