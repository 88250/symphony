<#include "macro-head.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body class="index">
        ${HeaderBannerLabel}
        <#include "header.ftl">
        <@subNav '' ''/>
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="item mid">
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
                    <a href="${servePath}/perfect" class="item-header" style="background-image: url(${perfectBgIcon});">${perfectLabel}</a>
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
                        <a rel="nofollow" href="${servePath}/tag/${tag.tagURI}">
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
                        <li><a class="title" href="<#if useCaptchaCheckin??>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a></li>
                        <li><a class="title" href="${servePath}/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a></li>
                        <li><a class="title" href="${servePath}/activity/1A0001">${activity1A0001Label}</a></li>
                        <li><a class="title" href="${servePath}/activity/character">${characterLabel}</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="slogan">
        ${indexIntroLabel}&nbsp;
        <a href="https://github.com/b3log/symphony" target="_blank">
            <svg class="ft-gray" height="16" width="16" viewBox="0 0 16 16">${githubIcon}</svg></a>
        <a href="http://weibo.com/u/2778228501" target="_blank">
            <svg class="ft-gray" width="18" height="18" viewBox="0 0 37 30">${weiboIcon}</svg></a>    
        <a target="_blank"
           href="http://shang.qq.com/wpa/qunwpa?idkey=981d9282616274abb1752336e21b8036828f715a1c4d0628adcf208f2fd54f3a">
            <svg class="ft-gray" width="16" height="16" viewBox="0 0 30 30">${qqIcon}</svg></a>
    </div>
    <#include "footer.ftl">
</body>
</html>