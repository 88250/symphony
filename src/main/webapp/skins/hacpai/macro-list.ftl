<#macro list listData>
<div class="article-list list">
    <ul>
        <#assign articleIds = "">
        <#list listData as article>
        <#assign articleIds = articleIds + article.oId>
        <#if article_has_next><#assign articleIds = articleIds + ","></#if>
        <li>
            <div class="fn-flex">
                <a rel="nofollow" class="ft-small"
                   href="/member/${article.articleAuthorName}" 
                   title="${article.articleAuthorName}"><img class="avatar responsive-hide" src="${article.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80" /></a>
                <div class="fn-flex-1 has-view">
                    <h2>
                        <a rel="nofollow" class="ft-small"
                           href="/member/${article.articleAuthorName}" 
                           title="${article.articleAuthorName}">
                            <img class="avatar-small responsive-show" src="${article.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80" />
                        </a>
                        <a data-id="${article.oId}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a></h2>
                    <span class="ft-small">
                        <span class="icon icon-tags"></span>
                        <#list article.articleTags?split(",") as articleTag>
                        <a rel="tag" href="/tag/${articleTag?url('UTF-8')}">
                            ${articleTag}</a><#if articleTag_has_next>, </#if>
                        </#list>
                        &nbsp; <span class="icon icon-date"></span> ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-small" href="${servePath}${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            <div class="commenters">
                <#list article.articleParticipants as comment>
                <a rel="nofollow" href="${servePath}${article.articlePermalink}#${comment.commentId}" title="${comment.articleParticipantName}">
                    <img class="avatar-small" src="${comment.articleParticipantThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80" />
                </a>
                </#list>
            </div>
            <i class="heat" style="width:${article.articleHeat*3}px"></i>
        </li>
        </#list>
    </ul>
</div>

<script src="${staticServePath}/js/lib/ws-flash/swfobject.js"></script>
<script src="${staticServePath}/js/lib/ws-flash/web_socket.js"></script>
<script src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
<script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    WEB_SOCKET_SWF_LOCATION = "${staticServePath}/js/lib/ws-flash/WebSocketMain.swf";

    // Init [Article List] channel
    ArticleListChannel.init("${wsScheme}://${serverHost}:${serverPort}/article-list-channel?articleIds=${articleIds}");
</script>
</#macro>