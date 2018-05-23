<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${activityDailyCheckinLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <h2 class="sub-head">
                            <div class="avatar-small tooltipped tooltipped-ne"
                                 aria-label="${activityDailyCheckinLabel}" style="background-image:url('${staticServePath}/images/activities/checkin.png')"></div>
                            ${dailyCheckinLabel}
                        </h2>
                        <div id="captcha" class="fn-content"></div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="//static.geetest.com/static/tools/gt.js"></script>
        <script>
            var handler = function (captchaObj) {
                captchaObj.appendTo("#captcha");
                captchaObj.onSuccess(function () {
                    var result = captchaObj.getValidate();
                    window.location.href = "${servePath}/activity/daily-checkin?geetest_challenge=" + result.geetest_challenge +
                            "&geetest_validate=" + result.geetest_validate + "&geetest_seccode=" + result.geetest_seccode;
                });
            };

            $.ajax({
                // 获取id，challenge，success（是否启用failback）
                url: "/geetest-captcha",
                type: "GET",
                dataType: "json", // 使用jsonp格式
                success: function (data) {
                    // 使用initGeetest接口
                    // 参数1：配置参数，与创建Geetest实例时接受的参数一致
                    // 参数2：回调，回调的第一个参数验证码对象，之后可以使用它做appendTo之类的事件
                    initGeetest({
                        gt: data.gt,
                        challenge: data.challenge,
                        product: "embed", // 产品形式
                        offline: !data.success
                    }, handler);
                }
            });
        </script>
    </body>
</html>