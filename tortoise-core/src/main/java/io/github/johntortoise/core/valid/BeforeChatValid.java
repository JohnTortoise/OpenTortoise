package io.github.johntortoise.core.valid;

import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;

public class BeforeChatValid {
    public static <T> void checkInit(Model model, TortoiseClient<T> tortoiseClient){
        if(EmptyUtil.isEmpty(tortoiseClient.getTortoiseChatManger())){
            throw new TortoiseBusinessException(ErrorCodeEnum.INIT_CLIENT_ERROR,"chatManger cannot be empty");
        }
        if(EmptyUtil.isEmpty(tortoiseClient.getTortoiseMessageHandler())){
            throw new TortoiseBusinessException(ErrorCodeEnum.INIT_CLIENT_ERROR,"messageHandler cannot be empty");
        }

        if(EmptyUtil.isEmpty(model)){
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"model is not empty");

        }
        if (EmptyUtil.isEmpty(model.getApiKey())) {
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"apiKey");
        }

        if (EmptyUtil.isEmpty(model.getCompleteUrl())) {
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"completeUrl");
        }


    }

    public static void validate(TortoiseMessage tortoiseMessage){
        if(EmptyUtil.isEmpty(tortoiseMessage)){
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"tortoiseMessage");
        }
        if (EmptyUtil.isEmpty(tortoiseMessage.getConversationId())) {
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"conversationId");
        }
        if (EmptyUtil.isEmpty(tortoiseMessage.getMessage())) {
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"message");
        }
        if (EmptyUtil.isEmpty(tortoiseMessage.getMessage().getContent())) {
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"message content");
        }
        if (EmptyUtil.isEmpty(tortoiseMessage.getMessage().getRole())) {
            throw new TortoiseBusinessException(ErrorCodeEnum.PARAMS_EMPTY_ERROR,"message role");
        }
    }
}
