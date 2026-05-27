import type { CSSProperties } from 'react'

const ICONS: Record<string, string> = {
  dashboard: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z',
  calendar: 'M8 2v4 M16 2v4 M3 10h18 M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z',
  book: 'M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2zM22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z',
  sparkle: 'M12 3v3 M12 18v3 M3 12h3 M18 12h3 M5.6 5.6l2.1 2.1 M16.3 16.3l2.1 2.1 M5.6 18.4l2.1-2.1 M16.3 7.7l2.1-2.1',
  brain:
    'M9.5 2A2.5 2.5 0 0 1 12 4.5v15a2.5 2.5 0 0 1-4.96.44 2.5 2.5 0 0 1-2.96-3.08 3 3 0 0 1-.34-5.58 2.5 2.5 0 0 1 1.32-4.24 2.5 2.5 0 0 1 1.98-3A2.5 2.5 0 0 1 9.5 2z M14.5 2A2.5 2.5 0 0 0 12 4.5v15a2.5 2.5 0 0 0 4.96.44 2.5 2.5 0 0 0 2.96-3.08 3 3 0 0 0 .34-5.58 2.5 2.5 0 0 0-1.32-4.24 2.5 2.5 0 0 0-1.98-3A2.5 2.5 0 0 0 14.5 2z',
  check: 'M20 6 9 17l-5-5',
  x: 'M18 6 6 18 M6 6l12 12',
  bell: 'M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9 M10.3 21a1.94 1.94 0 0 0 3.4 0',
  search: 'M11 11a7 7 0 1 0-14 0 7 7 0 0 0 14 0z M21 21l-4.3-4.3',
  plus: 'M5 12h14 M12 5v14',
  more: 'M5 12h.01 M12 12h.01 M19 12h.01',
  arrowRight: 'M5 12h14 m-6-6 6 6-6 6',
  clock: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 6v6l4 2',
  refresh: 'M21 12a9 9 0 0 0-15.5-6.3L3 8 M3 3v5h5 M3 12a9 9 0 0 0 15.5 6.3L21 16 M21 21v-5h-5',
  image: 'M3 3h18v18H3z M9 11a2 2 0 1 0 0-4 2 2 0 0 0 0 4z m12 6-5-5L5 21',
}

type IconProps = {
  name: keyof typeof ICONS | string
  size?: number
  stroke?: number
  className?: string
  style?: CSSProperties
}

export function Icon({ name, size = 18, stroke = 1.6, className = '', style }: IconProps) {
  const path = ICONS[name]
  if (!path) return null
  const segments = path.split(' M').map((seg, idx) => (idx === 0 ? seg : 'M' + seg))

  return (
    <svg
      aria-hidden="true"
      className={'icon ' + className}
      fill="none"
      height={size}
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={stroke}
      style={style}
      viewBox="0 0 24 24"
      width={size}
    >
      {segments.map((d, i) => (
        <path key={i} d={d} />
      ))}
    </svg>
  )
}
