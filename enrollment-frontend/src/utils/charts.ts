import * as echarts from 'echarts'

export function createBarOption(categories: string[], data: number[], name: string) {
  return {
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e5e7eb', textStyle: { color: '#374151' } },
    legend: { bottom: 0, textStyle: { color: '#6b7280' } },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 15, color: '#6b7280', fontSize: 11 }, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f3f4f6' } }, axisLabel: { color: '#6b7280' } },
    series: [{ name, type: 'bar', data, barWidth: 32, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#1a56db' }]), borderRadius: [6, 6, 0, 0] } }],
  }
}

export function createPieOption(data: { value: number; name: string }[], config?: { radius?: [string, string]; center?: [string, string] }) {
  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#ec4899']
  return {
    tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#e5e7eb', textStyle: { color: '#374151' }, formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#6b7280' } },
    color: colors,
    series: [{ type: 'pie', radius: config?.radius || ['45%', '70%'], center: config?.center || ['50%', '50%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 }, label: { show: true, formatter: '{b}\n{d}%', color: '#6b7280' }, emphasis: { label: { fontSize: 16, fontWeight: 'bold' } }, data }],
  }
}
