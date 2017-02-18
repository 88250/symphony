<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers"> class="current"</#if>>${watchingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
    <a href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel}</a>
</div>
<#if 0 == user.userFollowingUserStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow list">
    <#if userHomeFollowingUsers?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul>
        <#list userHomeFollowingUsers as followingUser>
        <li<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && followingUser_index == userHomeFollowingUsers?size - 1>
            class="last"</#if>>
            <div class="fn-flex">
                <a rel="nofollow ft-gray"  
                   href="${servePath}/member/${followingUser.userName}">
                    <div class="avatar fn-left tooltipped tooltipped-se" 
                         aria-label="${followingUser.userName} <#if followingUser.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" 
                         style="background-image:url('${followingUser.userAvatarURL}')"></div>
                </a>
                <div class="fn-flex-1">
                    <h2 class="fn-inline">
                        <a rel="nofollow" href="${servePath}/member/${followingUser.userName}" ><#if followingUser.userNickname != ''>${followingUser.userNickname}<#else>${followingUser.userName}</#if></a>
                    </h2>
                    <#if followingUser.userNickname != ''>
                    <a class='ft-fade' rel="nofollow" href="${servePath}/member/${followingUser.userName}" >${followingUser.userName}</a>
                    </#if>
                    <#if isLoggedIn && (userName != followingUser.userName)> 
                    <#if followingUser.isFollowing>
                    <button class="fn-right mid" onclick="Util.unfollow(this, '${followingUser.oId}', 'user')"> 
                        ${unfollowLabel}
                    </button>
                    <#else>
                    <button class="fn-right mid" onclick="Util.follow(this, '${followingUser.oId}', 'user')"> 
                        ${followLabel}
                    </button>
                    </#if>
                    </#if>
                    <div>
                        <#if followingUser.userArticleCount == 0>
                        <#if followingUser.userURL != "">
                        <a class="ft-gray" target="_blank" rel="friend" href="${followingUser.userURL?html}">${followingUser.userURL?html}</a>
                        <#else>
                        <span class="ft-gray">${symphonyLabel}</span>
                        ${followingUser.userNo?c}
                        <span class="ft-gray">${numVIPLabel}</span>
                        </#if>
                        <#else>
                        <span class="ft-gray">${articleLabel}</span> ${followingUser.userArticleCount?c} &nbsp;
                        <span class="ft-gray">${tagLabel}</span> ${followingUser.userTagCount?c}
                        </#if>
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/users"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>