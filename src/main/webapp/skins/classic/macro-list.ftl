<#macro list listData>
<div class="article-list list">
    <ul>
        <#assign articleIds = "">
        <#list listData as article>
        <#assign articleIds = articleIds + article.oId>
        <#if article_has_next><#assign articleIds = articleIds + ","></#if>
        <#include "common/list-item.ftl">
        </#list>
    </ul>
</div>
</#macro>
<#macro listScript>
<#if articleIds??>
<script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    // Init [Article List] channel
    ArticleListChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/article-list-channel?articleIds=${articleIds}");
</script>
</#if>
</#macro>
