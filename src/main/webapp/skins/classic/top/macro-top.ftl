<#macro top type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "balance">
        <@head title="${symphonyLabel} - ${pointLabel}${balanceLabel}${rankingLabel}">
        </@head>
        </#if>
        <#if type == "consumption">
        <@head title="${symphonyLabel} - ${pointLabel}${consumptionLabel}${rankingLabel}">
        </@head>
        </#if>
        <#if type == "checkin">
        <@head title="${symphonyLabel} - ${checkinTopLabel}${rankingLabel}">
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
                    <div class="fn-left">
                        <font style="color: black;">♠</font> <a href="/top/balance">${pointLabel}${balanceLabel}</a>
                        <font style="color: red;">♥</font> <a href="/charge/point">${chargePointLabel}</a>
                        <font style="color: black;">♣</font> <a href="/top/checkin">${checkinTopLabel}</a>
                        <font style="color: red;">♦</font> <a href="/top/consumption">${pointLabel}${consumptionLabel}</a>
                    </div>
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
