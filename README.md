# Tortoise
开箱即用的Java 调用LLM中间件，配置，调用，成本，记忆一把梭

## 快速开始
1.在项目的pom.xml引入如下依赖
```xml
     <dependency>
        <groupId>io.github.johntortoise</groupId>
        <artifactId>tortoise-core</artifactId>
        <version>1.0</version>
     </dependency>
```
2.创建TortoiseClient并调用
```java
package io.github.johntortoise.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import java.math.BigDecimal;

public class Test {

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.create(ChatCompletionResponse.class);

    public static void main(String[] args) throws JsonProcessingException {
        //定义模型信息
        Model model = Model.builder().modelName("doubao-seed-1-6-lite-251015")
                .apiKey("857299ff-3489-435a-8d1b-9aaad1c89d9b")
                .completeUrl("https://ark.cn-beijing.volces.com/api/v3/chat/completions")
                .temperature(new BigDecimal("0.7"))
                .build();

        //定义会话ID，一个聊天窗口对应唯一的conversationId
        String conversationId = "1";

        //定义第一条消息
        TortoiseMessage firstMessage = new TortoiseMessage("你好,我叫王大力");
        firstMessage.setConversationId(conversationId);

        //传入模型和消息
        ChatCompletionResponse firstMessageResp = tortoiseClient.chat(firstMessage, model);

        //定义第二条消息
        TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        secondMessage.setConversationId(conversationId);

        ChatCompletionResponse secondMessageResp = tortoiseClient.chat(secondMessage, model);
    }
}
```
3 控制台输出
```text
14:22:03.020 [main] INFO io.github.johntortoise.core.utils.LogUtil - [tortoise-llmReq] conversationId=1 | Url=https://ark.cn-beijing.volces.com/api/v3/chat/completions | Body={"model":"doubao-seed-1-6-lite-251015","messages":[{"content":"你好,我叫王大力","role":"user"}],"temperature":0.7,"stream":false,"stream_options":{"include_usage":false}}
14:22:05.582 [main] INFO io.github.johntortoise.core.utils.LogUtil - [tortoise-llmResp] conversationId=1 | resp={"choices":[{"finish_reason":"stop","index":0,"logprobs":null,"message":{"content":"你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？","reasoning_content":"\n用户现在说“你好，我叫王大力”，首先需要友好回应，打招呼，然后可以确认名字，保持亲切自然的语气。比如先回“你好呀王大力！很高兴认识你～”这样比较合适，符合日常交流的感觉，不需要太复杂，重点是友好回应对方的自我介绍。","role":"assistant"}}],"created":1765952525,"id":"02176595252337568fe52ee795c67552dfa14bf2f62824985d595","model":"doubao-seed-1-6-lite-251015","service_tier":"default","object":"chat.completion","usage":{"completion_tokens":89,"prompt_tokens":41,"total_tokens":130,"prompt_tokens_details":{"cached_tokens":0},"completion_tokens_details":{"reasoning_tokens":70}}}
14:22:05.683 [main] INFO io.github.johntortoise.core.utils.LogUtil - [tortoise-llmReq] conversationId=1 | Url=https://ark.cn-beijing.volces.com/api/v3/chat/completions | Body={"model":"doubao-seed-1-6-lite-251015","messages":[{"content":"你好,我叫王大力","role":"user"},{"content":"你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？","role":"assistant"},{"content":"我叫什么名字，直接告诉我","role":"user"}],"temperature":0.7,"stream":false,"stream_options":{"include_usage":false}}
14:22:07.468 [main] INFO io.github.johntortoise.core.utils.LogUtil - [tortoise-llmResp] conversationId=1 | resp={"choices":[{"finish_reason":"stop","index":0,"logprobs":null,"message":{"content":"你叫王大力。","reasoning_content":"\n用户现在问“我叫什么名字，直接告诉我”，首先看对话历史，前面用户说自己叫王大力，所以直接明确回答即可，不需要多余内容，保持简洁准确。","role":"assistant"}}],"created":1765952527,"id":"021765952525930c4dc4b915535437ad03877d2dfc54a033e1f3d","model":"doubao-seed-1-6-lite-251015","service_tier":"default","object":"chat.completion","usage":{"completion_tokens":47,"prompt_tokens":77,"total_tokens":124,"prompt_tokens_details":{"cached_tokens":0},"completion_tokens_details":{"reasoning_tokens":42}}}
```

## 1.初始化客户端
### 1.1直接创建
```java
    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.create(ChatCompletionResponse.class);
```
### 1.2接入管台创建
若有接入tortoise-admin，则可使用如下代码，传入管台host和唯一SK(见4.3)
```java
    private final static TortoiseClient<String> adminTortoiseClient = TortoiseClient.createForAdmin(String.class,host,"xxxxx");
```

## 2.重要组件说明
### 2.1 TortoiseChatManger
TortoiseChatManger是聊天管理接口，默认实现是DefaultTortoiseChatManger类，开发者也可自行实现该接口的方法
或者有计划接入tortoise-admin,其将由AdminTortoiseChatManger实现

