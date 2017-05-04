<li>
    <div class="fn-clear ft-smaller list-info">
        <#list article.articleTagObjs as articleTag>
        <a rel="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp;
        </#list>

        <span class="fn-right ft-fade">
            <#if article.articleCommentCount != 0>
                <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><b class="article-level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a> &nbsp;•&nbsp;
            </#if>

            <#if article.articleViewCount != 0>
                <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="article-level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>">${article.articleViewCount}</span> ${viewLabel}</a>
            </#if>
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
        <a class="ft-a-title" data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
        </a>
    </h2>
    <div class="fn-flex">
        <div class="fn-flex-1">
            <div class="fn-flex">
                <#if article.articleAnonymous == 0>
                    <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if><div
                    class="avatar"
                    style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>

                <div class="fn-ellipsis ft-fade ft-smaller list-info">
                    <#if article.articleAnonymous == 0>
                        <a rel="nofollow" class="author"
                           href="${servePath}/member/${article.articleAuthorName}"></#if>
                    ${article.articleAuthorName}
                    <#if article.articleAnonymous == 0></a></#if>

                    <#if article.articleAuthor.userIntro != '' && article.articleAnonymous == 0>
                        - ${article.articleAuthor.userIntro}
                    </#if>
                    <br>
                    <#if "" != article.articleLatestCmterName>
                        <#if "" != article.articleLatestCmt.clientCommentId>
                            <span class="author">${article.articleLatestCmterName}</span>
                        <#else>
                            <#if article.articleLatestCmterName != 'someone'>
                                    <a rel="nofollow" class="author" href="${servePath}/member/${article.articleLatestCmterName}"></#if><span class="author">${article.articleLatestCmterName}</span><#if article.articleLatestCmterName != 'someone'></a>
                            </#if>
                        </#if>
                        ${article.cmtTimeAgo}${cmtLabel}
                    </#if>
                </div>
            </div>

            <a class="abstract" href="${servePath}${article.articlePermalink}">
                ${article.articlePreviewContent}
            </a>
        </div>
        <#if "" != article.articleThumbnailURL>
            <a href="${servePath}${article.articlePermalink}" class="abstract-img" style="background-image:url('${article.articleThumbnailURL}')"></a>
        </#if>
    </div>

    <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>

    <#if article.articleStick gt 0>
        <span class="cb-stick tooltipped tooltipped-e" aria-label="<#if article.articleStick < 9223372036854775807>${stickLabel}${remainsLabel} ${article.articleStickRemains?c} ${minuteLabel}<#else>${adminLabel}${stickLabel}</#if>"><span class="icon-pin"></span></span>
    </#if>
</li>