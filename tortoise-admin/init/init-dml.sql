INSERT INTO db_tortoise.tortoise_sys_user (email, password_hash, role_code) VALUES ('admin@gmail.com', '123456', 1);

INSERT INTO db_tortoise.tortoise_llm_config (id, create_user_id, config_name, model_name, api_key, api_url, input_unit_price, output_unit_price, cache_price, status, description, temperature) VALUES (1, 1, '火山引擎', 'doubao', 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'https://ark.cn-beijing.volces.com/api/v3/chat/completions', 0.000100, 0.000100, 0.000100, 1, '火山引擎豆包大模型', 1.00);
INSERT INTO db_tortoise.tortoise_llm_config (id, create_user_id, config_name, model_name, api_key, api_url, input_unit_price, output_unit_price, cache_price, status, description, temperature) VALUES (2, 1, '深度求索', 'deepseek', 'xxxxxxx', 'https://api.deepseek.com/chat/completions', 0.000100, 0.000100, 0.000000, 1, '深度求索系列模型', 1.00);


INSERT INTO db_tortoise.tortoise_memory_policy (id, name, llm_config_id, create_user_id, config, is_deleted) VALUES (1, '截断策略', null, 1, '{"type":1,"threshold":10,"batchAnalysisSize":0,"withHistory":true,"maxLength":null,"basePrompt":"","overLengthPrompt":"","updatePrompt":""}', 0);
INSERT INTO db_tortoise.tortoise_memory_policy (id, name, llm_config_id, create_user_id, config, is_deleted) VALUES (2, '摘要策略', 1, 1, '{"type":2,"threshold":10,"batchAnalysisSize":10,"withHistory":true,"maxLength":1000,"basePrompt":"","overLengthPrompt":"","updatePrompt":""}', 0);
INSERT INTO db_tortoise.tortoise_memory_policy (id, name, llm_config_id, create_user_id, config, is_deleted) VALUES (3, '时序图谱策略', 2, 1, '{"type":3,"threshold":10,"batchAnalysisSize":10,"withHistory":true,"maxLength":1000,"basePrompt":"","overLengthPrompt":"","updatePrompt":""}', 0);


INSERT INTO db_tortoise.tortoise_chat_profile (id, name, llm_config_id, memory_policy_id, sk, is_default, create_user_id, status, is_deleted) VALUES (1, '聊天-豆包-记忆-截断', 1, 1, 'sk-428acddd-6ced-4e33-a9cb-ec96c980cf02', 0, 1, 1, 0);
INSERT INTO db_tortoise.tortoise_chat_profile (id, name, llm_config_id, memory_policy_id, sk, is_default, create_user_id, status, is_deleted) VALUES (2, '聊天-豆包-记忆-摘要', 1, 2, 'sk-c09eefbb-4172-4724-8af1-e14eb86d8a40', 0, 1, 1, 0);
INSERT INTO db_tortoise.tortoise_chat_profile (id, name, llm_config_id, memory_policy_id, sk, is_default, create_user_id, status, is_deleted) VALUES (3, '聊天-豆包-记忆-图谱', 1, 3, 'sk-b2254cc0-f3fa-4bd3-926b-ddc48fd2ac0e', 0, 1, 1, 0);


