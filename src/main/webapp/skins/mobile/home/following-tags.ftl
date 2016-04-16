<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingTags">
<div class="follow">
    <ol>
        <#list userHomeFollowingTags as followingTag>
        <li class="fn-clear">
            <#if "" != followingTag.tagIconPath>
            <a href="/tag/${followingTag.tagTitle?url('utf-8')}">
                <div title="${followingTag.tagTitle}" class="avatar fn-left" style="background-image:url('${staticServePath}/images/tags/${followingTag.tagIconPath}')"></div>
            </a>
            <#else>
            <a class="icon-tags fn-left" href="/tag/${followingTag.tagTitle?url('utf-8')}"></a>
            </#if>
            <div class="fn-left">
                <h3 class="fn-inline">
                    <a href="/tag/${followingTag.tagTitle?url('utf-8')}">${followingTag.tagTitle}</a>
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
                    <span class="ft-gray">${referenceLabel}</span> ${followingTag.tagReferenceCount?c}
                    <span class="ft-gray">${cmtLabel}</span> ${followingTag.tagCommentCount?c} 
                </div>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/following/tags"/>
</@home>