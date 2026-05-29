import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { PageQuery, PageResult } from '@/types/api'

export function usePagination(initial: PageQuery = { page: 1, size: 20 }) {
  const page = ref(initial.page ?? 1)
  const size = ref(initial.size ?? 20)
  const total = ref(0)
  const loading = ref(false)
  const items = ref<unknown[]>([])

  function applyPageResult<T>(result: PageResult<T>) {
    items.value = result.items
    page.value = result.page
    size.value = result.size
    total.value = result.total
  }

  function reset() {
    page.value = 1
    total.value = 0
    items.value = []
  }

  return { page, size, total, loading, items, applyPageResult, reset }
}

export function usePermission() {
  const auth = useAuthStore()
  return {
    has: (code: string) => auth.hasPermission(code),
    hasAny: (codes: string[]) => auth.hasAnyPermission(codes),
  }
}
