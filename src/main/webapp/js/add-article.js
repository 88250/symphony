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
 * @version 1.6.2.0, Jul 2, 2015
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
     */
    add: function (id) {
        var isError = false;
        if (this.editor.getValue().length < 4 || this.editor.getValue().length > 1048576) {
            $("#articleContentTip").addClass("tip-error").text(Label.articleContentErrorLabel);
        } else {
            isError = true;
            $("#articleContentTip").removeClass("tip-error").text("");
        }

        if (Validate.goValidate([{
                "id": "articleTitle",
                "type": 256,
                "msg": Label.articleTitleErrorLabel
            }, {
                "id": "articleTags",
                "type": "tags",
                "msg": Label.articleTagsErrorLabel
            }]) && isError) {
            var requestJSONObject = {
                articleTitle: $("#articleTitle").val().replace(/(^\s*)|(\s*$)/g, ""),
                articleContent: this.editor.getValue(),
                articleTags: $("#articleTags").val().replace(/(^\s*)|(\s*$)/g, ""),
                syncWithSymphonyClient: $("#syncWithSymphonyClient").prop("checked"),
                articleCommentable: $("#articleCommentable").prop("checked"),
                articleType: $("#articleType").prop("checked") ? 1 : 0,
                articleRewardContent: this.rewardEditor.getValue(),
                articleRewardPoint: $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")
            },
            url = "/article", type = "POST";

            if (id) {
                url = url + "/" + id;
                type = "PUT";
            }

            $.ajax({
                url: url,
                type: type,
                cache: false,
                data: JSON.stringify(requestJSONObject),
                beforeSend: function () {
                    $(".form button.red").attr("disabled", "disabled").css("opacity", "0.3");
                },
                success: function (result, textStatus) {
                    $(".form button.red").removeAttr("disabled").css("opacity", "1");
                    if (result.sc) {
                        window.location = "/member/" + Label.userName;
                    } else {
                        $("#addArticleTip").addClass("tip-error").text(result.msg);
                    }
                },
                complete: function () {
                    $(".form button.red").removeAttr("disabled").css("opacity", "1");
                }
            });
        }
    },
    /**
     * @description 初识化发文页面
     */
    init: function () {
        Util.initCodeMirror();

        // 初始化文章编辑器
        AddArticle.editor = CodeMirror.fromTextArea(document.getElementById("articleContent"), {
            mode: 'markdown',
            dragDrop: false,
            extraKeys: {
                "'@'": "autocompleteUserName",
                "Ctrl-/": "autocompleteEmoji",
                "F11": function (cm) {
                    cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                }
            }
        });

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

        AddArticle.editor.on('changes', function (cm) {
            if (cm.getValue().replace(/(^\s*)|(\s*$)/g, "") !== "") {
                $(".form .green").show();
            } else {
                $(".form .green").hide();
            }
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
            }
        });
    },
    /**
     * @description 显示简要语法
     */
    grammar: function () {
        var $grammar = $(".grammar"),
                $codemirror = $(".CodeMirror");
        if ($("#articleTitle").width() < 500) {
            // for mobile
            $grammar.toggle();
            return;
        }
        if ($codemirror.width() > 900) {
            $grammar.show();
            $codemirror.width(750);
        } else {
            $grammar.hide();
            $codemirror.width(996);
        }
    }
};

AddArticle.init();