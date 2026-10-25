# 接口定义

所有接口前缀为 `/api`，请求与响应均为 JSON（`Content-Type: application/json`）。

## 统一响应

```json
{ "code": 0, "message": "ok", "data": {} }
```

- `code = 0` 表示成功，非 0 表示业务错误
- 需要认证的接口须携带 `Authorization: Bearer <token>`
- 未认证或令牌失效返回 HTTP 401，`code = 401`

分页数据格式：

```json
{ "list": [], "total": 100, "page": 1, "size": 10 }
```

## 认证

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| POST | /api/auth/login | 登录 | `{ "username": "admin", "password": "admin123" }` |
| GET | /api/auth/me | 当前用户 | - |
| POST | /api/auth/logout | 退出登录 | - |

登录响应 `data`：`{ "token": "xxx", "user": { "id": 1, "username": "admin", "nickname": "管理员", "role": "ADMIN" } }`

## 项目

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /api/projects | 分页查询 | `keyword, page, size` |
| GET | /api/projects/all | 全部项目（下拉） | - |
| POST | /api/projects | 新建 | `{ "name", "code", "description", "status" }` |
| PUT | /api/projects/{id} | 更新 | 同上 |
| DELETE | /api/projects/{id} | 删除 | - |

## 模块

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /api/modules/tree | 模块树 | `projectId` |
| POST | /api/modules | 新建 | `{ "projectId", "parentId", "name", "sortOrder" }` |
| PUT | /api/modules/{id} | 更新 | 同上 |
| DELETE | /api/modules/{id} | 删除（存在子模块或用例时拒绝） | - |

## 测试用例

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /api/test-cases | 分页查询 | `projectId, moduleId, keyword, priority, status, page, size` |
| GET | /api/test-cases/{id} | 详情 | - |
| POST | /api/test-cases | 新建 | 见下方结构 |
| PUT | /api/test-cases/{id} | 更新 | 见下方结构 |
| DELETE | /api/test-cases/{id} | 删除 | - |

```json
{
  "projectId": 1,
  "moduleId": 2,
  "title": "用户使用正确密码登录",
  "priority": "P0",
  "type": "FUNCTIONAL",
  "status": "ACTIVE",
  "precondition": "已存在有效账号",
  "steps": [
    { "action": "打开登录页并输入账号密码", "expected": "输入框正常回显" },
    { "action": "点击登录", "expected": "跳转至工作台" }
  ],
  "tags": "登录,冒烟"
}
```

`priority`：P0/P1/P2/P3；`type`：FUNCTIONAL/API/UI/PERFORMANCE；`status`：DRAFT/ACTIVE/DEPRECATED

## 测试计划

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /api/test-plans | 分页查询 | `projectId, keyword, page, size` |
| POST | /api/test-plans | 新建 | `{ "projectId", "name", "description", "status", "startDate", "endDate" }` |
| PUT | /api/test-plans/{id} | 更新 | 同上 |
| DELETE | /api/test-plans/{id} | 删除（级联删除计划用例与执行记录） | - |
| GET | /api/test-plans/{id}/available-cases | 查询可添加到计划的用例（排除已选） | `moduleId, keyword, priority, status, page, size` |
| POST | /api/test-plans/{id}/cases | 批量添加用例 | `{ "caseIds": [1, 2] }` |
| DELETE | /api/test-plans/{id}/cases/{caseId} | 从计划移除用例（同时删除该用例执行记录） | - |

## 执行记录

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /api/test-plans/{id}/cases | 计划内已选用例及执行状态 | - |
| POST | /api/test-plans/{id}/runs | 提交单条执行结果（用例须已加入计划） | `{ "caseId", "status", "remark", "executor" }` |
| GET | /api/test-plans/{id}/stats | 计划执行统计（基于已选用例） | - |

`status`：PASS/FAIL/BLOCKED/SKIPPED；同一计划同一用例重复提交时更新原记录。

计划执行范围由 `test_plan_cases` 决定：执行清单、统计与提交校验均只针对已加入计划的用例。历史计划首次启动时自动把项目全部用例绑定到计划。

## 仪表盘

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/dashboard/stats | 全局统计 |

```json
{
  "projectCount": 1,
  "caseCount": 12,
  "planCount": 2,
  "runCount": 8,
  "passRate": 75.0,
  "priorityDistribution": [{ "name": "P0", "value": 4 }],
  "statusDistribution": [{ "name": "ACTIVE", "value": 10 }],
  "recentPlans": []
}
```
