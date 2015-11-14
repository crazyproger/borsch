package net.crazyproger.borsch.rpc

import net.crazyproger.borsch.rpc.BusinessErrors.Error

abstract class BusinessException(val protoError: Error, private val msg: String = "") : RuntimeException(msg)

class DuplicateNameException() : BusinessException(BusinessErrors.Error.DUPLICATE_NAME)
class RestrictedException() : BusinessException(BusinessErrors.Error.RESTRICTED)
class NotFoundException() : BusinessException(BusinessErrors.Error.NOT_FOUND)

private val factoryFor: Map<BusinessErrors.Error, Function0<BusinessException>> = mapOf(
        (BusinessErrors.Error.DUPLICATE_NAME to ::DuplicateNameException),
        (BusinessErrors.Error.RESTRICTED to ::RestrictedException),
        (BusinessErrors.Error.NOT_FOUND to ::NotFoundException)
)

fun BusinessErrors.Error.toException(): BusinessException = factoryFor[this]?.invoke() ?: object : BusinessException(this) {}