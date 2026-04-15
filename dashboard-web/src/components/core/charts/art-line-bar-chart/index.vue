<!-- 柱状图折线图混合图表 -->
<!-- MixedBarLineChart.vue -->
<template>
  <div ref="chartRef" :style="{ height: props.height }" v-loading="props.loading"></div>
</template>

<script setup lang="ts">
import { useChartOps, useChartComponent } from '@/hooks/core/useChart'
import { getCssVar } from '@/utils/ui'
import { graphic, type EChartsOption } from '@/plugins/echarts'
import type { MixedChartProps, BarDataItem, LineDataItem } from '@/types/component/chart'

defineOptions({ name: 'ArtLineBarChart' })

const props = withDefaults(defineProps<MixedChartProps>(), {
  // 基础配置
  height: useChartOps().chartHeight,
  loading: false,
  isEmpty: false,
  borderRadius: 4,

  // 颜色配置
  barColors: () => useChartOps().colors,
  lineColors: () => ['#FFAF20', '#4ABEFF', '#14DEBA', '#FA8A6C'],
  
  // 数据配置
  barData: () => [],
  lineData: () => [],
  xAxisData: () => [],
  
  // 样式配置
  barWidth: '40%',
  barStack: false,
  lineWidth: 2,
  showSymbol: true,
  smooth: false,
  
  // 轴线显示配置
  showAxisLabel: true,
  showAxisLine: true,
  showSplitLine: true,
  
  // 交互配置
  showTooltip: true,
  showLegend: true,
  legendPosition: 'top',
  
  // Y轴配置 - 修复这里
  yAxisType: 'single', // 'single' | 'dual'
  yAxisName: () => ['', ''] as [string, string],
  yAxisPosition: () => ['left', 'right'] as ['left' | 'right', 'left' | 'right']
})

// 判断是否有多组数据
const hasMultipleBars = computed(() => {
  return (
    Array.isArray(props.barData) &&
    props.barData.length > 0 &&
    typeof props.barData[0] === 'object' &&
    'name' in props.barData[0]
  )
})

const hasMultipleLines = computed(() => {
  return (
    Array.isArray(props.lineData) &&
    props.lineData.length > 0 &&
    typeof props.lineData[0] === 'object' &&
    'name' in props.lineData[0]
  )
})

// 获取颜色配置
const getBarColor = (customColor?: string, index?: number) => {
  if (customColor) return customColor

  if (index !== undefined) {
    return props.barColors[index % props.barColors.length]
  }

  // 默认柱状图渐变色
  return new graphic.LinearGradient(0, 0, 0, 1, [
    {
      offset: 0,
      color: getCssVar('--el-color-primary-light-4')
    },
    {
      offset: 1,
      color: getCssVar('--el-color-primary')
    }
  ])
}

const getLineColor = (index: number) => {
  if (props.lineColors && props.lineColors.length > 0) {
    return props.lineColors[index % props.lineColors.length]
  }
  
  // 默认折线颜色
  const defaultLineColors = ['#FFAF20', '#4ABEFF', '#14DEBA', '#FA8A6C']
  return defaultLineColors[index % defaultLineColors.length]
}

// 创建渐变色
const createGradientColor = (color: string) => {
  return new graphic.LinearGradient(0, 0, 0, 1, [
    {
      offset: 0,
      color: color
    },
    {
      offset: 1,
      color: color
    }
  ])
}

// 获取基础样式配置
const getBarItemStyle = (color: string | InstanceType<typeof graphic.LinearGradient> | undefined) => ({
  borderRadius: props.borderRadius,
  color: typeof color === 'string' ? createGradientColor(color) : color
})

