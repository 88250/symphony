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
 * @version 1.2.2.2, Jun 20, 2015
 */

/**
 * @description Add comment function.
 * @static
 */
var Comment = {
    _validateData: [{
            "id": "commentContent",
            "type": 1000,
            "msg": Label.commentErrorLabel
        }],
    editor: undefined,
    init: function () {
        Util.initCodeMirror();

        Comment.editor = CodeMirror.fromTextArea(document.getElementById("commentContent"), {
            mode: 'markdown',
            extraKeys: {
                "'@'": "autocompleteUserName",
                "Ctrl-/": "autocompleteEmoji"
            }
        });

        Comment.editor.on('changes', function (cm) {
            if (cm.getValue().replace(/(^\s*)|(\s*$)/g, "") !== "") {
                $(".form .green").show();
            } else {
                $(".form .green").hide();
            }

            $(".CodeMirror").next().removeClass("tip-error").text('');
        });

        Comment.editor.on('keypress', function (cm, evt) {
            if (evt.ctrlKey && 10 === evt.charCode) {
                Comment.add(Label.articleOId);

                return;
            }
        });

        $("#preview").dialog({
            "modal": true,
            "hideFooter": true
        });
    },
    /**
     * @description 添加评论
     */
    add: function (id) {
        var requestJSONObject = {
            articleId: id,
            commentContent: Comment.editor.getValue().replace(/(^\s*)|(\s*$)/g, "")
        };

        $.ajax({
            url: "/comment",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $(".form button.green").attr("disabled", "disabled").css("opacity", "0.3");
            },
            success: function (result, textStatus) {
                $(".form button.green").removeAttr("disabled").css("opacity", "1");
                if (result.sc) {
                    Comment.editor.setValue('');
                    // window.location.reload();
                } else {
                    $(".CodeMirror").next().addClass("tip-error").text(result.msg);
                }
            },
            complete: function () {
                $(".form button.green").removeAttr("disabled").css("opacity", "1");
            }
        });
    },
    /**
     * @description 预览文章
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
     * @description 初识化发文页面
     */
    init: function () {
        $("#preview").dialog({
            "modal": true,
            "hideFooter": true
        });
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
                    url = "http://symphony.b3log.org" + Label.articlePermalink,
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
        var isPrettify = false;

        $(".content-reset pre, .content-reset > p > code").each(function () {
            this.className = "prettyprint";
            isPrettify = true;
        });

        if (isPrettify) {
            prettyPrint();
        }
    }
};

Article.init();
Comment.init();

function startsWith(string, prefix) {
    return (string.match("^" + prefix) == prefix);
}
