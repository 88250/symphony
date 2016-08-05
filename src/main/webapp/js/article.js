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
 * @version 1.21.27.16, Aug 5, 2016
 */

/**
 * @description Add comment function.
 * @static
 */
var Comment = {
    editor: undefined,
    /**
     * 切换评论排序模式
     * @param {integer} mode 排序模式：0 传统模式，正序；1 实时模式，倒序
     * @returns {undefined}
     */
    exchangeCmtSort: function (mode) {
        mode = 0 === mode ? 1 : 0;

        window.location.href = window.location.pathname + "?m=" + mode;
    },
    /**
     * 设置评论来源
     * @returns {Boolean}
     */
    _setCmtVia: function () {
        $('.cmt-via').each(function () {
            var ua = $(this).data('ua'),
                    name = Util.getDeviceByUa(ua);
            if (name !== '') {
                $(this).html('via ' + name);
            }
        });
    },
    /**
     * 评论初始化
     * @returns {Boolean}
     */
    init: function (isLoggedIn) {
        $("#comments").on('dblclick', 'img', function () {
            window.open($(this).attr('src'));
        });

        this._setCmtVia();
        $.ua.set(navigator.userAgent);

        if (!isLoggedIn) {
            return false;
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            $('#commentContent').before('<form id="fileUpload" method="POST" enctype="multipart/form-data"><label class="btn">'
                    + Label.uploadLabel + '<input type="file"/></label></form>')
                    .css('margin', 0);
            Comment.editor = Util.initTextarea('commentContent',
                    function (editor) {
                        if (window.localStorage) {
                            window.localStorage[Label.articleOId] = JSON.stringify({
                                commentContent: editor.$it.val()
                            });
                        }
                    }
            );
        } else {
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
                    {name: 'image', html: '<form id="fileUpload" method="POST" enctype="multipart/form-data"><label class="icon-upload"><input type="file"/></label></form>'},
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
                    "Alt-R": "endAudioRecord"
                },
                status: false
            });
            commentEditor.render();

            commentEditor.codemirror['for'] = 'comment';

            Comment.editor = commentEditor.codemirror;
        }

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

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            return false;
        }

        Comment.editor.on('changes', function (cm) {
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
            // mac command + enter add article
            if ($.ua.os.name.indexOf('Mac OS') > -1 && evt.metaKey && evt.keyCode === 13) {
                Comment.add(Label.articleOId, Label.csrfToken);
                return false;
            }
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
     * @description 感谢评论.
     * @param {String} id 评论 id
     * @param {String} csrfToken CSRF 令牌
     * @param {String} tip 确认提示
     * @param {Integer} 0：公开评论，1：匿名评论
     */
    thank: function (id, csrfToken, tip, commentAnonymous) {
        if (0 === commentAnonymous && !confirm(tip)) {
            return false;
        }

        var requestJSONObject = {
            commentId: id
        };

        $.ajax({
            url: Label.servePath + "/comment/thank",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            error: function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            },
            success: function (result, textStatus) {
                if (result.sc) {
                    $("#" + id + 'Thx').remove();
                    var $cnt = $("#" + id + 'RewardedCnt'),
                            cnt = parseInt($cnt.text());
                    if ($cnt.length <= 0) {
                        $('#' + id + ' .comment-info > .fn-left .ft-smaller:last').
                                append('&nbsp;<span aria-label="' + Label.thankedLabel + ' 1" id="' +
                                        id + 'RewardedCnt" class="tooltipped tooltipped-s ft-smaller ft-red">'
                                        + '<span class="icon-heart"></span> 1</span>');
                    } else {
                        $cnt.attr('aria-label', Label.thankedLabel + ' ' + (cnt + 1));
                        $cnt.html('<span class="icon-heart"></span> ' + (cnt + 1)).addClass('ft-red').removeClass('ft-fade');
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
            commentAnonymous: $('#commentAnonymous').prop('checked'),
            commentContent: Comment.editor.getValue() // 实际提交时不去除空格，因为直接贴代码时需要空格
        };

        $.ajax({
            url: Label.servePath + "/comment",
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
                    // reset comment editor
                    $('.editor-preview').html('');
                    if ($('.icon-preview').hasClass('active')) {
                        $('.icon-preview').click();
                    }
                    // first comment, pls add icon
                    if ($('#comments > ul li').length === 0) {
                        $('#comments > div > span').show();
                    }

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
        $.ua.set(navigator.userAgent);
        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            var $it = $('#commentContent'),
                    it = $it[0],
                    index = 0;
            if (document.selection) { // IE
                try {
                    var cuRange = document.selection.createRange();
                    var tbRange = it.createTextRange();
                    tbRange.collapse(true);
                    tbRange.select();
                    var headRange = document.selection.createRange();
                    headRange.setEndPoint("EndToEnd", cuRange);
                    index = headRange.text.length;
                    cuRange.select();
                } catch (e) {
                    delete e;
                }
            } else {
                index = it.selectionStart;
            }

            $it.val($it.val().substr(0, index) + userName + ' ' + $it.val().substr(index));
            var insertIndex = ($it.val().substr(0, index) + userName).length;
            if (it.setSelectionRange) {
                it.focus();
                it.setSelectionRange(insertIndex, insertIndex);
            } else if (it.createTextRange) {
                var range = it.createTextRange();
                range.collapse(true);
                range.moveEnd('character', insertIndex);
                range.moveStart('character', insertIndex);
                range.select();
            }
            return false;
        }
        var cursor = Comment.editor.getCursor();
        Comment.editor.doc.replaceRange(userName, cursor, cursor);
    }
};

var Article = {
    /**
     * @description 赞同
     * @param {String} id 赞同的实体数据 id
     * @param {String} type 赞同的实体类型
     */
    voteUp: function (id, type) {
        var voteUpId = "#voteUp_" + type + id;
        var voteDownId = "#voteDown_" + type + id;

        if ($(voteUpId).hasClass("disabled")) {
            return false;
        }

        var requestJSONObject = {
            dataId: id
        };

        $(voteUpId).addClass("disabled");

        $.ajax({
            url: Label.servePath + "/vote/up/" + type,
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                $(voteUpId).removeClass("disabled");
                var upCnt = parseInt($(voteUpId).attr('aria-label').substr(3)),
                        downCnt = parseInt($(voteDownId).attr('aria-label').substr(3));
                if (result.sc) {
                    if (0 === result.type) { // cancel up
                        $(voteUpId).attr('aria-label', Label.upLabel + ' ' + (upCnt - 1)).children().removeClass("ft-red");
                        if ('comment' === type && upCnt - 1 < 1) {
                            $(voteUpId).addClass('fn-hidden').addClass('hover-show');
                        }
                    } else {
                        $(voteUpId).removeClass('fn-hidden').removeClass('hover-show').attr('aria-label', Label.upLabel + ' ' + (upCnt + 1)).children()
                                .addClass("ft-red");
                        if ($(voteDownId).children().hasClass('ft-red')) {
                            $(voteDownId).attr('aria-label', Label.downLabel + ' ' + (downCnt - 1)).children().removeClass("ft-red");
                            if ('comment' === type && downCnt - 1 < 1) {
                                $(voteDownId).addClass('fn-hidden').addClass('hover-show');
                            }
                        }
                    }

                    return;
                }

                alert(result.msg);
            }
        });
    },
    /**
     * @description 反对
     * @param {String} id 反对的实体数据 id
     * @param {String} type 反对的实体类型
     */
    voteDown: function (id, type) {
        var voteUpId = "#voteUp_" + type + id;
        var voteDownId = "#voteDown_" + type + id;

        if ($(voteDownId).hasClass("disabled")) {
            return false;
        }

        var requestJSONObject = {
            dataId: id
        };

        $(voteDownId).addClass("disabled");

        $.ajax({
            url: Label.servePath + "/vote/down/" + type,
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                $(voteDownId).removeClass("disabled");
                var upCnt = parseInt($(voteUpId).attr('aria-label').substr(3)),
                        downCnt = parseInt($(voteDownId).attr('aria-label').substr(3));
                if (result.sc) {
                    if (1 === result.type) { // cancel down
                        $(voteDownId).attr('aria-label', Label.downLabel + ' ' + (downCnt - 1)).children().removeClass("ft-red");
                        if ('comment' === type && downCnt - 1 < 1) {
                            $(voteDownId).addClass('fn-hidden').addClass('hover-show');
                        }
                    } else {
                        $(voteDownId).removeClass('fn-hidden').removeClass('hover-show')
                                .attr('aria-label', Label.downLabel + ' ' + (downCnt + 1)).children()
                                .addClass("ft-red");
                        if ($(voteUpId).children().hasClass('ft-red')) {
                            $(voteUpId).attr('aria-label', Label.upLabel + ' ' + (upCnt - 1)).children().removeClass("ft-red");
                            if ('comment' === type && upCnt - 1 < 1) {
                                $(voteUpId).addClass('fn-hidden').addClass('hover-show');
                            }
                        }
                    }

                    return false;
                }

                alert(result.msg);
            }
        });
    },
    /**
     * @description 初始化文章
     */
    init: function () {
        this.share();
        this.parseLanguage();

        $(".content-reset.article-content").on('dblclick', 'img', function () {
            if ($(this).hasClass('emoji')) {
                return false;
            }
            window.open($(this).attr('src'));
        });

        var ua = $('#articltVia').data('ua'),
                name = Util.getDeviceByUa(ua);
        if (name !== '') {
            $('#articltVia').text('via ' + name);
        }

        $('#revision').dialog({
            "width": $(window).width() - 50,
            "height": $(window).height() - 50,
            "modal": true,
            "hideFooter": true
        });

        this.initToc();
    },
    /**
     * 历史版本对比
     * @returns {undefined}
     */
    revision: function (articleId) {
        if (!Label.isLoggedIn) {
            Util.showLogin();
            return false;
        }
        if ($('.CodeMirror-merge').length > 0) {
            $('#revision').dialog('open');
            return false;
        }
        $.ajax({
            url: Label.servePath + '/article/' + articleId + '/revisions',
            cache: false,
            success: function (result, textStatus) {
                if (result.sc) {
                    if (0 === result.revisions.length // for legacy data
                            || 1 === result.revisions.length) {
                        $('#revisions').html('<b>' + Label.noRevisionLabel + '</b>');
                        return false;
                    }

                    $('#revisions').data('revisions', result.revisions).
                            before('<div class="fn-clear"><div class="pagination">' +
                                    '<a href="javascript:void(0)">&lt;</a><span class="current">' +
                                    (result.revisions.length - 1) + '~' + result.revisions.length + '/' +
                                    result.revisions.length + '</span><a class="fn-none">&gt;</a>' +
                                    '</div></div>');
                    if (result.revisions.length <= 2) {
                        $('#revision a').first().hide();
                    }
                    Article.mergeEditor = CodeMirror.MergeView(document.getElementById('revisions'), {
                        value: result.revisions[result.revisions.length - 1].revisionData.articleTitle +
                                '\n\n' + result.revisions[result.revisions.length - 1].revisionData.articleContent,
                        origLeft: result.revisions[result.revisions.length - 2].revisionData.articleTitle +
                                '\n\n' + result.revisions[result.revisions.length - 2].revisionData.articleContent,
                        revertButtons: false,
                        mode: "text/html",
                        collapseIdentical: true
                    });
                    Article._revisionsControls();
                    return false;
                }

                alert(result.msg);
            }
        });
        $('#revision').dialog('open');
    },
    /**
     * 上一版本，下一版本对比
     * @returns {undefined}
     */
    _revisionsControls: function () {
        var revisions = $('#revisions').data('revisions');
        $('#revision a').first().click(function () {
            var prevVersion = parseInt($('#revision .current').text().split('~')[0]);
            if (prevVersion <= 2) {
                $(this).hide();
            } else {
                $(this).show();
            }
            if (prevVersion < 2) {
                return false;
            }

            $('#revision a').last().show();

            $('#revision .current').html((prevVersion - 1) + '~' + prevVersion + '/' + revisions.length);
            Article.mergeEditor.edit.setValue(revisions[prevVersion - 1].revisionData.articleTitle + '\n\n' +
                    revisions[prevVersion - 1].revisionData.articleContent);
            Article.mergeEditor.leftOriginal().setValue(revisions[prevVersion - 2].revisionData.articleTitle + '\n\n' +
                    revisions[prevVersion - 2].revisionData.articleContent);
        });

        $('#revision a').last().click(function () {
            var prevVersion = parseInt($('#revision .current').text().split('~')[0]);
            if (prevVersion > revisions.length - 3) {
                $(this).hide();
            } else {
                $(this).show();
            }
            if (prevVersion > revisions.length - 2) {
                return false;
            }
            $('#revision a').first().show();
            $('#revision .current').html((prevVersion + 1) + '~' + (prevVersion + 2) + '/' + revisions.length);
            Article.mergeEditor.edit.setValue(revisions[prevVersion + 1].revisionData.articleTitle + '\n\n' +
                    revisions[prevVersion + 1].revisionData.articleContent);
            Article.mergeEditor.leftOriginal().setValue(revisions[prevVersion].revisionData.articleTitle + '\n\n' +
                    revisions[prevVersion].revisionData.articleContent);
        });
    },
    /**
     * @description 分享按钮
     */
    share: function () {
        var shareURL = $('#qrCode').data('shareurl');
        $('#qrCode').qrcode({
            width: 90,
            height: 90,
            text: shareURL
        });

        $('body').click(function () {
            $('#qrCode').slideUp();
        });

        $(".share > span").click(function () {
            var key = $(this).data("type");
            if (key === 'wechat') {
                $('#qrCode').slideToggle();
                return false;
            }

            if (key === 'copy') {
                return false;
            }

            var title = encodeURIComponent(Label.articleTitle + " - " + Label.symphonyLabel),
                    url = encodeURIComponent(shareURL),
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

        $('#qrCode').click(function () {
            $(this).hide();
        });

        if (typeof (ZeroClipboard) !== "undefined") {
            $('#shareClipboard').mouseover(function () {
                $(this).attr('aria-label', Label.copyLabel);
            });

            ZeroClipboard.config({
                hoverClass: "tooltipped-hover",
                swfPath: Label.staticServePath + "/js/lib/zeroclipboard/ZeroClipboard.swf"
            });

            var shareClipboard = new ZeroClipboard(document.getElementById("shareClipboard"));
            shareClipboard.on("ready", function (readyEvent) {
                shareClipboard.on("aftercopy", function (event) {
                    $('#shareClipboard').attr('aria-label', Label.copiedLabel);
                });
            });
        }
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
                url: Label.servePath + "/article/reward?articleId=" + articleId,
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
     * @description 感谢文章
     */
    thankArticle: function (articleId, articleAnonymous) {
        if (0 === articleAnonymous && !confirm(Label.thankArticleConfirmLabel)) {
            return false;
        }

        $.ajax({
            url: Label.servePath + "/article/thank?articleId=" + articleId,
            type: "POST",
            cache: false,
            success: function (result, textStatus) {
                if (result.sc) {
                    var thxCnt = parseInt($('#thankArticle').attr('aria-label').substr(3));
                    $("#thankArticle").removeAttr("onclick").find("span").addClass("ft-red");
                    $("#thankArticle").attr('aria-label', Label.thankLabel + ' ' + (thxCnt + 1));

                    return false;
                }

                alert(result.msg);
            }
        });
    },
    /**
     * @description 置顶
     */
    stick: function (articleId) {
        var r = confirm(Label.stickConfirmLabel);

        if (r) {
            $.ajax({
                url: Label.servePath + "/article/stick?articleId=" + articleId,
                type: "POST",
                cache: false,
                success: function (result, textStatus) {
                    alert(result.msg);

                    window.location.href = "/";
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
        var genThought = function (record, articleLinesList) {
            var units = record.split("");
            if (units.length === 3) {
                units.splice(0, 0, '');
            }
            var srcLinesContent = units[0],
                    from = units[2].split('-'),
                    to = units[3].split('-');
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
            return articleLinesList;
        };

        var records = articleContent.split("");
        for (var i = 0, j = 0; i < records.length; i++) {
            setTimeout(function () {
                if (!$('.article-content').data('text')) {
                    $('.article-content').data('text', '');
                }

                var articleLinesList = genThought(records[j++], $('.article-content').data('text').split(String.fromCharCode(10)));

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
                $('#thoughtProgress .bar').width('100%');
                $('#thoughtProgress .icon-video').css('left', '100%');
                clearInterval(interval);
            } else {
                currentTime += 50;
                $('#thoughtProgress .icon-video').css('left', (currentTime * 100 / amountTime) + '%');
                $('#thoughtProgress .bar').width((currentTime * 100 / amountTime) + '%');
            }

        }, 50);

        // preview
        for (var v = 0, k = 0; v < records.length; v++) {
            var articleLinesList = genThought(records[k++], $('#thoughtProgressPreview').data('text').split(String.fromCharCode(10)));

            var articleText = articleLinesList.join(String.fromCharCode(10));
            var articleHTML = articleText.replace(/\n/g, "<br>")
                    .replace(/ /g, "&nbsp;")
                    .replace(/	/g, "&nbsp;&nbsp;&nbsp;&nbsp;");

            $('#thoughtProgressPreview').data('text', articleText).html(articleHTML);
        }
        $("#thoughtProgressPreview").dialog({
            "modal": true,
            "hideFooter": true
        });
        $('#thoughtProgress .icon-video').click(function () {
            $("#thoughtProgressPreview").dialog("open");
        });
    },
    /**
     * @description 初始化目录.
     */
    initToc: function () {
        if ($('#articleToC').length === 0) {
            return false;
        }

        // 样式
        var $articleToc = $('#articleToC'),
                top = $articleToc.offset().top;

        $articleToc.css('width', $('.side').width() + 'px');
        $articleToc.next().css({
            'width': $('.side').width() + 'px',
            'top': ($articleToc.height() + 41) + 'px'
        });
        $articleToc.next().next().css({
            'width': $('.side').width() + 'px',
            'top': ($articleToc.height() + $articleToc.next().height() + 62) + 'px'
        });

        $('.article-toc').css({
            'overflow': 'auto',
            'max-height': $(window).height() - 127 + 'px'
        });

        // 目录点击
        $articleToc.find('li').click(function () {
            var $it = $(this);
            setTimeout(function () {
                $articleToc.find('li').removeClass('current');
                $it.addClass('current');
            }, 50);
        });

        var toc = [];
        $('.article-content [id^=toc]').each(function (i) {
            var $it = $(this);
            toc.push({
                id: $it.attr('id'),
                offsetTop: $it.offset().top
            });
        });

        $(window).scroll(function (event) {
            if ($('#articleToC').css('display') === 'none') {
                return false;
            }

            // 当前目录样式
            var scrollTop = $('body').scrollTop();

            for (var i = 0, iMax = toc.length; i < iMax; i++) {
                if (scrollTop < toc[i].offsetTop - 5) {
                    $articleToc.find('li').removeClass('current');
                    var index = i > 0 ? i - 1 : 0;
                    $articleToc.find('a[href=#' + toc[index].id + ']').parent().addClass('current');
                    break;
                }
            }

            if ($(window).height() + scrollTop >= $('body').height()) {
                $articleToc.find('li').removeClass('current');
                $articleToc.find('li:last').addClass('current');
            }

            // 位置是否固定
            if ($('body').scrollTop() > top - 20) {
                $articleToc.css('position', 'fixed');
                $articleToc.next().css('position', 'fixed');
                $articleToc.next().next().css('position', 'fixed');
            } else {
                $articleToc.css('position', 'inherit');
                $articleToc.next().css('position', 'inherit');
                $articleToc.next().next().css('position', 'inherit');
            }
        }).resize(function () {
            $articleToc.css('width', $('.side').width() + 'px');
            $articleToc.next().css({
                'width': $('.side').width() + 'px'
            });
            $articleToc.next().next().css({
                'width': $('.side').width() + 'px'
            });
        });

        $(window).scroll();
    },
    /**
     * @description 目录展现隐藏切换.
     */
    toggleToc: function () {
        var $articleToc = $('#articleToC');
        if ($articleToc.length === 0) {
            return false;
        }

        var $menu = $('.article-action .icon-unordered-list');
        if ($menu.hasClass('ft-red')) {
            $articleToc.hide();
            $menu.removeClass('ft-red');
        } else {
            $articleToc.show();
            $menu.addClass('ft-red');
            $articleToc.css('position', 'inherit');
            $articleToc.find('li').removeClass('current');
            $articleToc.find('li:first').addClass('current');
        }

        $articleToc.next().css('position', 'inherit');
        $articleToc.next().next().css('position', 'inherit');
    },
    /**
     * @description 标记消息通知为已读状态.
     */
    makeNotificationRead: function (articleId, commentIds) {
        var requestJSONObject = {
            articleId: articleId,
            commentIds: commentIds
        };

        $.ajax({
            url: Label.servePath + "/notification/read",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject)
        });
    }
};

Article.init();