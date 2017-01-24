<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${dataStatLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/statistic">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module article-module">
                        <h2 class="sub-head">${dataStatLabel}
                            <span class="ft-gray ft-13">${dataStatSubLabel}</span>
                        </h2>
                        <div class="fn-m10 content-reset">
                        <br><br>
                        <div id="chart30" style="height:400px"></div>
                        <br><br>
                        <div id="chartHistory" style="height:400px"></div>
                        <hr>
                        <ul>
                            <li>
                                <span class="ft-gray">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt?c}
                            </li>
                            <li>
                                <span class="ft-gray">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c} &nbsp;
                            </li>
                            <li>
                                <span class="ft-gray">${memberLabel}</span> ${statistic.statisticMemberCount?c} &nbsp;
                            </li>
                            <li>
                                <span class="ft-gray">${articleLabel}</span> ${statistic.statisticArticleCount?c} &nbsp;
                            </li>
                            <li>
                                <span class="ft-gray">${cmtLabel}</span> ${statistic.statisticCmtCount?c} &nbsp;
                            </li>
                            <li>
                                <span class="ft-gray">${domainLabel}</span> ${statistic.statisticDomainCount?c} &nbsp;
                            </li>
                            <li>
                                <span class="ft-gray">${tagLabel}</span> ${statistic.statisticTagCount?c} 
                            </li>
                        </ul>
                    </div>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">

        <script src="${staticServePath}/js/lib/echarts-2.2.7/echarts.js"></script>
        <script type="text/javascript">
            require.config({
                paths: {
                    echarts: '${staticServePath}/js/lib/echarts-2.2.7'
                }
            });

            require(
                    [
                        'echarts',
                        'echarts/chart/line'
                    ],
                    function (ec) {
                        var fontFamily = '"Helvetica Neue", "Luxi Sans", "DejaVu Sans", Tahoma, "Hiragino Sans GB", "Microsoft Yahei", sans-serif';
                        
                        var chart30 = ec.init(document.getElementById('chart30'), 'infographic');
                        option30 = {
                            title: {
                                text: '${last30DaysLabel}',
                                textStyle: {
                                    fontFamily: fontFamily
                                },
                            },
                            tooltip: {
                                trigger: 'axis'
                            },
                            legend: {
                                data: ['${statUserLabel}', '${statPostLabel}', '${statCmtLabel}']
                            },
                            xAxis: [
                                {
                                    type: 'category',
                                    boundaryGap: false,
                                    data: [
                                        <#list monthDays as day>
                                        '${day}'<#if day?has_next>,</#if>
                                        </#list>
                                    ]
                                }
                            ],
                            yAxis: [
                                {
                                    type: 'value'
                                }
                            ],
                            series: [
                                {
                                    name: '${statUserLabel}',
                                    type: 'line',
                                    smooth: true,
                                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                    data: [
                                        <#list userCnts as userCnt>
                                        '${userCnt?c}'<#if userCnt?has_next>,</#if>
                                        </#list>
                                    ]
                                },
                                {
                                    name: '${statPostLabel}',
                                    type: 'line',
                                    smooth: true,
                                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                    data: [
                                        <#list articleCnts as articleCnt>
                                        '${articleCnt?c}'<#if articleCnt?has_next>,</#if>
                                        </#list>
                                    ]
                                },
                                {
                                    name: '${statCmtLabel}',
                                    type: 'line',
                                    smooth: true,
                                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                    data: [
                                        <#list commentCnts as commentCnt>
                                        '${commentCnt?c}'<#if commentCnt?has_next>,</#if>
                                        </#list>
                                    ]
                                }
                            ]
                        };

                        chart30.setOption(option30);
                        
                        var chartHistory = ec.init(document.getElementById('chartHistory'), 'infographic');
                        optionHistory = {
                            title: {
                                text: '${historyLabel}',
                                textStyle: {
                                    fontFamily: fontFamily
                                },
                            },
                            tooltip: {
                                trigger: 'axis'
                            },
                            legend: {
                                data: ['${statUserLabel}', '${statPostLabel}', '${statCmtLabel}']
                            },
                            xAxis: [
                                {
                                    type: 'category',
                                    boundaryGap: false,
                                    data: [
                                        <#list months as month>
                                        '${month}'<#if month?has_next>,</#if>
                                        </#list>
                                    ]
                                }
                            ],
                            yAxis: [
                                {
                                    type: 'value'
                                }
                            ],
                            series: [
                                {
                                    name: '${statUserLabel}',
                                    type: 'line',
                                    smooth: true,
                                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                    data: [
                                        <#list historyUserCnts as userCnt>
                                        '${userCnt?c}'<#if userCnt?has_next>,</#if>
                                        </#list>
                                    ]
                                },
                                {
                                    name: '${statPostLabel}',
                                    type: 'line',
                                    smooth: true,
                                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                    data: [
                                        <#list historyArticleCnts as articleCnt>
                                        '${articleCnt?c}'<#if articleCnt?has_next>,</#if>
                                        </#list>
                                    ]
                                },
                                {
                                    name: '${statCmtLabel}',
                                    type: 'line',
                                    smooth: true,
                                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                    data: [
                                        <#list historyCommentCnts as commentCnt>
                                        '${commentCnt?c}'<#if commentCnt?has_next>,</#if>
                                        </#list>
                                    ]
                                }
                            ]
                        };

                        chartHistory.setOption(optionHistory);
                    }
            );
        </script>
    </body>
</html>