TortoiseChatManger接口中，分别有如下的三个方法
```java
package io.github.johntortoise.core.manger.chat;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import java.util.List;

public interface TortoiseChatManger {
    
    /**
     * 校验是否超过限制
     * @param conversationId conversationId 会话ID
     * @return true表示超过，此次调用直接被拦住
     */
    Boolean checkLimit(String conversationId);
    
    /**
     * 获取历史消息
     * @param conversationId 会话ID
     * @return 历史消息列表
     */
    List<Message> findHistoryChat(String conversationId);

    /**
     * 聊天后处理方法
     * @param afterChatDTO 存储于本次会话响应的所有输入和输出
     */
    void afterChat(AfterChatDTO afterChatDTO);
    
}
```

#### 2.1.1 checkLimit
通过client调用chat方法,最开始的逻辑,就是调用TortoiseChatManger的checkLimit方法
在DefaultTortoiseChatManger中，默认返回false，即不做限制
在AdminTortoiseChatManger中，会校验该conversationId是否超过token上限
开发者也可以自己定义并实现该方法，可以做调用次数，调用频率等的限制

#### 2.1.2 findHistoryChat
在快速入门中，我们第二次与大模型对话时，仍然只需要传入消息内容和conversationId
```text
        //定义第二条消息
        TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        secondMessage.setConversationId(conversationId);
        tortoiseClient.chat(secondMessage, model);
```
而在调用日志中，我们可以看到，这一次的输入的messages如下
```json
{
  "messages": [
    {
      "content": "你好,我叫王大力",
      "role": "user"
    },
    {
      "content": "你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？",
      "role": "assistant"
    },
    {
      "content": "我叫什么名字，直接告诉我",
      "role": "user"
    }
  ]
}
```
messages包含了我们的第一次对话，原因便是findHistoryChat方法，将历史的对话内容查找出来，一并给到了大模型

在DefaultTortoiseChatManger中，历史的对话消息，以key为conversationId,value为List<Message>存储在内存的concurrentHashMap中
因此DefaultTortoiseChatManger的findHistoryChat实现，直接通过map拿到message列表，在与大模型对话时，将message列表给到大模型

在AdminTortoiseChatManger中，历史的对话消息，存储在数据库中，但与单纯的对话消息不同，findHistoryChat的内容，取决于对话时的sk背后配置的记忆策略(见4.3)

#### 2.1.3 afterChat
在对话完成后，我们需要对这次对话的所有数据做一个处理，为整合后的数据
AfterChatDTO主要由conversationId和MainInfo组成

MainInfo存储着本次对话的历史消息，输入，输出，调用大模型产生的token数消耗
```java
package io.github.johntortoise.core.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainInfo {

    private List<Message> history;

    private Message input;

    private Message outPut;

    private Tokens tokens;


    @Data
    public static class Tokens{
        private Integer completionTokens = 0;

        private Integer promptTokens = 0 ;

        private Integer totalTokens = 0;

        private Integer cachedTokens = 0 ;

    }
}

```
在DefaultTortoiseChatManger中，直接将本次对话的内容，加入了map中
在AdminTortoiseChatManger中，其调用的tortoise-admin中的方法，将会触发消息存储，成本记忆，记忆生成三大流程


### 2.2 TortoiseMessageHandler
TortoiseMessageHandler是消息处理拦截器接口，考虑到部分大模型的输出不是业内通用标准，因此将此接口开放出来，开发者可以自己处理大模型的输出消息
```java
package io.github.johntortoise.core.message;

import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;

import java.util.List;

public interface TortoiseMessageHandler<T> {
    /**
     * 将大模型的输出结果，转为Java类
     * @param resp llm给回的完整响应
     * @return 目标java类
     */
    T convertForResult(String resp);

    /**
     * 整合本次聊天的数据
     * @param history 历史对话数据
     * @param input 输入
     * @param resp 大模型的输出
     * @param streamCallBack 流式调用回调函数
     * @return mainInfo，将作为TortoiseChatManger的afterChat的一部分
     */
    MainInfo convertForMainInfo(List<Message> history,Message input,String resp, StreamCallBack streamCallBack);
}
```
我们只为TortoiseMessageHandler提供给了一个基础的实现类DefaultTortoiseMessageHandler，这部分更多是在对输出做格式化，不再细讲

## 3.Client

### 3.1
TortoiseClient是核心的client，其将其他Client包含在内，使我们可以忽略具体的调用细节

其包含如下属性
TortoiseChatManger，即2.1提到的聊天管理器
TortoiseMessageHandler,即2.2提到的消息处理拦截器
Model，即快速开始中定义的模型信息
TortoiseAdminClient，即调用tortoise-admin的client，我们将所有与tortoise-admin的交互，都收缩到其中

我们提供了几个方法，支持不同需求的调用，具体分成两种

