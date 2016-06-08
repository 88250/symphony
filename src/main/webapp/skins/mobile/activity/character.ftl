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
                        <#if noCharacter??>
                        ${activityCharacterNotCharacterLabel}
                        <#else>
                        ${activityCharacterGuideLabel}
                        </#if>

                        <canvas id="charCanvas" width="280px" height="280px"></canvas>
                        <br/>
                        <br/>

                        <div class="fn-clear">
                            <button class="green fn-right" onclick="Activity.submitCharacter('charCanvas')">${submitLabel}</button>
                        </div>
                        <div class="fn-hr10"></div>
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