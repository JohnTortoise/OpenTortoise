package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.johntortoise.core.consts.HTTPConst;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.ReceiveMessageReq;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.model.TortoiseConversation;
import io.github.johntortoise.model.TortoiseLlmConfig;
import io.github.johntortoise.model.TortoiseUser;
import io.github.johntortoise.service.*;
import io.github.johntortoise.utils.SimpleLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;



@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    @Resource
    private TortoiseMessageService tortoiseMessageService;

    @Resource
    private TortoiseLlmUsageService tortoiseLlmUsageService;

    @Resource
    private TortoiseLlmConfigService tortoiseLlmConfigService;

    @Resource
    private TortoiseConversationService tortoiseConversationService;

    @Resource
    private TortoiseUserService tortoiseUserService;

    @Resource
    private TortoiseMemoryPolicyService tortoiseMemoryPolicyService;

    @Resource
    private TortoiseChatProfileService tortoiseChatProfileService;

    @Resource
    private TortoiseMemoryService tortoiseMemoryService;

    @Resource
    private TortoiseTokenTotalRecordService tortoiseTokenTotalRecordService;

    @GetMapping("connect")
    public ResponseEntity<Boolean> connect(){
        return ResponseEntity.ok(true);
    }


    @GetMapping("createConversation")
    public ResponseEntity<String> createConversation(
            @RequestParam @NotBlank(message = "客户ID不能为空") String customerId,
            @RequestHeader(name = HTTPConst.X_API_KEY) String sk) {
        String key = sk+customerId+"createConversation";
        boolean lockFlag = false;
        try {
            if(SimpleLock.tryLock(key,60)){
                lockFlag = true;
                LogUtil.info("创建对话: customerId={}, sk={}", customerId, sk);

                TortoiseChatProfile tortoiseChatProfile = tortoiseChatProfileService.findBySK(sk);
                if (Objects.isNull(tortoiseChatProfile)) {
                    LogUtil.warn("SK不存在或未启用: sk={}", sk);
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "SK不存在或未启用");
                }

                TortoiseLlmConfig config = tortoiseLlmConfigService.getById(tortoiseChatProfile.getLlmConfigId());
                if (Objects.isNull(config)) {
                    LogUtil.warn("模型不存在: llmConfigId={}", tortoiseChatProfile.getLlmConfigId());
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "模型不存在");
                }

                Long userId;
                TortoiseUser tortoiseUser = tortoiseUserService.getOne(new QueryWrapper<TortoiseUser>().lambda()
                        .eq(TortoiseUser::getCustomId, customerId));
                if (Objects.isNull(tortoiseUser)) {
                    userId = tortoiseUserService.createUser(customerId);
                    LogUtil.info("创建新用户: userId={}, customerId={}", userId, customerId);
                } else {
                    userId = tortoiseUser.getId();
                }

                TortoiseConversation conversation = tortoiseConversationService.createConversation(userId, tortoiseChatProfile.getId());
                LogUtil.info("创建对话成功: conversationId={}, userId={}", conversation.getConversationId(), userId);

                return ResponseEntity.ok(conversation.getConversationId());
            }
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"获取锁失败");
        } catch (TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("创建对话失败: customerId={}, sk={}", customerId, sk, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "创建对话失败", e);
        }finally {
            if(lockFlag){
                SimpleLock.unlock(key);
            }
        }
    }

    @GetMapping("getModelByConversationId")
    public ResponseEntity<Model> findModelIdByModelName(
            @RequestParam @NotBlank(message = "对话ID不能为空") String conversationId) {
        try {
            LogUtil.debug("获取模型配置: conversationId={}", conversationId);
            
            TortoiseConversationDTO conversation = tortoiseConversationService.getByConversationId(conversationId);
            if (Objects.isNull(conversation)) {
                LogUtil.warn("会话不存在: conversationId={}", conversationId);
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "会话不存在");
            }

            TortoiseLlmConfig config = tortoiseLlmConfigService.getById(conversation.getLlmConfigId());
            if (Objects.isNull(config)) {
                LogUtil.warn("模型配置不存在: llmConfigId={}", conversation.getLlmConfigId());
                return ResponseEntity.ok(null);
            }

            return ResponseEntity.ok(new Model(config.getApiUrl(), config.getApiKey(), config.getModelName(),config.getTemperature()));
        } catch (TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("获取模型配置失败: conversationId={}", conversationId, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "获取模型配置失败", e);
        }
    }

    @GetMapping("/getMemoriesByConversationId")
    public ResponseEntity<List<Message>> getMemoriesByConversationId(
            @RequestParam @NotBlank(message = "对话ID不能为空") String conversationId) {
        try {
            LogUtil.debug("获取记忆: conversationId={}", conversationId);
            List<Message> memories = tortoiseMemoryService.getMemoriesByConversationId(conversationId);
            return ResponseEntity.ok(memories);
        } catch (Exception e) {
            LogUtil.error("获取记忆失败: conversationId={}", conversationId, e);
            throw e;
        }
    }


    @PostMapping("afterChat")
    @Async(value = "afterChatAsyncTaskExecutor")
    public void afterChat(@Valid @RequestBody AfterChatDTO afterChatDTO) {
        try {
            MainInfo mainInfo = afterChatDTO.getMainInfo();
            String conversationId = afterChatDTO.getConversationId();

            LogUtil.debug("处理聊天后处理: conversationId={}", conversationId);
            
            if (mainInfo == null) {
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "主信息不能为空");
            }
            if (mainInfo.getInput() == null || mainInfo.getOutPut() == null) {
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "输入或输出消息不能为空");
            }

            Message input = new Message(mainInfo.getInput().getContent(), mainInfo.getInput().getRole());
            Message outPut = new Message(mainInfo.getOutPut().getContent(), mainInfo.getOutPut().getRole());

            
            tortoiseMessageService.writeMessagesAndRecordUsage(conversationId, input, outPut,afterChatDTO);
            
            memoryReconstructionByPolicy(conversationId);

            LogUtil.debug("聊天后处理完成: conversationId={}", conversationId);
        } catch (TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("聊天后处理失败: conversationId={}", afterChatDTO.getConversationId(), e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "聊天后处理失败", e);
        }
    }


    @PostMapping("receiveMessage")
    @Async(value = "afterChatAsyncTaskExecutor")
    public void receiveMessage(@RequestBody ReceiveMessageReq req){
        String conversationId = req.getConversationId();
        Message input = req.getInput();
        Message outPut = req.getOutPut();
        if(EmptyUtil.isEmpty(conversationId)){
            return;
        }
        if(EmptyUtil.isEmpty(input) || EmptyUtil.isEmpty(outPut)){
            return;
        }
        tortoiseMessageService.writeMessagesAndRecordUsage(conversationId, input, outPut,null);
        memoryReconstructionByPolicy(conversationId);
    }

    public void memoryReconstructionByPolicy(String conversationId){
        TortoiseConversationDTO tortoiseConversationDTO = tortoiseConversationService.getByConversationId(conversationId);
        Long memoryPolicyId = tortoiseConversationDTO.getMemoryPolicyId();
        if (Objects.nonNull(memoryPolicyId)) {
            tortoiseMemoryPolicyService.memoryReconstructionByPolicy(conversationId, memoryPolicyId);
        }
    }


    @GetMapping("checkLimit")
    public ResponseEntity<Boolean> checkLimit(@RequestParam @NotBlank(message = "对话ID不能为空") String conversationId) {
        return ResponseEntity.ok(tortoiseChatProfileService.checkLimit(conversationId));
    }

    @GetMapping("test")
    public Boolean test() throws InterruptedException {
        Thread.sleep(30000L);
        return true;
    }




}
