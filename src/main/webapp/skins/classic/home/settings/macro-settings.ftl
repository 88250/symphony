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
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="tabs fn-clear settings-tabs">
                        <a href="${servePath}/member/${user.userName}"><svg height="18" viewBox="0 1 16 16" width="16">${boolIcon}</svg> ${postLabel}</a>
                        <a href="${servePath}/member/${user.userName}/following/users"><svg height="18" viewBox="0 1 14 16" width="14">${starIcon}</svg> ${followLabel}</a>
                        <a href="${servePath}/member/${user.userName}/points"><svg height="18" viewBox="0 1 14 16" width="14">${giftIcon}</svg> ${pointLabel}</a>
                        <a href="${servePath}/member/${user.userName}/forge/link"><svg height="18" viewBox="0 1 16 16" width="16">${baguaIcon}</svg>  ${forgeLabel}</a>
                        <a class="selected"
                           href="${servePath}/settings"><svg height="18" viewBox="0 1 14 16" width="14">${settingIcon}</svg> ${settingsLabel}</a>
                    </div>
                    <#nested>
                </div>
                <div class="side">
                    <nav class="home-menu">
                        <a href="${servePath}/settings"<#if 'profile' == type> class="current"</#if>>${profilesLabel}</a>
                        <a href="${servePath}/settings/avatar"<#if 'avatar' == type> class="current"</#if>>${avatarLabel}</a>
                        <a href="${servePath}/settings/invite"<#if 'invite' == type> class="current"</#if>>${inviteLabel}</a>
                        <a href="${servePath}/settings/function"<#if 'function' == type> class="current"</#if>>${functionLabel}</a>
                        <a href="${servePath}/settings/point"<#if 'point' == type> class="current"</#if>>${pointLabel}</a>
                        <a href="${servePath}/settings/location"<#if 'location' == type> class="current"</#if>>${geoLabel}</a>
                        <a href="${servePath}/settings/privacy"<#if 'privacy' == type> class="current"</#if>>${privacyLabel}</a>
                        <a href="${servePath}/settings/password"<#if 'password' == type> class="current"</#if>>${passwordLabel}</a>
                        <a href="${servePath}/settings/b3"<#if 'b3' == type> class="current"</#if>>B3</a>
                        <a href="${servePath}/settings/data"<#if 'data' == type> class="current"</#if>>${dataLabel}</a>
                        <a href="${servePath}/settings/help"<#if 'help' == type> class="current"</#if>>${helpLabel}</a>
                    </nav>
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
