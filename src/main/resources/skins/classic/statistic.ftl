<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
    <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
    <link rel="canonical" href="${servePath}/statistic">
</head>
<body>
<#include "header.ftl">
<div class="main">
    <div class="wrapper">
        <div class="content">
            <div class="module">
                <h2 class="sub-head">${dataStatLabel}
                    <span class="ft-gray ft-13">${dataStatSubLabel}</span>
                </h2>
                <div class="fn-content vditor-reset">
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
                            <span class="ft-gray">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c}
                            &nbsp;
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

<script src="${staticServePath}/js/lib/vditor/dist/js/echarts/echarts.min.js"></script>
<script type="text/javascript">
  document.addEventListener('DOMContentLoaded', function () {
    echarts.init(document.getElementById('chart30')).setOption({
      title: {
        text: '${last30DaysLabel}',
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          lineStyle: {
            width: 0,
          },
        },
      },
      legend: {
        data: ['${statPostLabel}', '${statUserLabel}', '${statCmtLabel}', '${statCmtLabel}'],
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: [
            <#list monthDays as day>
            '${day}',
            </#list>],
          axisTick: {
            show: false,
          },
          axisLine: {
            show: false,
          },
        },
      ],
      yAxis: [
        {
          type: 'value',
          axisTick: {
            show: false,
          },
          axisLine: {
            show: false,
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(0, 0, 0, .38)',
              type: 'dashed',
            },
          },
        },
      ],
      series: [
        {
          name: '${statPostLabel}',
          type: 'line',
          smooth: true,
          itemStyle: {color: '#d23f31'},
          areaStyle: {normal: {}},
          z: 3,
          data: [
            <#list articleCnts as articleCnt>
            '${articleCnt?c}',
            </#list>],
        },
        {
          name: '${statUserLabel}',
          type: 'line',
          smooth: true,
          itemStyle: {color: '#f1e05a'},
          areaStyle: {normal: {}},
          z: 2,
          data: [
            <#list userCnts as userCnt>
            '${userCnt?c}',
            </#list>],
        },
        {
          name: '${statCmtLabel}',
          type: 'line',
          smooth: true,
          itemStyle: {color: '#4285f4'},
          areaStyle: {normal: {}},
          z: 1,
          data: [
            <#list commentCnts as commentCnt>
            '${commentCnt?c}',
            </#list>],
        }
      ],
    })

    echarts.init(document.getElementById('chartHistory')).setOption({
      title: {
        text: '${historyLabel}',
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          lineStyle: {
            width: 0,
          },
        },
      },
      legend: {
        data: ['${statPostLabel}', '${statUserLabel}', '${statCmtLabel}', '${statCmtLabel}'],
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: [
            <#list months as month>
            '${month}',
            </#list>],
          axisTick: {
            show: false,
          },
          axisLine: {
            show: false,
          },
        },
      ],
      yAxis: [
        {
          type: 'value',
          axisTick: {
            show: false,
          },
          axisLine: {
            show: false,
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(0, 0, 0, .38)',
              type: 'dashed',
            },
          },
        },
      ],
      series: [
        {
          name: '${statPostLabel}',
          type: 'line',
          smooth: true,
          itemStyle: {color: '#d23f31'},
          areaStyle: {normal: {}},
          z: 3,
          data: [
            <#list historyArticleCnts as articleCnt>
            '${articleCnt?c}',
            </#list>],
        },
        {
          name: '${statUserLabel}',
          type: 'line',
          smooth: true,
          itemStyle: {color: '#f1e05a'},
          areaStyle: {normal: {}},
          z: 2,
          data: [
            <#list historyUserCnts as userCnt>
            '${userCnt?c}',
            </#list>],
        },
        {
          name: '${statCmtLabel}',
          type: 'line',
          smooth: true,
          itemStyle: {color: '#4285f4'},
          areaStyle: {normal: {}},
          z: 1,
          data: [
            <#list historyCommentCnts as commentCnt>
            '${commentCnt?c}',
            </#list>],
        },
      ],
    })
  })
</script>
</body>
</html>
