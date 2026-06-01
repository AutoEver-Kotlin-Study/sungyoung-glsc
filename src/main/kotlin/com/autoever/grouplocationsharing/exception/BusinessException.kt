package com.autoever.grouplocationsharing.exception

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
