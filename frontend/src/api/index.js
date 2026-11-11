import http from './http'

export const authApi = {
  login: (payload) => http.post('/auth/login', payload),
  me: () => http.get('/auth/me'),
  logout: () => http.post('/auth/logout')
}

export const projectApi = {
  page: (params) => http.get('/projects', { params }),
  all: () => http.get('/projects/all'),
  create: (payload) => http.post('/projects', payload),
  update: (id, payload) => http.put(`/projects/${id}`, payload),
  remove: (id) => http.delete(`/projects/${id}`)
}

export const moduleApi = {
  tree: (projectId) => http.get('/modules/tree', { params: { projectId } }),
  create: (payload) => http.post('/modules', payload),
  update: (id, payload) => http.put(`/modules/${id}`, payload),
  remove: (id) => http.delete(`/modules/${id}`)
}

export const caseApi = {
  page: (params) => http.get('/test-cases', { params }),
  detail: (id) => http.get(`/test-cases/${id}`),
  create: (payload) => http.post('/test-cases', payload),
  update: (id, payload) => http.put(`/test-cases/${id}`, payload),
  remove: (id) => http.delete(`/test-cases/${id}`)
}

export const planApi = {
  page: (params) => http.get('/test-plans', { params }),
  detail: (id) => http.get(`/test-plans/${id}/cases`),
  create: (payload) => http.post('/test-plans', payload),
  update: (id, payload) => http.put(`/test-plans/${id}`, payload),
  remove: (id) => http.delete(`/test-plans/${id}`),
  availableCases: (id, params) => http.get(`/test-plans/${id}/available-cases`, { params }),
  addCases: (id, caseIds) => http.post(`/test-plans/${id}/cases`, { caseIds }),
  removeCase: (id, caseId) => http.delete(`/test-plans/${id}/cases/${caseId}`),
  submitRun: (id, payload) => http.post(`/test-plans/${id}/runs`, payload)
}

export const dashboardApi = {
  stats: () => http.get('/dashboard/stats')
}

export const aiAdminApi = {
  providers: () => http.get('/ai/admin/providers'),
  createProvider: (payload) => http.post('/ai/admin/providers', payload),
  updateProvider: (id, payload) => http.put(`/ai/admin/providers/${id}`, payload),
  removeProvider: (id) => http.delete(`/ai/admin/providers/${id}`),
  models: () => http.get('/ai/admin/models'),
  createModel: (payload) => http.post('/ai/admin/models', payload),
  updateModel: (id, payload) => http.put(`/ai/admin/models/${id}`, payload),
  removeModel: (id) => http.delete(`/ai/admin/models/${id}`),
  testModel: (id) => http.post(`/ai/admin/models/${id}/test`),
  prompts: (params) => http.get('/ai/admin/prompts', { params }),
  createPrompt: (payload) => http.post('/ai/admin/prompts', payload),
  updatePrompt: (id, payload) => http.put(`/ai/admin/prompts/${id}`, payload),
  removePrompt: (id) => http.delete(`/ai/admin/prompts/${id}`),
  quota: () => http.get('/ai/admin/quota'),
  updateQuota: (payload) => http.put('/ai/admin/quota', payload)
}

export const aiGenerationApi = {
  page: (params) => http.get('/ai/generations', { params }),
  detail: (id) => http.get(`/ai/generations/${id}`),
  create: (payload) => http.post('/ai/generations', payload),
  accept: (id, itemIds) => http.post(`/ai/generations/${id}/accept`, { itemIds }),
  remove: (id) => http.delete(`/ai/generations/${id}`)
}

export const aiInsightApi = {
  review: (payload) => http.post('/ai/reviews', payload),
  reviewHistory: (caseId) => http.get('/ai/reviews', { params: { caseId } }),
  report: (payload) => http.post('/ai/reports', payload),
  reportHistory: (planId) => http.get('/ai/reports', { params: { planId } }),
  latestReport: (planId) => http.get('/ai/reports/latest', { params: { planId } })
}

export const aiMonitorApi = {
  logs: (params) => http.get('/ai/monitor/logs', { params }),
  stats: () => http.get('/ai/monitor/stats'),
  tokensByScene: (days) => http.get('/ai/monitor/tokens-by-scene', { params: { days } }),
  quota: () => http.get('/ai/monitor/quota')
}

