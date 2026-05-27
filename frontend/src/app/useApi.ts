import { useEffect, useState } from 'react'

type UseApiResult<T> = {
  data: T | null
  loading: boolean
  error: unknown
  refetch: () => void
}

export function useApi<T>(fnFactory: () => Promise<T>, deps: ReadonlyArray<unknown> = []): UseApiResult<T> {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<unknown>(null)
  const [nonce, setNonce] = useState(0)

  useEffect(() => {
    let alive = true
    setLoading(true)
    fnFactory()
      .then((next) => {
        if (!alive) return
        setData(next)
        setError(null)
        setLoading(false)
      })
      .catch((err) => {
        if (!alive) return
        setError(err)
        setLoading(false)
      })
    return () => {
      alive = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, nonce])

  return { data, loading, error, refetch: () => setNonce((n) => n + 1) }
}
