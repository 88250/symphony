<#include "macro-admin.ftl">
<@admin "ad">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>

        <div class="module-panel form fn-clear">
            <form action="/admin/ad" method="POST">
                <label for="sideFullAd">${sideFullPosLabel}</label>
                <textarea rows="20" name="sideFullAd">${sideFullAd}</textarea>

                <br/><br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>
