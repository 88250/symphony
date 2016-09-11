<#macro home type>
<#include "../../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${settingsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="tabs">
            <div class="fn-clear wrapper home-nav">
                <a href="${servePath}/member/${user.userName}"><svg height="18" viewBox="0 1 16 16" width="16">${boolIcon}</svg> ${postLabel}</a>
                <a href="${servePath}/member/${user.userName}/following/users"><svg height="18" viewBox="0 1 14 16" width="14">${starIcon}</svg> ${followLabel}</a>
                <a href="${servePath}/member/${user.userName}/points"><svg height="18" viewBox="0 1 14 16" width="14">${giftIcon}</svg> ${pointLabel}</a>
                <a href="${servePath}/member/${user.userName}/forge/link"><svg height="18" viewBox="0 1 16 16" width="16">${linkIcon}</svg>  ${linkForgeLabel}</a>
                <a class="selected"
                   href="${servePath}/settings"><svg height="18" viewBox="0 1 14 16" width="14">${settingIcon}</svg> ${settingsLabel}</a>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <#nested>
                </div>
                <div class="side">
                    <ul class="home-list responsive-hide">
                        <li<#if 'profile' == type> class="current"</#if>><a href="${servePath}/settings">${profilesLabel}</a></li>
                        <li<#if 'avatar' == type> class="current"</#if>><a href="${servePath}/settings/avatar">${avatarLabel}</a></li>
                        <li<#if 'invite' == type> class="current"</#if>><a href="${servePath}/settings/invite">${inviteLabel}</a></li>
                        <li<#if 'function' == type> class="current"</#if>><a href="${servePath}/settings/function">${functionLabel}</a></li>
                        <li<#if 'point' == type> class="current"</#if>><a href="${servePath}/settings/point">${pointLabel}</a></li>
                        <li<#if 'location' == type> class="current"</#if>><a href="${servePath}/settings/location">${geoLabel}</a></li>
                        <li<#if 'privacy' == type> class="current"</#if>><a href="${servePath}/settings/privacy">${privacyLabel}</a></li>
                        <li<#if 'password' == type> class="current"</#if>><a href="${servePath}/settings/password">${passwordLabel}</a></li>
                        <li<#if 'b3' == type> class="current"</#if>><a href="${servePath}/settings/b3">B3</a></li>
                        <li<#if 'data' == type> class="current"</#if>><a href="${servePath}/settings/data">${dataLabel}</a></li>
                    </ul>
                    <#if 'profile' == type || 'avatar' == type> 
                    <div id="homeSidePanel" class="fn-none">
                        <#include "../home-side.ftl">
                    </div>
                    </#if>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
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
            Label.previewLabel = "${previewLabel}";
            Label.unPreviewLabel = "${unPreviewLabel}";
        </script>
    </body>
</html>
</#macro>
