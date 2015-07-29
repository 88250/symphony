<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${activityLabel} - ${activity1A0001Label}">
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content content-reset">
                    <ul>
                        <li><a href="/activity/daily-checkin">${activityDailyCheckinLabel}</a></li>
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