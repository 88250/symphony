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
 * @version 2.15.10.8, Aug 1, 2016
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
     * @csrfToken [string] CSRF 令牌
     */
    add: function (csrfToken) {
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
                articleAnonymous: $('#articleAnonymous').prop('checked')
            },
            url = Label.servePath + "/article", type = "POST";

            if (3 === parseInt(requestJSONObject.articleType)) { // 如果是“思绪”
                requestJSONObject.articleContent = window.localStorage.thoughtContent;
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
                    "Alt-R": "endAudioRecord",
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

            var username = Util.getParameterByName("at");
            $("#articleTitle").val("Hi, " + username);

            var tagTitles = "小黑屋";
            var tags = Util.getParameterByName("tags");
            if ("" !== tags) {
                tagTitles += "," + tags;
            }
            $("#articleTags").val(tagTitles);
        }

        var title = Util.getParameterByName("title");
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

        $("#articleTitle, #articleRewardPoint").keypress(function (event) {
            if (13 === event.keyCode) {
                AddArticle.add();
            }
        });

        // 初始化打赏区编辑器
        if (0 < $("#articleRewardPoint").val().replace(/(^\s*)|(\s*$)/g, "")) {
            $('#showReward').click();
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            AddArticle.rewardEditor = Util.initTextarea('articleRewardContent');
            $('#articleRewardContent').before('<form id="rewardFileUpload" method="POST" enctype="multipart/form-data"><label class="btn">'
                    + Label.uploadLabel + '<input type="file"/></label></form>')
                    .css('margin-top', 0);
        } else {
            var addArticleRewardEditor = new Editor({
                element: document.getElementById('articleRewardContent'),
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
        this._initTag();
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

            text = text.replace(/\s/g, 's');

            $("#articleTags").val('').data('val', '');

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
                $('#articleTags').prop('disabled', true).val('').data('val', '');
                return false;
            }

            $('.post .tags-selected').append('<span class="tag"><span class="text">'
                    + text + '</span><span class="close">x</span></span>');
            if ($.ua.device.type !== 'mobile') {
                $('.post .domains-tags, #articleTagsSelectedPanel').css('left', $('.post .tags-selected').width() + 'px');
            }
            $('#articleTags').width($('.tags-input').width() - $('.post .tags-selected').width() - 10);

            if ($('.tags-input .tag').length >= 4) {
                $('#articleTags').prop('disabled', true).val('').data('val', '');
            }
        };

        // domains 切换
        $('.domains-tags .btn').click(function () {
            $('.domains-tags .btn.current').removeClass('current');
            $(this).addClass('current');
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
        $('.tags-input .tag > span.close').live('click', function () {
            $(this).parent().remove();
            if ($.ua.device.type !== 'mobile') {
                $('.post .domains-tags, #articleTagsSelectedPanel').css('left', $('.post .tags-selected').width() + 'px');
            }
            $('#articleTags').width($('.tags-input').width() - $('.post .tags-selected').width() - 10);
            $('#articleTags').prop('disabled', false);
        });

        // 展现领域 tag 选择面板
        $('#articleTags').click(function () {
            $('.post .domains-tags').show();
            $('#articleTagsSelectedPanel').hide();
        }).blur(function () {
            $(this).val('').data('val', '');
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

                // 上线左右
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
                if (event.keyCode === 8 && ($("#articleTags").data('val') && $("#articleTags").data('val').length === 0 || !$("#articleTags").data('val'))) {
                    $('.tags-input .tag .close:last').click();
                    return false;
                }


                if ($("#articleTags").data('val') === $("#articleTags").val()) {
                    return false;
                }
                $("#articleTags").data('val', $("#articleTags").val());


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
                            $("#articleTags").completed('updateData', result.tags);
                        } else {
                            console.log(result);
                        }
                    }
                });
            }
        });
    },
    /**
     * @description 显示简要语法
     */
    grammar: function () {
        $(".grammar").slideToggle();
    }
};

AddArticle.init();
