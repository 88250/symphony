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
 * @version 1.2.2.3, Jun 12, 2016
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
    /**
     * paint brush
     * @param {string} id canvas id.
     * @returns {undefined}
     */
    charInit: function (id) {
        var el = document.getElementById(id),
                ctx = el.getContext('2d');
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 5;
        ctx.lineJoin = ctx.lineCap = 'round';
        ctx.shadowBlur = 5;
        ctx.shadowColor = 'rgb(0, 0, 0)';

        var isDrawing = false, x = 0, y = 0;

        el.onmousedown = function (e) {
            isDrawing = true;
            ctx.beginPath();
            x = e.clientX - e.target.offsetLeft + $(window).scrollLeft();
            y = e.clientY - e.target.offsetTop + $(window).scrollTop();
            ctx.moveTo(x, y);
        };

        el.onmousemove = function (e) {
            if (!isDrawing) {
                return;
            }

            x = e.clientX - e.target.offsetLeft + $(window).scrollLeft();
            y = e.clientY - e.target.offsetTop + $(window).scrollTop();
            ctx.lineTo(x, y);
            ctx.stroke();
        };

        el.onmouseup = function () {
            isDrawing = false;
        };

        el.addEventListener("touchstart", function (e) {
            ctx.beginPath();
            x = e.changedTouches[0].pageX - e.target.offsetLeft;
            y = e.changedTouches[0].pageY - e.target.offsetTop;
            ctx.moveTo(x, y);

        }, false);

        el.addEventListener("touchmove", function (e) {
            e.preventDefault();
            x = e.changedTouches[0].pageX - e.target.offsetLeft;
            y = e.changedTouches[0].pageY - e.target.offsetTop;
            ctx.lineTo(x, y);
            ctx.stroke();
        }, false);
    },
    /**
     * 提交写好字的图片.
     * 
     * @param {string} id canvas id.
     */
    submitCharacter: function (id) {
        var canvas = document.getElementById(id);

        var requestJSONObject = {
            dataURL: canvas.toDataURL(),
            character: $("h2.fn-inline a").text()
        };

        $.ajax({
            url: "/activity/character/submit",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                var $btn = $("button.green");
                $btn.attr("disabled", "disabled").css("opacity", "0.3").text($btn.text() + 'ing');
            },
            success: function (result, textStatus) {
                alert(result.msg);

                if (result.sc) {
                    window.location.reload();
                }
            },
            complete: function () {
                var $btn = $("button.green");
                $btn.removeAttr("disabled").css("opacity", "1").text($btn.text().substr(0, $btn.text().length - 3));
            }
        });
    },
    /**
     * clear canvas
     * 
     * @param {string} id canvas id.
     */
    clearCharacter: function (id) {
        var canvas = document.getElementById(id),
                ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    }
};