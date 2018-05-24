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
        <@head title="${commentAdminLabel} - ${symphonyLabel}">
        <link rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
        </@head>
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
        <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <#nested>
                <div class="side">
                    <div class="module">
                        <div class="module-panel fn-oh">
                            <nav class="home-menu">
                                <#if permissions["menuAdmin"].permissionGrant>
                                <a href="${servePath}/admin"<#if type == "index"> class="current"</#if>>${consoleIndexLabel}</a>
                                </#if>
                                <#if permissions["menuAdminUsers"].permissionGrant>
                                <a href="${servePath}/admin/users"<#if type == "users" || type == "addUser"> class="current"</#if>>${userAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminArticles"].permissionGrant>
                                <a href="${servePath}/admin/articles"<#if type == "articles" || type == "addArticle"> class="current"</#if>>${articleAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminComments"].permissionGrant>
                                <a href="${servePath}/admin/comments"<#if type == "comments"> class="current"</#if>>${commentAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminBreezemoons"].permissionGrant>
                                <a href="${servePath}/admin/breezemoons"<#if type == "breezemoons"> class="current"</#if>>${breezemoonAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminDomains"].permissionGrant>
                                <a href="${servePath}/admin/domains"<#if type == "domains" || type == "addDomain"> class="current"</#if>>${domainAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminTags"].permissionGrant>
                                <a href="${servePath}/admin/tags"<#if type == "tags" || type == "addTag"> class="current"</#if>>${tagAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminRWs"].permissionGrant>
                                <a href="${servePath}/admin/reserved-words"<#if type == "reservedWords" || type == "addReservedWord"> class="current"</#if>>${reservedWordAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminIcs"].permissionGrant>
                                <a href="${servePath}/admin/invitecodes"<#if type == "invitecodes"> class="current"</#if>>${invitecodeAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminAD"].permissionGrant>
                                <a href="${servePath}/admin/ad"<#if type == "ad"> class="current"</#if>>${adAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminRoles"].permissionGrant>
                                <a href="${servePath}/admin/roles"<#if type == "roles"> class="current"</#if>>${rolesAdminLabel}</a>
                                </#if>
                                <#if permissions["menuAdminMisc"].permissionGrant>
                                <a href="${servePath}/admin/misc"<#if type == "misc"> class="current"</#if>>${miscAdminLabel}</a>
                                </#if>
                            </nav>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <#if type == "comments">
        <script src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Settings.initHljs();
        </script>
        </#if>
    </body>
</html>
</#macro>
