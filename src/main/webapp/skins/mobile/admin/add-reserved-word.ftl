<#include "macro-admin.ftl">
<@admin "addReservedWord">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${allReservedWordLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/add-reserved-word" method="POST">
                <label>${reservedWordLabel}</label>
                <input name="word" type="text" />

                <br/><br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>   
</div>
</@admin>