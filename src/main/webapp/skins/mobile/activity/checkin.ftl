<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${activityDailyCheckinLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <div class="content">
                    <div id="captcha"></div>
                </div>
                <div class="fn-hr10"></div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="https://static.geetest.com/static/tools/gt.js"></script>
        <script>
            var handler = function (captchaObj) {
                captchaObj.appendTo("#captcha");
                captchaObj.onSuccess(function () {
                    var result = captchaObj.getValidate();
                    window.location.href = "/activity/daily-checkin?geetest_challenge=" + result.geetest_challenge +
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