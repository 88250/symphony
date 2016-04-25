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
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div>
                <#include "../common/ranking.ftl">
                <#nested>
                <div class="side wrapper">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>
</#macro>
