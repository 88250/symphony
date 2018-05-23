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
<#macro home type>
    <#include "../macro-head.ftl">
    <#include "../common/title-icon.ftl">
<!DOCTYPE html>
<html>
<head>
        <#if type == "home">
            <@head title="${articleLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description"
              content="<#list userHomeArticles as article><#if article_index<3>${article.articleTitle},</#if></#list>"/>
            </@head>
        <#elseif type == "comments">
            <@head title="${cmtLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${cmtLabel}"/>
            </@head>
        <#elseif type == "followingUsers">
            <@head title="${followingUsersLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingUsersLabel}"/>
            </@head>
        <#elseif type == "followingTags">
            <@head title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingTagsLabel}"/>
            </@head>
        <#elseif type == "followingArticles">
            <@head title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingArticlesLabel}"/>
            </@head>
        <#elseif type == "watchingArticles">
            <@head title="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${watchingArticlesLabel}"/>
            </@head>
        <#elseif type == "followers">
            <@head title="${followersLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followersLabel}"/>
            </@head>
        <#elseif type == "points">
            <@head title="${pointLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${pointLabel}"/>
            </@head>
        <#elseif type == "settings">
            <@head title="${settingsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
            </@head>
        <#elseif type == "articlesAnonymous">
            <@head title="${anonymousArticleLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
            </@head>
        <#elseif type == "commentsAnonymous">
            <@head title="${anonymousCommentLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
            </@head>
        <#elseif type == "linkForge">
            <@head title="${linkForgeLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${linkForgeLabel}"/>
            </@head>
        <#elseif type == "breezemoons">
            <@head title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="只与清风、明月为伴。清凉的风，明朗的月。"/>
            </@head>
        </#if>
    <link rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
</head>
<body>
        <#include "../header.ftl">
<div class="main">
    <div class="tab-current fn-clear">
        <div class="fn-hr5"></div>
        <div onclick="$(this).next().next().slideToggle()">
                    <#if type == "home">
                        ${articleLabel}
                    <#elseif type == "comments">
                        ${cmtLabel}
                    <#elseif type == "followingUsers">
                        ${followingUsersLabel}
                    <#elseif type == "followingTags">
                        ${followingTagsLabel}
                    <#elseif type == "followingArticles">
                        ${followingArticlesLabel}
                    <#elseif type == "watchingArticles">
                        ${watchingArticlesLabel}
                    <#elseif type == "followers">
                        ${followersLabel}
                    <#elseif type == "points">
                        ${pointLabel}
                    <#elseif type == "articlesAnonymous">
                        ${anonymousLabel}${articleLabel}
                    <#elseif type == "commentsAnonymous">
                        ${anonymousLabel}${cmtLabel}
                    <#elseif type == "settings">
                        ${settingsLabel}
                    <#elseif type == "linkForge">
                        ${linkForgeLabel}
                    <#elseif type == "breezemoons">
                        ${breezemoonLabel}
                    </#if>
            <svg class="fn-right">
                <use xlink:href="#chevron-down"></use>
            </svg>
        </div>
        <div class="fn-hr5"></div>
        <ul class="tab fn-clear fn-none">
            <li<#if type == "home"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}">${articleLabel}</a>
            </li>
            <li<#if type == "comments"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/comments">${cmtLabel}</a>
            </li>
            <li<#if type == "watchingArticles"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/watching/articles">${watchingArticlesLabel}</a>
            </li>
            <li<#if type == "followingUsers"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/following/users">${followingUsersLabel}</a>
            </li>
            <li<#if type == "followingTags"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/following/tags">${followingTagsLabel}</a>
            </li>
            <li<#if type == "followingArticles"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/following/articles">${followingArticlesLabel}</a>
            </li>
            <li<#if type == "followers"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/followers">${followersLabel}</a>
            </li>
            <li<#if type == "breezemoons"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/breezemoons">${breezemoonLabel}</a>
            </li>
            <li<#if type == "points"> class="fn-none"</#if>>
                <a href="${servePath}/member/${user.userName}/points">${pointLabel}</a>
            </li>
            <#if currentUser?? && currentUser.userName == user.userName>
            <li<#if type == "articlesAnonymous"> class="current"</#if>>
                <a href="${servePath}/member/${user.userName}/articles/anonymous">${anonymousArticleLabel}</a>
            </li>
            <li<#if type == "commentsAnonymous"> class="current"</#if>>
                <a href="${servePath}/member/${user.userName}/comments/anonymous">${anonymousCommentLabel}</a>
            </li>
            <li<#if type == "settings"> class="fn-none"</#if>>
                <a href="${servePath}/settings"><b class="ft-red">${settingsLabel}</b></a>
            </li>
            </#if>
        </ul>
    </div>
    <div class="fn-clear">
                <#nested>
    </div>
    <div class="side">
                <#include "home-side.ftl">
    </div>
</div>
        <#include "../footer.ftl">
<script src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
<script src="${staticServePath}/js/breezemoon${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    Label.followLabel = "${followLabel}"
    Label.unfollowLabel = "${unfollowLabel}"
    Label.invalidPasswordLabel = "${invalidPasswordLabel}"
    Label.amountNotEmpty = "${amountNotEmpty}"
    Label.invalidUserNameLabel = "${invalidUserNameLabel}"
    Label.loginNameErrorLabel = "${loginNameErrorLabel}"
    Label.updateSuccLabel = "${updateSuccLabel}"
    Label.transferSuccLabel = "${transferSuccLabel}"
    Label.invalidUserURLLabel = "${invalidUserURLLabel}"
    Label.tagsErrorLabel = "${tagsErrorLabel}"
    Label.invalidUserQQLabel = "${invalidUserQQLabel}"
    Label.invalidUserIntroLabel = "${invalidUserIntroLabel}"
    Label.invalidUserB3KeyLabel = "${invalidUserB3KeyLabel}"
    Label.invalidUserB3ClientURLLabel = "${invalidUserB3ClientURLLabel}"
    Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}"
    Label.invalidUserNicknameLabel = "${invalidUserNicknameLabel}"
    Label.forgeUploadSuccLabel = "${forgeUploadSuccLabel}"
    Label.type = '${type}'
    Label.userName = '${user.userName}'

    Settings.initHome()
</script>
</body>
</html>
</#macro>