INSERT INTO db_tortoise.tortoise_memory_prompt_config (type, base_prompt, over_length_prompt, update_prompt) VALUES ('2', '请分析以下用户与AI助手的历史对话内容，提取关键信息并生成简洁的摘要。要求如下：
1. **核心内容提取**：
   - 识别对话的主要话题和讨论重点
   - 提取用户的关键需求、问题或目标
   - 记录重要的决定、结论或行动计划
   - 保留关键的事实、数据和约束条件
2. **结构化组织**：
   - 按逻辑顺序组织信息
   - 使用清晰的段落或要点格式
   - 保持客观中立，不添加个人解读
3. **格式要求**：
   - 摘要长度控制在原始内容的20-30%
   - 使用简洁明确的语言
   - 保持原始对话的准确性和完整性', '请对以下文本进行精简，去除无关重要的内容，保留核心信息。要求如下：
1. **冗余识别与去除**：
   - 删除重复表述和冗余描述
   - 去除举例、比喻中不影响核心理解的部分
   - 简化过于详细的解释或背景信息
   - 移除与主要话题无关的离题内容
2. **重点保留原则**：
   - 保留核心论点、主要结论和关键数据
   - 保持逻辑结构和论证链条完整
   - 保留必要的定义、术语和概念解释
   - 维持原文的主要意图和目的
3. **语言优化**：
   - 将长句拆分为简洁的短句
   - 使用更精确的词汇替代冗长表达
   - 保持专业术语准确性的同时提高可读性
   - 确保精简后文本流畅自然
4. **精简标准**：
   - 目标精简率为原始长度的30-50%
   - 不改变原文的核心意思和立场
   - 保持信息的完整性和准确性
   - 标注重大删减或调整的部分', '请将以下几份记忆内容合并为一份统一的摘要。要求如下：
1. **内容融合**：
   - 比较两份记忆的核心内容和重点
   - 识别重叠信息，去重合并
   - 补充新增或更新的信息
   - 处理可能存在的矛盾或不一致（如存在矛盾，注明差异）
2. **结构优化**：
   - 重新组织信息结构，确保逻辑连贯
   - 按主题或时间顺序排列内容
   - 突出最重要的信息和更新点
3. **完整性保持**：
   - 确保合并后不丢失任何重要信息
   - 保留原始记忆的关键细节
   - 保持信息的准确性和一致性');
INSERT INTO db_tortoise.tortoise_memory_prompt_config (type, base_prompt, over_length_prompt, update_prompt) VALUES ('3', '你是一个“对话记忆压缩引擎”。

你的任务是：
将用户的历史对话压缩为一个「时序关系图谱」。

图谱只保留以下四个要素：
- subject（实体）
- relation（关系）
- target（实体）
- time（时间若无法确定则为 null）

严格规则：
1. 只抽取“主体之间的关系”，不要抽取语气词，无效对话
2. subject 和 target 都必须是明确的实体（人、组织、项目、系统、概念）。
3. relation 必须是可长期复用的关系动词或短语（如：开发、使用、位于、参与、依赖、研究）。
4. 不要生成任何解释性文字，只输出 JSON 数组。
5. 如果一句话无法抽象为“实体–关系–实体”，则忽略。
6. 不允许臆造实体或关系。
7. 抽取结果应尽可能精简，避免语义重复。

比如：对话内容是如下：
用户：我在做一个 Java 的大模型记忆管理框架。
用户：这个项目是从 2024 年 10 月开始的。
用户：主要是为了解决上下文太长的问题。
用户：我现在人在新加坡。
##那么你应该给我的输出应该如下所示
[
  {
    "subject": "用户",
    "relation": "开发",
    "target": "Java 大模型记忆管理框架",
    "time": "2024-10"
  },
  {
    "subject": "用户",
    "relation": "位于",
    "target": "新加坡",
    "time": null
  }
]', '你是一个“时序关系图谱精简引擎”。

你的任务是：
在不丢失长期记忆价值的前提下，对输入的「时序关系图谱 JSON 数组」进行最大程度精简。

输入：
- 一个 JSON 数组，每个元素结构为：
{
  "subject": string,
  "relation": string,
  "target": string,
  "time": string | null
}

精简规则（严格遵守）：
1. 删除语义完全重复的关系
2. 合并以下情况：
   - subject、target 相同
   - relation 含义相同或可抽象为同一长期关系
3. 如果多条关系仅在 time 上不同：
   - 保留时间信息量最大的那一条
4. 删除短期、一次性、无法复用的关系
5. 不允许引入新的实体、关系或时间
6. 不允许改变事实方向（subject / target 不可互换）

输出要求：
- 只输出精简后的 JSON 数组
- 不输出任何解释性文字
- 输出结果应尽可能少，但语义不丢失
', '你是一个“时序关系图谱融合引擎”。

你的任务是：
将两个「时序关系图谱 JSON 数组」融合为一个新的图谱。

输入：
- graph_a：一个 JSON 数组
- graph_b：一个 JSON 数组

每个元素结构固定为：
{
  "subject": string,
  "relation": string,
  "target": string,
  "time": string | null
}

融合规则（严格遵守）：
1. 如果 subject + relation + target 完全一致：
   - 仅保留一条
   - time 选择信息量更大的那个：
     - 非 null 优先于 null
     - 更精确时间（如 YYYY-MM > YYYY）优先
2. 如果 subject 和 target 相同，但 relation 语义等价或高度相似：
   - 合并为一条
   - 使用更通用、可长期复用的 relation 表达
3. 不允许产生任何新的实体或关系
4. 不允许改变原有事实含义
5. 不允许输出重复语义
6. 不要引入解释性字段或备注

输出要求：
- 只输出融合后的 JSON 数组
- 不输出任何解释性文字
- 保持结构与输入一致
- 数组顺序不重要
');


INSERT INTO db_tortoise.tortoise_tool (name, `desc`, field_config, url, method, headers) VALUES ('queryNowTime', '查询当前时间', null, 'http://localhost:8080/api/defaultTool/queryNowTime', 'GET', '[{"name": "Accept", "value": "*/*"}, {"name": "Accept-Language", "value": "zh-CN,zh;q=0.9"}, {"name": "Authorization", "value": "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJ1c2VySWQiOjEsInJvbGVDb2RlIjoxLCJpYXQiOjE3NjY2MjM1OTcsImV4cCI6MTc2NjcwOTk5N30.gc0Xbh1ZUkrLBsUPydOB_qdPz1Yn-58o1DtjMp3WrKzFXlT0l83gu9UiDpSGtvAV"}, {"name": "Connection", "value": "keep-alive"}, {"name": "Content-Type", "value": "application/json"}, {"name": "Referer", "value": "http://localhost:8080/"}, {"name": "Sec-Fetch-Dest", "value": "empty"}, {"name": "Sec-Fetch-Mode", "value": "cors"}, {"name": "Sec-Fetch-Site", "value": "same-origin"}, {"name": "User-Agent", "value": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36"}, {"name": "sec-ch-ua", "value": "\\"Not:A-Brand\\";v=\\"99\\", \\"Google Chrome\\";v=\\"145\\", \\"Chromium\\";v=\\"145\\""}, {"name": "sec-ch-ua-mobile", "value": "?0"}, {"name": "sec-ch-ua-platform", "value": "\\"Windows\\""}, {"name": "Cookie", "value": "JSESSIONID=643695089169895BE542BFA90641B182"}]');
INSERT INTO db_tortoise.tortoise_tool (name, `desc`, field_config, url, method, headers) VALUES ('queryWeather', '查询某一天的天气', '[{"name": "date", "type": "string", "required": true, "enumValues": null, "description": "日期"}]', 'http://localhost:8080/api/defaultTool/queryWeather?date=2025-12-25%2012%3A00%3A00', 'GET', '[{"name": "Authorization", "value": "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJ1c2VySWQiOjEsInJvbGVDb2RlIjoxLCJpYXQiOjE3NjY2MjM1OTcsImV4cCI6MTc2NjcwOTk5N30.gc0Xbh1ZUkrLBsUPydOB_qdPz1Yn-58o1DtjMp3WrKzFXlT0l83gu9UiDpSGtvAV"}]');
INSERT INTO db_tortoise.tortoise_tool (name, `desc`, field_config, url, method, headers) VALUES ('calc', '简单计算器', '[{"name": "sign", "type": "string", "required": true, "enumValues": ["+", "-", "×", "÷"], "description": "运算符"}, {"name": "firstNumber", "type": "number", "required": true, "enumValues": null, "description": "第一个数字，放在运算符的左边"}, {"name": "secondNumber", "type": "number", "required": true, "enumValues": null, "description": "第二个数字，放在运算符的右边"}]', 'http://localhost:8080/api/defaultTool/calc', 'POST', '[{"name": "Authorization", "value": "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJ1c2VySWQiOjEsInJvbGVDb2RlIjoxLCJpYXQiOjE3NjY2MjM1OTcsImV4cCI6MTc2NjcwOTk5N30.gc0Xbh1ZUkrLBsUPydOB_qdPz1Yn-58o1DtjMp3WrKzFXlT0l83gu9UiDpSGtvAV"}, {"name": "Content-Type", "value": "application/json"}]');
