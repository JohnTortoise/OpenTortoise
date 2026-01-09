package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.dto.TortoiseLlmConfigDTO;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.enums.EnabelEnum;
import io.github.johntortoise.generate.ApiKeyGenerator;
import io.github.johntortoise.mapper.TortoiseChatProfileMapper;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.model.TortoiseConversation;
import io.github.johntortoise.model.TortoiseTokenTotalRecord;
import io.github.johntortoise.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service
@Slf4j
public class TortoiseChatProfileServiceImpl extends ServiceImpl<TortoiseChatProfileMapper, TortoiseChatProfile> implements TortoiseChatProfileService {

    @Resource
    private TortoiseConversationService tortoiseConversationService;

    @Resource
    private TortoiseMemoryPolicyService tortoiseMemoryPolicyService;

    @Resource
    private TortoiseLlmConfigService tortoiseLlmConfigService;

    @Resource
    @Lazy
    private TortoiseTokenTotalRecordService tortoiseTokenTotalRecordService;


    @Override
    public Page<TortoiseChatProfile> page(String name, Long current, Long size,Boolean enable) {
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        
        LambdaQueryWrapper<TortoiseChatProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TortoiseChatProfile::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseChatProfile::getUpdateTime);
        if (EmptyUtil.isNotEmpty(name)) {
            queryWrapper.like(TortoiseChatProfile::getName, name);
        }
        if(EmptyUtil.isNotEmpty(enable)){
            if(enable){
                queryWrapper.eq(TortoiseChatProfile::getStatus,EnabelEnum.OPEN.getCode());
            }else {
                queryWrapper.eq(TortoiseChatProfile::getStatus,EnabelEnum.CLOSE.getCode());
            }
        }
        return this.baseMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    @Override
    public void addOrUpdate(TortoiseChatProfile tortoiseChatProfile) {
        if (tortoiseChatProfile == null) {
            throw new IllegalArgumentException("聊天配置不能为空");
        }
        if (tortoiseChatProfile.getName() == null || tortoiseChatProfile.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("配置名称不能为空");
        }
        if (tortoiseChatProfile.getLlmConfigId() == null) {
            throw new IllegalArgumentException("模型配置ID不能为空");
        }
        if (tortoiseChatProfile.getMemoryPolicyId() == null) {
            throw new IllegalArgumentException("记忆策略ID不能为空");
        }
        
        try {
            TortoiseChatProfile chatProfile = TortoiseChatProfile.builder()
                    .id(tortoiseChatProfile.getId())
                    .name(tortoiseChatProfile.getName())
                    .llmConfigId(tortoiseChatProfile.getLlmConfigId())
                    .memoryPolicyId(tortoiseChatProfile.getMemoryPolicyId())
                    .sk(tortoiseChatProfile.getSk())
                    .dailyTokenLimitConversation(tortoiseChatProfile.getDailyTokenLimitConversation())
                    .dailyTokenLimitTotal(tortoiseChatProfile.getDailyTokenLimitTotal())
                    .status(tortoiseChatProfile.getStatus())
                    .createUserId(TortoiseContext.getCurrentUserId())
                    .isDeleted(DeletedEnum.EXIST.getCode())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            TortoiseChatProfile tortoiseChatProfileDB = null;
            if (Objects.nonNull(tortoiseChatProfile.getId())) {
                tortoiseChatProfileDB = this.getById(tortoiseChatProfile.getId());
                if (tortoiseChatProfileDB == null) {
                    throw new io.github.johntortoise.core.exceptions.TortoiseBusinessException(
                            io.github.johntortoise.core.enums.ErrorCodeEnum.BUSINESS_ERROR, "聊天配置不存在");
                }
            } else {
                
                chatProfile.setSk(ApiKeyGenerator.generateUUIDKey());
            }
            this.saveOrUpdate(chatProfile);
        } catch (io.github.johntortoise.core.exceptions.TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("保存或更新聊天配置失败: id={}, name={}",
                    tortoiseChatProfile.getId(), tortoiseChatProfile.getName(), e);
            throw new io.github.johntortoise.core.exceptions.TortoiseBusinessException(
                    io.github.johntortoise.core.enums.ErrorCodeEnum.BUSINESS_ERROR, "保存或更新聊天配置失败", e);
        }
    }

