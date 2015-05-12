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
 * @fileoverview util and every page should be used.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.2.6, May 12, 2015
 */

/**
 * @description Util
 * @static
 */
var Util = {
    /**
     * @description 设置当前登录用户的未读提醒计数.
     */
    setUnreadNotificationCount: function () {
        $.ajax({
            url: "/notification/unread/count",
            type: "GET",
            cache: false,
            success: function (result, textStatus) {
                var count = result.unreadNotificationCount;
                if (0 !== count) {
                    $("#aNotifications").removeClass("no-msg").addClass("msg").text(count);
                    document.title = "(" + count + ") " + Label.symphonyLabel + " - " + Label.visionLabel;
                } else {
                    $("#aNotifications").removeClass("msg").addClass("no-msg").text(count);
                    document.title = Label.symphonyLabel + " - " + Label.visionLabel;
                }
            }
        });
    },
    /**
     * @description 关注
     * @param {BOM} it 触发事件的元素
     * @param {String} id 关注 id
     * @param {String} type 取消关注的类型
     */
    follow: function (it, id, type) {
        if ($(it).hasClass("disabled")) {
            return false;
        }

        var requestJSONObject = {
            followingId: id
        };

        $(it).addClass("disabled");

        $.ajax({
            url: "/follow/" + type,
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                if (result.sc) {
                    $(it).removeClass("disabled").removeClass("green").addClass("red")
                            .attr("onclick", "Util.unfollow(this, '" + id + "', '" + type + "')")
                            .text(Label.unfollowLabel);
                }
            }
        });
    },
    /**
     * @description 取消关注     
     * @param {String} type 取消关注的类型
     */
    unfollow: function (it, id, type) {
        if ($(it).hasClass("disabled")) {
            return false;
        }

        var requestJSONObject = {
            followingId: id
        };

        $(it).addClass("disabled");

        $.ajax({
            url: "/follow/" + type,
            type: "DELETE",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                if (result.sc) {
                    $(it).removeClass("disabled").removeClass("red").addClass("green")
                            .attr("onclick", "Util.follow(this, '" + id + "', '" + type + "')")
                            .text(Label.followLabel);
                }
            }
        });
    },
    /**
     * @description 回到顶部
     */
    goTop: function () {
        var acceleration = acceleration || 0.1;
        var y = $(window).scrollTop();
        var speed = 1 + acceleration;
        window.scrollTo(0, Math.floor(y / speed));
        if (y > 0) {
            var invokeFunction = "Util.goTop(" + acceleration + ")";
            window.setTimeout(invokeFunction, 16);
        }
    },
    /**
     * @description 页面初始化执行的函数 
     */
    showLogin: function () {
        $(".nav .form").slideToggle();
        $("#nameOrEmail").focus();
    },
    /**
     * @description 跳转到注册页面
     */
    goRegister: function () {
        window.location = "/register?goto=" + encodeURIComponent(location.href);
    },
    /**
     * @description 禁止 IE7 以下浏览器访问
     */
    _kill: function () {
        if ($.browser.msie && parseInt($.browser.version) < 8) {
            $.ajax({
                url: "/kill-browser",
                type: "GET",
                cache: false,
                success: function (result, textStatus) {
                    $("body").append(result);
                    $("#killBrowser").dialog({
                        "modal": true,
                        "hideFooter": true,
                        "height": 258,
                        "width": 530
                    });

                    $("#killBrowser").dialog("open");
                }
            });
        }
    },
    /**
     * @description 初识化前台页面
     */
    init: function () {
        //禁止 IE7 以下浏览器访问
        this._kill();

        // 导航
        this._initNav();

        // 登录密码输入框回车事件
        $("#loginPassword").keyup(function (event) {
            if (event.keyCode === 13) {
                Util.login();
            }
        });

        // search input
        $(".nav .icon-search").click(function () {
            $(".nav input.search").focus();
        });
        $(".nav input.search").focus(function () {
            $(".nav .tags").hide();
        }).blur(function () {
            $(".nav .tags").show("slow");
        });
        $(window).scroll(function () {
            if ($(window).scrollTop() >= $(document).height() - $(window).height()) {
                $(".icon-up").css({
                    "background-color": "#F8F8F8",
                    "border-radius": "5px 0 0 0",
                    "border-color": "#E0E0E0"
                }).show();
            } else if ($(window).scrollTop() > 20) {
                $(".icon-up").css({
                    "background-color": "#E7ECEE",
                    "border-radius": "5px 0 0 5px",
                    "border-color": "#D2D9DD"
                }).show();
            } else {
                $(".icon-up").hide();
            }
        });

        // 定时获取并设置未读提醒计数
        setInterval(function () {
            Util.setUnreadNotificationCount();
        }, 30000);
    },
    /**
     * @description 设置导航状态
     */
    _initNav: function () {
        var pathname = location.pathname;
        $(".nav .user-nav > a").each(function () {
            if (pathname.indexOf("/notifications/") > -1) {
                // 提醒下面有四个页面
                $("#aNotifications").addClass("current");
            } else if (pathname.indexOf($(this).attr("href")) > -1) {
                // 用户下面有两个页面：用户的评论及文章列表
                $(this).addClass("current");
            } else if (pathname === "/register") {
                // 注册没有使用 href，对其进行特殊处理
                $("#aRegister").addClass("current");
            }
        });
    },
    /**
     * @description 登录
     */
    login: function () {
        if (Validate.goValidate([{
                "id": "nameOrEmail",
                "type": 256,
                "msg": Label.loginNameErrorLabel
            }, {
                "id": "loginPassword",
                "type": "password",
                "msg": Label.invalidPasswordLabel
            }])) {
            var requestJSONObject = {
                nameOrEmail: $("#nameOrEmail").val().replace(/(^\s*)|(\s*$)/g, ""),
                userPassword: calcMD5($("#loginPassword").val())
            };
            $.ajax({
                url: "/login",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        window.location.reload();
                    } else {
                        $("#loginTip").text(result.msg).addClass("tip-error");
                    }
                },
                complete: function (jqXHR, textStatus) {
                }
            });
        }
    }
};
/**
 * @description 数据验证
 * @static
 */