当使用TortoiseClient.create创建客户端时
```java
package io.github.johntortoise.demo.controller;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import java.math.BigDecimal;

public class Test {

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.cr(ChatCompletionResponse.class);

    public static void main(String[] args) {
        //定义模型信息
        Model model = Model.builder().modelName("doubao-seed-1-6-lite-251015")
                .apiKey("857299ff-3489-435a-8d1b-9aaad1c89d9b")
                .completeUrl("https://ark.cn-beijing.volces.com/api/v3/chat/completions")
                .temperature(new BigDecimal("0.7"))
                .build();

        //定义会话ID，一个聊天窗口对应唯一的conversationId
        String conversationId = "1";

        //定义第一条消息
        TortoiseMessage firstMessage = new TortoiseMessage("你好,我叫王大力");
        firstMessage.setConversationId(conversationId);

        //非流式调用
        tortoiseClient.chat(firstMessage, model);

        //定义第二条消息
        TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        secondMessage.setConversationId(conversationId);

        //流式调用
        tortoiseClient.chatStream(secondMessage, model, new StreamCallBack() {
            @Override
            public void send(String content) {
                System.out.println(content);
            }
            @Override
            public void finish() {

            }
            @Override
            public void onFailure() {

            }
        });
    }
}
```

当使用TortoiseClient.chatForAdmin创建客户端时(即接入tortoise-admin)
**注意：下面的conversationId是通过tortoiseClient.createConversation创建的**

```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;

public class Test {
    
    public static String host = "http://localhost:8080";
    public static String sk = "sk-428acddd-6ced-4e33-a9cb-ec96c980cf02";

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(ChatCompletionResponse.class,host,sk);

    public static void main(String[] args) {
        //通过client创建唯一的会话ID
        String conversationId = tortoiseClient.createConversation("zgsssjznbdgj");

        //定义第一条消息
        TortoiseMessage firstMessage = new TortoiseMessage("你好,我叫王大力");
        firstMessage.setConversationId(conversationId);

        //非流式调用
        tortoiseClient.chatForAdmin(firstMessage);

        //定义第二条消息
        TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        secondMessage.setConversationId(conversationId);

        //流式调用
        tortoiseClient.chatStreamForAdmin(secondMessage, new StreamCallBack() {
            @Override
            public void send(String content) {
                System.out.println(content);
            }
            @Override
            public void finish() {

            }
            @Override
            public void onFailure() {

            }
        });
    }
}

```

### 3.2 LLMApiClient
考虑到有些开发者可能不想使用我们的TortoiseClient，而想完全自己与大模型交互
因此CompleteLLMApiClient和StreamLLMApiClient是允许直接使用的

#### 3.2.1 CompleteLLMApiClient
使用示例
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.client.llm.CompleteLLMApiClient;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.enums.RoleEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        try {
            //定义模型信息
            Model model = Model.builder().modelName("doubao-seed-1-6-lite-251015")
                    .apiKey("857299ff-3489-435a-8d1b-9aaad1c89d9b")
                    .completeUrl("https://ark.cn-beijing.volces.com/api/v3/chat/completions")
                    .temperature(new BigDecimal("0.7"))
                    .build();

            CompleteLLMApiClient client = CompleteLLMApiClient.createClient(model);

            String conversationId = "1";

            List<Message> list = new ArrayList<>();

            list.add(new Message("你好，我叫王大力",RoleEnum.USER.getCode()));
            list.add(new Message("你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？",RoleEnum.SYSTEM.getCode()));
            list.add(new Message("我叫什么名字，直接告诉我",RoleEnum.USER.getCode()));

            client.chatCompletion(list,conversationId);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}

```
#### 3.2.1 StreamLLMApiClient
StreamLLMApiClient 使用示例
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.llm.StreamLLMApiClient;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.enums.RoleEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        try {
            //定义模型信息
            Model model = Model.builder().modelName("doubao-seed-1-6-lite-251015")
                    .apiKey("857299ff-3489-435a-8d1b-9aaad1c89d9b")
                    .completeUrl("https://ark.cn-beijing.volces.com/api/v3/chat/completions")
                    .temperature(new BigDecimal("0.7"))
                    .build();

            StreamLLMApiClient client = StreamLLMApiClient.createClient(model);

            String conversationId = "1";

            List<Message> list = new ArrayList<>();

            list.add(new Message("你好，我叫王大力",RoleEnum.USER.getCode()));
            list.add(new Message("你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？",RoleEnum.SYSTEM.getCode()));
            list.add(new Message("我叫什么名字，直接告诉我",RoleEnum.USER.getCode()));

            client.chatCompletion(list, conversationId,  new StreamCallBack() {
                @Override
                public void send(String content) {
                    System.out.println(content);
                }

                @Override
                public void finish() {

                }

                @Override
                public void onFailure() {

                }
            },null);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}

```


### 3.3 TortoiseAdminClient
如果不打算接入tortoise-admin，可以跳过此部分

