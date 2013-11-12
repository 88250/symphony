<#include "macro-home.ftl">
<@home "followers">
<#list userHomeFollowerUsers as follower>
${follower}
</#list>
<@pagination url="/member/${user.userName}"/>
</@home>