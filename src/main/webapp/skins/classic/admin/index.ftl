<#include "../macro-head.ftl">
<#include "../macro-list.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${adminLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                ${adminLabel}
                <ul>
                    <li><a href="/admin/users">${userAdminLabel}</a></li>
                    <li><a href="/admin/articles">${articleAdminLabel}</a></li>
                    <li><a href="/admin/comments">${commentAdminLabel}</a></li>
                    <li><a href="/admin/misc">${miscAdminLabel}</a></li>
                </ul>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
