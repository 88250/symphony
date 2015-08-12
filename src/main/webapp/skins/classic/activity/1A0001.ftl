<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${activityLabel} - ${activity1A0001Label}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content content-reset activity">
                    ${activity1A0001TitleLabel}
                    ${activity1A0001GuideLabel}

                    <#if !closed && !closed1A0001 && !end && !collected && !participated>
                    <div id="betDiv">
                        <div>
                            ${activity1A0001BetSelectLabel}
                            <label><input name="smallOrLarge" type="radio" value="1" checked="checked" /> ${activity1A0001BetLargeLabel}</label>
                            <label><input name="smallOrLarge" type="radio" value="0" /> ${activity1A0001BetSmallLabel}</label>
                        </div>

                        <div>
                            ${activity1A0001BetAmountLabel}
                            <label><input name="amount" type="radio" value="200" checked="checked" /> 200</label>
                            <label><input name="amount" type="radio" value="300" /> 300</label>
                            <label><input name="amount" type="radio" value="400" /> 400</label>
                            <label><input name="amount" type="radio" value="500" /> 500</label>
                        </div>
                    </div>
                    </#if>
                    <#if participated || closed || closed1A0001 || collected || end>
                    <span id="tip" class="tip-succ" style="top: 307px;">${msg}</span>
                        <#if participated && hour?? && hour gt 15>
                        <div class="fn-clear">
                            <button id="collectBtn" class="red fn-right" onclick="Activity.collect1A0001()">${activityCollectLabel}</button>
                        </div>
                        </#if>
                    <#else>
                    <div class="fn-clear">
                        <button id="betBtn" class="red fn-right" onclick="Activity.bet1A0001()">${activityBetLabel}</button>
                    </div>
                    <span id="tip" style="top: 307px;"></span>
                    </#if>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>