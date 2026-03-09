import { ref, onMounted, onUnmounted } from 'vue'

const CART_KEY = 'cart'
const CART_UPDATED = 'cart-updated'

export function useCart() {
  const cartCount = ref(0)

  const refreshCount = () => {
    try {
      const raw = localStorage.getItem(CART_KEY)
      const cart = raw ? JSON.parse(raw) : []
      cartCount.value = cart.reduce((sum, it) => sum + (it.qty || 0), 0)
    } catch {
      cartCount.value = 0
    }
  }

  const getCart = () => {
    try {
      const raw = localStorage.getItem(CART_KEY)
      return raw ? JSON.parse(raw) : []
    } catch {
      return []
    }
  }

  const saveCart = (cart) => {
    localStorage.setItem(CART_KEY, JSON.stringify(cart))
    window.dispatchEvent(new CustomEvent(CART_UPDATED))
  }

  const addToCart = (item, qty = 1) => {
    const cart = [...getCart()]
    const idx = cart.findIndex((it) => it.productId === item.id)
    if (idx >= 0) {
      cart[idx].qty = (cart[idx].qty || 0) + qty
    } else {
      cart.push({
        productId: item.id,
        name: item.name,
        price: Number(item.price),
        qty: qty,
      })
    }
    saveCart(cart)
  }

  onMounted(() => {
    refreshCount()
    window.addEventListener(CART_UPDATED, refreshCount)
  })

  onUnmounted(() => {
    window.removeEventListener(CART_UPDATED, refreshCount)
  })

  return { cartCount, refreshCount, getCart, saveCart, addToCart }
}
