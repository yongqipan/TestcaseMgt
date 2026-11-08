const state = {
  view: 'dashboard',
  aiOpen: true,
  dockInit: false,
  caseFilter: { keyword: '', priority: 'all', status: 'all', aiOnly: false },
  reviewRunning: false,
  genStep: 1,
  modelActive: 'deepseek-v3'
};

const DATA = {
  kpi: { projects: 3, cases: 148, plans: 12, passRate: 92.4, aiCases: 61, aiAccept: 78 },
  projects: [
    { id: 1, name: '测试管理系统', code: 'TESTCASEMGT', modules: 2, cases: 10, owner: '王敏', ai: true, status: '进行中', updated: '2026-11-02' },
    { id: 2, name: '报价管理平台', code: 'QUOTEMGT', modules: 6, cases: 96, owner: '陈工', ai: true, status: '进行中', updated: '2026-10-30' },
    { id: 3, name: '设备主数据', code: 'MASTERDATA', modules: 4, cases: 42, owner: '李工', ai: false, status: '归档', updated: '2026-10-18' }
  ],
  modules: [
    { id: 5, project: '测试管理系统', name: '测试用例', parent: '-', cases: 10, owner: '王敏' },
    { id: 6, project: '测试管理系统', name: '测试计划', parent: '-', cases: 0, owner: '王敏' },
    { id: 21, project: '报价管理平台', name: '图纸识别', parent: '-', cases: 28, owner: '陈工' },
    { id: 22, project: '报价管理平台', name: '工艺拆解', parent: '-', cases: 24, owner: '陈工' },
    { id: 23, project: '报价管理平台', name: '成本核算', parent: '-', cases: 31, owner: '李工' }
  ],
  cases: [
    { no: 'TC-TESTCASEMGT-001', title: '使用正确账号密码登录系统', module: '测试用例', priority: 'P0', status: 'ACTIVE', owner: '王敏', ai: true, aiScore: 92, steps: 3 },
    { no: 'TC-TESTCASEMGT-002', title: '登录密码错误时提示并禁止进入', module: '测试用例', priority: 'P0', status: 'ACTIVE', owner: '王敏', ai: true, aiScore: 88, steps: 3 },
    { no: 'TC-TESTCASEMGT-003', title: '连续 5 次密码错误锁定账号', module: '测试用例', priority: 'P1', status: 'ACTIVE', owner: '王敏', ai: false, aiScore: 76, steps: 4 },
    { no: 'TC-TESTCASEMGT-004', title: '创建测试计划并添加用例', module: '测试计划', priority: 'P0', status: 'ACTIVE', owner: '王敏', ai: true, aiScore: 90, steps: 5 },
    { no: 'TC-TESTCASEMGT-005', title: '已开始计划不可删除', module: '测试计划', priority: 'P1', status: 'ACTIVE', owner: '王敏', ai: true, aiScore: 85, steps: 4 },
    { no: 'TC-TESTCASEMGT-006', title: '已执行用例不可从计划移除', module: '测试计划', priority: 'P1', status: 'ACTIVE', owner: '王敏', ai: false, aiScore: 68, steps: 3 },
    { no: 'TC-QUOTEMGT-101', title: '上传图纸并解析出字段', module: '图纸识别', priority: 'P0', status: 'ACTIVE', owner: '陈工', ai: true, aiScore: 94, steps: 6 },
    { no: 'TC-QUOTEMGT-102', title: '图纸水印预览与禁止下载', module: '图纸识别', priority: 'P2', status: 'DRAFT', owner: '陈工', ai: false, aiScore: 55, steps: 3 },
    { no: 'TC-QUOTEMGT-201', title: '工艺路线推荐可人工覆盖', module: '工艺拆解', priority: 'P1', status: 'ACTIVE', owner: '陈工', ai: true, aiScore: 87, steps: 5 }
  ],
  plans: [
    { id: 8, name: '测试管理第一次功能测试', project: '测试管理系统', status: 'COMPLETED', start: '2026-10-29', end: '2026-11-01', total: 10, executed: 10, pass: 10, ai: false },
    { id: 9, name: '报价平台图纸识别回归', project: '报价管理平台', status: 'IN_PROGRESS', start: '2026-10-30', end: '2026-11-05', total: 12, executed: 7, pass: 6, ai: true },
    { id: 10, name: '报价平台成本核算冒烟', project: '报价管理平台', status: 'PLANNED', start: '2026-11-03', end: '2026-11-06', total: 8, executed: 0, pass: 0, ai: true },
    { id: 4, name: '设备主数据回归', project: '设备主数据', status: 'PLANNED', start: '2026-11-08', end: '2026-11-10', total: 6, executed: 0, pass: 0, ai: false },
    { id: 5, name: '登录专项测试', project: '测试管理系统', status: 'IN_PROGRESS', start: '2026-11-01', end: '2026-11-03', total: 5, executed: 3, pass: 2, ai: false }
  ],
  genResult: [
    { no: 'TC-AI-QUOTE-001', title: '报价单金额小于等于 3000 时自动放行', priority: 'P0', type: '功能', conf: 94, selected: true, text: '验证提交金额 ≤ 3000 的报价单后，系统跳过人工审批直接进入已发送状态' },
    { no: 'TC-AI-QUOTE-002', title: '报价单金额大于 10000 时进入经理审批', priority: 'P0', type: '功能', conf: 91, selected: true, text: '验证提交金额 > 10000 的报价单后，审批流自动流转到报价经理节点' },
    { no: 'TC-AI-QUOTE-003', title: '客户等级 A 且非标件需工艺复核', priority: 'P1', type: '功能', conf: 86, selected: true, text: '验证客户等级为 A 且标记非标件时，系统强制增加工艺复核节点' },
    { no: 'TC-AI-QUOTE-004', title: '审批拒绝后报价单退回草稿', priority: 'P1', type: '异常', conf: 82, selected: true, text: '验证任一审批节点拒绝后，报价单状态回退为草稿并记录拒绝原因' },
    { no: 'TC-AI-QUOTE-005', title: '审批日志不可删除', priority: 'P2', type: '权限', conf: 79, selected: false, text: '验证操作日志对任何角色均无删除入口，且接口层拒绝删除请求' },
    { no: 'TC-AI-QUOTE-006', title: '并发审批时状态一致', priority: 'P2', type: '性能', conf: 64, selected: false, text: '验证两个审批人同时操作同一订单时状态最终一致，无重复流转' }
  ],
  review: [
    { dim: '需求覆盖', score: 88, note: '核心流程覆盖完整，缺少边界金额与并发场景' },
    { dim: '步骤可执行', score: 92, note: '步骤描述清晰，前置条件完整' },
    { dim: '预期结果', score: 90, note: '预期明确，建议补充异常提示文案' },
    { dim: '数据独立性', score: 74, note: '用例依赖已有账号数据，建议补充数据准备步骤' },
    { dim: '优先级合理', score: 83, note: 'P0/P1 分配合理，缺少 P2 兼容性用例' }
  ],
  models: [
    { id: 'deepseek-v3', name: 'DeepSeek-V3', provider: '私有化部署', type: '通用对话', ctx: '64K', price: '¥1 / 万 Token', status: '启用', active: true, latency: '1.2s', quality: 92 },
    { id: 'qwen-max', name: 'Qwen-Max', provider: '阿里云', type: '通用对话', ctx: '32K', price: '¥2.4 / 万 Token', status: '备用', active: false, latency: '0.9s', quality: 90 },
    { id: 'gpt-4o-mini', name: 'GPT-4o-mini', provider: 'OpenAI', type: '通用对话', ctx: '128K', price: '¥0.9 / 万 Token', status: '停用', active: false, latency: '1.8s', quality: 85 },
    { id: 'embed-bge', name: 'BGE-M3', provider: '私有化部署', type: '向量嵌入', ctx: '8K', price: '内置', status: '启用', active: false, latency: '0.06s', quality: 88 }
  ],
  prompts: [
    { id: 'gen-case', name: '生成测试用例', scene: 'AI 生成用例', version: 'v3', updated: '2026-10-28', enabled: true },
    { id: 'review-case', name: '用例质量评审', scene: '质量评审', version: 'v2', updated: '2026-10-25', enabled: true },
    { id: 'gen-report', name: '生成测试报告', scene: 'AI 测试报告', version: 'v4', updated: '2026-11-01', enabled: true },
    { id: 'fail-rca', name: '失败根因分析', scene: '失败分析', version: 'v2', updated: '2026-10-20', enabled: true },
    { id: 'gen-steps', name: '补全用例步骤', scene: '用例编辑', version: 'v1', updated: '2026-10-12', enabled: false }
  ],
  logs: [
    { time: '2026-11-02 10:42:18', scene: 'AI 生成用例', model: 'DeepSeek-V3', tokens: '3,842', latency: '4.8s', status: '成功', user: '王敏' },
    { time: '2026-11-02 10:31:05', scene: '用例质量评审', model: 'DeepSeek-V3', tokens: '2,110', latency: '3.1s', status: '成功', user: '王敏' },
    { time: '2026-11-02 09:58:44', scene: '失败根因分析', model: 'DeepSeek-V3', tokens: '1,764', latency: '2.6s', status: '成功', user: '陈工' },
    { time: '2026-11-01 17:20:11', scene: 'AI 测试报告', model: 'DeepSeek-V3', tokens: '5,203', latency: '6.4s', status: '成功', user: '王敏' },
    { time: '2026-11-01 16:02:39', scene: 'AI 生成用例', model: 'Qwen-Max', tokens: '3,412', latency: '—', status: '熔断降级', user: '陈工' },
    { time: '2026-11-01 14:47:52', scene: '用例质量评审', model: 'DeepSeek-V3', tokens: '1,988', latency: '2.9s', status: '成功', user: '李工' }
  ],
  usage: {
    month: '2026-11', tokens: 1286000, quota: 5000000, cost: 128.6,
    byScene: [
      { name: '生成用例', pct: 42, tokens: '54.0 万' },
      { name: '质量评审', pct: 24, tokens: '30.9 万' },
      { name: '测试报告', pct: 20, tokens: '25.7 万' },
      { name: '失败分析', pct: 14, tokens: '18.0 万' }
    ],
    daily: [12, 18, 26, 22, 31, 28, 24]
  }
};