var Validate = {
    /**
     * @description 提交时对数据进行统一验证。
     * @param {array} data 验证数据
     * @returns 验证通过返回 true，否则为 false。 
     */
    goValidate: function (data) {
        for (var j = 0; j < data.length; j++) {
            var $it = $("#" + data[j].id);
            for (var i = 0; i < data.length; i++) {
                if (data[i].id === $it.attr("id")) {
                    data[i].val = $it.val();
                    if (Validate.validate(data[i].type, data[i].val)) {
                        $it.next().removeClass("tip-error").text("");
                    } else {
                        $it.next().addClass("tip-error").text(data[i].msg);
                    }
                    break;
                }
            }
        }

        for (var j = 0; j < data.length; j++) {
            if ($("#" + data[j].id).next().text() !== "") {
                return false;
            }
        }
        return true;
    },
    /**
     * @description 数据验证。
     * @param {string} type 验证类型
     * @param {string} val 待验证数据 
     * @returns 验证通过返回 true，否则为 false。 
     */
    validate: function (type, val) {
        var isValidate = true;
        if (typeof (type) === "string" && type.indexOf("|") > -1) {
            var passwordId = type.split("|")[1];
            type = type.split("|")[0];
        }
        switch (type) {
            case "email":
                if (!/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test(val)) {
                    isValidate = false;
                }
                break;
            case "password":
                if (val.length === 0 || val.length > 16) {
                    isValidate = false;
                }
                break;
            case "confirmPassword":
                if (val !== $("#" + passwordId).val()) {
                    isValidate = false;
                }
                break;
            case "securityCode":
                if (val === "" || val.length > 4) {
                    isValidate = false;
                }
                break;
            case "tags":
                var tagList = val.split(",");
                if (val.replace(/(^\s*)|(\s*$)/g, "") === "" || tagList.length > 7) {
                    isValidate = false;
                }

                for (var i = 0; i < tagList.length; i++) {
                    if (tagList[i].replace(/(^\s*)|(\s*$)/g, "") === ""
                            || tagList[i].replace(/(^\s*)|(\s*$)/g, "").length > 50) {
                        isValidate = false;
                        break;
                    }
                }
                break;
            case "url":
                val = val.replace(/(^\s*)|(\s*$)/g, "");
                if (val !== "") {
                    if (!/^\w+:\/\//.test(val) || val.length > 100) {
                        isValidate = false;
                    }
                }
                break;
            default:
                val = val.replace(/(^\s*)|(\s*$)/g, "");
                if (typeof (type) === "string") {
                    if (val.length > type) {
                        isValidate = false;
                    }
                } else {
                    if (val.length === 0 || val.length > type) {
                        isValidate = false;
                    }
                }
                break;
        }
        return isValidate;
    }
};
/**
 * @description 全局变量
 */
var Label = {};
if (!Cookie) {
    /**
     * @description Cookie 相关操作
     * @static
     */
    var Cookie = {
        /**
         * @description 读取 cookie
         * @param {String} name cookie key
         * @returns {String} 对应 key 的值，如 key 不存在则返回 ""
         */
        readCookie: function (name) {
            var nameEQ = name + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) === ' ')
                    c = c.substring(1, c.length);
                if (c.indexOf(nameEQ) === 0)
                    return c.substring(nameEQ.length, c.length);
            }
            return "";
        },
        /**
         * @description 清除 Cookie
         * @param {String} name 清除 key 为 name 的该条 Cookie
         */
        eraseCookie: function (name) {
            this.createCookie(name, "", -1);
        },
        /**
         * @description 创建 Cookie
         * @param {String} name 每条 Cookie 唯一的 key
         * @param {String} value 每条 Cookie 对应的值
         * @param {Int} days Cookie 保存时间
         */
        createCookie: function (name, value, days) {
            var expires = "";
            if (days) {
                var date = new Date();
                date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
                expires = "; expires=" + date.toGMTString();
            }
            document.cookie = name + "=" + value + expires + "; path=/";
        }
    };
}