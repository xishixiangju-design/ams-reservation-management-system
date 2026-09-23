import { ref, onUnmounted, type Ref } from 'vue'

interface RequestOptions {
  debounce?: number
  manual?: boolean
  onSuccess?: (data: any) => void
  onError?: (err: any) => void
}

export function useRequest<T = any>(
  apiFn: (...args: any[]) => Promise<any>,
  options: RequestOptions = {}
) {
  const loading = ref(false)
  const data = ref<T | null>(null)
  const error = ref<any>(null)
  let abortController: AbortController | null = null
  let timer: any = null

  const run = async (...args: any[]) => {
    // Cancel previous request
    if (abortController) {
      abortController.abort()
    }
    abortController = new AbortController()
    const signal = abortController.signal

    // Debounce logic
    if (options.debounce && options.debounce > 0) {
      if (timer) clearTimeout(timer)
      await new Promise((resolve) => {
        timer = setTimeout(resolve, options.debounce)
      })
      if (signal.aborted) return
    }

    loading.value = true
    error.value = null

    try {
      // Pass signal as the last argument (assuming the API function accepts config as last arg)
      // or we expect the caller to handle how signal is passed, but here we enforce passing it as last arg
      // which matches our modification to getAdminAppointmentList(params, config)
      const res = await apiFn(...args, { signal })

      if (signal.aborted) return

      data.value = res.data // Adjust based on your API response structure
      options.onSuccess?.(res)
      return res
    } catch (err: any) {
      if (err.name === 'CanceledError' || err.code === 'ERR_CANCELED') {
        // Request was canceled, ignore
        return
      }
      error.value = err
      options.onError?.(err)
      // We don't rethrow here to prevent unhandled promise rejections in UI unless necessary
      console.error('Request failed:', err)
    } finally {
      // Only set loading to false if this is the current active request
      if (abortController?.signal === signal) {
        loading.value = false
      }
    }
  }

  onUnmounted(() => {
    if (abortController) {
      abortController.abort()
    }
    if (timer) {
      clearTimeout(timer)
    }
  })

  return { loading, data, error, run }
}
