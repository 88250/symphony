<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel}</a>
</div>
<#if 0 == user.userFollowingTagStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow">
    <ol>
        <#list userHomeFollowingTags as followingTag>
        <li class="fn-clear">
            <#if "" != followingTag.tagIconPath>
            <a href="${servePath}/tag/${followingTag.tagTitle?url('utf-8')}">
                <div class="avatar fn-left ft-gray tooltipped tooltipped-se"  
                   aria-label="${followingTag.tagTitle}" style="background-image:url('${staticServePath}/images/tags/${followingTag.tagIconPath}')"></div>
            </a>
            <#else>
            <a href="${servePath}/tag/${followingTag.tagTitle?url('utf-8')}"
               class="tooltipped tooltipped-se fn-left ft-a-icon" aria-label="${followingTag.tagTitle}"><div class="icon-tags"  
                   ></div></a>
            </#if>
            <div class="fn-left">
                <h3 class="fn-inline">
                    <a href="${servePath}/tag/${followingTag.tagTitle?url('utf-8')}">${followingTag.tagTitle}</a>
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
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>