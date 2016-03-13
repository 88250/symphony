<#include "macro-admin.ftl">
<@admin "domains">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${domain.oId}" readonly="readonly" />
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${tagLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <ul>
                <#list domain.domainTags as tag>
                <li>
                    <a class="btn small" target="_blank" href="/tags/${tag.tagTitle?url('UTF-8')}">${tag.tagTitle}</a>
                </li>
                </#list>
            </ul>

            <form method="POST" action="/admin/domain/${domain.oId}/add-tag">
                <label form="addTag">${addTagLabel}</label>
                <input type="text" name="tagTitle" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>

            <form method="POST" action="/admin/domain/${domain.oId}/remove-tag">
                <label form="addTag">${removeTagLabel}</label>
                <input type="text" name="tagTitle" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/domain/${domain.oId}" method="POST">
                <label for="domainTitle">${domainLabel}${updateCaseOnlyLabel}</label>
                <input type="text" id="domainTitle" name="domainTitle" value="${domain.domainTitle}" />

                <label for="domainDescription">${descriptionLabel}</label>
                <textarea rows="5" id="domainDescription" name="domainDescription">${domain.domainDescription}</textarea>

                <label for="domainIconPath">${iconPathLabel}</label>
                <input type="text" id="domainIconPath" name="domainIconPath" value="${domain.domainIconPath}" />

                <label>${statusLabel}</label>
                <select id="domainStatus" name="domainStatus">
                    <option value="0"<#if 0 == domain.domainStatus> selected</#if>>${validLabel}</option>
                    <option value="1"<#if 1 == domain.domainStatus> selected</#if>>${banLabel}</option>
                </select>

                <label for="domainSeoTitle">${seoTitleLabel}</label>
                <input type="text" id="domainSeoTitle" name="domainSeoTitle" value="${domain.domainSeoTitle}" />

                <label for="domainSeoKeywords">${seoKeywordsLabel}</label>
                <input type="text" id="domainSeoKeywords" name="domainSeoKeywords" value="${domain.domainSeoKeywords}" />

                <label for="domainSeoDesc">${seoDescLabel}</label>
                <input type="text" id="domainSeoDesc" name="domainSeoDesc" value="${domain.domainSeoDesc}" />

                <label for="domainSeoDesc">${seoDescLabel}</label>
                <input type="text" id="domainSeoDesc" name="domainSeoDesc" value="${domain.domainSeoDesc}" />

                <label for="domainCSS">CSS</label>
                <textarea type="text" rows="20" id="domainCSS" name="domainCSS">${domain.domainCSS}</textarea>

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>