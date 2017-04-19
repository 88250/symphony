<#macro list listData>
<div class="article-list list">
    <ul>
        <#assign articleIds = "">
        <#list listData as article>
        <#assign articleIds = articleIds + article.oId>
        <#if article_has_next><#assign articleIds = articleIds + ","></#if>
        <li<#if article.articleStickRemains gt 0 && articleStickCheck??> class="stick"</#if>>
            <div class="fn-flex">
                <#if article.articleAnonymous == 0>
                <a rel="nofollow" class="ft-gray"
                   href="${servePath}/member/${article.articleAuthorName}"
                   ></#if><div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>
                <div class="fn-flex-1 has-view">
                    <h2>
                        <#if 1 == article.articlePerfect>
                        <svg height="14" viewBox="2 1 11 12" width="12">${perfectIcon}</svg>
                        </#if>
                        <#if 1 == article.articleType>
                        <span class="icon-locked" title="${discussionLabel}"></span>
                        <#elseif 2 == article.articleType>
                        <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                        <#elseif 3 == article.articleType>
                        <span class="icon-video" title="${thoughtLabel}"></span>
                        </#if>
                        <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark"
                           href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                        </a>
                        <#if article.articleStick gt 0 && article.articleStick < 9223372036854775807>
                        <span class="ft-smaller ft-red stick-remains fn-none">${stickLabel}${remainsLabel} ${article.articleStickRemains?c} ${minuteLabel}</span>
                        </#if>
                    </h2>
                    <div class="ft-smaller">
                    <span class="ft-fade">${article.timeAgo}</span>
                    <#if "" != article.articleLatestCmterName>
                    <span class="ft-fade">•&nbsp;${latestCmtFromLabel}</span> 
                    <#if article.articleLatestCmterName != 'someone'>
                    <a rel="nofollow" class="ft-gray" href="${servePath}/member/${article.articleLatestCmterName}"><#else><span class="ft-gray"></#if>${article.articleLatestCmterName}<#if article.articleLatestCmterName != 'someone'></a><#else></span></#if>
                    </#if>
                    </div>
                </div>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-gray" href="${servePath}${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            <i class="heat" style="width:${article.articleHeat*3}px"></i>
        </li>
        </#list>
    </ul>
</div>
</#macro>
<#macro listScript>
<#if articleIds??>
<script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    // Init [Article List] channel
    ArticleListChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/article-list-channel?articleIds=${articleIds}");
</script>
</#if>
</#macro>