const AI_REPLIES = {
  gen: `我读取了你提供的信息，可以按「需求 → 场景拆解 → 用例生成」的流程执行。
    <ul class="m-list">
      <li>已识别 4 个功能场景、2 个异常场景</li>
      <li>建议生成 6 条用例，含优先级与预期结果</li>
      <li>生成后进入「AI 生成用例」页逐条确认再入库</li>
    </ul>
    <div class="m-actions"><button class="m-btn primary" data-act="go-gen">去生成用例</button></div>`,
  review: `我按 5 个维度评审当前用例：
    <div class="m-kv">
      <b>需求覆盖</b><span>88 分 · 缺边界金额与并发场景</span>
      <b>步骤可执行</b><span>92 分 · 前置条件完整</span>
      <b>数据独立性</b><span>74 分 · 建议补充数据准备</span>
    </div>
    综合评分 <b>85</b>，建议补充 2 条边界用例后再归档。
    <div class="m-actions"><button class="m-btn primary" data-act="go-review">查看评审详情</button></div>`,
  fail: `本次计划失败用例集中在审批状态流转：
    <ul class="m-list">
      <li>根因：并发审批时状态覆盖，缺少乐观锁</li>
      <li>影响：2 条 P0 用例失败，阻断发布</li>
      <li>建议：补充并发审批用例并回归</li>
    </ul>
    <div class="m-actions"><button class="m-btn" data-act="go-report">生成测试报告</button></div>`,
  report: `已汇总本次计划执行数据，报告草稿：
    <div class="m-kv">
      <b>执行</b><span>12 条 · 通过 10 · 失败 2</span>
      <b>通过率</b><span>83.3%（较上轮 -6.1pt）</span>
      <b>结论</b><span>不建议发布，需修复并发缺陷</span>
    </div>
    <div class="m-actions"><button class="m-btn primary" data-act="go-report">查看完整报告</button></div>`,
  default: `我可以帮你：生成测试用例、评审用例质量、分析失败根因、生成测试报告，或解答模型设置相关问题。点击下方快捷按钮或直接描述需求。`
};

function $(s, r = document) { return r.querySelector(s); }
function $$(s, r = document) { return Array.from(r.querySelectorAll(s)); }
function esc(s) { return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }

function toast(msg, type = '') {
  const el = document.createElement('div');
  el.className = 'toast ' + type;
  el.textContent = msg;
  $('#toastWrap').appendChild(el);
  setTimeout(() => { el.style.opacity = '0'; el.style.transition = '.2s'; }, 2200);
  setTimeout(() => el.remove(), 2500);
}

function openModal(title, body, foot = '') {
  $('#modalRoot').innerHTML = `
    <div class="modal-mask" data-close></div>
    <div class="modal">
      <div class="modal-head"><h3>${title}</h3><button class="modal-close" data-close>×</button></div>
      <div class="modal-body">${body}</div>
      ${foot ? `<div class="modal-foot">${foot}</div>` : ''}
    </div>`;
  $('#modalRoot').classList.add('open');
  $$('#modalRoot [data-close]').forEach(b => b.onclick = closeModal);
}
function closeModal() { $('#modalRoot').classList.remove('open'); $('#modalRoot').innerHTML = ''; }

