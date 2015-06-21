<#macro list listData>
<div class="article-list list">
    <ul>
        <#assign articleIds = "">
        <#list listData as article>
        <#assign articleIds = articleIds + article.oId>
        <#if article_has_next><#assign articleIds = articleIds + ","></#if>
        <li>
            <div class="fn-clear">
                <a rel="nofollow"
                   href="/member/${article.articleAuthorName}" 
                   title="${article.articleAuthorName}"><img class="avatar fn-left" src="${article.articleAuthorThumbnailURL}" /></a>
                <div class="fn-left list-content">
                    <h2><a data-id="${article.oId}" rel="bookmark" href="${article.articlePermalink}">${article.articleTitleEmoj}</a></h2>
                    <span class="ft-small">
                        <span class="icon icon-tags"></span>
                        <#list article.articleTags?split(",") as articleTag>
                        <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                            ${articleTag}</a><#if articleTag_has_next>, </#if>
                        </#list>
                        &nbsp; <span class="icon icon-date"></span> ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="ft-small cmts">
                <span class="icon icon-cmts"></span>
                ${article.articleCommentCount}
            </div>
            </#if>
            <div class="commenters">
                <#list article.articleParticipants as comment>
                <a rel="nofollow" href="${article.articlePermalink}#${comment.commentId}" title="${comment.articleParticipantName}">
                    <img class="avatar-small" src="${comment.articleParticipantThumbnailURL}" />
                </a>
                </#list>
            </div>
            <i class="heat" style="width:${article.articleHeat*3}px"></i>
        </li>
        </#list>
    </ul>
</div>

<script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/swfobject.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/web_socket.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/reconnecting-websocket.min.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    WEB_SOCKET_SWF_LOCATION = "${staticServePath}/js/lib/ws-flash/WebSocketMain.swf";
    
    // Init [Article List] channel
    ArticleListChannel.init("ws://${serverHost}:${serverPort}/article-list-channel?articleIds=${articleIds}");
</script>
</#macro>