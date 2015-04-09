<#include "macro-admin.ftl">
<@admin "misc">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>

        <div class="module-panel form fn-clear">
            <form action="/admin/misc" method="POST">
                <#list options as item>
                <label for="${item.oId}">${item.label}</label>
                <input type="text" id="${item.oId}" name="${item.oId}" value="${item.optionValue}" />

                </#list>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>
