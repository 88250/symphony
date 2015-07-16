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
 * @fileoverview Activity.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.0.0.0, Jul 16, 2015
 */

/**
 * @description Activity
 * @static
 */
var Activity = {
    /**
     * @description 上证指数博彩活动下注
     */
    bet1A0001: function () {
        var requestJSONObject = {
            smallOrLarge: $("input[name=smallOrLarge]:checked").val(),
            amount: $("input[name=amount]:checked").val()
        };

        $.ajax({
            url: "/activity/1A0001",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            success: function (result, textStatus) {
                if (result.sc) {
                    $("#betTip").addClass("tip-succ").text(result.msg);
                } else {
                    $("#betTip").addClass("tip-error").text(result.msg);
                }

                $("#betTip").show();

                setTimeout(function () {
                    $("#betTip").hide();
                }, 3000);
            }
        });
    },
    init: function () {

    }
};

Activity.init();