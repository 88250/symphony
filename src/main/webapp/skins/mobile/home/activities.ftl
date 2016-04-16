<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${activityLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content content-reset">
                    <h2>${activityLabel}</h2>
                    <ul>
                        <li><a href="<#if useCaptchaCheckin>/activity/checkin<#else>/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a></li>
                        <li><a href="/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a></li>
                        <li><a href="/activity/1A0001">${activity1A0001Label}</a></li>
                    </ul>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>