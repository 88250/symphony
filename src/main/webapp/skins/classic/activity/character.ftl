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
                <div class="content">
                    <div class="content-reset">
                    字如其人 ^_^
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