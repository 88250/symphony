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
 * @fileoverview Activity.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.1.2.0, Aug 29, 2015
 */

/**
 * @description Activity
 * @static
 */
var Activity = {
    /**
     * @description 上证指数博彩活动下注
     * @argument {String} csrfToken CSRF token
     */
    bet1A0001: function (csrfToken) {
        var requestJSONObject = {
            smallOrLarge: $("input[name=smallOrLarge]:checked").val(),
            amount: $("input[name=amount]:checked").val()
        };

        $.ajax({
            url: "/activity/1A0001/bet",
            type: "POST",
            headers: {"csrfToken": csrfToken},            
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                $("#betDiv").remove();
                if (result.sc) {
                    $("#betBtn").remove();
                    $("#tip").addClass("succ").removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                } else {
                    $("#tip").addClass("error").removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                }

                $("#tip").show();

                setTimeout(function () {
                    $("#tip").hide();
                }, 3000);
            }
        });
    },
    /**
     * @description 上证指数博彩活动兑奖
     */
    collect1A0001: function () {
        var requestJSONObject = {
        };

        $.ajax({
            url: "/activity/1A0001/collect",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                $("#tip").show();
                if (result.sc) {
                    $("#tip").addClass("succ").removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    $("#collectBtn").remove();
                } else {
                    $("#tip").addClass("error").removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                    setTimeout(function () {
                        $("#tip").hide();
                    }, 3000);
                }
            }
        });
    },
    init: function () {

    }
};

Activity.init();