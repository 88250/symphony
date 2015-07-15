<#include "macro-head.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${visionLabel}">
        <meta name="keywords" content="${visionLabel},${symphonyLabel}"/>
        <meta name="description" content="${symphonyLabel}${b3logDescriptionLabel}"/>
        </@head>
    </head>
    <body style="background-color: #FFF;">
        <div class="nav"> 
            <div class="wrapper fn-clear">
                <h1><a target="_blank" rel="nofollow" href="/">Sym</a></h1>
                <span class="tags">
                    <#list navTrendTags as trendTag>
                    <a target="_blank" rel="tag" href="/tags/${trendTag.tagTitle?url('UTF-8')}">${trendTag.tagTitle}</a>
                    </#list>
                </span>
                <div class="fn-clear user-nav">
                    <#if isLoggedIn>
                    <a target="_blank" id="logout" href="${logoutURL}" title="${logoutLabel}" class="last icon icon-logout"></a>
                    <#if "adminRole" == userRole>
                    <a target="_blank" href="/admin" title="${adminLabel}" class="icon icon-setting"></a>
                    </#if>
                    <a target="_blank" id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
                    <a target="_blank" href="/add-article" title="${addArticleLabel}${articleLabel}" 
                       class="icon icon-addfile"></a>
                    <#else>
                    <a target="_blank" href="/register" class="last icon icon-register" 
                       title="${registerLabel}"></a>
                    <a href="javascript: Util.showLogin();" class="icon icon-login" title="${loginLabel}"></a>
                    <div class="form fn-none">
                        <table cellspacing="0" cellpadding="0">
                            <tr>
                                <td width="40">
                                    <label for="nameOrEmail">${accountLabel}</label>
                                </td>
                                <td>
                                    <input id="nameOrEmail" type="text" placeholder="${nameOrEmailLabel}" />
                                    <span style="top: 28px; left: 246px;"></span>
                                </td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="loginPassword">${passwordLabel}</label>
                                </td>
                                <td>
                                    <input type="password" id="loginPassword" />
                                    <span style="top: 83px; left: 246px;"></span>
                                </td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr>
                                <td colspan="3" align="right">
                                    <span id="loginTip" style="right: 82px; top: 126px;"></span>
                                    <button class="red" onclick="Util.login()">${loginLabel}</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                    </#if>
                </div>
            </div>
        </div>
        <div class="footer" style="margin-top: 54px;border-top-width: 0;border-bottom: 1px solid #e0e0e0;padding-bottom: 54px;">
            <div class="wrapper fn-clear">
                <div class="fn-left">
                    <a target="_blank" href="http://b3log.org" target="_blank"><img src="http://b3log.org/images/b3log.png" alt="b3log" width="48" /></a>
                    &nbsp;
                    <a target="_blank" href="https://wide.b3log.org" target="_blank"><img src="http://b3log.org/images/wide.png" alt="wide" width="48" /></a>
                </div>
                <div class="fn-right">
                    <div class="footer-nav">
                        <a target="_blank" rel="help" href="/about">${aboutLabel}</a> 
                        <a target="_blank" rel="help" href="/tags/B3log%20Announcement">${b3logAnnouncementLabel}</a>
                        <a target="_blank" rel="help" href="/tags/Q%26A">${qnaLabel}</a>
                        <a target="_blank" href="/tags" class="last">${tagLabel}</a>
                    </div>
                    <div class="fn-clear">
                        <div class="fn-left info">
                            <span class="ft-small">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt?c} &nbsp;
                            <span class="ft-small">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c} &nbsp;
                            <span class="ft-small">${memberLabel}</span> ${statistic.statisticMemberCount?c} &nbsp;
                            <span class="ft-small">${articleLabel}</span> ${statistic.statisticArticleCount?c} &nbsp;
                            <span class="ft-small">${tagLabel}</span> ${statistic.statisticTagCount?c} &nbsp;
                            <span class="ft-small">${cmtLabel}</span> ${statistic.statisticCmtCount?c}
                        </div>
                        <div class="fn-right">
                            ${visionLabel}
                            <span class="ft-small">&COPY; ${year} </span>
                            <a target="_blank" rel="copyright" href="http://b3log.org" target="_blank">B3LOG.ORG</a>
                            <span class="ft-small">${version}</span>
                            <span class="fn-none">
                                ${siteVisitStatCode}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="icon-up icon" onclick="Util.goTop()"></div>
        <script>var isLoggedIn = ${isLoggedIn?c};</script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.min.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/md5.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.linkify-1.0-min.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.notification-1.0.5.js?${staticResourceVersion}"></script>
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
