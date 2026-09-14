package com.itcjj.campusmart.exception;

import com.itcjj.campusmart.common.CodeEnum;

public class BizException extends RuntimeException{
    private final CodeEnum codeEnum;

    public BizException(CodeEnum codeEnum){
        super(codeEnum.getMsg());
        this.codeEnum=codeEnum;
    }

    public CodeEnum getCodeEnum(){
        return codeEnum;
    }
}
