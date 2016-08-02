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
        <#if type == "addTag">
        <@head title="${addTagLabel} - ${symphonyLabel}"></@head>
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
        <#if type == "ad">
        <@head title="${adAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <#if type == "misc">
        <@head title="${miscAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="responsive-show">
                    <ul class="tab fn-clear">
                        <li<#if type == "index"> class="current"</#if>><a href="${servePath}/admin">${consoleIndexLabel}</a></li>
                        <li<#if type == "users" || type == "addUser"> class="current"</#if>><a href="${servePath}/admin/users">${userAdminLabel}</a></li>
                        <li<#if type == "articles" || type == "addArticle"> class="current"</#if>><a href="${servePath}/admin/articles">${articleAdminLabel}</a></li>
                        <li<#if type == "comments"> class="current"</#if>><a href="${servePath}/admin/comments">${commentAdminLabel}</a></li>
                        <li<#if type == "domains" || type == "addDomain"> class="current"</#if>><a href="${servePath}/admin/domains">${domainAdminLabel}</a></li>
                        <li<#if type == "tags" || type == "addTag"> class="current"</#if>><a href="${servePath}/admin/tags">${tagAdminLabel}</a></li>
                        <li<#if type == "reservedWords" || type == "addReservedWord"> class="current"</#if>><a href="${servePath}/admin/reserved-words">${reservedWordAdminLabel}</a></li>
                        <li<#if type == "invitecodes"> class="current"</#if>><a href="${servePath}/admin/invitecodes">${invitecodeAdminLabel}</a></li>
                        <li<#if type == "ad"> class="current"</#if>><a href="${servePath}/admin/ad">${adAdminLabel}</a></li>
                        <li<#if type == "misc"> class="current"</#if>><a href="${servePath}/admin/misc">${miscAdminLabel}</a></li>
                    </ul>
                    <br/>
                </div>
                <#nested>
                <div class="side">
                    <ul class="note-list responsive-hide">
                        <li<#if type == "index"> class="current"</#if>><a href="${servePath}/admin">${consoleIndexLabel}</a></li>
                        <li<#if type == "users" || type == "addUser"> class="current"</#if>><a href="${servePath}/admin/users">${userAdminLabel}</a></li>
                        <li<#if type == "articles" || type == "addArticle"> class="current"</#if>><a href="${servePath}/admin/articles">${articleAdminLabel}</a></li>
                        <li<#if type == "comments"> class="current"</#if>><a href="${servePath}/admin/comments">${commentAdminLabel}</a></li>
                        <li<#if type == "domains" || type == "addDomain"> class="current"</#if>><a href="${servePath}/admin/domains">${domainAdminLabel}</a></li>
                        <li<#if type == "tags" || type == "addTag"> class="current"</#if>><a href="${servePath}/admin/tags">${tagAdminLabel}</a></li>
                        <li<#if type == "reservedWords" || type == "addReservedWord"> class="current"</#if>><a href="${servePath}/admin/reserved-words">${reservedWordAdminLabel}</a></li>
                        <li<#if type == "invitecodes"> class="current"</#if>><a href="${servePath}/admin/invitecodes">${invitecodeAdminLabel}</a></li>
                        <li<#if type == "ad"> class="current"</#if>><a href="${servePath}/admin/ad">${adAdminLabel}</a></li>
                        <li<#if type == "misc"> class="current"</#if>><a href="${servePath}/admin/misc">${miscAdminLabel}</a></li>
                    </ul>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
</#macro>
