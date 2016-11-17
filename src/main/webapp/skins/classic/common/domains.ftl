<#if domains?size != 0>
<div class="module domains-module">
    <div class="module-header">
        <h2>${domainNavLabel}</h2>
        <a href="${servePath}/domains" class="ft-gray fn-right">All Domains</a>
    </div>
    <div class="module-panel">
        <ul class="module-list domain">
            <#list domains as domain>
            <#if domain.domainTags?size gt 0>
            <li>
                <a rel="nofollow" class="slogan ft-a-title" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                <div class="title">
                    <#list domain.domainTags as tag>
                    <a class="ft-gray ft-13" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a> &nbsp;
                    </#list>
                </div>
            </li>
            </#if>
            </#list>
        </ul>
    </div>
</div>
</#if>