由TortoiseClient直接使用的如下
connect,触发时机,并调用tortoiseClient.checkConnection()
createConversation，触发时机：通过 tortoiseClient.createConversation()
findModel，触发时机：通过tortoiseClient.chatForAdmin()或者tortoiseClient.chatStreamForAdmin(),可以注意到这里不需要传入模型，因此将通过findModel找到sk绑定的模型
afterChat，触发时机：tortoiseChatManger.afterChat()
getMemoriesByConversationId,触发时机：tortoiseChatManger.findHistoryChat()
checkLimit，触发时机：tortoiseChatManger.checkLimit()

非TortoiseClient直接使用的如下
receiveMessage，触发时机：此方法是开放给仅有记忆功能需求的开发，当调用此接口时，将会触发消息存储和生成记忆
详见：（4.4）


## 4管理台 
我们提供了tortoise-admin模块，该模块是前后端一体的项目
在运行该模块之前，需要先执行tortoise-admin模块的init目录下的init-ddl.sql和init-dml.sql文件

执行成功后，修改application.properties文件中的Mysql配置
spring.datasource.url
spring.datasource.username
spring.datasource.password

## 启动
在启动之前，请先检查.env文件的配置
通常你需要对这几项进行修改
SPRING_DATASOURCE_HOST=192.168.3.11
SPRING_DATASOURCE_PORT=3306
SPRING_DATASOURCE_DATABASE=db_tortoise
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=usiytvsiid5843


本地启动
cd tortoise-admin
start-app

访问:http://localhost:8080/login

docker启动
cd tortoise-admin
docker-compose up -d
访问:http://ip:8080/login

默认账号是admin@gmail.com，密码123456，如要修改，请查看init-dml.sql文件，或者在登录后，右上角点击个人信息即可输入密码修改

### 4.1 大模型配置页面

#### 界面概览

![大模型配置页面](docs/img/llmConfig.png)

#### 初始化配置

在项目初始化时，`init-dml.sql` 文件会预置示例模型配置。您需要将其修改为实际的模型配置。

**操作方式：**
- 点击"编辑"按钮修改现有配置
- 点击"新增"按钮创建新配置

#### 新增/编辑配置界面

![大模型配置弹窗](docs/img/llmConfigAddOrUpdate.png)
![大模型配置弹窗](docs/img/llmConfigAddOrUpdate-1.png)

#### 配置参数详解

#### 基本信息
- **模型名称**  
  调用第三方API时传入的 `model` 参数（例如：`qwen-flash`）

- **配置名称**  
  自定义标识名称，建议与模型名称区分，避免混淆

#### API 连接配置
- **API 密钥**  
  第三方LLM服务商提供的密钥（仅需填入"Bearer "后面的部分）

- **API URL**  
  ⚠️ **请填入完整的API端点路径**  
  **说明**：由于OpenAI SDK设计了 `baseUrl + /v1/chat/completions` 的拼接方式，而部分厂商的API不支持此模式。为确保兼容性，此处需填写完整的请求地址。

#### 模型参数
- **状态**  
  `启用` / `禁用` - 仅启用的模型可在其他功能页面中被搜索和使用

- **温度值**  
  对应大模型API的 `temperature` 参数，控制生成文本的随机性

#### 成本管理
- **输入价格**  
  每 token 的成本单价（支持最多6位小数）

- **输出价格**  
  每 token 的成本单价（支持最多6位小数）

- **缓存价格**  
  部分模型在调用时可能命中缓存，若厂商未提供此信息，请设置为 `0`

**价格单位说明**：  
不同厂商可能提供"每百万token"或"每千token"的计价方式，此处已统一为"每token"计算。如需调整精度，请修改 `tortoise_llm_config` 表中的字段类型。

#### 其他信息
- **配置描述**  
  自定义说明信息，可用于记录配置的用途（例如："A组 - XX项目专用配置"）

### 4.2 记忆策略

在讲解具体配置前，需要明确一个核心的通识性问题：**记忆是与大模型进行连贯对话的基石。**

#### 记忆的本质是什么？

通常，我们通过 API 与大模型对话时，服务提供方本身**不提供**记忆功能。所谓的“记忆”，完全由调用方在每次请求时提供的 `messages` 上下文数组决定。

**让我们通过一个例子来理解这个流程：**

1.  **第一次发起对话**
    ```json
    {
      "messages": [
        {
          "content": “你好，我叫王大力“,
          “role“: “user“
        }
      ]
    }
    ```

2.  **大模型回复**
    ```json
    {
      “content“: “你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？“,
      “role“: “assistant“
    }
    ```

3.  **第二次发起对话（常见错误方式）**
    如果仅发送新问题，模型将失去之前的上下文。
    ```json
    {
      “messages“: [
        {
          “content“: “我叫什么名字，直接告诉我“,
          “role“: “user“
        }
      ]
    }
    ```
    **此时，大模型并不知道你的名字。**