// 创建柱状图系列
const createBarSeriesItem = (config: {
  name?: string
  data: number[]
  color?: string | InstanceType<typeof graphic.LinearGradient>
  barWidth?: string | number
  stack?: string
  yAxisIndex?: number
}) => {
  const animationConfig = getAnimationConfig()

  return {
    name: config.name,
    data: config.data,
    type: 'bar' as const,
    stack: config.stack,
    yAxisIndex: config.yAxisIndex || 0,
    itemStyle: getBarItemStyle(config.color),
    barWidth: config.barWidth || props.barWidth,
    ...animationConfig,
    emphasis: {
      focus: 'series'
    }
  }
}

// 创建折线图系列
const createLineSeriesItem = (config: {
  name?: string
  data: number[]
  color?: string
  lineWidth?: number
  showSymbol?: boolean
  smooth?: boolean
  yAxisIndex?: number
}) => {
  const animationConfig = getAnimationConfig(80, 1800)

  return {
    name: config.name,
    data: config.data,
    type: 'line' as const,
    yAxisIndex: config.yAxisIndex || (props.yAxisType === 'dual' ? 1 : 0),
    itemStyle: {
      color: config.color
    },
    lineStyle: {
      width: config.lineWidth || props.lineWidth,
      color: config.color
    },
    symbol: config.showSymbol !== false ? 'circle' : 'none',
    symbolSize: 6,
    smooth: config.smooth || props.smooth,
    ...animationConfig,
    emphasis: {
      focus: 'series',
      lineStyle: {
        width: (config.lineWidth || props.lineWidth) + 2
      }
    },
    areaStyle: props.showArea ? {
      color: new graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: config.color + '80' },
        { offset: 1, color: config.color + '10' }
      ])
    } : undefined
  }
}

