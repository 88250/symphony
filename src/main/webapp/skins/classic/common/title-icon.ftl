<#macro icon perfect type>
<#if 1 == perfect>
    <span class="tooltipped tooltipped-e" aria-label="${perfectLabel}"><svg><use xlink:href="#perfect"></use></svg></span>
</#if>
<#if 1 == type>
    <span class="tooltipped tooltipped-e" aria-label="${discussionLabel}"><svg><use xlink:href="#locked"></use></svg></span>
<#elseif 2 == type>
    <span class="tooltipped tooltipped-e" aria-label="${cityBroadcastLabel}"><svg><use xlink:href="#feed"></use></svg></span>
<#elseif 3 == type>
    <span class="tooltipped tooltipped-e" aria-label="${thoughtLabel}"><svg><use xlink:href="#video"></use></svg></span>
</#if>
</#macro>