/* ---------- Dashboard ---------- */
function renderDashboard(root) {
  const d = DATA.kpi;
  root.innerHTML = `
    <div class="page-head">
      <div>
        <h1>工作台</h1>
        <p>2026 年 11 月 2 日 · 本周 AI 辅助生成 24 条用例，采纳率 78%</p>
      </div>
      <div class="head-actions">
        <button class="outline-btn" id="newPlanBtn">＋ 新建计划</button>
        <button class="primary-btn ai" id="aiGenBtn">✦ AI 生成用例</button>
      </div>
    </div>

    <div class="grid g4" style="margin-bottom:14px">
      ${statCard('项目总数', d.projects, '进行中的测试资产')}
      ${statCard('测试用例', d.cases, `其中 AI 生成 ${d.aiCases} 条`, 'up')}
      ${statCard('测试计划', d.plans, '历史与进行中的计划')}
      ${statCard('整体通过率', d.passRate + '%', '近 30 天执行统计', 'up')}
    </div>

    <div class="grid g3 section">
      <div class="page-card" style="grid-column:span 2">
        <div class="page-header">
          <h3 class="page-title">最近测试计划</h3>
          <button class="link-btn outline-btn" id="goPlans">查看全部</button>
        </div>
        <table class="tbl"><thead><tr><th>计划</th><th>状态</th><th>进度</th><th>通过率</th></tr></thead><tbody>
          ${DATA.plans.slice(0, 4).map(p => `
            <tr class="row-click" data-plan="${p.id}">
              <td><div class="strong">${esc(p.name)}</div><div class="sub">${esc(p.project)} · ${p.start}</div></td>
              <td>${planTag(p.status)}</td>
              <td style="min-width:110px"><div class="progress"><i style="width:${p.total ? Math.round(p.executed / p.total * 100) : 0}%"></i></div><div class="sub">${p.executed}/${p.total}</div></td>
              <td>${p.executed ? Math.round(p.pass / p.executed * 100) + '%' : '-'}</td>
            </tr>`).join('')}
        </tbody></table>
      </div>

      <div class="page-card">
        <div class="page-header"><h3 class="page-title">AI 能力概览</h3><span class="tag t-ai">本月</span></div>
        <div class="dist-row"><span class="dist-name">生成用例</span><div class="progress ai" style="flex:1"><i style="width:78%"></i></div><span class="dist-value">61</span></div>
        <div class="dist-row"><span class="dist-name">质量评审</span><div class="progress ai" style="flex:1"><i style="width:54%"></i></div><span class="dist-value">42</span></div>
        <div class="dist-row"><span class="dist-name">测试报告</span><div class="progress ai" style="flex:1"><i style="width:30%"></i></div><span class="dist-value">12</span></div>
        <div class="dist-row"><span class="dist-name">失败分析</span><div class="progress ai" style="flex:1"><i style="width:22%"></i></div><span class="dist-value">8</span></div>
        <div class="ai-strip" style="margin:12px 0 0">
          <span>✦</span><span>AI 输出仅供参考，采纳后需人工确认入库。</span>
        </div>
      </div>
    </div>

    <div class="page-card section">
      <div class="page-header">
        <h3 class="page-title">AI 采纳趋势</h3>
        <span class="muted">近 7 天 · 生成 vs 采纳</span>
      </div>
      <div class="chart-bars">
        ${[12, 18, 26, 22, 31, 28, 24].map((v, i) => `<div class="cb"><b>${v}</b><i style="height:${v / 31 * 100}%"></i><span>${['10-27','10-28','10-29','10-30','10-31','11-01','11-02'][i]}</span></div>`).join('')}
      </div>
    </div>`;

  $('#aiGenBtn').onclick = () => go('aigen');
  $('#goPlans').onclick = () => go('plans');
  $('#newPlanBtn').onclick = () => toast('新建计划（原型示意）', 'info');
  $$('[data-plan]', root).forEach(r => r.onclick = () => go('plans'));
}

function statCard(label, value, hint, hintCls = '') {
  return `<div class="stat-card"><div class="label">${label}</div><div class="value">${value}</div><div class="hint ${hintCls}">${hint}</div></div>`;
}
function planTag(s) {
  const map = { COMPLETED: ['t-ok', '已完成'], IN_PROGRESS: ['t-blue', '进行中'], PLANNED: ['t-gray', '未开始'] };
  const [c, t] = map[s] || ['t-gray', s];
  return `<span class="tag ${c}">${t}</span>`;
}

/* ---------- Projects ---------- */
function renderProjects(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>项目管理</h1><p>管理测试项目与所属模块，AI 可按项目维度生成用例</p></div>
      <div class="head-actions"><button class="primary-btn" id="newProj">＋ 新建项目</button></div>
    </div>
    <div class="page-card">
      <table class="tbl"><thead><tr><th>项目</th><th>编码</th><th>模块</th><th>用例</th><th>负责人</th><th>AI</th><th>状态</th></tr></thead><tbody>
        ${DATA.projects.map(p => `
          <tr>
            <td><div class="strong">${esc(p.name)}</div><div class="sub">更新于 ${p.updated}</div></td>
            <td><span class="tag t-gray">${p.code}</span></td>
            <td>${p.modules}</td>
            <td>${p.cases}</td>
            <td>${p.owner}</td>
            <td>${p.ai ? '<span class="tag t-ai">已开通</span>' : '<span class="tag t-gray">未开通</span>'}</td>
            <td>${p.status === '归档' ? '<span class="tag t-gray">已归档</span>' : '<span class="tag t-ok">进行中</span>'}</td>
          </tr>`).join('')}
      </tbody></table>
    </div>`;
  $('#newProj').onclick = () => toast('新建项目（原型示意）', 'info');
}

/* ---------- Modules ---------- */
function renderModules(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>模块管理</h1><p>按项目维护模块树，用例归属到具体模块</p></div>
      <div class="head-actions"><button class="primary-btn" id="newMod">＋ 新建模块</button></div>
    </div>
    <div class="page-card">
      <table class="tbl"><thead><tr><th>模块</th><th>所属项目</th><th>上级模块</th><th>用例数</th><th>负责人</th></tr></thead><tbody>
        ${DATA.modules.map(m => `
          <tr>
            <td class="strong">${esc(m.name)}</td>
            <td>${esc(m.project)}</td>
            <td>${m.parent === '-' ? '<span class="muted">顶级</span>' : esc(m.parent)}</td>
            <td>${m.cases}</td>
            <td>${m.owner}</td>
          </tr>`).join('')}
      </tbody></table>
    </div>`;
  $('#newMod').onclick = () => toast('新建模块（原型示意）', 'info');
}

