import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const auth = useAuthStore()
  const value = binding.value
  const allowed = Array.isArray(value)
    ? auth.hasAnyPermission(value)
    : auth.hasPermission(value)
  el.style.display = allowed ? '' : 'none'
}

export const vPerm: Directive = {
  mounted: checkPermission,
  updated: checkPermission,
}
