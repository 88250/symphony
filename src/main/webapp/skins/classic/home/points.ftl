<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "pointtransfers">
<div class="list">
    <table border="1">
        <thead>
            <tr>
                <th>${timeLabel}</th><th>${typeLabel}</th><th>${sumLabel}</th><th>${balanceLabel}</th><th>${descriptionLabel}</th>
            </tr>
        </thead>
        <tbody>
            <#list userHomePoints as point>
            <tr>
                <th>${point.createTime?string('yyyy-MM-dd HH:mm')}</th>
                <th>${point.displayType}</th>
                <th>${point.sum?c}</th>
                <th>${point.fromBalance?c}</th>
                <th>${point.fromId} -> ${point.toId}</th>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<@pagination url="/member/${user.userName}/points"/>
</@home>