4.  **正确的记忆方式**
    必须将完整的历史对话包含在 `messages` 中。
    ```json
    {
      “messages“: [
        {
          “content“: “你好，我叫王大力“,
          “role“: “user“
        },
        {
          “content“: “你好呀王大力！很高兴认识你～有什么我可以帮到你的吗？“,
          “role“: “assistant“
        },
        {
          “content“: “我叫什么名字，直接告诉我“,
          “role“: “user“
        }
      ]
    }
    ```
    这就是**记忆的本质**：由调用方维护并提供的对话上下文。

#### 为什么需要记忆策略？

随着对话轮数增加，`messages` 会不断增长，导致消耗的 Token 数量激增，产生高昂费用。因此，我们需要对上下文进行优化和缩短。

#### 业内通用的记忆优化策略

当前主流的解决方案有以下几种：

| 策略 | 核心思想 | 特点 |
| :--- | :--- | :--- |
| **截断** | 仅保留最近 N 轮对话。 | 简单直接，但可能丢失重要早期信息。 |
| **摘要** | 对话后，用大模型将历史总结成一段文字，下次对话时传入摘要和新问题。 | 平衡成本与信息保留，需要额外模型调用。 |
| **相关性过滤** | 维护一个“对话池”，根据相关性评分动态替换池中最不相关的对话。 | 智能化保留，实现相对复杂。 |
| **分期记忆** | 将记忆分层处理：久远记忆→关键词，中期记忆→摘要，近期记忆→完整上下文。 | 结构精细，能较好兼顾远近信息。 |
| **检索** | 根据当前输入，从所有历史对话中检索出相关的片段传入上下文。 | 类似“联网搜索”，精准但依赖检索质量。 |
| **图谱** | 将对话内容提炼成“实体-关系”图谱（如：`[我] -[是]-> [程序员]`），动态更新图谱作为记忆。 | 结构化程度高，能理解复杂关系。 |

#### 本系统提供的记忆策略

考虑到易用性和通用性，我们提供了以下三种策略，分为两大类型：

-   **无需大模型协助**
 **截断**：配置简单，资源消耗低。


-   **需要大模型协助**（需确保对应模型可用） **1.摘要**：在对话后自动生成历史摘要。 **2.图谱**：构建并维护知识图谱作为记忆。

#### 配置操作指引

现在回到配置页面：
![记忆配置页面](docs/img/memory-policy.png)

**重要提示**：
在 `init-dml.sql` 脚本中，系统会初始化记忆配置。如果您选择的策略类型是**摘要**或**图谱**，请务必在配置页面上将其关联的模型修改为 **4.1 章节中已配置并启用的模型**。

#### 4.2.1 截断
考虑到对话的完整性为一问一答，因此我们在实现截断时，基本维度就是一问一答，包括我们的表设计tortoise_message,其也包含了input和output字段
截断类型的配置非常简单，如下所示
![记忆配置页面](docs/img/memory-policy-1.png)

### 4.2.2 摘要与图谱记忆策略

![记忆配置页面](docs/img/memory-policy-2.png)

> 以上两种记忆策略的实现逻辑较为相似，因此合并说明。

#### 📌 记忆阈值
- **是什么**：触发自动记忆总结的对话轮数开关。例如设置为`20`，则每累计完成**20轮对话**，系统便会自动对这20轮内容进行一次记忆提炼。
- **为什么**：控制记忆生成的**节奏与颗粒度**。阈值越小，记忆越频繁（成本越高）；阈值越大，记忆越稀疏（可能遗漏细节）。

#### 🔢 单次分析消息数量
- **是什么**：每次调用模型进行分析时，输入给模型的**数据块大小**，主要受模型上下文长度限制。
- **示例**：记忆阈值=`100`，单次分析量=`20` → 系统会将100轮对话切分为5个20轮的块，分批分析后汇总结果。
- **为什么**：解决**长上下文无法一次性处理**的问题，实现大对话量的分块分析。

#### 📏 记忆最大长度
- **是什么**：对生成的记忆内容（摘要/图谱）设定的**长度限制器**。当记忆内容超过该字数限制时，系统会自动触发压缩，保留核心、舍弃次要信息。
- **为什么**：防止记忆**无限膨胀**，影响后续使用的检索效率、准确性和成本。

#### 🔄 “是否利用历史记忆” 的逻辑差异
假设 **记忆阈值 = 20**，当前已对话 **39 轮**，且第1-20轮的对话已生成**历史记忆A**。
当**第40轮对话完成**时（`40 % 20 = 0`），会触发新一轮记忆生成。

