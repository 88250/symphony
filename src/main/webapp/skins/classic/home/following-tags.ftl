<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers"> class="current"</#if>>${watchingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
    <a href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel}</a>
</div>
<#if 0 == user.userFollowingTagStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow list">
    <#if userHomeFollowingTags?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul>
        <#list userHomeFollowingTags as followingTag>
        <li<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && followingTag_index == userHomeFollowingTags?size - 1>
             class="last"</#if>>
            <div class="fn-flex">
                <#if "" != followingTag.tagIconPath>
                <a href="${servePath}/tag/${followingTag.tagURI}">
                    <div class="avatar fn-left ft-gray tooltipped tooltipped-se"  
                         aria-label="${followingTag.tagTitle}" style="background-image:url('${staticServePath}/images/tags/${followingTag.tagIconPath}')"></div>
                </a>
                <#else>
                <a href="${servePath}/tag/${followingTag.tagURI}"
                   class="tooltipped tooltipped-se fn-left ft-a-title" aria-label="${followingTag.tagTitle}"><div class="icon-tags"></div></a>
                </#if>
                <div class="fn-flex-1">
                    <h2 class="fn-inline">
                        <a href="${servePath}/tag/${followingTag.tagURI}">${followingTag.tagTitle}</a>
                    </h2>
                    &nbsp;
                    <#if isLoggedIn> 
                    <#if followingTag.isFollowing>
                    <button class="fn-right mid" onclick="Util.unfollow(this, '${followingTag.oId}', 'tag')"> 
                        ${unfollowLabel}
                    </button>
                    <#else> 
                    <button class="fn-right mid" onclick="Util.follow(this, '${followingTag.oId}', 'tag')"> 
                        ${followLabel}
                    </button>
                    </#if>
                    </#if>
                    <div>
                        <span class="ft-gray">${referenceLabel}</span> ${followingTag.tagReferenceCount?c} &nbsp;
                        <span class="ft-gray">${cmtLabel}</span> ${followingTag.tagCommentCount?c} 
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/tags"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>