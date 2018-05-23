<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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
            <div class="fn-clear comment-info">
                <span class="fn-left ft-smaller">
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
                    <#if isLoggedIn && comment.commentAuthorName == currentUser.userName && permissions["commonRemoveComment"].permissionGrant>
                        <span onclick="Comment.remove('${comment.oId}')" aria-label="${removeCommentLabel}"
                              class="tooltipped tooltipped-n ft-a-title hover-show fn-hidden">
                         <svg class="ft-red"><use xlink:href="#remove"></use></svg></span>&nbsp;
                    </#if>
                    <#if permissions["commonViewCommentHistory"].permissionGrant>
                        <span onclick="Article.revision('${comment.oId}', 'comment')" aria-label="${historyLabel}"
                              class="tooltipped tooltipped-n ft-a-title hover-show fn-hidden
                          <#if comment.commentRevisionCount &lt; 2>fn-none</#if>">
                        <svg class="icon-history"><use xlink:href="#history"></use></svg></span> &nbsp;
                    </#if>
                    <#if isLoggedIn && comment.commentAuthorName == currentUser.userName && permissions["commonUpdateComment"].permissionGrant>
                        <span class="tooltipped tooltipped-n ft-a-title hover-show fn-hidden" onclick="Comment.edit('${comment.oId}')"
                           aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></span> &nbsp;
                    </#if>
                    <#if permissions["commentUpdateCommentBasic"].permissionGrant>
                    <a class="tooltipped tooltipped-n ft-a-title hover-show fn-hidden" href="${servePath}/admin/comment/${comment.oId}"
                       aria-label="${adminLabel}"><svg class="icon-setting"><use xlink:href="#setting"></use></svg></a> &nbsp;
                    </#if>
                    <#if comment.commentOriginalCommentId != ''>
                        <span class="fn-pointer ft-a-title tooltipped tooltipped-nw" aria-label="${goCommentLabel}"
                              onclick="Comment.showReply('${comment.commentOriginalCommentId}', this, 'comment-get-comment')"><svg class="icon-reply-to"><use xlink:href="#reply-to"></use></svg>
                        <div class="avatar-small" style="background-image:url('${comment.commentOriginalAuthorThumbnailURL}')"></div>
                    </span>
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
                            ${comment.commentReplyCnt} ${replyLabel} <svg class="icon-chevron-down fn-text-top"><use xlink:href="#chevron-down"></use></svg>
                        </span>
                    </#if>
                    <span class="fn-right fn-hidden hover-show action-btns">
                        <#assign hasRewarded = isLoggedIn && comment.commentAuthorId != currentUser.oId && comment.rewarded>
                        <span class="tooltipped tooltipped-n <#if hasRewarded>ft-red</#if>" aria-label="${thankLabel}"
                        <#if !hasRewarded && permissions["commonThankComment"].permissionGrant>
                            onclick="Comment.thank('${comment.oId}', '${csrfToken}', '${comment.commentThankLabel}', ${comment.commentAnonymous}, this)"
                        <#elseif !hasRewarded>
                              onclick="Article.permissionTip(Label.noPermissionLabel)"
                        </#if>><svg class="fn-text-top icon-heart"><use xlink:href="#heart"></use></svg> ${comment.rewardedCnt}</span> &nbsp;
                    <span class="tooltipped tooltipped-n<#if isLoggedIn && 0 == comment.commentVote> ft-red</#if>"
                          aria-label="${upLabel}"
                    <#if permissions["commonGoodComment"].permissionGrant>
                          onclick="Article.voteUp('${comment.oId}', 'comment', this)"
                        <#else>
                            onclick="Article.permissionTip(Label.noPermissionLabel)"
                    </#if>><svg class="icon-thumbs-up"><use xlink:href="#thumbs-up"></use></svg> ${comment.commentGoodCnt}</span> &nbsp;
                    <span class="tooltipped tooltipped-n<#if isLoggedIn && 1 == comment.commentVote> ft-red</#if>"
                          aria-label="${downLabel}"
                    <#if permissions["commonBadComment"].permissionGrant>
                          onclick="Article.voteDown('${comment.oId}', 'comment', this)"
                        <#else>
                            onclick="Article.permissionTip(Label.noPermissionLabel)"
                    </#if>><svg class="icon-thumbs-down"><use xlink:href="#thumbs-down"></use></svg> ${comment.commentBadCnt}</span> &nbsp;
                    <#if isLoggedIn && comment.commentAuthorName != currentUser.userName && permissions["commonAddComment"].permissionGrant>
                        <span aria-label="${replyLabel}" class="icon-reply-btn tooltipped tooltipped-n"
                              onclick="Comment.reply('${comment.commentAuthorName}', '${comment.oId}')">
                        <svg class="icon-reply"><use xlink:href="#reply"></use></svg></span>
                    </#if>
                    </span>
                </div>
                <div class="comment-replies list"></div>
            </div>
        </div>
    </div>
</li>