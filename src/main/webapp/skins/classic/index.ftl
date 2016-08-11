<#include "macro-head.ftl">
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
        <div class="index-wrap first">
            <div class="wrapper">
                <div class="slogan">
                    ${indexIntroLabel}
                    <a href="https://github.com/b3log/symphony" target="_blank"><svg height="18" version="1.1" viewBox="0 0 16 16" width="18">${githubIcon}</svg></a>
                    &nbsp;
                    <a target="_blank"
                       href="http://shang.qq.com/wpa/qunwpa?idkey=f77a54e7d2bd53bed4043f70838da92fa49eccda53e706ef2124943cb0df4df5">
                        <svg width="16" height="16" viewBox="0 0 28 28">${qqIcon}</svg></a>
                </div>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="item">
                    <div class="item-header" style="background-image: url(${recentBgIcon});">
                        <a href="${servePath}/recent">${latestLabel} <svg height="16" viewBox="0 0 14 16" width="14">${timeIcon}</svg></a>
                    </div>
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
                                <a rel="nofollow" class="title" href="${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="item mid">
                    <div class="item-header" style="background-image: url(${hotBgIcon});">
                        <a href="${servePath}/hot">${hotLabel} <svg height="16" viewBox="0 0 12 16" width="12">${hotIcon}</svg></a>
                    </div>
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
                                <a rel="nofollow" class="title" href="${article.articlePermalink}">${article.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="item">
                    <div class="item-header" style="background-image: url(${perfectBgIcon});">
                        <a href="/">${perfectLabel}</a>
                    </div>
                    <div>
                        Coming Soon!
                    </div>
                </div>
            </div>
        </div>
        <div class="index-wrap">
            <div class="wrapper domains fn-clear">
                <#list domains as domain>
                <a href="${servePath}/domain/${domain.domainURI}">${domain.domainIconPath}&nbsp;${domain.domainTitle}</a>
                </#list>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="item">
                    <div class="item-header" style="background-image: url(${timelineBgIcon});">
                        <a href="${servePath}/timeline">${timelineLabel} 
                            <svg height="14" viewBox="0 0 16 14" width="16">${timelineIcon}</svg></a>
                    </div>
                    <div>
                        Coming Soon!
                    </div>
                </div>
                <#if ADLabel != ''>
                <div class="item mid">
                    <div class="item-header" style="background-image: url(${adBgIcon})">
                        <a href="https://hacpai.com/article/1460083956075">${wantPutOnLabel}</a>
                    </div>
                    <div class="ad module-panel">
                        ${ADLabel}
                    </div>
                </div>
                </#if>
                <div class="item">
                    <div class="item-header" style="background-image: url(${activityBgIcon});">
                        <a href="${servePath}/pre-post">${postArticleLabel}</a>
                    </div>
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
        <div class="index-wrap last">
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
        <#include "footer.ftl">
    </body>
</html>
