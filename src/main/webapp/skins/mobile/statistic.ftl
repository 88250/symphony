<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${dataStatLabel} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content content-reset">
                    <h1>${dataStatLabel}</h1>
                    <i class="ft-gray">${dataStatSubLabel}</i>
                    <br><br>
                    <div id="chart30" style="height:400px"></div>
                    <br><br>
                    <div id="chartHistory" style="height:400px"></div>
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