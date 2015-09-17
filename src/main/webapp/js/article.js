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
 * @fileoverview article page and add comment.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.11.12.6, Sep 17, 2015
 */

/**
 * @description Add comment function.
 * @static
 */
var Comment = {
    editor: undefined,
    init: function () {
        $("#comments").on('dblclick', 'img', function () {
            window.open($(this).attr('src'));
        });

        if (!isLoggedIn) {
            return false;
        }

        Util.initCodeMirror();
        Comment.editor = CodeMirror.fromTextArea(document.getElementById("commentContent"), {
            mode: 'markdown',
            dragDrop: false,
            lineWrapping: true,
            extraKeys: {
                "'@'": "autocompleteUserName",
                "Ctrl-/": "autocompleteEmoji",
                "Alt-S": "startAudioRecord",
                "Alt-E": "endAudioRecord"
            }
        });

        if (window.localStorage && window.localStorage[Label.articleOId]) {
            var localData = null;

            try {
                localData = JSON.parse(window.localStorage[Label.articleOId]);
            } catch (e) {
                var emptyContent = {
                    commentContent: ""
                };

                window.localStorage[Label.articleOId] = JSON.stringify(emptyContent);
                localData = JSON.parse(window.localStorage[Label.articleOId]);
            }

            if ("" !== localData.commentContent.replace(/(^\s*)|(\s*$)/g, "")) {
                Comment.editor.setValue(localData.commentContent);
            }
        }

        Comment.editor.on('changes', function (cm) {
            if (cm.getValue().replace(/(^\s*)|(\s*$)/g, "") !== "") {
                $(".form .green").show();
            } else {
                $(".form .green").hide();
            }

            $("#addCommentTip").removeClass("error succ").html('');

            if (window.localStorage) {
                window.localStorage[Label.articleOId] = JSON.stringify({
                    commentContent: cm.getValue()
                });
            }
        });

        Comment.editor.on('keypress', function (cm, evt) {
            if (evt.ctrlKey && 10 === evt.charCode) {
                Comment.add(Label.articleOId, Label.csrfToken);

                return;
            }
        });

        Comment.editor.on('keydown', function (cm, evt) {
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

        $("#preview").dialog({
            "modal": true,
            "hideFooter": true
        });
    },
    /**
     * @description 感谢.
     * @param {String} id 评论 id
     * @param {String} csrfToken CSRF 令牌
     * @param {string} tip 确认提示
     * @param {string} thxed 已感谢文案
     */
    thank: function (id, csrfToken, tip, thxed) {
        if (!confirm(tip)) {
            return false;
        }

        var requestJSONObject = {
            commentId: id
        };

        $.ajax({
            url: "/comment/thank",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                if (result.sc) {
                    $("#" + id + 'Thx').text(thxed).removeAttr('onclick').removeClass('fn-none thx fn-pointer');
                    var cnt = parseInt($("#" + id + 'RewardedCnt').text());
                    if ($("#" + id + 'RewardedCnt').length <= 0) {
                        $('#' + id + ' .comment-info > .fn-left .ft-smaller:last').
                                append('&nbsp;<span class="icon-heart ft-smaller ft-fade"></span> <span class="ft-smaller ft-fade">1</span>');
                    } else {
                        $("#" + id + 'RewardedCnt').text(cnt + 1);
                    }
                } else {
                    alert(result.msg);
                }
            }
        });
    },
    /**
     * @description 添加评论
     * @param {String} id 文章 id
     * @param {String} csrfToken CSRF 令牌
     */
    add: function (id, csrfToken) {
        if (!Validate.goValidate({
            target: $("#addCommentTip"),
            data: [{
                    "target": Comment.editor,
                    "type": 'editor',
                    'max': 1000,
                    "msg": Label.commentErrorLabel
                }]
        })) {
            return false;
        }

        var requestJSONObject = {
            articleId: id,
            commentContent: Comment.editor.getValue() // 实际提交时不去除空格，因为直接贴代码时需要空格
        };

        $.ajax({
            url: "/comment",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $(".form button.red").attr("disabled", "disabled").css("opacity", "0.3");
                Comment.editor.setOption("readOnly", "nocursor");
            },
            success: function (result, textStatus) {
                $(".form button.red").removeAttr("disabled").css("opacity", "1");
                if (result.sc) {
                    Comment.editor.setValue('');
                    // window.location.reload();

                    if (window.localStorage) {
                        var emptyContent = {
                            commentContent: ""
                        };

                        window.localStorage[Label.articleOId] = JSON.stringify(emptyContent);
                    }
                } else {
                    $("#addCommentTip").addClass("error").html('<ul><li>' + result.msg + '</li></ul>');
                }
            },
            error: function (result) {
                $("#addCommentTip").addClass("error").html('<ul><li>' + result.statusText + '</li></ul>');
            },
            complete: function () {
                $(".form button.red").removeAttr("disabled").css("opacity", "1");
                Comment.editor.setOption("readOnly", false);
            }
        });
    },
    /**
     * @description 预览评论
     */
    preview: function () {
        $.ajax({
            url: "/markdown",
            type: "POST",
            cache: false,
            data: {
                markdownText: Comment.editor.getValue()
            },
            success: function (result, textStatus) {
                $(".dialog-background").height($("body").height());
                $("#preview").dialog("open");
                $("#preview").html(result.html);
                hljs.initHighlighting.called = false;
                hljs.initHighlighting();
            }
        });
    },
    /**
     * @description 点击回复评论时，把当楼层的用户名带到评论框中
     * @param {String} userName 用户名称
     */
    replay: function (userName) {
        Comment.editor.focus();
        var cursor = Comment.editor.getCursor();

        Comment.editor.doc.replaceRange(userName, cursor, cursor);
    }
};

