/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * @fileoverview add-article.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 2.24.19.20, May 4, 2017
 */

/**
 * @description Add article function.
 * @static
 */
var AddArticle = {
    editor: undefined,
    rewardEditor: undefined,
    /**
     * @description 删除文章
     * @csrfToken [string] CSRF 令牌
     * @it [bom] 调用事件的元素
     */
    remove: function (csrfToken, it) {
        if (!confirm(Label.confirmRemoveLabel)) {
            return;
        }

        $.ajax({
            url: Label.servePath + "/article/" + Label.articleOId + "/remove",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            beforeSend: function () {
                $(it).attr("disabled", "disabled").css("opacity", "0.3");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#addArticleTip").addClass('error').html('<ul><li>' + errorThrown + '</li></ul>');
            },
            success: function (result, textStatus) {
                $(it).removeAttr("disabled").css("opacity", "1");
                if (0 === result.sc) {
                    window.location.href = Label.servePath + "/member/" + Label.userName;
                } else {
                    $("#addArticleTip").addClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                }
            },
            complete: function () {
                $(it).removeAttr("disabled").css("opacity", "1");
            }
        });
    },
    /**
     * @description 发布文章
     * @csrfToken [string] CSRF 令牌
     * @it [Bom] 触发事件的元素
     */
    add: function (csrfToken, it) {
        if (Validate.goValidate({
                target: $('#addArticleTip'),
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
                }]
            })) {
            // 打赏区启用后积分不能为空
            if ($('#articleRewardPoint').data('orval')
                && !/^\+?[1-9][0-9]*$/.test($('#articleRewardPoint').val())) {
                $("#addArticleTip").addClass('error').html('<ul><li>'
                    + Label.articleRewardPointErrorLabel + '</li></ul>');
                return false;
            }

            var articleTags = '';
            $('.tags-input .tag .text').each(function () {
                articleTags += $(this).text() + ',';
            });
            var requestJSONObject = {
                    articleTitle: $("#articleTitle").val().replace(/(^\s*)|(\s*$)/g, ""),
                    articleContent: this.editor.getValue(),
                    articleTags: articleTags,
                    articleCommentable: true,
                    articleType: $("input[type='radio'][name='articleType']:checked").val(),
                    articleRewardContent: this.rewardEditor.getValue(),
                    articleRewardPoint: $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, ""),
                    articleAnonymous: $('#articleAnonymous').prop('checked'),
                    syncWithSymphonyClient: $('#syncWithSymphonyClient').prop('checked')
                },
                url = Label.servePath + "/article", type = "POST";

            if (3 === parseInt(requestJSONObject.articleType)) { // 如果是“思绪”
                requestJSONObject.articleContent = JSON.parse(window.localStorage.postData).thoughtContent;
            }

            if (Label.articleOId) {
                url = url + "/" + Label.articleOId;
                type = "PUT";
            }

            $.ajax({
                url: url,
                type: type,
                headers: {"csrfToken": csrfToken},
                cache: false,
                data: JSON.stringify(requestJSONObject),
                beforeSend: function () {
                    $(it).attr("disabled", "disabled").css("opacity", "0.3");
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    $("#addArticleTip").addClass('error').html('<ul><li>' + errorThrown + '</li></ul>');
                },
                success: function (result, textStatus) {
                    $(it).removeAttr("disabled").css("opacity", "1");
                    if (0 === result.sc) {
                        window.location.href = Label.servePath + "/article/" + result.articleId;
                        localStorage.removeItem('postData');
                    } else {
                        $("#addArticleTip").addClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    }
                },
                complete: function () {
                    $(it).removeAttr("disabled").css("opacity", "1");
                }
            });
        }
    },
    /**
     * @description 初始化发文
     */
    init: function () {
        $.ua.set(navigator.userAgent);

        // local data
        if (location.search.indexOf('?id=') > -1) {
            localStorage.removeItem('postData');
        }

        var postData = undefined;
        if (!localStorage.postData) {
            postData = {
                title: '',
                content: '',
                tags: '',
                thoughtContent: '',
                rewardContent: '',
                rewardPoint: ''
            };
            localStorage.postData = JSON.stringify(postData);
        } else {
            postData = JSON.parse(localStorage.postData);
        }

        // init content editor
        if ("" !== postData.content) {
            $('#articleContent').val(postData.content);
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            AddArticle.editor = Util.initTextarea('articleContent',
                function (editor) {
                    var postData = JSON.parse(localStorage.postData);
                    postData.content = editor.getValue()
                    localStorage.postData = JSON.stringify(postData);
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
                htmlURL: Label.servePath + "/markdown",
                readOnly: Label.requisite,
                extraKeys: {
                    "Alt-/": "autocompleteUserName",
                    "Ctrl-/": "autocompleteEmoji",
                    "Cmd-/": "autocompleteEmoji",
                    "Alt-S": "startAudioRecord",
                    "Alt-R": "endAudioRecord"
                },
                toolbar: [
                    {name: 'emoji'},
                    {name: 'bold'},
                    {name: 'italic'},
                    {name: 'quote'},
                    {name: 'link'},
                    {
                        name: 'image',
                        html: '<div class="tooltipped tooltipped-n" aria-label="' + Label.uploadFileLabel + '" ><form id="fileUpload" method="POST" enctype="multipart/form-data"><label class="icon-upload"><svg><use xlink:href="#upload"></use></svg><input type="file"/></label></form></div>'
                    },
                    {name: 'unordered-list'},
                    {name: 'ordered-list'},
                    {name: 'view'},
                    {name: 'fullscreen'},
                    {name: 'question', action: 'https://hacpai.com/guide/markdown'}
                ],
                status: false
            });
            addArticleEditor.render();

            AddArticle.editor = addArticleEditor.codemirror;
        }

        // 默认使用 preview
        $('.post-article-content .editor-toolbar .icon-view:eq(0)').parent().click();

        // 私信 at 默认值
        var atIdx = location.href.indexOf("at=");
        if (-1 !== atIdx) {
            if ("" == postData.content) {
                var at = AddArticle.editor.getValue();
                AddArticle.editor.setValue("\n\n\n" + at);
                AddArticle.editor.setCursor(CodeMirror.Pos(0, 0));
                AddArticle.editor.focus();
            }

            if ("" == postData.title) {
                var username = Util.getParameterByName("at");
                $("#articleTitle").val("Hi, " + username);
            }

            if ("" !== postData.tags) {
                var tagTitles = Label.discussionLabel;
                var tags = Util.getParameterByName("tags");
                if ("" !== tags) {
                    tagTitles += "," + tags;
                }
                $("#articleTags").val(tagTitles);
            }
        }

        // set url title
        if ("" == postData.title) {
            var title = Util.getParameterByName("title");
            if (title && title.length > 0) {
                $("#articleTitle").val(title);
            }
        }

        // set localStorage
        if ("" !== postData.title) {
            $("#articleTitle").val(postData.title);
        }
        $("#articleTitle").keyup(function () {
            var postData = JSON.parse(localStorage.postData);
            postData.title = $(this).val();
            localStorage.postData = JSON.stringify(postData);
        });

        if ("" !== postData.tags) {
            $("#articleTags").val(postData.tags);
        }

        this._initTag();

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
                var postData = JSON.parse(localStorage.postData);
                postData.content = cm.getValue();

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

                postData.thoughtContent += change;
                localStorage.postData = JSON.stringify(postData);

                if ($('.post-article-content .editor-preview-active').length === 0) {
                    return false;
                }

                $.ajax({
                    url: Label.servePath + "/markdown",
                    type: "POST",
                    cache: false,
                    data: {
                        markdownText: cm.getValue()
                    },
                    success: function (result, textStatus) {
                        $('.post-article-content .editor-preview-active').html(result.html);
                        hljs.initHighlighting.called = false;
                        hljs.initHighlighting();Util.parseMarkdown();
                    }
                });
            });
        }

        // focus
        if ($("#articleTitle").val().length <= 0) {
            $("#articleTitle").focus();
        }

        // check title is repeat
        $("#articleTitle").blur(function () {
            if ($.trim($(this).val()) === '') {
                return false;
            }

            if (1 === Label.articleType) { // 小黑屋不检查
                return;
            }

            $.ajax({
                url: Label.servePath + "/article/check-title",
                type: "POST",
                data: JSON.stringify({
                    'articleTitle': $.trim($(this).val()),
                    'articleId': Label.articleOId // 更新时才有值
                }),
                success: function (result, textStatus) {
                    if (!result.sc) {
                        if ($('#articleTitleTip').length === 1) {
                            $('#articleTitleTip').html(result.msg);
                        } else {
                            $('#articleTitle').after('<div class="module" id="articleTitleTip">' + result.msg + '</div>');
                        }

                    } else {
                        $('#articleTitleTip').remove();
                    }
                }
            });
        });

        // 快捷发文
        $("#articleTags, #articleRewardPoint").keypress(function (event) {
            if (event.ctrlKey && 10 === event.charCode) {
                AddArticle.add();
                return false;
            }
        });

        // 初始化打赏区编辑器
        if (0 < $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")) {
            $('#showReward').click();
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            AddArticle.rewardEditor = Util.initTextarea('articleRewardContent',
                function (editor) {
                    var postData = JSON.parse(localStorage.postData);
                    postData.rewardContent = editor.getValue()
                    localStorage.postData = JSON.stringify(postData);
                }
            );

            $('#articleRewardContent').before('<form id="rewardFileUpload" method="POST" enctype="multipart/form-data"><label class="btn">'
                + Label.uploadLabel + '<input type="file"/></label></form>')
                .css('margin-top', 0);
        } else {
            var addArticleRewardEditor = new Editor({
                element: document.getElementById('articleRewardContent'),
                dragDrop: false,
                lineWrapping: true,
                htmlURL: Label.servePath + "/markdown",
                toolbar: [
                    {name: 'emoji'},
                    {name: 'bold'},
                    {name: 'italic'},
                    {name: 'quote'},
                    {name: 'link'},
                    {
                        name: 'image',
                        html: '<div class="tooltipped tooltipped-n" aria-label="' + Label.uploadFileLabel + '" ><form id="rewardFileUpload" method="POST" enctype="multipart/form-data"><label class="icon-upload"><svg><use xlink:href="#upload"></use></svg><input type="file"/></label></form></div>'
                    },
                    {name: 'unordered-list'},
                    {name: 'ordered-list'},
                    {name: 'view'},
                    {name: 'fullscreen'},
                    {name: 'question', action: 'https://hacpai.com/guide/markdown'}
                ],
                extraKeys: {
                    "Alt-/": "autocompleteUserName",
                    "Ctrl-/": "autocompleteEmoji",
                    "Cmd-/": "autocompleteEmoji",
                    "Alt-S": "startAudioRecord",
                    "Alt-R": "endAudioRecord"
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

                var postData = JSON.parse(localStorage.postData);
                postData.rewardContent = cm.getValue();
                localStorage.postData = JSON.stringify(postData);

                if ($('.article-reward-content .editor-preview-active').length === 0) {
                    return false;
                }

                $.ajax({
                    url: Label.servePath + "/markdown",
                    type: "POST",
                    cache: false,
                    data: {
                        markdownText: cm.getValue()
                    },
                    success: function (result, textStatus) {
                        $('.article-reward-content .editor-preview-active').html(result.html);
                        hljs.initHighlighting.called = false;
                        hljs.initHighlighting();Util.parseMarkdown();
                    }
                });
            });
        }

        $("#articleContent").next().next().height(330);

        if ("" !== postData.rewardContent) {
            $('#showReward').click();
            AddArticle.rewardEditor.setValue(postData.rewardContent);
        }

        if ("" !== postData.rewardPoint) {
            $('#showReward').click();
            $('#articleRewardPoint').val(postData.rewardPoint);
        }
        $("#articleRewardPoint").keyup(function () {
            var postData = JSON.parse(localStorage.postData);
            postData.rewardPoint = $(this).val();
            localStorage.postData = JSON.stringify(postData);
        });

    },
    /**
     * @description 初始化标签编辑器
     * @returns {undefined}
     */
    _initTag: function () {
        $.ua.set(navigator.userAgent);

        // 添加 tag 到输入框
        var addTag = function (text) {
            if (text.replace(/\s/g, '') === '') {
                return false;
            }
            var hasTag = false;
            text = text.replace(/\s/g, '').replace(/,/g, '');
            $("#articleTags").val('');

            // 重复添加处理
            $('.tags-input .text').each(function () {
                var $it = $(this);
                if (text === $it.text()) {
                    $it.parent().addClass('haved');
                    setTimeout(function () {
                        $it.parent().removeClass('haved');
                    }, 900);
                    hasTag = true;
                }
            });

            if (hasTag) {
                return false;
            }

            // 长度处理
            if ($('.tags-input .tag').length >= 4) {
                $('#articleTags').val('').data('val', '');
                return false;
            }

            $('.post .tags-selected').append('<span class="tag"><span class="text">'
                + text + '</span><span class="close">x</span></span>');
            $('#articleTags').width($('.tags-input').width() - $('.post .tags-selected').width() - 10);

            // set tags to localStorage
            if (location.search.indexOf('?id=') === -1) {
                var articleTags = '';
                $('.tags-input .tag .text').each(function () {
                    articleTags += $(this).text() + ',';
                });

                var postData = JSON.parse(localStorage.postData);
                postData.tags = articleTags;
                localStorage.postData = JSON.stringify(postData);
            }

            if ($('.tags-input .tag').length >= 4) {
                $('#articleTags').val('').data('val', '');
            }
        };

        // domains 切换
        $('.domains-tags .btn').click(function () {
            $('.domains-tags .btn.current').removeClass('current green');
            $(this).addClass('current').addClass('green');
            $('.domains-tags .domain-tags').hide();
            $('#tags' + $(this).data('id')).show();
        });

        // tag 初始化渲染
        var initTags = $('#articleTags').val().split(',');
        for (var j = 0, jMax = initTags.length; j < jMax; j++) {
            addTag(initTags[j]);
        }

        // 领域 tag 选择
        $('.domain-tags .tag').click(function () {
            addTag($(this).text());
        });

        // 移除 tag
        $('.tags-input').on('click', '.tag > span.close', function () {
            $(this).parent().remove();
            $('#articleTags').width($('.tags-input').width() - $('.post .tags-selected').width() - 10);

            // set tags to localStorage
            if (location.search.indexOf('?id=') === -1) {
                var articleTags = '';
                $('.tags-input .tag .text').each(function () {
                    articleTags += $(this).text() + ',';
                });

                var postData = JSON.parse(localStorage.postData);
                postData.tags = articleTags;
                localStorage.postData = JSON.stringify(postData);
            }
        });

        // 展现领域 tag 选择面板
        $('#articleTags').click(function () {
            $('.post .domains-tags').show();
            if ($.ua.device.type !== 'mobile') {
                $('.post .domains-tags').css('left', ($('.post .tags-selected').width() + 10) + 'px');
            }
            $('#articleTagsSelectedPanel').hide();
        }).blur(function () {
            if ($('#articleTagsSelectedPanel').css('display') === 'block') {
                // 鼠标点击 completed 面板时避免把输入框的值加入到 tag 中
                return false;
            }
            addTag($(this).val());
        });

        // 关闭领域 tag 选择面板
        $('body').click(function (event) {
            if ($(event.target).closest('.tags-input').length === 1 || $(event.target).closest('.domains-tags').length === 1) {
            } else {
                $('.post .domains-tags').hide();
            }
        });

        // 自动补全 tag
        $("#articleTags").completed({
            height: 170,
            onlySelect: true,
            data: [],
            afterSelected: function ($it) {
                addTag($it.text());
            },
            afterKeyup: function (event) {
                $('.post .domains-tags').hide();
                // 遇到分词符号自动添加标签
                if (event.key === ',' || event.key === '，' ||
                    event.key === '、' || event.key === '；' || event.key === ';') {
                    var text = $("#articleTags").val();
                    addTag(text.substr(0, text.length - 1));
                    return false;
                }

                // 回车，自动添加标签
                if (event.keyCode === 13) {
                    addTag($("#articleTags").val());
                    return false;
                }

                // 上下左右
                if (event.keyCode === 37 || event.keyCode === 39 ||
                    event.keyCode === 38 || event.keyCode === 40) {
                    return false;
                }

                // ECS 隐藏面板
                if (event.keyCode === 27) {
                    $('#articleTagsSelectedPanel').hide();
                    return false;
                }

                // 删除 tag
                if (event.keyCode === 8 && event.data.settings.chinese === 8
                    && event.data.settings.keydownVal.replace(/\s/g, '') === '') {
                    $('.tags-input .tag .close:last').click();
                    return false;
                }

                if ($("#articleTags").val().replace(/\s/g, '') === '') {
                    return false;
                }

                $.ajax({
                    url: Label.servePath + '/tags/query?title=' + $("#articleTags").val(),
                    error: function (jqXHR, textStatus, errorThrown) {
                        $("#addArticleTip").addClass('error').html('<ul><li>' + errorThrown + '</li></ul>');
                    },
                    success: function (result, textStatus) {
                        if (result.sc) {
                            if ($.ua.device.type !== 'mobile') {
                                $('#articleTagsSelectedPanel').css('left', ($('.post .tags-selected').width() + 10) + 'px');
                            }
                            $("#articleTags").completed('updateData', result.tags);
                        } else {
                            console.log(result);
                        }
                    }
                });
            }
        });
    }
};

AddArticle.init();