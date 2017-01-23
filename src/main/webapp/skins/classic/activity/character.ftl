<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${characterLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/character">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module article-module">
                        <h2 class="sub-head">
                            <div class="avatar-small tooltipped tooltipped-ne"
                                 aria-label="${characterLabel}" style="background-image:url('${staticServePath}/images/activities/char.png')"></div>
                            ${characterLabel}
                            <span class="ft-13 ft-gray">${activityCharacterTitleLabel}</span>
                        </h2>
                        <div class="fn-clear fn-m10">   
                            <br>
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
                        <div class="content-reset fn-m10">
                            <ul>
                                <li>${userCharacterProgressLabel}${colonLabel}${userProgress}</li>
                                <li>${totalCharacterProgressLabel}${colonLabel}${totalProgress}</li>
                            </ul>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                Activity.charInit('charCanvas');
        </script>
    </body>
</html>