var Article = {
    /**
     * @description 初识化文章
     */
    init: function () {
        this.share();
        this.parseLanguage();
    },
    /**
     * @description 分享按钮
     */
    share: function () {
        $(".share span").click(function () {
            var key = $(this).data("type");
            var title = encodeURIComponent(Label.articleTitle + " - " + Label.symphonyLabel),
                    url = "http://hacpai.com" + Label.articlePermalink,
                    pic = $(".content-reset img").attr("src");
            var urls = {};
            urls.tencent = "http://share.v.t.qq.com/index.php?c=share&a=index&title=" + title +
                    "&url=" + url + "&pic=" + pic;
            urls.weibo = "http://v.t.sina.com.cn/share/share.php?title=" +
                    title + "&url=" + url + "&pic=" + pic;
            urls.google = "https://plus.google.com/share?url=" + url;
            urls.twitter = "https://twitter.com/intent/tweet?status=" + title + " " + url;
            window.open(urls[key], "_blank", "top=100,left=200,width=648,height=618");
        });
    },
    /*
     * @description 解析语法高亮
     */
    parseLanguage: function () {
        $(".content-reset pre").each(function () { // 兼容从 Solo 同步过来的文章
            $(this).wrapInner('<code></code>');
            $(this).removeAttr('class');
        });

        hljs.initHighlightingOnLoad();
    },
    /**
     * @description 打赏
     */
    reward: function (articleId) {
        var r = confirm(Label.rewardConfirmLabel);

        if (r) {
            $.ajax({
                url: "/article/reward?articleId=" + articleId,
                type: "POST",
                cache: false,
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#articleRewardContent").removeClass("reward").html(result.articleRewardContent);
                        return;
                    }

                    alert(result.msg);
                }
            });
        }
    },
    /**
     * @description 播放思绪
     */
    playThought: function (articleContent) {
        // - 0x1E: Record Separator (记录分隔符)
        // + 0x1F: Unit Separator (单元分隔符)

        var records = articleContent.split("");
        for (var i = 0, j = 0; i < records.length - 1; i++) {
            setTimeout(function () {
                var units = records[j++].split("");

                var text = units[0].replace("<p>", "").replace("</p>", ""),
                        from = units[2].split('-'),
                        to = units[3].split('-'),
                        time = units[1],
                        article = $.trim($('.article-content').html()),
                        lines = article.split('<br>'),
                        texts = text.split(""),
                        result = '';


                if (units.length === 5) {
                    // remove
                    var removeLines = [];
                    for (var n = from[1], m = 0; n <= to[1]; n++, m++) {
                        if (n === from[1]) {
                            lines[n] = lines[n].substring(0, from[0] - 1) +
                                    lines[n].substr(from[0] - 1 + texts[m]);
                        } else if (n === to[1]) {
                            lines[n] = lines[n].substring(0, to[0] - 1) +
                                    lines[n].substr(to[0] - 1 + texts[m]);
                        } else {
                            removeLines[n];
                        }
                    }
                    for (var o = 0; o < removeLines.length; o++) {
                        lines.split(removeLines[o] - o, 1);
                    }
                } else {
                    for (var l = 0; l < texts.length; l++) {
                        if (l === texts.length - 1) {
                            text += texts[l];
                        } else {
                            text += texts[l] + '<br>';
                        }
                    }

                    lines[from[1]] = lines[from[1]].substring(0, from[0]) + text
                            + lines[from[1]].substr(from[0]);
                }
                console.log(lines);
                for (var k = 0; k < lines.length; k++) {
                    result += lines[k] + '<br>';
                }
                $('.article-content').html(result.slice(0, result.length - 4));

            }, 1000 + i * 300);
        }

    }
};

Article.init();
Comment.init();
