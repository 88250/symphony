<li id="${comment.oId}"
    class="<#if comment.commentStatus == 1>cmt-shield</#if><#if comment.commentNice> cmt-perfect</#if><#if comment.commentReplyCnt != 0> cmt-selected</#if>">
    <div class="fn-flex">
        <#if !comment.fromClient>
            <div>
                <#if comment.commentAnonymous == 0>
                    <a rel="nofollow" href="${servePath}/member/${comment.commentAuthorName}"></#if>
                <div class="avatar tooltipped tooltipped-se"
                     aria-label="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}')"></div>
                <#if comment.commentAnonymous == 0></a></#if>
            </div>
            <#else>
                <div class="avatar tooltipped tooltipped-se"
                     aria-label="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}')"></div>
        </#if>
        <div class="fn-flex-1">
            <div class="comment-get-comment list"></div>
            <div class="fn-clear comment-info ft-smaller">
                <span class="fn-left">
                    <#if !comment.fromClient>
                    <#if comment.commentAnonymous == 0><a rel="nofollow" href="${servePath}/member/${comment.commentAuthorName}" class="ft-gray"></#if><span class="ft-gray">${comment.commentAuthorName}</span><#if comment.commentAnonymous == 0></a></#if>
                    <#else><span class="ft-gray">${comment.commentAuthorName}</span>
                    <span class="ft-fade"> • </span>
                    <a rel="nofollow" class="ft-green" href="https://hacpai.com/article/1457158841475">API</a>
                    </#if>
                    <span class="ft-fade">• ${comment.timeAgo}</span>
                    <#if 0 == comment.commenter.userUAStatus><span class="cmt-via ft-fade hover-show fn-hidden" data-ua="${comment.commentUA}"></span></#if>
                </span>
                <span class="fn-right">
                    <#if comment.commentOriginalCommentId != ''>
                    <span class="fn-pointer ft-fade tooltipped tooltipped-nw" aria-label="${goCommentLabel}"
                          onclick="Comment.showReply('${comment.commentOriginalCommentId}', this, 'comment-get-comment')"><span class="icon-reply-to"></span>
                        <div class="avatar-small" style="background-image:url('${comment.commentOriginalAuthorThumbnailURL}')"></div>
                    </span>
                    </#if>
                    <#if permissions["commentUpdateCommentBasic"].permissionGrant>
                    <a class="tooltipped tooltipped-n ft-a-title hover-show fn-hidden" href="${servePath}/admin/comment/${comment.oId}"
                       aria-label="${adminLabel}"><span class="icon-setting"></span></a>
                    </#if>
                </span>
            </div>
            <div class="content-reset comment">
                ${comment.commentContent}
            </div>
            <div class="comment-action">
                <div class="ft-fade fn-clear">
                    <#if comment.commentReplyCnt != 0>
                        <span class="fn-pointer ft-smaller fn-left" onclick="Comment.showReply('${comment.oId}', this, 'comment-replies')">
                            ${comment.commentReplyCnt} ${replyLabel} <span class="icon-chevron-down"></span>
                        </span>
                    </#if>
                    <span class="fn-right fn-hidden hover-show action-btns">
                        <#assign hasRewarded = isLoggedIn && comment.commentAuthorId != currentUser.oId && comment.rewarded>
                        <span class="tooltipped tooltipped-n <#if hasRewarded>ft-red</#if>" aria-label="${thankLabel}"
                        <#if !hasRewarded && permissions["commonThankComment"].permissionGrant>
                            onclick="Comment.thank('${comment.oId}', '${csrfToken}', '${comment.commentThankLabel}', ${comment.commentAnonymous}, this)"
                        </#if>><span class="icon-heart"></span> ${comment.rewardedCnt}</span> &nbsp;
                    <span class="tooltipped tooltipped-n<#if isLoggedIn && 0 == comment.commentVote> ft-red</#if>"
                          aria-label="${upLabel}"
                    <#if permissions["commonGoodComment"].permissionGrant>
                          onclick="Article.voteUp('${comment.oId}', 'comment', this)"
                    </#if>><span class="icon-thumbs-up"></span> ${comment.commentGoodCnt}</span> &nbsp;
                    <span class="tooltipped tooltipped-n<#if isLoggedIn && 1 == comment.commentVote> ft-red</#if>"
                          aria-label="${downLabel}"
                    <#if permissions["commonBadComment"].permissionGrant>
                          onclick="Article.voteDown('${comment.oId}', 'comment', this)"
                    </#if>><span class="icon-thumbs-down"></span> ${comment.commentBadCnt}</span> &nbsp;
                    <#if comment.commentAuthorName != currentUser.userName && permissions["commonAddComment"].permissionGrant>
                        <span aria-label="${replyLabel}" class="icon-reply-btn tooltipped tooltipped-n"
                              onclick="Comment.reply('${comment.commentAuthorName}', '${comment.oId}')">
                        <span class="icon-reply"></span></span>
                    </#if>
                    </span>
                </div>
                <div class="comment-replies list"></div>
            </div>
        </div>
    </div>
</li>