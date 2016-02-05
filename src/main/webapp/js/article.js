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
 * @fileoverview article page and add comment.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.12.15.7, Feb 5, 2016
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

        var commentEditor = new Editor({
            element: document.getElementById('commentContent'),
            dragDrop: false,
            lineWrapping: true,
            toolbar: [
                {name: 'bold'},
                {name: 'italic'},
                '|',
                {name: 'quote'},
                {name: 'unordered-list'},
                {name: 'ordered-list'},
                '|',
                {name: 'link'},
                {name: 'image', html: '<form id="fileUpload" method="POST" enctype="multipart/form-data"><input type="file" class="icon-image"/></form>'},
                '|',
                {name: 'redo'},
                {name: 'undo'},
                '|',
                {name: 'preview'}
            ],
            extraKeys: {
                "Alt-/": "autocompleteUserName",
                "Ctrl-/": "autocompleteEmoji",
                "Alt-S": "startAudioRecord",
                "Alt-E": "endAudioRecord"
            },
            status: false
        });
        commentEditor.render();

        Comment.editor = commentEditor.codemirror;

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

            var cursor = cm.getCursor();
            var token = cm.getTokenAt(cursor);
            if (token.string.indexOf('@') === 0) {
                cm.showHint({hint: CodeMirror.hint.userName, completeSingle: false});
                return CodeMirror.Pass;
            }
        });

        Comment.editor.on('keypress', function (cm, evt) {
            if (evt.ctrlKey && 10 === evt.charCode) {
                Comment.add(Label.articleOId, Label.csrfToken);

                return;
            }
        });

        Comment.editor.on('keydown', function (cm, evt) {
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
                    'max': 2000,
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
     * @param {string} articleContent 记录过程
     */
    playThought: function (articleContent) {
        // - 0x1E: Record Separator (记录分隔符)
        // + 0x1F: Unit Separator (单元分隔符)

        var fast = 2;

        var records = articleContent.split("");
        for (var i = 0, j = 0; i < records.length; i++) {
            setTimeout(function () {
                if (!$('.article-content').data('text')) {
                    $('.article-content').data('text', '');
                }
                var units = records[j++].split(""),
                        srcLinesContent = units[0],
                        from = units[2].split('-'),
                        to = units[3].split('-'),
                        articleLinesList = $('.article-content').data('text').split(String.fromCharCode(10));
                from[0] = parseInt(from[0]);
                from[1] = parseInt(from[1]);
                to[0] = parseInt(to[0]);
                to[1] = parseInt(to[1]);

                if (srcLinesContent === "") {
                    // remove
                    var removeLines = [];
                    for (var n = from[1], m = 0; n <= to[1]; n++, m++) {
                        if (from[1] === to[1]) {
                            articleLinesList[n] = articleLinesList[n].substring(0, from[0]) +
                                    articleLinesList[n].substr(to[0]);
                            break;
                        }

                        if (n === from[1]) {
                            articleLinesList[n] = articleLinesList[n].substr(0, from[0]);
                        } else if (n === to[1]) {
                            articleLinesList[from[1]] += articleLinesList[n].substr(to[0]);
                            articleLinesList.splice(n, 1);
                        } else {
                            removeLines.push(n);
                        }
                    }
                    for (var o = 0; o < removeLines.length; o++) {
                        articleLinesList.splice(removeLines[o] - o, 1);
                    }
                } else {
                    var addLines = srcLinesContent.split(String.fromCharCode(29))[0],
                            removedLines = srcLinesContent.split(String.fromCharCode(29))[1];

                    if (removedLines === '') {
                        articleLinesList[from[1]] = articleLinesList[from[1]].substring(0, from[0]) +
                                articleLinesList[to[1]].substr(to[0]);
                    }

                    articleLinesList[from[1]] = articleLinesList[from[1]].substring(0, from[0]) + addLines
                            + articleLinesList[from[1]].substr(from[0]);
                }


                var articleText = articleLinesList.join(String.fromCharCode(10));
                var articleHTML = articleText.replace(/\n/g, "<br>")
                        .replace(/ /g, "&nbsp;")
                        .replace(/	/g, "&nbsp;&nbsp;&nbsp;&nbsp;");

                $('.article-content').data('text', articleText).html(articleHTML);

            }, parseInt(records[i].split("")[1]) / fast);
        }

        // progress
        var currentTime = 0,
                amountTime = parseInt(records[i - 1].split("")[1]) / fast + 300;
        var interval = setInterval(function () {
            if (currentTime >= amountTime) {
                $('#thoughtProgress div').width('100%');
                clearInterval(interval);
            } else {
                currentTime += 50;
                $('#thoughtProgress div').width((currentTime * 100 / amountTime) + '%');
            }

        }, 50);
    }
};

Article.init();
Comment.init();
