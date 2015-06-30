<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "points">
<b>TODO: Style</b>
<div class="list">
    <table>
        <thead>
            <tr>
                <th>${timeLabel}</th><th>${typeLabel}</th><th>${sumLabel}</th><th>${balanceLabel}</th><th>${descriptionLabel}</th>
            </tr>
        </thead>
        <tbody>
            <#list userHomePoints as point>
            <tr>
                <td>${point.createTime?string('yyyy-MM-dd HH:mm')}</td>
                <td>${point.displayType}</td>
                <td>${point.sum?c}</td>
                <td>${point.fromBalance?c}</td>
                <td>${point.description}</td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<@pagination url="/member/${user.userName}/points"/>
</@home>