/* ---------- Test cases ---------- */
function renderCases(root) {
  const f = state.caseFilter;
  let rows = DATA.cases.filter(c =>
    (f.priority === 'all' || c.priority === f.priority) &&
    (f.status === 'all' || c.status === f.status) &&
    (!f.aiOnly || c.ai) &&
    (!f.keyword || c.title.includes(f.keyword) || c.no.includes(f.keyword))
  );
  root.innerHTML = `
    <div class="page-head">
      <div><h1>测试用例</h1><p>共 ${DATA.cases.length} 条用例，点击行查看详情</p></div>
      <div class="head-actions">
        <button class="outline-btn ai" id="aiGen">✦ AI 生成</button>
        <button class="primary-btn" id="newCase">＋ 新建用例</button>
      </div>
    </div>

    <div class="toolbar">
      <div class="search-box"><span>⌕</span><input id="kw" placeholder="搜索用例编号 / 标题" value="${esc(f.keyword)}" /></div>
      <div class="chip-group" id="prioChips">
        ${['all', 'P0', 'P1', 'P2'].map(p => `<button class="chip ${f.priority === p ? 'on' : ''}" data-prio="${p}">${p === 'all' ? '全部优先级' : p}</button>`).join('')}
      </div>
      <div class="chip-group">
        <button class="chip ai ${f.aiOnly ? 'on' : ''}" id="aiOnly">✦ 仅 AI 生成</button>
      </div>
    </div>

    <div class="page-card">
      <table class="tbl"><thead><tr><th>用例编号</th><th>标题</th><th>模块</th><th>优先级</th><th>状态</th><th>来源</th><th>AI 评分</th></tr></thead><tbody>
        ${rows.length ? rows.map(c => `
          <tr class="row-click" data-case="${c.no}">
            <td><span class="tag t-gray">${c.no}</span></td>
            <td><div class="strong">${esc(c.title)}</div><div class="sub">${c.steps} 个步骤</div></td>
            <td>${esc(c.module)}</td>
            <td><span class="tag ${c.priority === 'P0' ? 't-err' : c.priority === 'P1' ? 't-warn' : 't-gray'}">${c.priority}</span></td>
            <td>${c.status === 'ACTIVE' ? '<span class="tag t-ok">生效</span>' : '<span class="tag t-gray">草稿</span>'}</td>
            <td>${c.ai ? '<span class="tag t-ai">AI 生成</span>' : '<span class="tag t-gray">人工</span>'}</td>
            <td><span class="conf">${c.aiScore}</span></td>
          </tr>`).join('') : '<tr><td colspan="7"><div class="empty">没有匹配的用例</div></td></tr>'}
      </tbody></table>
    </div>`;

  $('#kw').oninput = (e) => { state.caseFilter.keyword = e.target.value; renderCases(root); };
  $$('#prioChips .chip').forEach(b => b.onclick = () => { state.caseFilter.priority = b.dataset.prio; renderCases(root); });
  $('#aiOnly').onclick = () => { state.caseFilter.aiOnly = !state.caseFilter.aiOnly; renderCases(root); };
  $('#aiGen').onclick = () => go('aigen');
  $('#newCase').onclick = () => toast('新建用例（原型示意）', 'info');
  $$('[data-case]', root).forEach(r => r.onclick = () => showCaseDetail(r.dataset.case));
}

function showCaseDetail(no) {
  const c = DATA.cases.find(x => x.no === no);
  if (!c) return;
  openModal(`用例详情 · ${c.no}`, `
    <div class="form-row"><div class="form-label">标题</div><div>${esc(c.title)}</div></div>
    <div class="form-row"><div class="form-label">模块</div><div>${esc(c.module)}</div></div>
    <div class="form-row"><div class="form-label">优先级</div><div><span class="tag ${c.priority === 'P0' ? 't-err' : 't-warn'}">${c.priority}</span></div></div>
    <div class="form-row"><div class="form-label">前置条件</div><div>用户已登录，拥有对应模块权限</div></div>
    <div class="form-row"><div class="form-label">测试步骤</div><div>
      <ol style="margin:0;padding-left:18px;line-height:2">
        <li>进入 ${esc(c.module)} 页面</li>
        <li>执行核心操作并触发表单校验</li>
        <li>检查页面提示与数据落库结果</li>
      </ol>
    </div></div>
    <div class="form-row"><div class="form-label">AI 质量评分</div><div><span class="conf">${c.aiScore}</span> ${c.ai ? '· AI 生成，已人工确认' : '· 人工编写'}</div></div>
  `, `<button class="outline-btn" data-close>关闭</button><button class="primary-btn ai" id="caseReview">✦ AI 评审此用例</button>`);
  $$('#modalRoot [data-close]').forEach(b => b.onclick = closeModal);
  $('#caseReview').onclick = () => { closeModal(); go('aireview'); };
}

