<#include "macro-notifications.ftl">
<@notifications "point">
<#if pointNotifications?size != 0>
<ul class="notification">
    <#list pointNotifications as notification>
    <li class="<#if notification.hasRead>read</#if>">
        ${notification.description}
        <span class="fn-right ft-gray">${notification.createTime?string('yyyy-MM-dd HH:mm')}</span>
    </li>
    </#list>
</ul>
<#else>
<div class="no-list">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/point"/></@notifications>