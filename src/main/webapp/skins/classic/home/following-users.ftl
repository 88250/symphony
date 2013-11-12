<#include "macro-home.ftl">
<@home "followingUsers">
<#list userHomeFollowingUsers as follower>
${follower}
</#list>
</@home>