/* ---------- Plans ---------- */
function renderPlans(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>测试计划</h1><p>组织用例执行，AI 可辅助分析与生成报告</p></div>
      <div class="head-actions"><button class="primary-btn" id="newPlan">＋ 新建计划</button></div>
    </div>
    <div class="page-card">
      <table class="tbl"><thead><tr><th>计划</th><th>状态</th><th>进度</th><th>通过率</th><th>周期</th><th>AI</th></tr></thead><tbody>
        ${DATA.plans.map(p => `
          <tr class="row-click" data-plan="${p.id}">
            <td><div class="strong">${esc(p.name)}</div><div class="sub">${esc(p.project)}</div></td>
            <td>${planTag(p.status)}</td>
            <td style="min-width:120px"><div class="progress"><i style="width:${p.total ? Math.round(p.executed / p.total * 100) : 0}%"></i></div><div class="sub">${p.executed}/${p.total}</div></td>
            <td>${p.executed ? Math.round(p.pass / p.executed * 100) + '%' : '-'}</td>
            <td class="sub" style="color:var(--ink-2)">${p.start} ~ ${p.end}</td>
            <td>${p.ai ? '<span class="tag t-ai">AI 报告</span>' : '<span class="tag t-gray">-</span>'}</td>
          </tr>`).join('')}
      </tbody></table>
    </div>`;
  $('#newPlan').onclick = () => toast('新建计划（原型示意）', 'info');
  $$('[data-plan]', root).forEach(r => r.onclick = () => showPlanDetail(r.dataset.plan));
}

function showPlanDetail(id) {
  const p = DATA.plans.find(x => x.id == id);
  if (!p) return;
  openModal(`计划详情 · ${p.name}`, `
    <div class="form-row"><div class="form-label">项目</div><div>${esc(p.project)}</div></div>
    <div class="form-row"><div class="form-label">状态</div><div>${planTag(p.status)}</div></div>
    <div class="form-row"><div class="form-label">周期</div><div>${p.start} ~ ${p.end}</div></div>
    <div class="form-row"><div class="form-label">执行进度</div><div>${p.executed}/${p.total} · 通过 ${p.pass}</div></div>
    <div class="ai-strip"><span>✦</span><span>AI 可根据执行结果自动生成测试报告与失败根因分析。</span></div>
  `, `<button class="outline-btn" data-close>关闭</button><button class="primary-btn ai" id="planReport">✦ AI 生成报告</button>`);
  $$('#modalRoot [data-close]').forEach(b => b.onclick = closeModal);
  $('#planReport').onclick = () => { closeModal(); go('aireport'); };
}

/* ---------- AI generate ---------- */
function renderAIGen(root) {
  const r = DATA.genResult;
  root.innerHTML = `
    <div class="page-head">
      <div><h1>AI 生成用例</h1><p>输入需求或上传文档，AI 拆解场景并生成结构化用例，确认后入库</p></div>
      <div class="head-actions"><span class="tag t-ai">当前模型 DeepSeek-V3</span></div>
    </div>

    <div class="split-lg">
      <div>
        <div class="page-card">
          <div class="page-header"><h3 class="page-title">输入需求</h3><button class="outline-btn" id="uploadReq">＋ 上传文档</button></div>
          <textarea class="textarea" id="reqText" style="min-height:130px">需求：报价单提交后按金额自动流转审批。金额 ≤ 3000 元自动放行；金额 > 10000 元需报价经理和总经理审批；客户等级 A 且非标件需增加工艺复核节点。审批拒绝后报价单退回草稿并记录原因，操作日志不可删除。</textarea>
          <div class="inline" style="margin-top:12px">
            <select class="select" id="genProject" style="width:auto"><option>报价管理平台</option><option>测试管理系统</option><option>设备主数据</option></select>
            <select class="select" id="genType" style="width:auto"><option>功能测试</option><option>异常测试</option><option>接口测试</option><option>性能测试</option></select>
            <select class="select" id="genCount" style="width:auto"><option>生成 6 条</option><option>生成 10 条</option><option>生成 15 条</option></select>
            <button class="primary-btn ai" id="genRun">✦ 开始生成</button>
          </div>
          <div class="form-help">AI 会输出场景拆解、优先级建议与预期结果；生成结果需逐条确认后才写入用例库。</div>
        </div>

        <div class="page-card section">
          <div class="page-header">
            <h3 class="page-title">生成结果 · ${r.length} 条</h3>
            <div class="inline">
              <button class="ghost-btn" id="genAll">全选</button>
              <button class="primary-btn" id="genSave">写入用例库</button>
            </div>
          </div>
          <table class="tbl"><thead><tr><th style="width:34px"></th><th>用例</th><th>优先级</th><th>类型</th><th>置信度</th></tr></thead><tbody>
            ${r.map((c, i) => `
              <tr>
                <td><button class="switch ${c.selected ? 'on' : ''}" data-sel="${i}"></button></td>
                <td><div class="strong">${esc(c.title)}</div><div class="sub">${esc(c.text)}</div></td>
                <td><span class="tag ${c.priority === 'P0' ? 't-err' : c.priority === 'P1' ? 't-warn' : 't-gray'}">${c.priority}</span></td>
                <td><span class="tag t-gray">${c.type}</span></td>
                <td><span class="conf">${c.conf}%</span></td>
              </tr>`).join('')}
          </tbody></table>
          <div class="ai-strip" style="margin-top:12px"><span>✦</span><span>低置信度（<70%）用例建议人工复核后再入库。</span></div>
        </div>
      </div>

      <div>
        <div class="page-card">
          <div class="page-header"><h3 class="page-title">AI 分析过程</h3></div>
          <div class="info-strip"><span class="dot">1</span><span>读取需求文本，识别 4 个功能规则、1 个权限规则</span></div>
          <div class="info-strip"><span class="dot">2</span><span>按边界值拆解金额阈值：≤3000 / 3000~10000 / >10000</span></div>
          <div class="info-strip"><span class="dot">3</span><span>补充异常场景：审批拒绝、并发流转</span></div>
          <div class="info-strip"><span class="dot">4</span><span>为每条用例生成前置条件、步骤、预期结果、优先级</span></div>
          <div class="ai-strip" style="margin:0"><span>✦</span><span>已在 4.8s 内完成，消耗 3,842 Token</span></div>
        </div>

        <div class="page-card section">
          <div class="page-header"><h3 class="page-title">复用资产</h3></div>
          <div class="muted">相似历史用例</div>
          <div class="info-strip" style="margin-top:8px"><span class="dot">◎</span><span>审批流金额阈值用例（3 条，相似度 0.91）</span></div>
          <div class="info-strip" style="margin:0"><span class="dot">◎</span><span>报价单状态流转用例（2 条，相似度 0.84）</span></div>
        </div>
      </div>
    </div>`;

  $('#genRun').onclick = () => {
    toast('AI 正在分析需求并生成用例…', 'ai');
    setTimeout(() => toast('已生成 6 条用例，请确认入库', 'ok'), 1200);
  };
  $$('[data-sel]', root).forEach(b => b.onclick = () => {
    const i = +b.dataset.sel; r[i].selected = !r[i].selected; b.classList.toggle('on', r[i].selected);
  });
  $('#genAll').onclick = () => { r.forEach(c => c.selected = true); renderAIGen(root); };
  $('#genSave').onclick = () => {
    const n = r.filter(c => c.selected).length;
    if (!n) return toast('请至少选择一条用例', 'err');
    toast(`已写入用例库 ${n} 条用例`, 'ok');
  };
  $('#uploadReq').onclick = () => { $('#fileInput').click(); focusDock(); };
}

/* ---------- AI review ---------- */
function renderAIReview(root) {
  const total = Math.round(DATA.review.reduce((s, x) => s + x.score, 0) / DATA.review.length);
  root.innerHTML = `
    <div class="page-head">
      <div><h1>用例质量评审</h1><p>AI 从覆盖度、可执行性、预期结果等维度打分并给出改进建议</p></div>
      <div class="head-actions"><button class="primary-btn ai" id="reviewRun">✦ 评审选中用例</button></div>
    </div>

    <div class="split-lg">
      <div class="page-card">
        <div class="page-header"><h3 class="page-title">评审对象</h3><span class="muted">已选 5 条用例</span></div>
        <table class="tbl"><thead><tr><th>用例</th><th>优先级</th><th>来源</th></tr></thead><tbody>
          ${DATA.cases.slice(0, 5).map(c => `<tr><td><div class="strong">${esc(c.title)}</div><div class="sub">${c.no}</div></td><td>${c.priority}</td><td>${c.ai ? '<span class="tag t-ai">AI</span>' : '<span class="tag t-gray">人工</span>'}</td></tr>`).join('')}
        </tbody></table>
        <div class="ai-strip" style="margin-top:12px"><span>✦</span><span>评审结果仅作参考，不自动修改用例内容。</span></div>
      </div>

      <div class="page-card">
        <div class="page-header"><h3 class="page-title">综合评分</h3><span class="tag t-ai">AI</span></div>
        <div style="display:flex;align-items:center;gap:16px">
          <div class="score-ring" style="--p:${total}%"><span>${total}</span></div>
          <div>
            <div class="strong" style="font-size:13px">整体质量良好</div>
            <div class="muted" style="margin-top:4px">建议补充边界与并发场景后归档</div>
          </div>
        </div>
        <div style="margin-top:16px">
          ${DATA.review.map(rv => `
            <div style="margin-bottom:12px">
              <div style="display:flex;justify-content:space-between;font-size:11.5px;margin-bottom:4px">
                <span class="strong">${rv.dim}</span><span class="conf">${rv.score}</span>
              </div>
              <div class="progress ai"><i style="width:${rv.score}%"></i></div>
              <div class="sub" style="margin-top:4px">${rv.note}</div>
            </div>`).join('')}
        </div>
      </div>
    </div>

    <div class="page-card section">
      <div class="page-header"><h3 class="page-title">改进建议</h3><button class="outline-btn" id="applySuggest">一键采纳建议</button></div>
      <div class="info-strip"><span class="dot">!</span><span>缺少金额边界用例：建议补充 2999 / 3000 / 3001 三档验证</span></div>
      <div class="info-strip"><span class="dot">!</span><span>缺少并发场景：两个审批人同时操作同一报价单</span></div>
      <div class="info-strip" style="margin:0"><span class="dot">!</span><span>数据独立性不足：用例依赖已有测试账号，建议补充专用测试数据</span></div>
    </div>`;

  $('#reviewRun').onclick = () => { toast('AI 正在评审 5 条用例…', 'ai'); setTimeout(() => toast('评审完成，综合评分 ' + total, 'ok'), 1200); };
  $('#applySuggest').onclick = () => toast('已生成 3 条改进建议草稿，待人工确认', 'ok');
}

/* ---------- AI report ---------- */
function renderAIReport(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>AI 测试报告</h1><p>基于计划执行结果自动生成测试报告，含结论、风险与发布建议</p></div>
      <div class="head-actions">
        <select class="select" id="reportPlan" style="width:auto">
          <option>报价平台图纸识别回归</option><option>测试管理第一次功能测试</option><option>登录专项测试</option>
        </select>
        <button class="primary-btn ai" id="reportGen">✦ 生成报告</button>
      </div>
    </div>

    <div class="split-lg">
      <div class="page-card">
        <div class="page-header"><h3 class="page-title">测试报告</h3><span class="tag t-ai">AI 草稿</span></div>
        <h4 style="margin:0 0 10px">1. 执行概况</h4>
        <div class="grid g3" style="margin-bottom:14px">
          ${statCard('用例总数', 12, '')}
          ${statCard('执行 / 通过', '12 / 10', '通过率 83.3%')}
          ${statCard('失败', 2, '均为 P0 阻塞', '')}
        </div>
        <h4 style="margin:14px 0 8px">2. 失败用例分析</h4>
        <table class="tbl"><thead><tr><th>用例</th><th>优先级</th><th>失败原因</th></tr></thead><tbody>
          <tr><td class="strong">报价单金额 > 10000 进入经理审批</td><td><span class="tag t-err">P0</span></td><td>并发审批时状态被覆盖</td></tr>
          <tr><td class="strong">审批拒绝后报价单退回草稿</td><td><span class="tag t-err">P0</span></td><td>拒绝原因未落库</td></tr>
        </tbody></table>
        <h4 style="margin:14px 0 8px">3. AI 结论与建议</h4>
        <div class="ai-strip" style="margin:0"><span>✦</span><span>本次通过率 83.3%，存在 2 个 P0 阻塞缺陷，<b>不建议发布</b>。建议修复并发审批状态覆盖问题并补充乐观锁，回归通过后再发布。</span></div>
      </div>

      <div>
        <div class="page-card">
          <div class="page-header"><h3 class="page-title">报告参数</h3></div>
          <div class="form-row"><div class="form-label">报告类型</div><div><select class="select"><option>功能测试报告</option><option>回归测试报告</option><option>专项测试报告</option></select></div></div>
          <div class="form-row"><div class="form-label">包含内容</div><div class="muted" style="line-height:1.9">执行概况 · 失败分析 · AI 结论 · 风险提示 · 发布建议</div></div>
          <div class="form-row"><div class="form-label">导出格式</div><div class="inline"><span class="tag t-gray">PDF</span><span class="tag t-gray">Word</span><span class="tag t-gray">Markdown</span></div></div>
          <button class="primary-btn" style="width:100%;margin-top:12px" id="exportReport">导出报告</button>
        </div>

        <div class="page-card section">
          <div class="page-header"><h3 class="page-title">发布决策</h3></div>
          <div class="info-strip" style="background:#fdeeee;border-color:#f3cfcf;color:#8d2b2b"><span class="dot" style="background:#f3cfcf;color:#8d2b2b">!</span><span>存在阻塞缺陷，AI 建议暂缓发布</span></div>
          <div class="inline"><button class="outline-btn">强制发布（需审批）</button><button class="primary-btn">创建修复任务</button></div>
        </div>
      </div>
    </div>`;

  $('#reportGen').onclick = () => { toast('AI 正在汇总执行数据生成报告…', 'ai'); setTimeout(() => toast('报告已生成', 'ok'), 1300); };
  $('#exportReport').onclick = () => toast('报告已导出（原型示意）', 'ok');
}

