<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${latestLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <#if 0 < stickArticles?size> 
                    <div class="module">
                        <div class="article-list list">
                            <ul class="stick">
                                <#list stickArticles as article>
                                <li>
                                    <div class="fn-clear ft-smaller list-info ">
                                        <span class="fn-left">
                                            <#list article.articleTagObjs as articleTag>
                                            <a rel="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp; 
                                            </#list>
                                        </span>
                                        <span class="fn-right ft-fade">
                                            <#if article.articleCommentCount != 0>
                                            <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><b class="level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a> &nbsp;•&nbsp;
                                            </#if>   

                                            <#if article.articleViewCount != 0> 
                                            <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>">${article.articleViewCount}</span> ${viewLabel}</a> &nbsp;•&nbsp;
                                            </#if>   
                                            <span class="ft-fade">${article.timeAgo} </span>
                                        </span>
                                    </div>
                                    <h2>
                                        <#if 1 == article.articlePerfect>
                                        <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" width="14" viewBox="3 2 11 12">${perfectIcon}</svg></span>
                                        </#if>
                                        <#if 1 == article.articleType>
                                        <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                                        <#elseif 2 == article.articleType>
                                        <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                                        <#elseif 3 == article.articleType>
                                        <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                                        </#if>
                                        <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                                        </a>

                                        <#if articleStickCheck?? && article.articleStick < 9223372036854775807>
                                            <span class="ft-smaller ft-red stick-remains fn-none">${stickLabel}${remainsLabel} ${article.articleStickRemains?c} ${minuteLabel}</span>
                                        </#if>
                                    </h2>
                                    <div class="ft-smaller fn-clear list-info fn-flex">
                                        <span class="fn-ellipsis fn-flex-1">
                                            <#if article.articleAnonymous == 0>
                                            <a rel="nofollow" 
                                               href="${servePath}/member/${article.articleAuthorName}"></#if><div
                                               class="avatar-small"
                                               style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>&nbsp;
                                            <#if article.articleAnonymous == 0>
                                            <a rel="nofollow" class="author"
                                               href="${servePath}/member/${article.articleAuthorName}"></#if>
                                               ${article.articleAuthorName}
                                            <#if article.articleAnonymous == 0></a></#if>
                                            <#if article.articleAuthor.userIntro != ''><span class="ft-fade"> - ${article.articleAuthor.userIntro}</span></a></#if>
                                        </span>

                                        <span class="fn-right ft-fade fn-hidden">
                                            <#if "" != article.articleLatestCmterName>
                                                &nbsp; ${article.cmtTimeAgo} 
                                                <#if "" != article.articleLatestCmt.clientCommentId>
                                                    <span class="author">${article.articleLatestCmterName}</span>
                                                <#else>
                                                    <#if article.articleLatestCmterName != 'someone'>
                                                    <a rel="nofollow" class="author" href="${servePath}/member/${article.articleLatestCmterName}"></#if><span class="author">${article.articleLatestCmterName}</span><#if article.articleLatestCmterName != 'someone'></a>
                                                    </#if>
                                                </#if> 
                                                ${cmtLabel}
                                            </#if>
                                        </span>
                                    </div>
                                    <a class="abstract" href="${servePath}${article.articlePermalink}">
                                        ${article.articlePreviewContent}
                                    </a>
                                    <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#if>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right ft-fade">
                                <a class="<#if "" == current>ft-gray</#if>" href="${servePath}/recent">${defaultLabel}</a>
                                /
                                <a class="<#if "/hot" == current>ft-gray</#if>" href="${servePath}/recent/hot">${hotArticlesLabel}</a>
                                /
                                <a class="<#if "/good" == current>ft-gray</#if>" href="${servePath}/recent/good">${goodCmtsLabel}</a>
                                /
                                <a class="<#if "/reply" == current>ft-gray</#if>" href="${servePath}/recent/reply">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=latestArticles/>
                        <@pagination url="${servePath}/recent${current}"/>
                    </div>
                    <#include "common/domains.ftl">
                </div>

                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
