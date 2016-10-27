<#macro top type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "balance">
        <@head title="${wealthRankLabel} - ${symphonyLabel}">
        <link rel="canonical" href="${servePath}/top/balance">
        </@head>
        </#if>
        <#if type == "consumption">
        <@head title="${consumptionRankLabel} - ${symphonyLabel}">
        <link rel="canonical" href="${servePath}/top/consumption">
        </@head>
        </#if>
        <#if type == "checkin">
        <@head title="${checkinTopLabel} - ${symphonyLabel}">
        <link rel="canonical" href="${servePath}/top/checkin">
        </@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/responsive.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                    <#nested>
                    <#include "../common/ranking.ftl">
                    <br>    
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
