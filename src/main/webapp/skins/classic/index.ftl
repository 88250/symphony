<#include "macro-head.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body class="index">
        <#include "header.ftl">     

        <@subNav '' ''/>

        <div class="main first">
            <div class="wrapper">
                <div class="item first">
                    <a href="${servePath}/hot" class="item-header" style="background-image: url(${hotBgIcon});">${hotLabel}</a>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list hotArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="item">
                    <a href="/perfect" class="item-header" style="background-image: url(${perfectBgIcon});">${perfectLabel}</a>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list perfectArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <#if tags?size != 0>
        <div class="index-wrap">
            <div class="wrapper">
                <ul class="tag-desc fn-clear">
                    <#list tags as tag>
                    <li>
                         <a rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">
                            <#if tag.tagIconPath!="">
                            <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" />
                            </#if>
                            ${tag.tagTitle}
                        </a>
                    </li>
                    </#list>
                </ul>
            </div>
        </div>
        </#if>
        <div class="main<#if ADLabel == ''> first</#if>">
            <div class="wrapper">
                <div class="item<#if ADLabel == ''> first</#if>">
                    <a href="${servePath}/timeline" class="item-header" style="background-image: url(${timelineBgIcon});">${timelineLabel}</a>
                    <div class="module-panel">
                        <#if timelines?size <= 0>
                        <div id="emptyTimeline">${emptyTimelineLabel}</div>
                        </#if>
                        <ul class="module-list timeline">
                            <#list timelines as article>
                            <#if article_index < 9>
                            <li<#if !article_has_next> class="last"</#if>>
                                ${article.content}
                            </#if>
                        </li>
                        </#list>
                    </ul>
                </div>
            </div>
            <#if ADLabel != ''>
            <div class="item mid">
                <a class="item-header" style="background-image: url(${adBgIcon})" href="https://hacpai.com/article/1460083956075">${sponsorLabel}</a>
                <div class="ad module-panel">
                    ${ADLabel}
                </div>
            </div>
            </#if>
            <div class="item">
                <a class="item-header" style="background-image: url(${activityBgIcon});" href="${servePath}/pre-post">${postArticleLabel}</a>
                <div class="module-panel">
                    <ul class="module-list">
                        <li><a class="title" href="<#if useCaptchaCheckin??>/activity/checkin<#else>/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a></li>
                        <li><a class="title" href="${servePath}/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a></li>
                        <li><a class="title" href="${servePath}/activity/1A0001">${activity1A0001Label}</a></li>
                        <li><a class="title" href="${servePath}/activity/character">${characterLabel}</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    
    <div class="footer">
        <div class="wrapper">
            <div class="slogan">
                ${indexIntroLabel} &nbsp; &nbsp;
                <a href="https://github.com/b3log/symphony" target="_blank" class="tooltipped tooltipped-n" aria-label="${siteCodeLabel}">
                    <svg class="ft-gray" height="16" width="16" viewBox="0 0 16 16">${githubIcon}</svg></a> &nbsp;
                <a href="http://weibo.com/u/2778228501" target="_blank" class="tooltipped tooltipped-n" aria-label="${followWeiboLabel}">
                    <svg class="ft-gray" width="18" height="18" viewBox="0 0 37 30">${weiboIcon}</svg></a>   &nbsp; 
                <a target="_blank" class="tooltipped tooltipped-n" aria-label="${joinQQGroupLabel}"
                   href="http://shang.qq.com/wpa/qunwpa?idkey=f77a54e7d2bd53bed4043f70838da92fa49eccda53e706ef2124943cb0df4df5">
                    <svg class="ft-gray" width="16" height="16" viewBox="0 0 30 30">${qqIcon}</svg></a>
            </div>
        </div>

        <div class="wrapper">
            <div class="fn-flex-1">
                <div class="footer-nav fn-clear">
                    <a rel="help" href="https://hacpai.com/article/1440573175609">${aboutLabel}</a>
                    <a href="https://hacpai.com/article/1457158841475">API</a>
                    <a href="${servePath}/tag/系统公告">${symAnnouncementLabel}</a>
                    <a href="${servePath}/tag/Q%26A">${qnaLabel}</a>
                    <a href="${servePath}/domains">${domainLabel}</a>
                    <a href="${servePath}/tags">${tagLabel}</a>
                    <a href="https://hacpai.com/article/1460083956075">${adDeliveryLabel}</a>
                    <a href="${servePath}/statistic" class="last">${dataStatLabel}</a>

                    <div class="fn-right">
                        <span class="ft-gray">&COPY; ${year}</span>
                        <a rel="copyright" href="https://hacpai.com" target="_blank">hacpai.com</a>
                        ${visionLabel}</div>
                </div>
                <div class="fn-clear ft-smaller ft-fade">
                    ${sloganLabel}
                    <div class="fn-right">
                       Powered by <a href="http://b3log.org" target="_blank" class="ft-gray">B3log 开源</a> • 
                            <a href="https://github.com/b3log/symphony" class="ft-gray" target="_blank">Sym</a>
                            ${version} • ${elapsed?c}ms
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="icon-up" onclick="Util.goTop()"></div>
    <script type="text/javascript" src="${staticServePath}/js/lib/compress/libs.min.js"></script>
    <script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
    <script>
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
                desktopNotificationTemplateLabel: "${desktopNotificationTemplateLabel}",
                servePath: "${servePath}",
                staticServePath: "${staticServePath}",
                isLoggedIn: ${isLoggedIn?c}
        };
        Util.init(${isLoggedIn?c});
        <#if isLoggedIn>
                // Init [User] channel
                Util.initUserChannel("${wsScheme}://${serverHost}:${serverPort}${contextPath}/user-channel");
        </#if>
    </script>
    <#if algoliaEnabled>
    <script src="${staticServePath}/js/lib/algolia/algolia.min.js"></script>
    <script>
        Util.initSearch('${algoliaAppId}', '${algoliaSearchKey}', '${algoliaIndex}');
    </script>
    </#if>
</body>
</html>
