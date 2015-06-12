<#include "macro-notifications.ftl">
<@notifications "followingUser">
<#if followingUserNotifications?size != 0>
<ul class="notification">
    <#list followingUserNotifications as notification>
    <li class="fn-clear<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}">
            <img class="avatar fn-left" src="${notification.thumbnailURL}"/>
        </a>
        <div class="list-content fn-left">
            <h2> <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a></h2>
            <div class="comment">
                <p class="ft-small">
                    <span class="icon icon-tags"></span>
                    <#list notification.articleTags?split(",") as articleTag>
                    <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                        ${articleTag}</a><#if articleTag_has_next>, </#if>
                    </#list> &nbsp;
                    <span class="icon icon-date"></span>
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </p>
            </div> 
        </div>
        <#if notification.articleCommentCount != 0>
        <div class="ft-small cmts">
             <span class="icon icon-cmts"></span>
            ${notification.articleCommentCount}
            ${cmtLabel}
        </div>
        </#if>
    </li>
    </#list>
</ul>
<#else>
${noMessageLabel}
</#if>

<@pagination url="/notifications/following-user"/>
</@notifications>