<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${characterLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content activity">
                    <div class="content-reset">
                        ${activityCharacterTitleLabel}
                        <div class="fn-clear">
                            <div class="fn-left">
                                <#if noCharacter??>
                                ${activityCharacterNotCharacterLabel}
                                <#else>
                                ${activityCharacterGuideLabel}
                                </#if>
                            </div>
                            <div class="fn-right">
                                <button class="red" onclick="Activity.clearCharacter('charCanvas')">${clearLabel}</button>
                                &nbsp;
                                <button class="green" onclick="Activity.submitCharacter('charCanvas')">${submitLabel}</button>
                            </div>
                        </div>
                        <canvas id="charCanvas" width="500" height="490"></canvas>
                        <#if !noCharacter??>
                        <ul>
                            <li>${userCharacterProgressLabel}${colonLabel}${userProgress}</li>
                            <li>${totalCharacterProgressLabel}${colonLabel}${totalProgress}</li>
                        </ul>
                        </#if>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                    Activity.charInit('charCanvas');
        </script>
    </body>
</html>