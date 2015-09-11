<#include "macro-head.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${searchLabel}">
        </@head>
    </head>
    <body style="background-color: #FFF;">
        <div class="nav"> 
            <div class="wrapper fn-clear" style="margin-left: 66px">
                <div class="head-fn fn-clear">
                    <h1 class="fn-left">
                        <a href="/" target="_blank">
                            <img src="${staticServePath}/images/hacpai.png" alt="${symphonyLabel}" title="${symphonyLabel}" width="42" class="fn-pointer" />
                        </a>
                    </h1>
                    <form class="responsive-hide" target="_blank" action="http://search.hacpai.com/cse/search">
                        <span class="icon-search"></span>
                        <input class="search" type="text" name="q">
                        <input type="hidden" value="140632643792102269" name="s">
                        <input type="hidden" name="cc" value="hacpai.com">
                        <input type="submit" class="fn-none" value="">
                    </form>
                    <div class="fn-right">
                        <a target="_blank" href="/timeline" class="icon-clock last" title="${timelineLabel}"></a>
                        <#if isLoggedIn>
                        <a target="_blank" href="/city/my" class="icon-compass" title="${sameCityLabel}"></a>
                        </#if>
                        <a target="_blank" href="/recent" class="icon-refresh" title="${recentArticleLabel}"></a>
                    </div>
                </div>
                <div class="fn-clear user-nav">
                    <#if isLoggedIn>
                    <a id="logout" href="${logoutURL}" title="${logoutLabel}" class="last icon-logout"></a>
                    <#if "adminRole" == userRole>
                    <a target="_blank" href="/admin" title="${adminLabel}" class="icon-userrole"></a>
                    </#if>
                    <a target="_blank" href="/settings" title="${settingsLabel}" class="icon-setting"></a>
                    <a target="_blank" href="/activities" title="${activityLabel}" class="icon-flag"></a>
                    <a target="_blank" href="/pre-post" title="${addArticleLabel}" 
                       class="icon-addfile"></a>
                    <a target="_blank" id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
                    <#else>
                    <a target="_blank" id="aRegister" href="javascript:Util.goRegister()" class="last icon-register" 
                       title="${registerLabel}"></a>
                    <a target="_blank" href="javascript: Util.showLogin();" class="icon-login" title="${loginLabel}"></a>
                    <div class="form fn-none">
                        <table cellspacing="0" cellpadding="0">
                            <tr>
                                <td width="40">
                                    <label for="nameOrEmail">${accountLabel}</label>
                                </td>
                                <td>
                                    <input id="nameOrEmail" type="text" placeholder="${nameOrEmailLabel}" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="loginPassword">${passwordLabel}</label>
                                </td>
                                <td>
                                    <input type="password" id="loginPassword" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <div id="loginTip" class="tip"></div><br/>
                                    <button class="red" onclick="Util.login()">${loginLabel}</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                    </#if>
                </div>

            </div>

        </div>
        <div class="footer" style="margin-top: 63px;border-top-width: 0;border-bottom: 1px solid #e0e0e0;padding-bottom: 63px;">
            <div class="wrapper">
                <div class="fn-flex-1">
                    <div class="footer-nav fn-clear">
                        <a target="_blank" rel="help" href="http://hacpai.com/article/1440573175609">${aboutLabel}</a>
                        <a target="_blank" class="responsive-hide" href="/timeline">${timelineLabel}</a>
                        <#if isLoggedIn><a target="_blank" class="responsive-hide" href="/city/my">${sameCityLabel}</a></#if>
                        <a target="_blank" href="/tags/系统公告">${symAnnouncementLabel}</a>
                        <a target="_blank" href="/tags/Q%26A">${qnaLabel}</a>
                        <a target="_blank" href="/tags" class="last">${tagLabel}</a>

                        <div class="fn-right">${visionLabel}</div>
                    </div>
                    <div class="fn-clear">
                        <div class="fn-left info responsive-hide">
                            <span class="ft-gray">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt?c} &nbsp;
                            <span class="ft-gray">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c} &nbsp;
                            <span class="ft-gray">${memberLabel}</span> ${statistic.statisticMemberCount?c} &nbsp;
                            <span class="ft-gray">${articleLabel}</span> ${statistic.statisticArticleCount?c} &nbsp;
                            <span class="ft-gray">${tagLabel}</span> ${statistic.statisticTagCount?c} &nbsp;
                            <span class="ft-gray">${cmtLabel}</span> ${statistic.statisticCmtCount?c}
                        </div>
                        <div class="fn-right">
                            <span class="ft-gray">&COPY; ${year} </span>
                            <a target="_blank" rel="copyright" href="http://hacpai.com" target="_blank">hacpai.com</a>
                            <span class="ft-gray">${version} • ${elapsed?c}ms</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script>var isLoggedIn = ${isLoggedIn?c};</script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/md5.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.linkify-1.0-min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.notification-1.0.5.js"></script>
        <script>
            Util.init();
            var Label = {
                invalidPasswordLabel: "${invalidPasswordLabel}",
                loginNameErrorLabel: "${loginNameErrorLabel}",
                followLabel: "${followLabel}",
                unfollowLabel: "${unfollowLabel}",
                symphonyLabel: "${symphonyLabel}",
                visionLabel: "${visionLabel}",
                cmtLabel: "${cmtLabel}",
                collectLabel: "${collectLabel}",
                uncollectLabel: "${uncollectLabel}",
                desktopNotificationTemplateLabel: "${desktopNotificationTemplateLabel}"
            };
        </script>

    </div>
</body>
</html>