/* ---------- AI models ---------- */
function renderAIModels(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>模型设置</h1><p>配置大语言模型提供商、密钥、路由与降级策略</p></div>
      <div class="head-actions"><button class="outline-btn" id="testConn">测试连接</button><button class="primary-btn" id="addModel">＋ 添加模型</button></div>
    </div>

    <div class="page-card">
      <div class="page-header"><h3 class="page-title">模型列表</h3><span class="muted">当前默认：DeepSeek-V3</span></div>
      <div class="grid g2">
        ${DATA.models.map(m => `
          <div class="model-card ${m.active ? 'active' : ''}">
            ${m.active ? '<span class="model-badge tag t-ai">默认</span>' : ''}
            <div class="model-head"><strong>${esc(m.name)}</strong><span class="tag ${m.status === '启用' ? 't-ok' : m.status === '备用' ? 't-warn' : 't-gray'}">${m.status}</span></div>
            <div class="model-meta">
              提供商：${esc(m.provider)}<br>
              类型：${esc(m.type)} · 上下文 ${m.ctx}<br>
              计费：${m.price}<br>
              延迟：${m.latency} · 质量分 ${m.quality}
            </div>
            <div class="inline" style="margin-top:10px">
              ${m.active ? '<button class="ghost-btn" disabled>已设为默认</button>' : `<button class="outline-btn" data-default="${m.id}">设为默认</button>`}
              <button class="ghost-btn" data-edit="${m.id}">编辑</button>
            </div>
          </div>`).join('')}
      </div>
    </div>

    <div class="split-lg section">
      <div class="page-card">
        <div class="page-header"><h3 class="page-title">路由与降级</h3></div>
        <div class="form-row"><div class="form-label">默认模型</div><div><select class="select" id="defaultModel">${DATA.models.map(m => `<option ${m.active ? 'selected' : ''}>${m.name}</option>`).join('')}</select><div class="form-help">所有 AI 场景默认使用该模型</div></div></div>
        <div class="form-row"><div class="form-label">备用模型</div><div><select class="select"><option>Qwen-Max</option><option>GPT-4o-mini</option></select><div class="form-help">主模型失败或超时自动切到备用模型</div></div></div>
        <div class="form-row"><div class="form-label">熔断降级</div><div class="inline"><button class="switch on" id="swFallback"></button><span class="muted">连续失败 3 次后自动降级，不阻塞业务流程</span></div></div>
        <div class="form-row"><div class="form-label">场景路由</div><div class="muted" style="line-height:2">生成用例 → DeepSeek-V3<br>质量评审 → DeepSeek-V3<br>嵌入检索 → BGE-M3</div></div>
      </div>

      <div class="page-card">
        <div class="page-header"><h3 class="page-title">连接参数</h3></div>
        <div class="form-row"><div class="form-label">API Base URL</div><div><input class="input" value="https://api.internal/deepseek/v1" /></div></div>
        <div class="form-row"><div class="form-label">API Key</div><div><input class="input" type="password" value="sk-********" /><div class="form-help">密钥加密存储，仅用于服务端调用</div></div></div>
        <div class="form-row"><div class="form-label">温度</div><div><input class="input sm" value="0.3" style="width:100px" /></div></div>
        <div class="form-row"><div class="form-label">单次最大 Token</div><div><input class="input sm" value="8192" style="width:120px" /></div></div>
        <div class="form-row"><div class="form-label">超时</div><div><input class="input sm" value="60s" style="width:100px" /></div></div>
        <button class="primary-btn" style="width:100%;margin-top:10px" id="saveModel">保存配置</button>
      </div>
    </div>`;

  $$('[data-default]', root).forEach(b => b.onclick = () => { toast('已设为默认模型（原型示意）', 'ok'); });
  $$('[data-edit]', root).forEach(b => b.onclick = () => toast('编辑模型（原型示意）', 'info'));
  $('#testConn').onclick = () => { toast('正在测试连接…', 'ai'); setTimeout(() => toast('连接成功 · 延迟 1.2s', 'ok'), 900); };
  $('#addModel').onclick = () => toast('添加模型（原型示意）', 'info');
  $('#saveModel').onclick = () => toast('模型配置已保存', 'ok');
  $('#swFallback').onclick = (e) => e.currentTarget.classList.toggle('on');
}

/* ---------- Prompts ---------- */
function renderAIPrompts(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>Prompt 模板</h1><p>集中管理各 AI 场景的提示词模板与版本</p></div>
      <div class="head-actions"><button class="primary-btn" id="newPrompt">＋ 新建模板</button></div>
    </div>
    <div class="page-card">
      <table class="tbl"><thead><tr><th>模板名称</th><th>场景</th><th>版本</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead><tbody>
        ${DATA.prompts.map(p => `
          <tr>
            <td class="strong">${esc(p.name)}</td>
            <td>${esc(p.scene)}</td>
            <td><span class="tag t-gray">${p.version}</span></td>
            <td>${p.enabled ? '<span class="tag t-ok">启用</span>' : '<span class="tag t-gray">停用</span>'}</td>
            <td class="muted">${p.updated}</td>
            <td><button class="ghost-btn" data-prompt="${p.id}">编辑</button></td>
          </tr>`).join('')}
      </tbody></table>
    </div>`;
  $('#newPrompt').onclick = () => toast('新建模板（原型示意）', 'info');
  $$('[data-prompt]', root).forEach(b => b.onclick = () => {
    const p = DATA.prompts.find(x => x.id === b.dataset.prompt);
    openModal(`编辑模板 · ${p.name}`, `
      <div class="form-row"><div class="form-label">模板名称</div><div><input class="input" value="${esc(p.name)}" /></div></div>
      <div class="form-row"><div class="form-label">适用场景</div><div><input class="input" value="${esc(p.scene)}" /></div></div>
      <div class="form-row"><div class="form-label">Prompt</div><div><textarea class="textarea" style="min-height:140px">你是一名资深测试工程师。请根据以下需求生成结构化测试用例，输出 JSON：包含 title、priority、type、precondition、steps、expected。

需求：
{{requirement}}</textarea></div></div>
      <div class="form-row"><div class="form-label">变量</div><div class="muted">{{requirement}} · {{project}} · {{module}}</div></div>
    `, `<button class="outline-btn" data-close>取消</button><button class="primary-btn" id="savePrompt">保存</button>`);
    $$('#modalRoot [data-close]').forEach(x => x.onclick = closeModal);
    $('#savePrompt').onclick = () => { closeModal(); toast('模板已保存', 'ok'); };
  });
}

