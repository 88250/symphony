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
 * @fileoverview settings.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.13.5.11, Aug 8, 2016
 */

/**
 * @description Settings function
 * @static
 */
var Settings = {
    /**
     * 初始化个人设置中的头像图片上传.
     * 
     * @returns {Boolean}
     */
    initUploadAvatar: function (params, succCB, succCBQN) {
        if ("" === params.qiniuUploadToken) { // 说明没有使用七牛，而是使用本地
            $('#' + params.id).fileupload({
                acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                maxFileSize: parseInt(params.maxSize),
                multipart: true,
                pasteZone: null,
                dropZone: null,
                url: "/upload",
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
                formData: function (form) {
                    var data = form.serializeArray();
                    data.push({name: 'token', value: params.qiniuUploadToken});
                    data.push({name: 'key', value: 'avatar/' + params.userId});
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
            case "misc":
                requestJSONObject = this._validateMisc();
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
                    $("#" + type.replace(/\//g, "") + "Tip").addClass("succ").removeClass("error").html('<ul><li>' + Label.updateSuccLabel + '</li></ul>');
                    if (type === 'profiles') {
                        $('#userNicknameDom').text(requestJSONObject.userNickname);
                        $('#userTagsDom').text(requestJSONObject.userTags);
                        $('#userURLDom').text(requestJSONObject.userURL).attr('href', requestJSONObject.userURL);
                        $('#userIntroDom').text(requestJSONObject.userIntro);
                        $('#avatarURLDom, .user-nav .avatar-small').attr('style', 'background-image:url(' + requestJSONObject.userAvatarURL + ')');
                    }
                } else {
                    $("#" + type.replace(/\//g, "") + "Tip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
                }
                $("#" + type.replace(/\//g, "") + "Tip").show();

                setTimeout(function () {
                    $("#" + type.replace(/\//g, "") + "Tip").hide();
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
                userIntro: $("#userIntro").val().replace(/(^\s*)|(\s*$)/g, ""),
                userCommentViewMode: $("#userCommentViewMode").val()
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
     * @description settings 页面其他（杂项）数据校验
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validateMisc: function () {
        return {
            userUAStatus: $("#userUAStatus").prop("checked"),
            userNotifyStatus: $("#userNotifyStatus").prop("checked"),
            userArticleStatus: $("#userArticleStatus").prop("checked"),
            userCommentStatus: $("#userCommentStatus").prop("checked"),
            userFollowingUserStatus: $("#userFollowingUserStatus").prop("checked"),
            userFollowingTagStatus: $("#userFollowingTagStatus").prop("checked"),
            userFollowingArticleStatus: $("#userFollowingArticleStatus").prop("checked"),
            userFollowerStatus: $("#userFollowerStatus").prop("checked"),
            userPointStatus: $("#userPointStatus").prop("checked"),
            userOnlineStatus: $("#userOnlineStatus").prop("checked"),
            userTimelineStatus: $("#userTimelineStatus").prop("checked"),
            userJoinPointRank: $("#joinPointRank").prop("checked"),
            userJoinUsedPointRank: $("#joinUsedPointRank").prop("checked"),
            userListPageSize: $("#userListPageSize").val()
        };
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
    }
};