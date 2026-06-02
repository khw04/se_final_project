export const TODAY = '2026-06-04T00:00:00Z'

export const delay = (ms = 80) => new Promise<void>((resolve) => setTimeout(resolve, ms))

export const iso = (date: number | string | Date) => new Date(date).toISOString()

export function calcDDay(isoDate: string, todayISO: string = TODAY) {
  const a = new Date(isoDate)
  a.setHours(0, 0, 0, 0)
  const b = new Date(todayISO)
  b.setHours(0, 0, 0, 0)
  return Math.round((a.getTime() - b.getTime()) / 86400000)
}

export function relativeKo(isoDate: string, todayISO: string = TODAY) {
  const d = calcDDay(isoDate, todayISO)
  if (d === 0) return '오늘'
  if (d === 1) return '내일'
  if (d === -1) return '어제'
  if (d > 0) return `${d}일 후`
  return `${-d}일 전`
}
