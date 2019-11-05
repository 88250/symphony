<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
        <meta name="description" content="${user.userName}${deLabel}${anonymousArticleLabel}"/>
            </@head>
        <#elseif type == "commentsAnonymous">
            <@head title="${anonymousCommentLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${anonymousCommentLabel}"/>
            </@head>
        <#elseif type == "breezemoons">
            <@head title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}">
       <meta name="description" content="只与清风、明月为伴。清凉的风，明朗的月。"/>
            </@head>
        </#if>
    <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}"/>
</head>
<body class="fn__flex-column">
        <#include "../header.ftl">
<div class="main fn__flex-1">
    <div class="wrapper">
        <div class="content" id="home-pjax-container">
                    <#if pjax><!---- pjax {#home-pjax-container} start ----></#if>
            <div class="module">
                    <#nested>
            </div><#if pjax><!---- pjax {#home-pjax-container} end ----></#if>
        </div>
        <div class="side">
                    <#include "home-side.ftl">
            <div class="module fn-none">
                <div class="module-header"><h2>${goHomeLabel}</h2></div>
                <div class="module-panel fn-oh">
                    <nav class="home-menu">
                        <a pjax-title="${articleLabel} - ${user.userName} - ${symphonyLabel}" <#if type == "home" || type == "comments" || type == "articlesAnonymous" || type == "commentsAnonymous">
                                    class="current"</#if>
                           href="${servePath}/member/${user.userName}">
                            <svg>
                                <use xlink:href="#addfile"></use>
                            </svg> ${postLabel}</a>
                        <a pjax-title="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}" <#if type == "watchingArticles" || type == "followingUsers" || type == "followingTags" || type == "followingArticles" || type == "followers" || type == "breezemoons">
                           class="current"</#if>
                           href="${servePath}/member/${user.userName}/watching/articles">
                            <svg>
                                <use xlink:href="#star"></use>
                            </svg> ${followLabel}</a>
                        <a pjax-title="${pointLabel} - ${user.userName} - ${symphonyLabel}" <#if type == "points">
                           class="current"</#if> href="${servePath}/member/${user.userName}/points">
                            <svg>
                                <use xlink:href="#goods"></use>
                            </svg> ${pointLabel}</a>
                    </nav>
                </div>
            </div>
        </div>
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
    Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}"
    Label.invalidUserNicknameLabel = "${invalidUserNicknameLabel}"
    Label.type = '${type}'
    Label.userName = '${user.userName}'

    Settings.initHome()
</script>
</body>
</html>
</#macro>
