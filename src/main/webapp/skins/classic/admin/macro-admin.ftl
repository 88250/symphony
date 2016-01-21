<#macro admin type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "index">
        <@head title="${symphonyLabel} - ${adminLabel}"></@head>
        </#if>
        <#if type == "users">
        <@head title="${symphonyLabel} - ${userAdminLabel}"></@head>
        </#if>
        <#if type == "addUser">
        <@head title="${symphonyLabel} - ${addUserLabel}"></@head>
        </#if>
        <#if type == "articles">
        <@head title="${symphonyLabel} - ${articleAdminLabel}"></@head>
        </#if>
        <#if type == "comments">
        <@head title="${symphonyLabel} - ${commentAdminLabel}"></@head>
        </#if>
        <#if type == "tags">
        <@head title="${symphonyLabel} - ${tagAdminLabel}"></@head>
        </#if>
        <#if type == "misc">
        <@head title="${symphonyLabel} - ${miscAdminLabel}"></@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <#nested>
                <div class="side">
                    <ul class="note-list">
                        <li<#if type == "index"> class="current"</#if>><a href="/admin">${adminLabel}</a></li>
                        <li<#if type == "users" || type == "addUser"> class="current"</#if>><a href="/admin/users">${userAdminLabel}</a></li>
                        <li<#if type == "articles"> class="current"</#if>><a href="/admin/articles">${articleAdminLabel}</a></li>
                        <li<#if type == "comments"> class="current"</#if>><a href="/admin/comments">${commentAdminLabel}</a></li>
                        <li<#if type == "tags"> class="current"</#if>><a href="/admin/tags">${tagAdminLabel}</a></li>
                        <li<#if type == "misc"> class="current"</#if>><a href="/admin/misc">${miscAdminLabel}</a></li>
                    </ul>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
</#macro>
