<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}">
    </head>
    <body class="index">
        <#include "header.ftl">   
        <div class="main first">
            <div class="wrapper">
                <div class="item first">
                    <a href="${servePath}/recent" class="item-header" style="background-image: url(${hotBgIcon});">${latestLabel}</a>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list recentArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                                <a class="fn-right count ft-gray ft-smaller" href="${servePath}${article.articlePermalink}">${article.articleViewCount}</a>
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
                                <a class="fn-right count ft-gray ft-smaller" href="${servePath}${article.articlePermalink}">${article.articleViewCount}</a>
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
                        <ul class="module-list timeline ft-gray">
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
                        <li>
                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                 aria-label="${activityDailyCheckinLabel}" style="background-image:url('${staticServePath}/images/activities/checkin.png')"></div>
                            <a class="title" href="<#if useCaptchaCheckin??>/activity/checkin<#else>/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a>
                        </li>
                        <li>
                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                 aria-label="${activityYesterdayLivenessRewardLabel}" style="background-image:url('${staticServePath}/images/activities/yesterday.png')"></div>
                            <a class="title" href="${servePath}/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a></li>
                        <li>
                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                 aria-label="${activity1A0001Label}" style="background-image:url('${staticServePath}/images/activities/1A0001.png')"></div>
                            <a class="title" href="${servePath}/activity/1A0001">${activity1A0001Label}</a></li>
                        <li>
                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                 aria-label="${characterLabel}" style="background-image:url('${staticServePath}/images/activities/char.png')"></div>
                            <a class="title" href="${servePath}/activity/character">${characterLabel}</a>
                        </li>
                        <li>
                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                 aria-label="${eatingSnakeLabel}" style="background-image:url('${staticServePath}/images/activities/snak.png')"></div>
                            <a class="title" href="${servePath}/activity/eating-snake">${eatingSnakeLabel}</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <#include "footer.ftl">     
</body>
</html>
