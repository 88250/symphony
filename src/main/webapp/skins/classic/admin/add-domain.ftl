<#include "macro-admin.ftl">
<@admin "addDomain">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${addDomainLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/add-domain" method="POST">
                <label>${titleLabel}</label>
                <input name="domainTitle" type="text" />

                <br/><br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>   
</div>
</@admin>