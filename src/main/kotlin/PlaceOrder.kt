package co.uk.domainmodelingmadefunktional

import arrow.core.Either
import kotlinx.coroutines.Deferred

interface IPlaceOrder {

    fun checkAddressExists(unvalidatedAddress: UnvalidatedAddress): Deferred<Either<AddressValidationError, CheckedAddress>>

    fun placeOrder(
        checkProductCodeExist: (productCode: ProductCode) -> Boolean = ::checkProductCodeExist,
        checkAddressExists: (unvalidatedAddress: UnvalidatedAddress) -> Deferred<Either<CheckedAddress, AddressValidationError>>,
        getProductPrice: (ProductCode) -> Price,
        createOrderAcknowledgmentLetter: (pricedOrder: PlaceOrderEvent.PricedOrder) -> HtmlString,
        sendOrderAcknowledgment: (orderAcknowledgment: OrderAcknowledgment) -> SendResult,
        unvalidatedOrder: UnvalidatedOrder
    ): Deferred<Either<PlaceOrderError, List<PlaceOrderEvent>>>

    fun validateOrder(unvalidatedOrder: UnvalidatedOrder): Deferred<Either<ValidationError, ValidatedOrder>>

    fun priceOrder(validatedOrder: ValidatedOrder): Either<PlaceOrderError.PricingError, PlaceOrderEvent.PricedOrder>

    fun createOrderAcknowledgmentLetter(pricedOrder: PlaceOrderEvent.PricedOrder): HtmlString

    fun acknowledgeOrder(pricedOrder: PlaceOrderEvent.PricedOrder): PlaceOrderEvent.OrderAcknowledgmentSent?

    fun createEvents(
        pricedOrder: PlaceOrderEvent.PricedOrder,
        orderAcknowledgmentSent: PlaceOrderEvent.OrderAcknowledgmentSent?
    ): List<PlaceOrderEvent>
}

fun getProductPrice() = { productCode: ProductCode -> Price }

fun checkProductCodeExist(productCode: ProductCode) = true

fun checkProductCodeExists(productCode: ProductCode) = true