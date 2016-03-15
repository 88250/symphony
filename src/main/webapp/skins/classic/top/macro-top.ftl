<#macro top type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "balance">
        <@head title="${wealthLabel}${rankingLabel} - ${symphonyLabel}">
        </@head>
        </#if>
        <#if type == "consumption">
        <@head title="${consumptionLabel}${rankingLabel} - ${symphonyLabel}">
        </@head>
        </#if>
        <#if type == "checkin">
        <@head title="${checkinTopLabel}${rankingLabel} - ${symphonyLabel}">
        </@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/responsive${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <#nested>
                    <#include "../common/ranking.ftl">
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>
</#macro>
