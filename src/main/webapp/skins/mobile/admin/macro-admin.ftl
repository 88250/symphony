<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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
        <#if type == "breezemoons">
        <@head title="${breezemoonAdminLabel} - ${symphonyLabel}"></@head>
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
        <#if type == "roles">
            <@head title="${rolesAdminLabel} - ${symphonyLabel}"></@head>
        </#if>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="tab-current fn-clear">
                <div class="fn-hr5"></div>
                <div onclick="$(this).next().next().slideToggle()">
                    <#if type == "index" && permissions["menuAdmin"].permissionGrant>
                    ${consoleIndexLabel}
                    </#if>
                    <#if (type == "users" || type == "addUser") && permissions["menuAdminUsers"].permissionGrant>
                    ${userAdminLabel}
                    </#if>
                    <#if (type == "articles" || type == "addArticle") && permissions["menuAdminArticles"].permissionGrant>
                    ${articleAdminLabel}
                    </#if>
                    <#if type == "comments" && permissions["menuAdminComments"].permissionGrant>
                    ${commentAdminLabel}
                    </#if>
                    <#if type == "breezemoons" && permissions["menuAdminBreezemoons"].permissionGrant>
                    ${breezemoonAdminLabel}
                    </#if>
                    <#if (type == "domains" || type == "addDomain") && permissions["menuAdminDomains"].permissionGrant>
                    ${domainAdminLabel}
                    </#if>
                    <#if (type == "tags" || type == "addTag") && permissions["menuAdminTags"].permissionGrant>
                    ${tagAdminLabel}
                    </#if>
                    <#if (type == "reservedWords" || type == "addReservedWord") && permissions["menuAdminRWs"].permissionGrant>
                    ${reservedWordAdminLabel}
                    </#if>
                    <#if type == "invitecodes" && permissions["menuAdminIcs"].permissionGrant>
                    ${invitecodeAdminLabel}
                    </#if>
                    <#if type == "ad" && permissions["menuAdminAD"].permissionGrant>
                    ${adAdminLabel}
                    </#if>
                    <#if type == "roles" && permissions["menuAdminRoles"].permissionGrant>
                    ${rolesAdminLabel}
                    </#if>
                    <#if type == "misc"  && permissions["menuAdminMisc"].permissionGrant>
                    ${miscAdminLabel}
                    </#if>
                    <svg class="fn-right"><use xlink:href="#chevron-down"></use></svg>
                </div>
                <div class="fn-hr5"></div>
                <ul class="tab fn-clear fn-none">
                    <#if permissions["menuAdmin"].permissionGrant>
                    <li<#if type == "index"> class="fn-none"</#if>><a href="${servePath}/admin">${consoleIndexLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminUsers"].permissionGrant>
                    <li<#if type == "users" || type == "addUser"> class="fn-none"</#if>><a href="${servePath}/admin/users">${userAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminArticles"].permissionGrant>
                    <li<#if type == "articles" || type == "addArticle"> class="fn-none"</#if>><a href="${servePath}/admin/articles">${articleAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminComments"].permissionGrant>
                    <li<#if type == "comments"> class="fn-none"</#if>><a href="${servePath}/admin/comments">${commentAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminBreezemoons"].permissionGrant>
                    <li<#if type == "breezemoons"> class="fn-none"</#if>><a href="${servePath}/admin/breezemoons">${breezemoonAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminDomains"].permissionGrant>
                    <li<#if type == "domains" || type == "addDomain"> class="fn-none"</#if>><a href="${servePath}/admin/domains">${domainAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminTags"].permissionGrant>
                    <li<#if type == "tags" || type == "addTag"> class="fn-none"</#if>><a href="${servePath}/admin/tags">${tagAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminRWs"].permissionGrant>
                    <li<#if type == "reservedWords" || type == "addReservedWord"> class="fn-none"</#if>><a href="${servePath}/admin/reserved-words">${reservedWordAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminIcs"].permissionGrant>
                    <li<#if type == "invitecodes"> class="fn-none"</#if>><a href="${servePath}/admin/invitecodes">${invitecodeAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminAD"].permissionGrant>
                    <li<#if type == "ad"> class="fn-none"</#if>><a href="${servePath}/admin/ad">${adAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminRoles"].permissionGrant>
                    <li<#if type == "roles"> class="fn-none"</#if>><a href="${servePath}/admin/roles">${rolesAdminLabel}</a></li>
                    </#if>
                    <#if permissions["menuAdminMisc"].permissionGrant>
                    <li<#if type == "misc"> class="fn-none"</#if>><a href="${servePath}/admin/misc">${miscAdminLabel}</a></li>
                    </#if>
                </ul>
            </div>
            <div class="fn-hr10"></div>
            <#nested>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
</#macro>
