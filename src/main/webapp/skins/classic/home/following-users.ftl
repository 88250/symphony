<#include "macro-home.ftl">
<@home "followingUsers">
<div class="follow">
<#list userHomeFollowingUsers as follower>
<img class="avatar" src="${follower.userThumbnailURL}"/>
<button class="red" onclick="Util.unfollow(this, '${follower.oId}')">${unfollowLabel}</button>
userName
userOnlineFlag
userArticleCount
userTagCount
userURL
userIntro   
</#list>
</div>
</@home>