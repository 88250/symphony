<#include "macro-notifications.ftl">
<@notifications "point">
<#if pointNotifications?size != 0>
<ul class="notification">
    <#list pointNotifications as notification>
    <li class="<#if notification.hasRead>read</#if>">
        ${notification.description} <span class="ft-gray ft-nowrap"> • ${notification.createTime?string('yyyy-MM-dd HH:mm')}</span>
    </li>
    </#list>
</ul>
<#else>
<div class="fn-hr10"></div>
<div class="ft-center">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/point"/></@notifications>