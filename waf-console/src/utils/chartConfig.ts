import { CHART_COLORS } from './constants';

export const getBaseChartOptions = () => ({
  backgroundColor: 'transparent',
  textStyle: {
    color: '#ffffff'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
    backgroundColor: 'transparent'
  },
  tooltip: {
    backgroundColor: '#242c3a',
    borderColor: '#3a4553',
    textStyle: {
      color: '#ffffff'
    }
  },
  legend: {
    textStyle: {
      color: '#a8b2c1'
    }
  }
});

export const getTrafficChartOptions = (data: any[]) => ({
  ...getBaseChartOptions(),
  title: {
    text: '실시간 트래픽',
    textStyle: {
      color: '#ffffff',
      fontSize: 16
    }
  },
  xAxis: {
    type: 'category',
    data: data.map(d => new Date(d.timestamp).toLocaleTimeString()),
    axisLine: {
      lineStyle: {
        color: '#3a4553'
      }
    },
    axisLabel: {
      color: '#a8b2c1'
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: '#3a4553'
      }
    },
    axisLabel: {
      color: '#a8b2c1'
    },
    splitLine: {
      lineStyle: {
        color: '#3a4553'
      }
    }
  },
  series: [
    {
      name: '전체 요청',
      type: 'line',
      data: data.map(d => d.totalRequests),
      lineStyle: {
        color: CHART_COLORS.primary
      },
      itemStyle: {
        color: CHART_COLORS.primary
      },
      smooth: true
    },
    {
      name: '차단된 요청',
      type: 'line',
      data: data.map(d => d.blockedRequests),
      lineStyle: {
        color: CHART_COLORS.danger
      },
      itemStyle: {
        color: CHART_COLORS.danger
      },
      smooth: true
    },
    {
      name: '허용된 요청',
      type: 'line',
      data: data.map(d => d.allowedRequests),
      lineStyle: {
        color: CHART_COLORS.success
      },
      itemStyle: {
        color: CHART_COLORS.success
      },
      smooth: true
    }
  ]
});

export const getAttackTypesChartOptions = (data: any[]) => ({
  ...getBaseChartOptions(),
  title: {
    text: '공격 유형별 분포',
    textStyle: {
      color: '#ffffff',
      fontSize: 16
    }
  },
  series: [
    {
      name: '공격 유형',
      type: 'pie',
      radius: '60%',
      data: data.map(item => ({
        value: item.count,
        name: item.type,
        itemStyle: {
          color: getAttackTypeColor(item.type)
        }
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
});

export const getResponseTimeChartOptions = (data: any[]) => ({
  ...getBaseChartOptions(),
  title: {
    text: '응답 시간',
    textStyle: {
      color: '#ffffff',
      fontSize: 16
    }
  },
  xAxis: {
    type: 'category',
    data: data.map(d => new Date(d.timestamp).toLocaleTimeString()),
    axisLine: {
      lineStyle: {
        color: '#3a4553'
      }
    },
    axisLabel: {
      color: '#a8b2c1'
    }
  },
  yAxis: {
    type: 'value',
    name: 'ms',
    axisLine: {
      lineStyle: {
        color: '#3a4553'
      }
    },
    axisLabel: {
      color: '#a8b2c1'
    },
    splitLine: {
      lineStyle: {
        color: '#3a4553'
      }
    }
  },
  series: [
    {
      name: '응답 시간',
      type: 'bar',
      data: data.map(d => d.responseTime),
      itemStyle: {
        color: CHART_COLORS.info
      }
    }
  ]
});

const getAttackTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    'sql_injection': CHART_COLORS.danger,
    'xss': CHART_COLORS.warning,
    'file_upload': CHART_COLORS.info,
    'command_injection': CHART_COLORS.danger,
    'path_traversal': CHART_COLORS.warning,
    'other': CHART_COLORS.secondary
  };
  return colorMap[type] || CHART_COLORS.secondary;
};