/* ---------- Logs ---------- */
function renderAILogs(root) {
  root.innerHTML = `
    <div class="page-head">
      <div><h1>调用日志</h1><p>记录每次 AI 调用的场景、模型、Token 与耗时，便于审计与排障</p></div>
      <div class="head-actions"><button class="outline-btn" id="exportLog">导出日志</button></div>
    </div>
    <div class="toolbar">
      <div class="search-box"><span>⌕</span><input placeholder="搜索用户 / 场景" /></div>
      <select class="select-sm"><option>全部状态</option><option>成功</option><option>失败</option><option>熔断降级</option></select>
      <select class="select-sm"><option>全部模型</option><option>DeepSeek-V3</option><option>Qwen-Max</option></select>
    </div>
    <div class="page-card">
      <table class="tbl"><thead><tr><th>时间</th><th>场景</th><th>模型</th><th>Token</th><th>耗时</th><th>状态</th><th>用户</th></tr></thead><tbody>
        ${DATA.logs.map(l => `
          <tr>
            <td class="muted">${l.time}</td>
            <td class="strong">${esc(l.scene)}</td>
            <td>${esc(l.model)}</td>
            <td>${l.tokens}</td>
            <td>${l.latency}</td>
            <td><span class="tag ${l.status === '成功' ? 't-ok' : l.status === '熔断降级' ? 't-warn' : 't-err'}">${l.status}</span></td>
            <td>${l.user}</td>
          </tr>`).join('')}
      </tbody></table>
    </div>`;
  $('#exportLog').onclick = () => toast('日志已导出（原型示意）', 'ok');
}

/* ---------- Usage ---------- */
function renderAIUsage(root) {
  const u = DATA.usage;
  root.innerHTML = `
    <div class="page-head">
      <div><h1>用量与成本</h1><p>监控 Token 消耗、费用与各场景占比，支持配额预警</p></div>
      <div class="head-actions"><select class="select" style="width:auto"><option>2026 年 11 月</option><option>2026 年 10 月</option></select></div>
    </div>

    <div class="grid g4" style="margin-bottom:14px">
      ${statCard('本月 Token', '128.6 万', '配额 500 万 · 已用 26%')}
      ${statCard('本月费用', '¥' + u.cost, '按实际调用计费')}
      ${statCard('调用次数', '1,842', '日均 61 次', 'up')}
      ${statCard('平均延迟', '2.9s', 'P95：6.4s')}
    </div>

    <div class="split-lg">
      <div class="page-card">
        <div class="page-header"><h3 class="page-title">近 7 天调用趋势</h3><span class="muted">单位：次</span></div>
        <div class="chart-bars">
          ${u.daily.map((v, i) => `<div class="cb"><b>${v}</b><i style="height:${v / 31 * 100}%"></i><span>${['10-27','10-28','10-29','10-30','10-31','11-01','11-02'][i]}</span></div>`).join('')}
        </div>
      </div>
      <div class="page-card">
        <div class="page-header"><h3 class="page-title">场景占比</h3><span class="tag t-ai">Token</span></div>
        ${u.byScene.map(s => `
          <div class="dist-row"><span class="dist-name">${s.name}</span><div class="progress ai" style="flex:1"><i style="width:${s.pct}%"></i></div><span class="dist-value">${s.pct}%</span></div>
        `).join('')}
        <div class="info-strip" style="margin-top:12px"><span class="dot">i</span><span>配额剩余 74%，按当前增速预计月末使用 480 万 Token。</span></div>
      </div>
    </div>

    <div class="page-card section">
      <div class="page-header"><h3 class="page-title">配额与预算</h3><button class="outline-btn" id="editQuota">调整配额</button></div>
      <div class="form-row"><div class="form-label">月度 Token 配额</div><div>500 万 <span class="muted">· 已用 128.6 万</span></div></div>
      <div class="form-row"><div class="form-label">用量预警阈值</div><div>80% 时通知管理员</div></div>
      <div class="form-row"><div class="form-label">超配额策略</div><div><span class="tag t-warn">降级到备用模型</span></div></div>
    </div>`;
  $('#editQuota').onclick = () => toast('调整配额（原型示意）', 'info');
}

/* ---------- AI dock ---------- */
function dockOpen() {
  document.body.classList.remove('dock-closed');
  state.aiOpen = true;
  $('#aiDock').classList.remove('collapsed');
  if (window.innerWidth <= 980) $('#dockMask').classList.add('open');
}
function dockClose() {
  state.aiOpen = false;
  $('#aiDock').classList.add('collapsed');
  $('#dockMask').classList.remove('open');
  document.body.classList.add('dock-closed');
}
function dockToggle() { if (state.aiOpen) dockClose(); else dockOpen(); }

