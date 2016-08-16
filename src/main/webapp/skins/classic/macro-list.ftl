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
                <a rel="nofollow" 
                   href="${servePath}/member/${article.articleAuthorName}"></#if><div
                   class="avatar ft-gray tooltipped tooltipped-se"  
                   aria-label="${article.articleAuthorName}"
                   style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>
                <div class="fn-flex-1 has-view">
                    <h2>
                        <#if 1 == article.articlePerfect>
                        <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" width="14" viewBox="3 3 11 12">${perfectIcon}</svg></span>
                        </#if>
                        <#if 1 == article.articleType>
                        <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                        <#elseif 2 == article.articleType>
                        <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                        <#elseif 3 == article.articleType>
                        <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                        </#if>
                        <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark"
                           href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                        </a>
                        <#if articleStickCheck??>
                        <#if article.articleStick < 9223372036854775807>
                        <span class="ft-smaller ft-red stick-remains fn-none">${stickLabel}${remainsLabel} ${article.articleStickRemains?c} ${minuteLabel}</span>
                        </#if>
                        </#if>
                    </h2>
                    <#list article.articleTags?split(",") as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag?url('UTF-8')}">${articleTag}</a>
                    </#list>
                    <span class="ft-fade ft-smaller">&nbsp;•&nbsp;${article.timeAgo}</span>
                    <#if "" != article.articleLatestCmterName>
                    <#if article.syncWithSymphonyClient>
                    <span class="ft-fade ft-smaller">•&nbsp;${latestCmtFromLabel}</span> <span class="ft-gray">${article.articleLatestCmterName}</span>
                    <#else>
                    <span class="ft-fade ft-smaller">•&nbsp;${latestCmtFromLabel}</span> 
                    <#if article.articleLatestCmterName != 'someone'><a rel="nofollow" class="ft-gray" href="${servePath}/member/${article.articleLatestCmterName}"><#else><span class="ft-gray"></#if>${article.articleLatestCmterName}<#if article.articleLatestCmterName != 'someone'></a><#else></span></#if>
                    </#if>
                    </#if>
                </div>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="cmts tooltipped tooltipped-w" aria-label="${cmtLabel}${quantityLabel}">
                <a class="count ft-gray" href="${servePath}${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            <i class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></i>
        </li>
        </#list>
    </ul>
</div>
</#macro>
<#macro listScript>
<#if articleIds??>
<script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    // Init [Article List] channel
    ArticleListChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/article-list-channel?articleIds=${articleIds}");
</script>
</#if>
</#macro>