| 选项 | 工作模式 | 核心流程 | 优点与场景 |
| :--- | :--- | :--- | :--- |
| **启用（是）**<br>**_增量更新_** | 在已有记忆基础上更新。 | 1. 用**基础提示词**分析第21-40轮，生成**新记忆B**。<br>2. 用**更新提示词**，将**历史记忆A**与**新记忆B**合并，生成**最终记忆C**。<br>3. 存入**记忆C**。 | **优点**：效率高、成本低（仅分析新增对话）。<br>**场景**：常规生产环境。提示词稳定，需持续累积记忆。 |
| **不启用（否）**<br>**_全量重算_** | 忽略已有记忆，重新计算全部。 | 1. 用**基础提示词****重新分析**第1-20轮，生成**新记忆B‘**（重新计算）。<br>2. 用**基础提示词**分析第21-40轮，生成**新记忆C‘**。<br>3. 用**更新提示词**，将**B‘** 和 **C‘** 合并，生成**最终记忆D**。<br>4. 存入**记忆D**。 | **缺点**：成本高（重复分析旧数据）、效率低。<br>**场景**：**提示词迭代调试期**。修改提示词后，需要所有历史数据按新规则重新生成，保证全局一致性。 |

#### ✍️ 自定义提示词
前述记忆功能涉及多个需要提示词的环节，因此系统支持**自定义提示词**。如不填写，则默认采用系统预置配置。

我们将提示词分为三类：
1.  **基础提示词**：用于从原始对话块中首次提取记忆。
2.  **更新提示词**：用于将新旧两份记忆合并成一份更新的记忆。
3.  **超长提示词**：用于当记忆内容超过“记忆最大长度”时，对其进行压缩和精简。

**🎯 综合示例**
- **限制条件**：记忆阈值=`20`，单次分析量=`20`，启用历史记忆，最大长度=`500`
- **当前状态**：刚完成第100次对话，已有**记忆A (1-80)**
- **执行流程**：
 1.  使用**基础提示词**，分析第81-100轮对话，提取得到**记忆B (81-100)**。
 2.  使用**更新提示词**，将**记忆A (1-80)** 和 **记忆B (81-100)** 合并，得到**记忆C (1-100)**。
 3.  检查发现**记忆C**长度超过500字，触发压缩。
 4.  使用**超长提示词**，对**记忆C**进行精简，得到**记忆D (1-100)**。
 5.  最终，将**记忆D**存入数据库。

> **提示**：各项参数（阈值、分析量、长度等）的最佳配置值，需根据具体的应用场景、成本预算和对记忆精细度的要求来决定。


## 4.3 聊天配置

![聊天配置页面](docs/img/chat-profile.png)

聊天配置页面的核心功能是**生成和管理访问密钥（SK）**。其核心操作在创建配置的弹窗中完成。

![聊天配置创建弹窗](docs/img/chat-profile-1.png)

