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
            <div class="wrapper fn-clear">
                <div class="content content-reset">
                    ${activity1A0001TitleLabel}
                    ${activity1A0001GuideLabel}

                    <div>
                        ${activity1A0001BetSelectTipLabel}
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

                    <span id="betTip" style="right: 550px; top: 322px;"></span>
                    <button class="red fn-right" onclick="Activity.bet1A0001()">${activityBetLabel}</button>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>