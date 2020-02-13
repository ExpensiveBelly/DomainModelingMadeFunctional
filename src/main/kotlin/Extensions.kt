package co.uk.domainmodelingmadefunktional

import arrow.core.*
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative

fun UnvalidatedOrderLine.toValidatedOrderLine(): Validated<Nel<ValidationError>, ValidatedOrderLine> {
    val orderQuantityValidated: Validated<NonEmptyList<ValidationError>, OrderQuantity> =
        Validated.applicative<Nel<ValidationError>>(Nel.semigroup())
            .tupled(
                ProductCode(productCode).toValidatedNel(),
                Quantity(quantity).valid().toValidatedNel()
            ).fix().fold({ it.invalid() }, { (a, b) ->
                OrderQuantity(a, b).toValidatedNel()
            })

    val validatedOrderValidated =
        orderQuantityValidated.fold({ it.invalid() },
            { orderQuantity ->
                val validatedOrderValidated = Validated.applicative<Nel<ValidationError>>(Nel.semigroup())
                    .tupled(
                        OrderLineId(orderLineId).toValidatedNel(),
                        ProductCode(productCode).toValidatedNel()
                    ).fix().fold({ it.invalid() },
                        { (orderLineId, productCode) ->
                            ValidatedOrderLine(orderLineId, productCode, orderQuantity).validNel()
                        })
                validatedOrderValidated
            })
    return validatedOrderValidated
}

fun UnvalidatedCustomerInfo.toCustomerInfo(): Validated<NonEmptyList<ValidationError>, CustomerInfo> =
    Validated.applicative<Nel<ValidationError>>(Nel.semigroup())
        .tupled(
            String50(firstName).toValidatedNel(),
            String50(lastName).toValidatedNel(),
            EmailAddress(emailAddress).toValidatedNel()
        ).fix().fold(
            { it.invalid() },
            { (firstName, lastName, emailAddress) ->
                CustomerInfo(PersonalName(firstName, lastName), emailAddress).validNel()
            })

fun UnvalidatedAddress.toAddress(): Validated<NonEmptyList<ValidationError>, Address> =
    Validated.applicative<Nel<ValidationError>>(Nel.semigroup())
        .tupled(
            String50(addressLine1).toValidatedNel(),
            String50(city).toValidatedNel(),
            ZipCode(zipCode).toValidatedNel()
        ).fix().fold(
            { it.invalid() },
            { (line1String50, cityString50, zipCode) ->
                val line2: Validated<ValidationError, String50>? = addressLine2?.let { String50(it) }
                val line3: Validated<ValidationError, String50>? = addressLine3?.let { String50(it) }
                val line4: Validated<ValidationError, String50>? = addressLine4?.let { String50(it) }

                Option.fromNullable(line2).map { validatedLine2 ->
                    Option.fromNullable(line3).map { validatedLine3 ->
                        Option.fromNullable(line4).map { validatedLine4 ->
                            Validated.applicative<Nel<ValidationError>>(Nel.semigroup()).tupled(
                                validatedLine2.toValidatedNel(),
                                validatedLine3.toValidatedNel(),
                                validatedLine4.toValidatedNel()
                            ).fix().fold({ it.invalid() }, { (line2String50, line3String50, line4String50) ->
                                Address(
                                    line1String50,
                                    line2String50,
                                    line3String50,
                                    line4String50,
                                    cityString50,
                                    zipCode
                                ).validNel()
                            })
                        }.getOrElse {
                            Validated.applicative<Nel<ValidationError>>(Nel.semigroup()).tupled(
                                validatedLine2.toValidatedNel(),
                                validatedLine3.toValidatedNel()
                            ).fix().fold({ it.invalid() }, { (line2String50, line3String50) ->
                                Address(
                                    addressLine1 = line1String50,
                                    addressLine2 = line2String50,
                                    addressLine3 = line3String50,
                                    city = cityString50,
                                    zipCode = zipCode
                                ).validNel()
                            })
                        }
                    }.getOrElse {
                        validatedLine2.toValidatedNel().fold(
                            { it.invalid() },
                            {
                                Address(
                                    addressLine1 = line1String50,
                                    addressLine2 = it,
                                    city = cityString50,
                                    zipCode = zipCode
                                ).validNel()
                            })
                    }
                }.getOrElse {
                    Address(
                        addressLine1 = line1String50,
                        city = cityString50,
                        zipCode = zipCode
                    ).valid().toValidatedNel()
                }
            })
