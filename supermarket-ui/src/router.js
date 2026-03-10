import { createRouter, createWebHistory } from 'vue-router'
import Login from './components/Login.vue'
import UserRegister from './components/UserRegister.vue'
import AdminLayout from './views/AdminLayout.vue'
import UserLayout from './views/UserLayout.vue'

import AdminProducts from './views/admin/AdminProducts.vue'
import AdminUsers from './views/admin/AdminUsers.vue'
import AdminDelivery from './views/admin/AdminDelivery.vue'
import AdminWarehouse from './views/admin/AdminWarehouse.vue'
import AdminProcurement from './views/admin/AdminProcurement.vue'
import AdminSystem from './views/admin/AdminSystem.vue'

import UserProfile from './views/user/UserProfile.vue'
import UserShop from './views/user/UserShop.vue'
import UserCart from './views/user/UserCart.vue'
import UserAfterSales from './views/user/UserAfterSales.vue'
import UserProductDetail from './views/user/UserProductDetail.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: UserRegister },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/products',
    children: [
      { path: 'products', component: AdminProducts },
      { path: 'products/:id', component: UserProductDetail },
      { path: 'personnel', component: AdminUsers },
      { path: 'delivery', component: AdminDelivery },
      { path: 'warehouse', component: AdminWarehouse },
      { path: 'procurement', component: AdminProcurement },
      { path: 'system', component: AdminSystem },
    ],
  },
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/shop',
    children: [
      { path: 'profile', component: UserProfile },
      { path: 'shop', component: UserShop },
       { path: 'shop/:id', component: UserProductDetail },
      { path: 'cart', component: UserCart },
      { path: 'after-sales', component: UserAfterSales },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