### 🔧 创建聊天配置
创建新的聊天配置（即生成一个SK），需填写以下关键信息：
1.  **选择模型**：从已配置的模型（参见 [4.1 模型配置](#41-模型配置)）中选择一个。
2.  **选择记忆策略**：从已配置的记忆策略（参见 [4.2 记忆策略](#42-记忆策略)）中选择一种。
3.  **输入Token上限**：设置**此SK下所有会话合计**允许的**单日Token消耗上限**。
4.  **点击保存**：完成创建。

### 🔑 获取与使用SK
- **获取SK**：配置创建成功后，页面将**展示生成的SK值**。
- **使用SK**：如在 [1.2 通过管理台创建会话](#12-通过管理台创建会话) 中所述，在管理台创建新会话时，需要传入的正是此SK值。

### 💡 配置关系说明
- **一个SK对应多个会话**：每个聊天配置（SK）下可以创建和管理多个独立的聊天会话。
- **Token限额设计**：
 - **聊天配置的Token上限**：指该SK下**所有会话累计**的每日消耗限额。
 - **会话的Token上限**：指**单个会话**的每日消耗限额（在记忆策略中配置）。
 - **关系建议**：为确保业务正常运行，`聊天配置的Token上限` 应 **大于** `会话Token上限 × 预估的平均会话数`。具体数值需根据实际业务场景和并发量进行规划。


## 4.4 历史会话管理

![历史会话列表页面](docs/img/conversation.png)

当通过客户端调用`chat`接口后，系统会触发`afterChat`方法。此方法将结合 **[4.1 模型配置](#41-模型配置)** 和 **[4.2 记忆策略](#42-记忆策略)** 中的设定，自动记录本次会话的**消息内容**、**成本消耗**与**记忆摘要**。

### 📜 消息内容页面
此页面按时间顺序完整展示会话中的所有对话记录。

![消息内容详情](docs/img/conversation-1.png)

### 🧠 记忆页面
> 此页面展示的消息内容，取决于具体的记忆策略，和记忆进度，比如，如下为截断类型的记忆

![消息内容详情](docs/img/conversation-11.png)

### 💰 成本页面
此页面详细列出该会话产生的所有Token消耗及对应的成本计算。

![会话成本详情](docs/img/conversation-2.png)

- **查看明细**：点击 **`详情`** 按钮，查看某次具体调用时的输入(Input)与输出(Output)详情

![单次调用成本详情](docs/img/conversation-3.png)


## 5记忆策略Demo

### 5.1截断Demo
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;

public class Test {

    public static String host = "http://localhost:8080";

    /**
     *  此sk，请配置记忆策略为截断策略，最新消息数为2
     */
    public static String sk = "sk-428acddd-6ced-4e33-a9cb-ec96c980cf02";

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(ChatCompletionResponse.class,host,sk);

    public static void main(String[] args) throws InterruptedException {
        String conversationId = tortoiseClient.createConversation("zgsssjznbdgj");

        //第一条
        TortoiseMessage firstMessage = new TortoiseMessage("你好,我叫王大力");
        firstMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(firstMessage);

        Thread.sleep(2000L);

        //第二条
        TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        secondMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(secondMessage);

        Thread.sleep(2000L);

        //第三条(上下文是1-2)
        TortoiseMessage thirdMessage = new TortoiseMessage("今天北京的天气怎么样？");
        thirdMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(thirdMessage);

        Thread.sleep(2000L);

        //第四条(上下文是2-3)
        TortoiseMessage fourthMessage = new TortoiseMessage("我需要穿什么衣服？");
        fourthMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(fourthMessage);

        Thread.sleep(2000L);

        //第五条(上下文是3-4，这里应该回答不出来了)
        TortoiseMessage fifthMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        fifthMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(fifthMessage);

    }
}
```
看一下demo运行后的对话内容
![历史会话页面](docs/img/conversation-4.png)

由于配置的最近消息数是两轮，所以当第五次询问大模型时，其已经丢失了名字信息
再看一下记忆

![历史会话页面](docs/img/conversation-5.png)
也只有最后两轮


### 5.2摘要Demo
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;

public class Test {

 public static String host = "http://localhost:8080";

 /**
  *  此sk，请配置记忆策略为摘要策略，阈值为2，单次分析2，启用历史记忆，最大记忆长度为100
  */
 public static String sk = "sk-c09eefbb-4172-4724-8af1-e14eb86d8a40";

 private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(ChatCompletionResponse.class,host,sk);

 public static void main(String[] args) throws InterruptedException {
  String conversationId = tortoiseClient.createConversation("zgsssjznbdgj");

  //第一条
  TortoiseMessage firstMessage = new TortoiseMessage("你好,我叫王大力");
  firstMessage.setConversationId(conversationId);
  tortoiseClient.chatForAdmin(firstMessage);

  Thread.sleep(2000L);

  //第二条
  TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
  secondMessage.setConversationId(conversationId);
  tortoiseClient.chatForAdmin(secondMessage);

  //这里睡眠10秒等一下记忆
  Thread.sleep(10000L);

  //第三条(上下文：记忆A(1-2))
  TortoiseMessage thirdMessage = new TortoiseMessage("今天北京的天气怎么样？");
  thirdMessage.setConversationId(conversationId);
  tortoiseClient.chatForAdmin(thirdMessage);

  Thread.sleep(2000L);

  //第四条(上下文：记忆A(1-2) 和3的输入输出)
  TortoiseMessage fourthMessage = new TortoiseMessage("我需要穿什么衣服？");
  fourthMessage.setConversationId(conversationId);
  tortoiseClient.chatForAdmin(fourthMessage);

  //这里睡眠10秒等一下记忆
  Thread.sleep(10000L);

  //第五条(上下文 ：记忆B(1-4) 或 上下文呢记忆A(1-2)和3，4的输入输出 取决于记忆有没有生成，如果没生成，拿的是记忆和记忆之外的所有消息))
  TortoiseMessage fifthMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
  fifthMessage.setConversationId(conversationId);
  tortoiseClient.chatForAdmin(fifthMessage);

 }
}
```
看一下对话过程
![历史会话页面](docs/img/conversation-5.png)


从对话可以看到，最后一问的时候回答出了我们的名字，与截断不同

再看当前的记忆
![历史会话页面](docs/img/conversation-6.png)

可以看到一段总结，还有第五轮对话，因为第五轮对话还未生成记忆

使用成本
![历史会话页面](docs/img/conversation-7.png)

可以看到是 会话→会话→记忆生成(达到阈值触发)→会话→会话→记忆生成(达到阈值触发)→记忆更新(这里是将1-2的记忆和3-4的记忆更新)→记忆精简(由于最终更新的记忆还是超过了长度，所以触发了精简)→会话

### 5.3图谱Demo
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;

public class Test {

    public static String host = "http://localhost:8080";

    /**
     *  此sk，请配置记忆策略为摘要策略，阈值为2，单次分析2，启用历史记忆
     */
    public static String sk = "sk-b2254cc0-f3fa-4bd3-926b-ddc48fd2ac0e";

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(ChatCompletionResponse.class,host,sk);

    public static void main(String[] args) throws InterruptedException {
        String conversationId = tortoiseClient.createConversation("zgsssjznbdgj");

        //第一条
        TortoiseMessage firstMessage = new TortoiseMessage("你好,我叫王大力");
        firstMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(firstMessage);

        Thread.sleep(2000L);

        //第二条
        TortoiseMessage secondMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        secondMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(secondMessage);

        //这里睡眠10秒等一下记忆
        Thread.sleep(10000L);

        //第三条(上下文：记忆A(1-2))
        TortoiseMessage thirdMessage = new TortoiseMessage("今天北京的天气怎么样？");
        thirdMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(thirdMessage);

        Thread.sleep(2000L);

        //第四条(上下文：记忆A(1-2) 和3的输入输出)
        TortoiseMessage fourthMessage = new TortoiseMessage("我需要穿什么衣服？");
        fourthMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(fourthMessage);

        //这里睡眠30秒等一下记忆
        Thread.sleep(30000L);

        //第五条(上下文 ：记忆B(1-4) 或 上下文呢记忆A(1-2)和3，4的输入输出 取决于记忆有没有生成，如果没生成，拿的是记忆和记忆之外的所有消息))
        TortoiseMessage fifthMessage = new TortoiseMessage("我叫什么名字，直接告诉我");
        fifthMessage.setConversationId(conversationId);
        tortoiseClient.chatForAdmin(fifthMessage);

    }
}
```

看一下记忆
![历史会话页面](docs/img/conversation-9.png)

## 6. 接入记忆功能

> **前置要求**：请先按照 **4.3 章节** 完成相关配置。

### 6.1 导入历史数据

我们提供了数据导入接口，您可以通过以下步骤导入历史对话数据。

#### 操作步骤
1. **点击导入按钮**
   ![导入按钮](docs/img/import.png)

2. **进入导入页面**
   ![导入页面](docs/img/import-1.png)

3. **下载并填写模板**
    - 点击 **“下载模板”** 获取标准格式文件。

#### 模板字段说明
| 字段名 | 说明 |
| :--- | :--- |
| `conversation_id` | 会话标识。一个会话（Conversation）可包含多条对话记录。 |
| `chat_profile_id` | 根据 4.3 配置后生成的配置ID。**注意：这不是SK**。 |
| `unique_id` | 代表一次“一问一答”的唯一标识符。 |

#### 数据填写示例
填写后的模板文件示例如下：
![数据示例](docs/img/import-3.png)

#### 执行导入
1. 点击 **“选择文件”**，上传填写好的数据文件。
2. 我们以 **50万行** 的数据文件为例（路径：`docs/img/file/conversation_import_data_500k.xlsx`）。

#### 导入状态
- **处理中状态**
  ![处理中](docs/img/import-deal.png)
  > 本次示例导入50万行数据，耗时约 **87秒**。

- **导入成功状态**
  ![导入成功](docs/img/import-finish.png)

### 6.2 触发记忆生成

当推送消息时，如果 **未生成记忆的消息数量达到或超过您设定的阈值**，系统将自动触发记忆生成。

#### 调用示例代码
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.ReceiveMessageReq;
import io.github.johntortoise.core.enums.RoleEnum;

public class Test {

    public static String host = "http://localhost:8080";

    /**
     * 此 SK，请填写对应 chat_profile_id 所配置的 SK
     */
    public static String sk = "sk-428acddd-6ced-4e33-a9cb-ec96c980cf02";

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(ChatCompletionResponse.class, host, sk);

    public static void main(String[] args) {
        ReceiveMessageReq build = ReceiveMessageReq.builder().conversationId("257386521051582337").build();
        build.setInput(new Message("你好，我叫王大力", RoleEnum.SYSTEM.getCode()));
        build.setOutPut(new Message("王大力你好", RoleEnum.SYSTEM.getCode()));
        tortoiseClient.sendMessage(build);
    }
}
```

#### ⚠️ 重要提示
如果您的历史记忆数据量很大，请务必**在导入前调整 `chat_profile_id` 对应的记忆上限配置**，否则可能会触发系统报错。
![记忆上限错误提示](docs/img/error.png)

### 6.3 获取记忆内容

您可能会担心以下情况：
- 一次性导入了大批量的历史对话
- 导入时记忆尚未完成生成

针对上述情况，我们设计了**兜底策略**：
如果记忆尚未生成，系统将返回您所配置的**最近 X 轮对话**作为记忆内容，其中 **X 即为您配置的记忆阈值**。

#### 调用示例
```java
package io.github.johntortoise.demo.controller;

import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.ReceiveMessageReq;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import io.github.johntortoise.core.enums.RoleEnum;

import java.util.List;

public class Test {

    public static String host = "http://localhost:8080";

    /**
     * 此 SK 请填写对应 chat_profile_id 所配置的 SK
     */
    public static String sk = "sk-428acddd-6ced-4e33-a9cb-ec96c980cf02";

    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(ChatCompletionResponse.class, host, sk);

    public static void main(String[] args) {
        List<Message> memory = tortoiseClient.getMemory("257386521051582337");
        System.out.println(memory);
    }
}