function addMsg(who, html, opts = {}) {
  const el = document.createElement('div');
  el.className = 'msg ' + who;
  el.innerHTML = (who === 'ai' ? '<span class="m-tag">✦ TCM AI</span><br>' : '') + html;
  $('#chatScroll').appendChild(el);
  $('#chatScroll').scrollTop = $('#chatScroll').scrollHeight;
  return el;
}
function aiTyping() {
  const el = document.createElement('div');
  el.className = 'typing';
  el.id = 'typingIndicator';
  el.innerHTML = '<i></i><i></i><i></i>';
  $('#chatScroll').appendChild(el);
  $('#chatScroll').scrollTop = $('#chatScroll').scrollHeight;
}
function aiSay(html, delay = 900) {
  aiTyping();
  setTimeout(() => { $('#typingIndicator')?.remove(); addMsg('ai', html); }, delay);
}
function sendAI(text) {
  const t = (text || '').trim();
  if (!t) return;
  addMsg('me', esc(t));
  let key = 'default';
  if (/生成|用例|需求/.test(t)) key = 'gen';
  else if (/评审|质量|覆盖/.test(t)) key = 'review';
  else if (/失败|根因|缺陷/.test(t)) key = 'fail';
  else if (/报告|总结|结论/.test(t)) key = 'report';
  aiSay(AI_REPLIES[key]);
}
function handleFiles(files) {
  if (!files || !files.length) return;
  Array.from(files).slice(0, 3).forEach(f => {
    const ext = (f.name.split('.').pop() || 'DOC').toUpperCase().slice(0, 4);
    addMsg('me', `<div class="m-file"><div class="fi">${ext}</div><div class="fm"><strong>${esc(f.name)}</strong><span>${(f.size / 1024).toFixed(0)} KB · 正在解析</span></div></div>`);
  });
  aiTyping();
  setTimeout(() => {
    $('#typingIndicator')?.remove();
    addMsg('ai', `已读取文档，识别出可测试点：
      <div class="m-kv">
        <b>功能规则</b><span>4 条 · 含金额阈值审批</span>
        <b>权限规则</b><span>1 条 · 操作日志不可删除</span>
        <b>异常场景</b><span>2 条 · 审批拒绝、并发</span>
      </div>
      需要我据此生成测试用例吗？
      <div class="m-actions"><button class="m-btn primary" data-act="go-gen">生成用例</button></div>`);
  }, 1100);
}
function showWelcome() {
  addMsg('ai', `你好，我是 TCM AI 助手。我可以帮你生成测试用例、评审质量、分析失败根因、生成测试报告。
    <ul class="m-list"><li>直接输入需求，或拖拽需求 / 接口文档到这里</li><li>生成与评审结果均需人工确认后才写入用例库</li><li>模型可在「模型设置」中切换与配置降级</li></ul>`);
}
function focusDock() {
  dockOpen();
  if (window.innerWidth > 980) $('#aiInput').focus();
}

function bindDock() {
  $('#dockClose').onclick = dockClose;
  $('#dockToggle').onclick = dockToggle;
  $('#dockMask').onclick = dockClose;
  $('#dockReset').onclick = () => {
    $('#chatScroll').innerHTML = '';
    showWelcome();
    toast('对话已重置');
  };
  $$('#chatQuick button').forEach(b => b.onclick = () => sendAI(b.dataset.prompt));
  $('#attachBtn').onclick = () => $('#fileInput').click();
  $('#fileInput').onchange = (e) => { handleFiles(e.target.files); e.target.value = ''; };
  $('#aiSend').onclick = () => { const t = $('#aiInput').value; $('#aiInput').value = ''; sendAI(t); };
  $('#aiInput').onkeydown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      const t = e.target.value; e.target.value = '';
      if (t.trim()) sendAI(t); else $('#fileInput').click();
    }
  };
  $('#chatScroll').onclick = (e) => {
    const b = e.target.closest('[data-act]');
    if (!b) return;
    const act = b.dataset.act;
    if (act === 'go-gen') go('aigen');
    else if (act === 'go-review') go('aireview');
    else if (act === 'go-report') go('aireport');
  };
  const chatDrop = $('#chatDrop');
  const enter = (e) => { e.preventDefault(); chatDrop.classList.add('hot'); };
  const leave = () => chatDrop.classList.remove('hot');
  ['dragenter', 'dragover'].forEach(ev => $('#aiDock').addEventListener(ev, enter));
  ['dragleave', 'drop'].forEach(ev => $('#aiDock').addEventListener(ev, leave));
  $('#aiDock').addEventListener('drop', (e) => { e.preventDefault(); handleFiles(e.dataTransfer.files); });
}

/* ---------- routing ---------- */
const ROUTES = {
  dashboard: { crumb: '工作台', render: renderDashboard },
  projects: { crumb: '测试资产 / 项目管理', render: renderProjects },
  modules: { crumb: '测试资产 / 模块管理', render: renderModules },
  cases: { crumb: '测试资产 / 测试用例', render: renderCases },
  plans: { crumb: '测试资产 / 测试计划', render: renderPlans },
  aigen: { crumb: 'AI 能力 / AI 生成用例', render: renderAIGen },
  aireview: { crumb: 'AI 能力 / 用例质量评审', render: renderAIReview },
  aireport: { crumb: 'AI 能力 / AI 测试报告', render: renderAIReport },
  aimodels: { crumb: 'AI 平台 / 模型设置', render: renderAIModels },
  aiprompts: { crumb: 'AI 平台 / Prompt 模板', render: renderAIPrompts },
  ailogs: { crumb: 'AI 平台 / 调用日志', render: renderAILogs },
  aiusage: { crumb: 'AI 平台 / 用量与成本', render: renderAIUsage }
};

function go(view) {
  state.view = view;
  $$('#nav .nav-item').forEach(a => a.classList.toggle('active', a.dataset.view === view));
  const r = ROUTES[view];
  if (!r) return;
  $('#crumb').textContent = r.crumb;
  const v = $('#view');
  v.innerHTML = '';
  v.classList.remove('view-enter');
  void v.offsetWidth;
  v.classList.add('view-enter');
  r.render(v);
  v.scrollTop = 0;
}

function init() {
  $$('#nav .nav-item').forEach(a => a.onclick = () => { go(a.dataset.view); closeSidebar(); });
  const sidebar = $('.sidebar'), mask = $('#sidebarMask');
  const openSidebar = () => { sidebar.classList.add('open'); mask.classList.add('open'); };
  const closeSidebar = () => { sidebar.classList.remove('open'); mask.classList.remove('open'); };
  $('#menuBtn').onclick = openSidebar;
  mask.onclick = closeSidebar;

  bindDock();
  if (window.innerWidth <= 980) dockClose();
  else state.aiOpen = true;
  showWelcome();

  $('#projectBtn').onclick = () => toast('切换项目（原型示意）', 'info');
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeModal(); });
  go('dashboard');
}

document.addEventListener('DOMContentLoaded', init);