// 使用新的图表组件抽象
const {
  chartRef,
  getAxisLineStyle,
  getAxisLabelStyle,
  getAxisTickStyle,
  getSplitLineStyle,
  getAnimationConfig,
  getTooltipStyle,
  getLegendStyle,
  getGridWithLegend
} = useChartComponent({
  props,
  checkEmpty: () => {
    // 检查柱状图数据
    const barEmpty = hasMultipleBars.value 
      ? (props.barData as BarDataItem[]).every(item => !item.data?.length || item.data.every(val => val === 0))
      : (props.barData as number[]).every(val => val === 0)
    
    // 检查折线图数据
    const lineEmpty = hasMultipleLines.value 
      ? (props.lineData as LineDataItem[]).every(item => !item.data?.length || item.data.every(val => val === 0))
      : (props.lineData as number[]).every(val => val === 0)
    
    return barEmpty && lineEmpty
  },
  watchSources: [
    () => props.barData, 
    () => props.lineData, 
    () => props.xAxisData,
    () => props.barColors,
    () => props.lineColors
  ],
 generateOptions: (): EChartsOption => {
  // 直接从原始 props 中获取值
  const {
    showLegend,
    legendPosition,
    showTooltip,
    showAxisLine,
    showAxisLabel,
    showSplitLine,
    yAxisType,
    yAxisName,
    yAxisPosition,
    xAxisData,
    barData,
    lineData,
    barStack,
    showDataZoom,
    tooltipFormatter
  } = props

  const options: EChartsOption = {
    grid: getGridWithLegend(showLegend, legendPosition, {
      top: showLegend && legendPosition === 'top' ? 50 : 30,
      right: yAxisType === 'dual' ? 60 : 20,
      left: 60,
      bottom: 40,
      containLabel: true
    }),
    tooltip: showTooltip
      ? {
          ...getTooltipStyle(),
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          },
          ...(tooltipFormatter ?? {})
        }
      : undefined,
    legend: showLegend
      ? {
          ...getLegendStyle(legendPosition),
          data: [
            ...(hasMultipleBars.value 
              ? (barData as BarDataItem[]).map(item => item.name)
              : ['柱状图']),
            ...(hasMultipleLines.value 
              ? (lineData as LineDataItem[]).map(item => item.name)
              : ['折线图'])
          ]
        }
      : undefined,
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: getAxisTickStyle(),
      axisLine: getAxisLineStyle(showAxisLine),
      axisLabel: getAxisLabelStyle(showAxisLabel),
      boundaryGap: true
    }
  }

  // 设置Y轴
  if (yAxisType === 'dual') {
    // 双Y轴配置
    options.yAxis = [
      {
        type: 'value',
        name: yAxisName[0] || '',
        position: yAxisPosition[0] || 'left',
        axisLine: getAxisLineStyle(showAxisLine),
        axisLabel: getAxisLabelStyle(showAxisLabel),
        splitLine: getSplitLineStyle(showSplitLine),
        nameTextStyle: {
          color: getAxisLabelStyle().color,
          fontSize: getAxisLabelStyle().fontSize - 1
        }
      },
      {
        type: 'value',
        name: yAxisName[1] || '',
        position: yAxisPosition[1] || 'right',
        axisLine: getAxisLineStyle(showAxisLine),
        axisLabel: getAxisLabelStyle(showAxisLabel),
        splitLine: { show: false },
        nameTextStyle: {
          color: getAxisLabelStyle().color,
          fontSize: getAxisLabelStyle().fontSize - 1
        }
      }
    ]
  } else {
    // 单Y轴配置
    options.yAxis = {
      type: 'value',
      name: yAxisName[0] || '',
      axisLine: getAxisLineStyle(showAxisLine),
      axisLabel: getAxisLabelStyle(showAxisLabel),
      splitLine: getSplitLineStyle(showSplitLine),
      nameTextStyle: {
        color: getAxisLabelStyle().color,
        fontSize: getAxisLabelStyle().fontSize - 1
      }
    }
  }

  // 生成系列数据
  const series: any[] = []

  // 添加柱状图系列
  if (hasMultipleBars.value) {
    const multiBarData = barData as BarDataItem[]
    multiBarData.forEach((item, index) => {
      const computedColor = getBarColor(item.color, index)
      
      series.push(createBarSeriesItem({
        name: item.name,
        data: item.data,
        color: computedColor,
        barWidth: item.barWidth,
        stack: barStack ? item.stack || 'total' : undefined,
        yAxisIndex: item.yAxisIndex || 0
      }))
    })
  } else if (barData.length > 0) {
    // 单组柱状图数据
    const singleBarData = barData as number[]
    const computedColor = getBarColor()
    
    series.push(createBarSeriesItem({
      name: '柱状图',
      data: singleBarData,
      color: computedColor,
      yAxisIndex: 0
    }))
  }

  // 添加折线图系列
  if (hasMultipleLines.value) {
    const multiLineData = lineData as LineDataItem[]
    multiLineData.forEach((item, index) => {
      const lineColor = item.color || getLineColor(index)
      
      series.push(createLineSeriesItem({
        name: item.name,
        data: item.data,
        color: lineColor,
        lineWidth: item.lineWidth,
        showSymbol: item.showSymbol,
        smooth: item.smooth,
        yAxisIndex: item.yAxisIndex || (yAxisType === 'dual' ? 1 : 0)
      }))
    })
  } else if (lineData.length > 0) {
    // 单组折线图数据
    const singleLineData = lineData as number[]
    const lineColor = getLineColor(0)
    
    series.push(createLineSeriesItem({
      name: '折线图',
      data: singleLineData,
      color: lineColor,
      yAxisIndex: yAxisType === 'dual' ? 1 : 0
    }))
  }

  options.series = series

  // 添加数据缩放功能（如果数据点较多）
  if (xAxisData.length > 10 && showDataZoom) {
    options.dataZoom = [
      {
        type: 'inside',
        xAxisIndex: 0,
        start: 0,
        end: 100
      },
      {
        type: 'slider',
        xAxisIndex: 0,
        start: 0,
        end: 100,
        bottom: 10,
        height: 20
      }
    ]
  }

  return options
}
})
</script>