    public void batchMemoryReconstructionByPolicy(Long tortoiseChatProfileId,Long memoryPolicyId){

        int batchSize = 1000;

        TortoiseConversation minConversation = tortoiseConversationService.getMinIdByChatProfileId(tortoiseChatProfileId);

        TortoiseConversation maxConversation = tortoiseConversationService.getMaxIdByChatProfileId(tortoiseChatProfileId);


        if(Objects.isNull(minConversation) || Objects.isNull(maxConversation)){
            return;
        }

        Long minId = minConversation.getId();
        Long maxId = maxConversation.getId();

        for (long currentMinId = minId; currentMinId <= maxId; currentMinId += batchSize) {
            long currentMaxId = Math.min(currentMinId + batchSize - 1, maxId);

            List<TortoiseConversation> list = tortoiseConversationService.listByChatProfileIdAndIdRange(
                    tortoiseChatProfileId, currentMinId, currentMaxId);

            if(EmptyUtil.isEmpty(list)){
                return;
            }

            List<String> conversationIds = list.stream().map(TortoiseConversation::getConversationId)
                    .collect(Collectors.toList());
            for (String conversationId : conversationIds) {
                try {
                    tortoiseMemoryPolicyService.memoryReconstructionByPolicy(conversationId, memoryPolicyId);
                } catch (Exception e) {
                    LogUtil.error("重构会话id"+conversationId+"记忆出错",e);
                }
            }
        }
    }


    @Override
    public TortoiseChatProfile findBySK(String sk) {
        if (sk == null || sk.trim().isEmpty()) {
            throw new IllegalArgumentException("SK不能为空");
        }
        
        try {
            return this.getOne(new QueryWrapper<TortoiseChatProfile>().lambda()
                    .eq(TortoiseChatProfile::getSk, sk)
                    .eq(TortoiseChatProfile::getStatus, 1));
        } catch (Exception e) {
            LogUtil.error("根据SK查找聊天配置失败: sk={}", sk, e);
            throw e;
        }
    }

    @Override
    public List<TortoiseLlmConfigDTO> searchModel(String keyword) {
        if (keyword == null) {
            keyword = "";
        }
        
        try {
            Page<TortoiseLlmConfigDTO> configListByPage = tortoiseLlmConfigService.getConfigListByPageAsDTO(1L, 10L, "", keyword);
            return configListByPage.getRecords().stream().filter(a->a.getStatus().equals(EnabelEnum.OPEN.getCode())).collect(Collectors.toList());
        } catch (Exception e) {
            LogUtil.error("搜索模型失败: keyword={}", keyword, e);
            throw e;
        }
    }

    @Override
    public List<TortoiseMemoryPolicyDTO> searchMemoryPolicy(String keyword) {
        if (keyword == null) {
            keyword = "";
        }
        
        try {
            Page<TortoiseMemoryPolicyDTO> page = tortoiseMemoryPolicyService.page(keyword, 1L, 10L);
            return page.getRecords();
        } catch (Exception e) {
            LogUtil.error("搜索记忆策略失败: keyword={}", keyword, e);
            throw e;
        }
    }

    @Async(value = "batchRefreshMemoryExecutor")
    public void refreshMemory(Long tortoiseChatProfileId){
        TortoiseChatProfile tortoiseChatProfile = this.getById(tortoiseChatProfileId);
        if(Objects.nonNull(tortoiseChatProfile)){
            Long memoryPolicyId = tortoiseChatProfile.getMemoryPolicyId();
            batchMemoryReconstructionByPolicy(tortoiseChatProfileId,memoryPolicyId);
        }
    }

    @Override
    public Boolean checkLimit(String conversationId){
        try {
            LogUtil.debug("检查限制: conversationId={}", conversationId);

            TortoiseConversationDTO conversationDTO = tortoiseConversationService.getByConversationId(conversationId);
            Long tortoiseChatProfileId = conversationDTO.getTortoiseChatProfileId();

            TortoiseChatProfile tortoiseChatProfile = this.getById(tortoiseChatProfileId);
            if (tortoiseChatProfile == null) {
                LogUtil.warn("聊天配置不存在: chatProfileId={}", tortoiseChatProfileId);
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "聊天配置不存在");
            }

            
            Long dailyTokenLimitTotal = tortoiseChatProfile.getDailyTokenLimitTotal();
            if (dailyTokenLimitTotal != null && dailyTokenLimitTotal > 0) {
                TortoiseTokenTotalRecord chatProfileRecord =
                        tortoiseTokenTotalRecordService.queryIfNoExistAutoCreateByChatProfileId(tortoiseChatProfile.getId());

                if (chatProfileRecord.getTotal() >= dailyTokenLimitTotal) {
                    LogUtil.info("达到总日上限: conversationId={}, total={}, limit={}",
                            conversationId, chatProfileRecord.getTotal(), dailyTokenLimitTotal);
                    return true;
                }
            }

            
            Long dailyTokenLimitConversation = tortoiseChatProfile.getDailyTokenLimitConversation();
            if (dailyTokenLimitConversation != null && dailyTokenLimitConversation > 0) {
                TortoiseTokenTotalRecord conversationRecord =
                        tortoiseTokenTotalRecordService.queryIfNoExistAutoCreateByConversationId(conversationId);

                if (conversationRecord.getTotal() >= dailyTokenLimitConversation) {
                    LogUtil.info("达到会话上限: conversationId={}, total={}, limit={}",
                            conversationId, conversationRecord.getTotal(), dailyTokenLimitConversation);
                    return true;
                }
            }

            return false;
        } catch (TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("检查限制失败: conversationId={}", conversationId, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "检查限制失败", e);
        }
    }
}