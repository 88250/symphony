<#include "macro-admin.ftl">
    <@admin "roles">
    <div class="content admin">
        <div class="module list">
            <div class="form fn-clear">
                <button class="green fn-right">Save</button>
            </div>
            <ul>
                <#list permissionCategories?keys as category>
                    <li class="last">
                        <div class="fn-clear">
                            <h2 class="fn-left">${category}</h2>
                            <div class="fn-right">
                                <button class="mid">全选</button>
                                &nbsp;
                                <button class="mid">反选</button>
                            </div>
                        </div>
                        <#list permissionCategories[category] as permission>
                            <label><input type="checkbox" <#if permission.permissionGrant>checked</#if>>${permission.permissionLabel}</label><br>
                        </#list>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
</@admin>