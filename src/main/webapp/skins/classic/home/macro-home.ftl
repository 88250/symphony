<#macro home type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "home">
        <@head title="${articleLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="<#list userHomeArticles as article><#if article_index<3>${article.articleTitle},</#if></#list>"/>
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
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="tabs">
            <div class="fn-clear wrapper">
                <a <#if type == "home" || type == "comments" || type == "articlesAnonymous" || type == "commentsAnonymous">
                    class="selected"</#if>
                    href="${servePath}/member/${user.userName}">${postLabel}</a>
                <a <#if type == "followingUsers" || type == "followingTags" || type == "followingArticles" || type == "followers"> class="selected"</#if>
                    href="${servePath}/member/${user.userName}/following/users">${followLabel}</a>
                <a <#if type == "points"> class="selected"</#if> href="${servePath}/member/${user.userName}/points">${pointLabel}</a>
                <#if currentUser?? && currentUser.userName == user.userName>
                <a <#if type == "settings"> class="selected"</#if>
                    href="${servePath}/settings"><svg height="16" version="1.1" viewBox="0 0 14 16" width="14"><path d="M14 8.77v-1.6l-1.94-.64-.45-1.09.88-1.84-1.13-1.13-1.81.91-1.09-.45-.69-1.92h-1.6l-.63 1.94-1.11.45-1.84-.88-1.13 1.13.91 1.81-.45 1.09L0 7.23v1.59l1.94.64.45 1.09-.88 1.84 1.13 1.13 1.81-.91 1.09.45.69 1.92h1.59l.63-1.94 1.11-.45 1.84.88 1.13-1.13-.92-1.81.47-1.09L14 8.75v.02zM7 11c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"></path></svg> ${settingsLabel}</a>
                </#if>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="fn-clear">
                        <#nested>
                    </div>
                </div>
                <div class="side">
                    <#include "home-side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Label.followLabel = "${followLabel}";
            Label.unfollowLabel = "${unfollowLabel}";
            Label.invalidPasswordLabel = "${invalidPasswordLabel}";
            Label.amountNotEmpty = "${amountNotEmpty}";
            Label.invalidUserNameLabel = "${invalidUserNameLabel}";
            Label.loginNameErrorLabel = "${loginNameErrorLabel}";
            Label.updateSuccLabel = "${updateSuccLabel}";
            Label.transferSuccLabel = "${transferSuccLabel}";
            Label.invalidUserURLLabel = "${invalidUserURLLabel}";
            Label.invalidAvatarURLLabel = "${invalidAvatarURLLabel}";
            Label.tagsErrorLabel = "${tagsErrorLabel}";
            Label.invalidUserQQLabel = "${invalidUserQQLabel}";
            Label.invalidUserIntroLabel = "${invalidUserIntroLabel}";
            Label.invalidUserB3KeyLabel = "${invalidUserB3KeyLabel}";
            Label.invalidUserB3ClientURLLabel = "${invalidUserB3ClientURLLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
            Label.invalidUserNicknameLabel = "${invalidUserNicknameLabel}";
        </script>
    </body>
</html>
</#macro>
