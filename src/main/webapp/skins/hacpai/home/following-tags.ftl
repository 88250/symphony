<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingTags">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowingTags as followingTag>
        <li<#if followingTag_index % 2 = 1> class="even"</#if>>
            <#if "" != followingTag.tagIconPath>
            <a href="/tags/${followingTag.tagTitle?url('utf-8')}">
                <img title="${followingTag.tagTitle}" class="tag-img" src="${staticServePath}/images/tags/${followingTag.tagIconPath}">
            </a>
            <#else>
            <a class="icon icon-tags" href="/tags/${followingTag.tagTitle?url('utf-8')}"></a>
            </#if>
            <div>
                <h3>
                    <a href="/tags/${followingTag.tagTitle?url('utf-8')}">${followingTag.tagTitle}</a>
                </h3>

                <span class="ft-small">${referenceLabel}</span> ${followingTag.tagReferenceCount}
                <span class="ft-small">${cmtLabel}</span> ${followingTag.tagCommentCount} &nbsp;
                <br/>
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
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/following/tags"/>
</@home>