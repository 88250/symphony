<#include "../macro-head.ftl">
<#include "../macro-list.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${visionLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                ${articleAdminLabel}
            </div>
            <div>
                ${articles}
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
