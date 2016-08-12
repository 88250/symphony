<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body class="index">
        <#include "header.ftl">        
        <div class="slogan">
            ${indexIntroLabel}&nbsp;
            <a href="https://github.com/b3log/symphony" target="_blank">
                <svg class="ft-gray" height="16" width="16" viewBox="0 0 16 16">${githubIcon}</svg></a>
                <a href="http://weibo.com/u/2778228501" target="_blank">
                <svg class="ft-gray" width="18" height="18" viewBox="0 0 37 30">${weiboIcon}</svg></a>    
            <a target="_blank"
               href="http://shang.qq.com/wpa/qunwpa?idkey=f77a54e7d2bd53bed4043f70838da92fa49eccda53e706ef2124943cb0df4df5">
                <svg class="ft-gray" width="16" height="16" viewBox="0 0 30 30">${qqIcon}</svg></a>
        </div>

        <div class="index-wrap">
            <div class="domains wrapper fn-clear">
                <#list domains as domain>
                <a href="${servePath}/domain/${domain.domainURI}">${domain.domainIconPath} &nbsp; ${domain.domainTitle}</a>
                </#list>
            </div>
        </div>

        <div class="main">
            <div class="wrapper">
                <div class="item">
                    <a class="item-header" style="background-image: url(${recentBgIcon});" href="${servePath}/recent">${latestLabel}</a>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list recentArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="item mid">
                    <a href="${servePath}/hot" class="item-header" style="background-image: url(${hotBgIcon});">${hotLabel}</a>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list hotArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="item">
                    <a href="/hot" class="item-header" style="background-image: url(${perfectBgIcon});">${perfectLabel}</a>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list perfectArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <div class="index-wrap">
            <div class="wrapper">
                <ul class="tag-desc fn-clear">
                    <#list tags as tag>
                    <li>
                        <span>
                            <#if tag.tagIconPath!="">
                            <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" />
                            </#if>
                            <a rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                        </span>
                    </li>
                    </#list>
                </ul>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="item">
                    <a href="${servePath}/timeline" class="item-header" style="background-image: url(${timelineBgIcon});">${timelineLabel}</a>
                    <div class="module-panel">
                        <#if timelines?size <= 0>
                        <div id="emptyTimeline">${emptyTimelineLabel}</div>
                        </#if>
                        <ul class="module-list">
                            <#list timelines as article>
                            <#if article_index < 3>
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
                <a class="item-header" style="background-image: url(${adBgIcon})" href="https://hacpai.com/article/1460083956075">${wantPutOnLabel}</a>
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
    <#include "footer.ftl">
</body>
</html>
