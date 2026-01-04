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

export const getSecurityTrafficChartOptions = (data: any[], maxBlockRate: number = 100) => {
  const timestamps = data.map(d => new Date(d.timestamp).toLocaleTimeString());

  // 차단율 최대값을 데이터 기반으로 계산 (최소 20%, 최대 100%)
  const actualMaxBlockRate = Math.max(
    20,
    Math.min(100, Math.ceil(maxBlockRate / 10) * 10 + 10)
  );

  return {
    ...getBaseChartOptions(),
    grid: {
      left: '10px',
      right: '10px',
      top: '40px',
      bottom: '50px',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: timestamps,
      axisLine: { lineStyle: { color: '#3a4553' } },
      axisLabel: {
        color: '#a8b2c1',
        rotate: 45,
        fontSize: 11
      },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        position: 'left',
        axisLine: { show: false },
        axisLabel: {
          color: '#a8b2c1',
          formatter: '{value}'
        },
        splitLine: { lineStyle: { color: '#3a4553', type: 'dashed' } }
      },
      {
        type: 'value',
        position: 'right',
        max: actualMaxBlockRate,
        axisLine: { show: false },
        axisLabel: {
          color: '#a8b2c1',
          formatter: '{value}%'
        },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '총 요청',
        type: 'line',
        yAxisIndex: 0,
        data: data.map(d => d.totalRequests),
        lineStyle: {
          color: CHART_COLORS.primary,
          width: 2
        },
        itemStyle: {
          color: CHART_COLORS.primary
        },
        symbol: 'circle',
        symbolSize: 4,
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(72, 202, 228, 0.3)' },
              { offset: 1, color: 'rgba(72, 202, 228, 0.05)' }
            ]
          }
        }
      },
      {
        name: '차단율',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(d => {
          const total = d.totalRequests || 1;
          return parseFloat(((d.blockedRequests / total) * 100).toFixed(2));
        }),
        lineStyle: {
          color: CHART_COLORS.danger,
          width: 3
        },
        itemStyle: {
          color: CHART_COLORS.danger,
          borderWidth: 2
        },
        symbol: 'circle',
        symbolSize: 6,
        smooth: true
      }
    ],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#a8b2c1' }
      },
      backgroundColor: '#242c3a',
      borderColor: '#3a4553',
      textStyle: { color: '#ffffff' },
      formatter: (params: any) => {
        const timestamp = params[0].axisValue;
        let result = `<div style="font-weight: bold; margin-bottom: 8px;">${timestamp}</div>`;
        params.forEach((param: any) => {
          const value = param.seriesName === '차단율'
            ? `${param.value}%`
            : param.value;
          result += `
            <div style="display: flex; align-items: center; margin-top: 4px;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background-color: ${param.color}; margin-right: 8px;"></span>
              <span>${param.seriesName}: <strong>${value}</strong></span>
            </div>
          `;
        });
        return result;
      }
    },
    legend: {
      data: ['총 요청', '차단율'],
      textStyle: { color: '#a8b2c1' },
      top: '5px',
      right: '20px'
    }
  };
};

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