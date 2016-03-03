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
                <label>${item.label}</label>
                <select id="${item.oId}" name="${item.oId}">
                    <option value="0"<#if "0" == item.optionValue> selected</#if>>${yesLabel}</option>
                    <option value="1"<#if "1" == item.optionValue> selected</#if>>${noLabel}</option>
                </select>
                </#list>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>
