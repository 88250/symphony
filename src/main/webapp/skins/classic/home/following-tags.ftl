<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingTags">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowingTags as followingTag>
        <li class="fn-flex">
            <#if "" != followingTag.tagIconPath>
            <a href="/tags/${followingTag.tagTitle?url('utf-8')}">
                <img title="${followingTag.tagTitle}" class="tag-img" src="${staticServePath}/images/tags/${followingTag.tagIconPath}">
            </a>
            <#else>
            <a class="icon icon-tags" href="/tags/${followingTag.tagTitle?url('utf-8')}"></a>
            </#if>
            <div class="fn-flex-1">
                <h3 class="fn-inline">
                    <a href="/tags/${followingTag.tagTitle?url('utf-8')}">${followingTag.tagTitle}</a>
                </h3>
                &nbsp;
                <#if isLoggedIn> 
                <#if followingTag.isFollowing>
                <button class="red small" onclick="Util.unfollow(this, '${followingTag.oId}', 'tag')"> 
                    ${unfollowLabel}
                </button>
                <#else>
                <button class="green small" onclick="Util.follow(this, '${followingTag.oId}', 'tag')"> 
                    ${followLabel}
                </button>
                </#if>
                </#if>
                <div>
                    <span class="ft-small">${referenceLabel}</span> ${followingTag.tagReferenceCount?c}
                    <span class="ft-small">${cmtLabel}</span> ${followingTag.tagCommentCount?c} 
                </div>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/following/tags"/>
</@home>