<#macro admin type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "index">
        <@head title="${consoleIndexLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "users">
        <@head title="${userAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "addUser">
        <@head title="${addUserLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "articles">
        <@head title="${articleAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "comments">
        <@head title="${commentAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "addDomain">
        <@head title="${addDomainLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "domains">
        <@head title="${domainAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "tags">
        <@head title="${tagAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "reservedWords">
        <@head title="${reservedWordAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "addReservedWord">
        <@head title="${allReservedWordLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "addArticle">
        <@head title="${addArticleLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "invitecodes">
        <@head title="${invitecodeAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "misc">
        <@head title="${miscAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="tab-current fn-clear">
                <div onclick="$(this).next().next().toggle()">
                    <#if type == "index">
                    ${consoleIndexLabel}
                    <#elseif type == "users" || type == "addUser">
                    ${userAdminLabel}
                    <#elseif type == "articles" || type == "addArticle">
                    ${articleAdminLabel}
                    <#elseif type == "comments">
                    ${commentAdminLabel}
                    <#elseif type == "domains" || type == "addDomain">
                    ${domainAdminLabel}
                    <#elseif type == "tags">
                    ${tagAdminLabel}
                    <#elseif type == "reservedWords" || type == "addReservedWord">
                    ${reservedWordAdminLabel}
                    <#elseif type == "invitecodes">
                    ${invitecodeAdminLabel}
                    <#elseif type == "misc">
                    ${miscAdminLabel}
                    </#if>
                    <span class="icon-chevron-down fn-right"></span>
                </div>
                <div class="fn-hr5"></div>
                <ul class="tab fn-clear fn-none">
                    <li<#if type == "index"> class="fn-none"</#if>><a href="/admin">${consoleIndexLabel}</a></li>
                    <li<#if type == "users" || type == "addUser"> class="fn-none"</#if>><a href="/admin/users">${userAdminLabel}</a></li>
                    <li<#if type == "articles" || type == "addArticle"> class="fn-none"</#if>><a href="/admin/articles">${articleAdminLabel}</a></li>
                    <li<#if type == "comments"> class="fn-none"</#if>><a href="/admin/comments">${commentAdminLabel}</a></li>
                    <li<#if type == "domains" || type == "addDomain"> class="fn-none"</#if>><a href="/admin/domains">${domainAdminLabel}</a></li>
                    <li<#if type == "tags"> class="fn-none"</#if>><a href="/admin/tags">${tagAdminLabel}</a></li>
                    <li<#if type == "reservedWords" || type == "addReservedWord"> class="fn-none"</#if>><a href="/admin/reserved-words">${reservedWordAdminLabel}</a></li>
                    <li<#if type == "invitecodes"> class="current"</#if>><a href="/admin/invitecodes">${invitecodeAdminLabel}</a></li>
                    <li<#if type == "misc"> class="fn-none"</#if>><a href="/admin/misc">${miscAdminLabel}</a></li>
                    </ul>
                </div>
                <#nested>
                </div>
        <#include "../footer.ftl">
    </body>
</html>
</#macro>
