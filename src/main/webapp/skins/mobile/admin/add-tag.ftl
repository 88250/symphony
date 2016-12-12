<#include "macro-admin.ftl">
<@admin "addTag">
<div class="wrapper">
    <div class="module">
        <div class="module-header">
            <h2>${addTagLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/add-tag" method="POST">
                <label for="tagTitle">${tagLabel}</label>
                <input type="text" name="tagTitle" />

                <br/><br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>   
</div>
</@admin>