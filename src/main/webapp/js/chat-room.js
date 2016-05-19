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
 * @fileoverview 聊天室
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.0.0.0, Apr 12, 2016
 */

/**
 * @description Add comment function.
 * @static
 */
var ChatRoom = {
    init: function () {
        // 聊天窗口高度设置
        if ($.ua.device.type !== 'mobile') {
            $('.list').height($('.side').height() - $('.reply').height() + 173);
        } else {
            $('.list').height($(window).height() - 173);
        }

        // 没用登陆就不需要编辑起初始化了 
        if ($('#chatContent').length === 0) {
            return false;
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            // editor 不支持时，使用 textarea
            $('#chatContent').before('<form id="fileUpload" method="POST" enctype="multipart/form-data"><label class="btn">'
                    + Label.uploadLabel + '<input type="file"/></label></form>')
                    .css('margin', 0);
            ChatRoom.editor = Util.initTextarea('chatContent',
                    function (editor) {
                        if (window.localStorage) {
                            window.localStorage.chatRoom = editor.$it.val();
                        }
                    }
            );
        } else {
            Util.initCodeMirror();

            var commentEditor = new Editor({
                element: document.getElementById('chatContent'),
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
                    "Alt-E": "endAudioRecord"
                },
                status: false
            });
            commentEditor.render();
            ChatRoom.editor = commentEditor.codemirror;
        }

        // 页面刷新需要保存输入框内容
        if (window.localStorage && window.localStorage.chatRoom) {
            if ("" !== window.localStorage.chatRoom.replace(/(^\s*)|(\s*$)/g, "")) {
                ChatRoom.editor.setValue(window.localStorage.chatRoom);
            }
        }

        if ($.ua.device.type === 'mobile' && ($.ua.device.vendor === 'Apple' || $.ua.device.vendor === 'Nokia')) {
            return false;
        }

        // at 及本地保存输入框内容
        ChatRoom.editor.on('changes', function (cm) {
            $("#chatContentTip").removeClass("error succ").html('');

            if (window.localStorage) {
                window.localStorage.chatRoom = cm.getValue();
            }

            var cursor = cm.getCursor();
            var token = cm.getTokenAt(cursor);
            if (token.string.indexOf('@') === 0) {
                cm.showHint({hint: CodeMirror.hint.userName, completeSingle: false});
                return CodeMirror.Pass;
            }
        });

        // ctrl ＋ enter 快速提交
        ChatRoom.editor.on('keypress', function (cm, evt) {
            if (evt.ctrlKey && 10 === evt.charCode) {
                ChatRoom.send();
                return;
            }
        });

        ChatRoom.editor.on('keydown', function (cm, evt) {
            if ($.ua.os.name.indexOf('Mac OS') > -1 && evt.metaKey && evt.keyCode === 13) {
                ChatRoom.send();
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
     * 发送聊天内容
     * @returns {undefined}
     */
    send: function () {
        var content = ChatRoom.editor.getValue();
        var requestJSONObject = {
            content: content
        };

        $.ajax({
            url: "/chat-room/send",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $(".form button.red").attr("disabled", "disabled").css("opacity", "0.3");
                ChatRoom.editor.setOption("readOnly", "nocursor");
            },
            success: function (result, textStatus) {
                if (result.sc) {
                    $("#chatContentTip").removeClass("error succ").html('');

                    ChatRoom.editor.setValue('');
                    // reset comment editor
                    $('.editor-preview').html('');
                    if ($('.icon-preview').hasClass('active')) {
                        $('.icon-preview').click();
                    }

                    if (window.localStorage) {
                        window.localStorage.chatRoom = '';
                    }
                } else {
                    $("#chatContentTip").addClass("error").html('<ul><li>' + result.msg + '</li></ul>');
                }
            },
            error: function (result) {
                $("#chatContentTip").addClass("error").html('<ul><li>' + result.statusText + '</li></ul>');
            },
            complete: function (jqXHR, textStatus) {
                $(".form button.red").removeAttr("disabled").css("opacity", "1");
                ChatRoom.editor.setOption("readOnly", false);
            }
        });
    }
};
ChatRoom.init();
