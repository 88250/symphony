/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @fileoverview settings.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Zephyr
 * @version 1.19.11.17, Jan 23, 2017
 */

/**
 * @description Settings function
 * @static
 */
var Settings = {
    /**
     * 有代码片段时，需要进行高亮
     * @returns {Boolean}
     */
    initHljs: function () {
        if ($('pre code').length === 0) {
            return false;
        }
        $.ajax({
            method: "GET",
            url: Label.servePath + "/js/lib/highlight.js-9.6.0/highlight.pack.js",
            dataType: "script"
        }).done(function () {
            $('pre code').each(function (i, block) {
                hljs.highlightBlock(block);
            });
        });
    },
    /**
     * 个人设置预览
     */
    preview: function (it) {
        if ($('#homeSidePanel').css('display') === 'block') {
            $('#homeSidePanel').hide();
            $(it).text(Label.previewLabel);
        } else {
            $('#homeSidePanel').show();
            $('#userNicknameDom').text($('#userNickname').val());
            $('#userTagsDom').text($('#userTags').val());
            $('#userURLDom').text($('#userURL').val()).attr('href', $('#userURL').val());
            $('#userIntroDom').text($('#userIntro').val());

            $(it).text(Label.unPreviewLabel);
        }
    },
    /**
     * 初始化个人设置中的头像图片上传.
     * 
     * @returns {Boolean}
     */
    initUploadAvatar: function (params, succCB, succCBQN) {
        var ext = "";
        if ("" === params.qiniuUploadToken) { // 说明没有使用七牛，而是使用本地
            $('#' + params.id).fileupload({
                acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                maxFileSize: parseInt(params.maxSize),
                multipart: true,
                pasteZone: null,
                dropZone: null,
                url: Label.servePath + "/upload",
                add: function (e, data) {
                    ext = data.files[0].type.split("/")[1];

                    if (window.File && window.FileReader && window.FileList && window.Blob) {
                        var reader = new FileReader();
                        reader.readAsArrayBuffer(data.files[0]);
                        reader.onload = function (evt) {
                            var fileBuf = new Uint8Array(evt.target.result.slice(0, 11));
                            var isImg = isImage(fileBuf);

                            if (!isImg) {
                                alert("Image only~");

                                return;
                            }

                            if (evt.target.result.byteLength > 1024 * 1024) {
                                alert("This image is too large (max 1M)");

                                return;
                            }

                            data.submit();
                        };
                    } else {
                        data.submit();
                    }
                },
                formData: function (form) {
                    var data = form.serializeArray();
                    return data;
                },
                submit: function (e, data) {
                },
                done: function (e, data) {
                    var qiniuKey = data.result.key;
                    if (!qiniuKey) {
                        alert("Upload error");
                        return;
                    }

                    succCB(data);
                },
                fail: function (e, data) {
                    alert("Upload error: " + data.errorThrown);
                }
            }).on('fileuploadprocessalways', function (e, data) {
                var currentFile = data.files[data.index];
                if (data.files.error && currentFile.error) {
                    alert(currentFile.error);
                }
            });
        } else {
            $('#' + params.id).fileupload({
                acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                maxFileSize: parseInt(params.maxSize),
                multipart: true,
                pasteZone: null,
                dropZone: null,
                url: "https://up.qbox.me/",
                add: function (e, data) {
                    ext = data.files[0].type.split("/")[1];

                    if (window.File && window.FileReader && window.FileList && window.Blob) {
                        var reader = new FileReader();
                        reader.readAsArrayBuffer(data.files[0]);
                        reader.onload = function (evt) {
                            var fileBuf = new Uint8Array(evt.target.result.slice(0, 11));
                            var isImg = isImage(fileBuf);

                            if (!isImg) {
                                alert("Image only~");

                                return;
                            }

                            if (evt.target.result.byteLength > 1024 * 1024) {
                                alert("This image is too large (max 1M)");

                                return;
                            }

                            data.submit();
                        };
                    } else {
                        data.submit();
                    }
                },
                formData: function (form) {
                    var data = form.serializeArray();
                    data.push({name: 'token', value: params.qiniuUploadToken});
                    data.push({name: 'key', value: 'avatar/' + params.userId + "_" + new Date().getTime() + "." + ext});

                    console.log(data);

                    return data;
                },
                submit: function (e, data) {
                },
                done: function (e, data) {
                    var qiniuKey = data.result.key;
                    if (!qiniuKey) {
                        alert("Upload error");
                        return;
                    }

                    succCBQN(data);
                },
                fail: function (e, data) {
                    alert("Upload error: " + data.errorThrown);
                }
            }).on('fileuploadprocessalways', function (e, data) {
                var currentFile = data.files[data.index];
                if (data.files.error && currentFile.error) {
                    alert(currentFile.error);
                }
            });
        }
    },
    /**
     * 数据导出.
     */
    exportPosts: function () {
        $.ajax({
            url: Label.servePath + "/export/posts",
            type: "POST",
            cache: false,
            success: function (result, textStatus) {
                if (!result.sc) {
                    alert("TBD: V, tip display it....");

                    return;
                }

                window.open(result.url);
            }
        });
    },
    /**
     * @description 修改地理位置状态
     * @param {type} csrfToken CSRF token
     */
    changeGeoStatus: function (csrfToken) {
        var requestJSONObject = {
            "userGeoStatus": $('#geoStatus').val()
        };

        $.ajax({
            url: Label.servePath + "/settings/geo/status",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                console.log(result);
            }
        });
    },
    /**
     * @description 积分转账
     * @argument {String} csrfToken CSRF token
     */
    pointTransfer: function (csrfToken) {
        if (Validate.goValidate({target: $('#pointTransferTip'),
            data: [{
                    "target": $("#pointTransferUserName"),
                    "type": "string",
                    "max": 256,
                    "msg": Label.invalidUserNameLabel
                }, {
                    "target": $("#pointTransferAmount"),
                    "type": "string",
                    'max': 50,
                    "msg": Label.amountNotEmpty
                }]})) {
            var requestJSONObject = {
                "userName": $("#pointTransferUserName").val(),
                "amount": $("#pointTransferAmount").val()
            };

            $.ajax({
                url: Label.servePath + "/point/transfer",
                type: "POST",
                headers: {"csrfToken": csrfToken},
                cache: false,
                data: JSON.stringify(requestJSONObject),
                beforeSend: function () {
                    $("#pointTransferTip").removeClass("succ").removeClass("error").html("");
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(errorThrown);
                },
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#pointTransferTip").addClass("succ").removeClass("error").html('<ul><li>' + Label.transferSuccLabel + '</li></ul>');
                    } else {
                        $("#pointTransferTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
                    }

                    $("#pointTransferTip").show();

                    setTimeout(function () {
                        $("#pointTransferTip").hide();
                    }, 2000);
                }
            });
        }
    },
    /**
     * @description 积分兑换邀请码
     * @argument {String} csrfToken CSRF token
     */
    pointBuyInvitecode: function (csrfToken) {
        var requestJSONObject = {};

        $.ajax({
            url: Label.servePath + "/point/buy-invitecode",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $("#pointBuyInvitecodeTip").removeClass("succ").removeClass("error").html("");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            },
            success: function (result, textStatus) {
                if (result.sc) {
                    $(".list ul").prepend('<li class="content-reset"><code>' + result.msg.split(' ')[0] + '</code>' + result.msg.substr(16) + '</li>');
                } else {
                    $("#pointBuyInvitecodeTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
                }
                $("#pointBuyInvitecodeTip").show();
            }
        });
    },
    /**
     * @description 查询邀请码状态
     * @param {String} csrfToken CSRF token
     * @returns {undefined}
     */
    queryInvitecode: function (csrfToken) {
        var requestJSONObject = {
            invitecode: $('#invitecode').val()
        };

        $.ajax({
            url: Label.servePath + "/invitecode/state",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $("#invitecodeStateTip").removeClass("succ").removeClass("error").html("");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            },
            success: function (result, textStatus) {
                switch (result.sc) {
                    case -1:
                    case 0:
                    case 2:
                        $("#invitecodeStateTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');

                        break;
                    case 1:
                        $("#invitecodeStateTip").addClass("succ").removeClass("error").html('<ul><li>' + result.msg + '</li></ul>');

                        break;
                    default:
                        $("#invitecodeStateTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
                }
                S
                $("#invitecodeStateTip").show();
            }
        });
    },
    /**
     * @description 更新 settings 页面数据.
     * @argument {String} csrfToken CSRF token
     */
    update: function (type, csrfToken) {
        var requestJSONObject = {};

        switch (type) {
            case "profiles":
                requestJSONObject = this._validateProfiles();

                break;
            case "sync/b3":
                requestJSONObject = this._validateSyncB3();

                break;
            case "password":
                requestJSONObject = this._validatePassword();

                break;
            case "privacy":
                requestJSONObject = {
                    userArticleStatus: $("#userArticleStatus").prop("checked"),
                    userCommentStatus: $("#userCommentStatus").prop("checked"),
                    userFollowingUserStatus: $("#userFollowingUserStatus").prop("checked"),
                    userFollowingTagStatus: $("#userFollowingTagStatus").prop("checked"),
                    userFollowingArticleStatus: $("#userFollowingArticleStatus").prop("checked"),
                    userWatchingArticleStatus: $("#userWatchingArticleStatus").prop("checked"),
                    userFollowerStatus: $("#userFollowerStatus").prop("checked"),
                    userPointStatus: $("#userPointStatus").prop("checked"),
                    userOnlineStatus: $("#userOnlineStatus").prop("checked"),
                    userJoinPointRank: $("#joinPointRank").prop("checked"),
                    userJoinUsedPointRank: $("#joinUsedPointRank").prop("checked"),
                    userUAStatus: $("#userUAStatus").prop("checked"),
                    userTimelineStatus: $("#userTimelineStatus").prop("checked"),
                    userForgeLinkStatus: $("#userForgeLinkStatus").prop("checked")
                };

                break;
            case "function":
                requestJSONObject = {
                    userListPageSize: $("#userListPageSize").val(),
                    userCommentViewMode: $("#userCommentViewMode").val(),
                    userAvatarViewMode: $("#userAvatarViewMode").val(),
                    userNotifyStatus: $('#userNotifyStatus').prop("checked"),
                    userSubMailStatus: $('#userSubMailStatus').prop("checked"),
                    userKeyboardShortcutsStatus: $('#userKeyboardShortcutsStatus').prop("checked")
                };

                break;
            case "emotionList":
                requestJSONObject = this._validateEmotionList();

                break;
            case "i18n":
                requestJSONObject = {
                    userLanguage: $("#userLanguage").val(),
                    userTimezone: $("#userTimezone").val()
                };

                break;
            default:
                console.log("update settings has no type");
        }

        if (!requestJSONObject) {
            return false;
        }

        $.ajax({
            url: Label.servePath + "/settings/" + type,
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $("#" + type.replace(/\//g, "") + "Tip").removeClass("succ").removeClass("error").html("");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            },
            success: function (result, textStatus) {
                if (result.sc) {
                    $("#" + type.replace(/\//g, "") + "Tip").addClass("succ").removeClass("error")
                    .html('<ul><li>' + Label.updateSuccLabel + '</li></ul>').show();
                    if (type === 'profiles') {
                        $('#userNicknameDom').text(requestJSONObject.userNickname);
                        $('#userTagsDom').text(requestJSONObject.userTags);
                        $('#userURLDom').text(requestJSONObject.userURL).attr('href', requestJSONObject.userURL);
                        $('#userIntroDom').text(requestJSONObject.userIntro);

                        return;
                    }


                } else {
                    $("#" + type.replace(/\//g, "") + "Tip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
                }

                $("#" + type.replace(/\//g, "") + "Tip").show();

                setTimeout(function () {
                    $("#" + type.replace(/\//g, "") + "Tip").hide();

                    if (type === 'i18n') {
                        window.location.reload();
                    }
                }, 5000);

            }
        });
    },
    /**
     * @description 需要在上传完成后调用该函数来更新用户头像数据.
     * @argument {String} csrfToken CSRF token
     */
    updateAvatar: function (csrfToken) {
        var requestJSONObject = {
            userAvatarURL: $("#avatarURL").data("imageurl"),
        };

        $.ajax({
            url: Label.servePath + "/settings/avatar",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            },
            success: function (result, textStatus) {
                if (result.sc) {
                    $('#avatarURLDom, .user-nav .avatar-small').attr('style', 'background-image:url(' + requestJSONObject.userAvatarURL + ')');
                }
            }
        });
    },
    /**
     * @description settings 页面 profiles 数据校验
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validateProfiles: function () {
        if (Validate.goValidate({target: $('#profilesTip'),
            data: [{
                    "target": $("#userNickname"),
                    "type": "string",
                    "min": 0,
                    "max": 20,
                    "msg": Label.invalidUserNicknameLabel
                }, {
                    "target": $("#userTags"),
                    "type": "string",
                    "min": 0,
                    "max": 255,
                    "msg": Label.tagsErrorLabel
                }, {
                    "target": $("#userURL"),
                    "type": "string",
                    "min": 0,
                    "max": 255,
                    "msg": Label.invalidUserURLLabel
                }, {
                    "target": $("#userIntro"),
                    "type": "string",
                    "min": 0,
                    "max": 255,
                    "msg": Label.invalidUserIntroLabel
                }]})) {
            return {
                userNickname: $("#userNickname").val().replace(/(^\s*)|(\s*$)/g, ""),
                userTags: $("#userTags").val().replace(/(^\s*)|(\s*$)/g, ""),
                userURL: $("#userURL").val().replace(/(^\s*)|(\s*$)/g, ""),
                userIntro: $("#userIntro").val().replace(/(^\s*)|(\s*$)/g, "")
            };
        } else {
            return false;
        }
    },
    /**
     * @description settings 页面 solo 数据同步校验
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validateSyncB3: function () {
        if (Validate.goValidate({target: $("#syncb3Tip"),
            data: [{
                    "target": $("#soloKey"),
                    "type": "string",
                    'max': 20,
                    "msg": Label.invalidUserB3KeyLabel
                }, {
                    "target": $("#soloPostURL"),
                    "type": "url",
                    "msg": Label.invalidUserB3ClientURLLabel
                }, {
                    "target": $("#soloUpdateURL"),
                    "type": "url",
                    "msg": Label.invalidUserB3ClientURLLabel
                }, {
                    "target": $("#soloCmtURL"),
                    "type": "url",
                    "msg": Label.invalidUserB3ClientURLLabel
                }]})) {
            return {
                userB3Key: $("#soloKey").val().replace(/(^\s*)|(\s*$)/g, ""),
                userB3ClientAddArticleURL: $("#soloPostURL").val().replace(/(^\s*)|(\s*$)/g, ""),
                userB3ClientUpdateArticleURL: $("#soloUpdateURL").val().replace(/(^\s*)|(\s*$)/g, ""),
                userB3ClientAddCommentURL: $("#soloCmtURL").val().replace(/(^\s*)|(\s*$)/g, ""),
                syncWithSymphonyClient: $("#syncWithSymphonyClient").prop("checked")
            };
        } else {
            return false;
        }
    },
    /**
     * @description settings 页面密码校验
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validatePassword: function () {
        var pwdVal = $("#pwdOld").val(),
                newPwdVal = $("#pwdNew").val();
        if (Validate.goValidate({target: $('#passwordTip'),
            data: [{
                    "target": $("#pwdOld"),
                    "type": "password",
                    "msg": Label.invalidPasswordLabel
                }, {
                    "target": $("#pwdNew"),
                    "type": "password",
                    "msg": Label.invalidPasswordLabel
                }, {
                    "target": $("#pwdRepeat"),
                    "type": "password",
                    "oranginal": $('#pwdNew'),
                    "msg": Label.confirmPwdErrorLabel
                }]})) {
            if (newPwdVal !== $("#pwdRepeat").val()) {
                return false;
            }
            var data = {};
            data.userPassword = calcMD5(pwdVal);
            data.userNewPassword = calcMD5(newPwdVal);
            return data;
        }
        return false;
    },
    /**
     * @description settings 页面表情校验（不知道有啥可校验的，暂不做校验）
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validateEmotionList: function () {
        return {
            emotions: $("#emotionList").val()
        };
    },
    /**
     * @description 标记所有消息通知为已读状态.
     */
    makeAllNotificationsRead: function () {
        $.ajax({
            url: Label.servePath + "/notification/all-read",
            type: "GET",
            cache: false,
            success: function (result, textStatus) {
                if (result.sc) {
                    window.location.reload();
                }
            }
        });
    },
    /**
     * @description 设置常用表情点击事件绑定.
     */
    initFunction: function () {
        $("#emojiGrid img").click(function () {
            var emoji = $(this).attr('alt');
            if ($("#emotionList").val().indexOf(emoji) !== -1) {
                return;
            }
            if ($("#emotionList").val() !== "") {
                $("#emotionList").val($("#emotionList").val() + "," + emoji);
            } else {
                $("#emotionList").val(emoji);
            }
        });
    }
};
