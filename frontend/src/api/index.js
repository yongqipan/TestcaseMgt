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
