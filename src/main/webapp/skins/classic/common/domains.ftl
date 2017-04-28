<#if domains?size != 0>
<div class="main__down">
    <div class="wrapper">
    <div class="module domains-module">
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
    </div>
</div>
</#if>