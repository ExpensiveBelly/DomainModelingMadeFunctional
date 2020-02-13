package co.uk.domainmodelingmadefunktional

import arrow.core.*
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import java.net.URI
import java.util.*
import java.util.regex.Pattern

class String50 private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String): Validated<ValidationError, String50> =
            if (value.isBlank() || value.length > 50) {
                ValidationError("Length can't be empty or greater than 50").invalid()
            } else {
                String50(value).valid()
            }
    }
}

object Patterns {
    val EMAIL_ADDRESS = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}\$", Pattern.CASE_INSENSITIVE)
}

class EmailAddress private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String) =
            if (Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                EmailAddress(value).valid()
            } else {
                 ValidationError("Invalid email address").invalid()
            }
    }
}

class ZipCode private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String) =
            if (value.isDigitsOnly() && value.length == 5) {
                ZipCode(value).valid()
            } else {
                ValidationError("Only 5 digits allowed").invalid()
            }
    }
}

private fun String.isDigitsOnly() = toCharArray().all { Character.isDigit(it) }

class OrderId private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String) = OrderId(UUID.fromString(value).toString())
    }
}

class OrderLineId private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String) =
            if (value.isBlank() || value.length > 50) {
                OrderLineId(value).valid()
            } else {
                ValidationError("Length must be greather than 50").invalid()
            }
    }
}

sealed class ProductCode {

    companion object {
        operator fun invoke(value: String): Validated<ValidationError, ProductCode> =
            when {
                value.isBlank() -> ValidationError("ProductCode cannot be blank").invalid()
                value.startsWith("W") -> WidgetCode(value)
                value.startsWith("G") -> GizmoCode(value)
                else -> ValidationError("Format not recognised").invalid()
            }
    }

    class WidgetCode private constructor(val value: String) : ProductCode() {
        companion object {
            operator fun invoke(value: String) =
                if (value.isDigitsOnly() && value.length == 4) {
                    WidgetCode(value).valid()
                } else {
                    ValidationError("Only 4 digits").invalid()
                }
        }
    }

    class GizmoCode private constructor(val value: String) : ProductCode() {
        companion object {
            operator fun invoke(value: String) =
                if (value.isDigitsOnly() && value.length == 3) {
                    GizmoCode(value).valid()
                } else {
                    ValidationError("Only 4 digits").invalid()
                }
        }
    }
}

inline class Quantity(val value: Double)

sealed class OrderQuantity {

    companion object {
        operator fun invoke(productCode: ProductCode, quantity: Quantity): Validated<ValidationError, OrderQuantity> =
            when (productCode) {
                is ProductCode.WidgetCode -> UnitQuantity(quantity.value.toInt())
                is ProductCode.GizmoCode -> KilogramQuantity(quantity.value)
            }
    }

    class UnitQuantity private constructor(val value: Int) : OrderQuantity() {
        companion object {
            operator fun invoke(value: Int) =
                if (value in (1..1000)) UnitQuantity(value).valid() else ValidationError("Must be between 1 and 1000").invalid()
        }
    }

    class KilogramQuantity private constructor(val value: Double) : OrderQuantity() {
        companion object {
            operator fun invoke(value: Double) =
                if (value in (0.05..100.00)) KilogramQuantity(value).valid() else ValidationError("Must be between 0.05 and 100.00").invalid()
        }
    }
}


class Price private constructor(val value: Double) {
    companion object {
        operator fun invoke(value: Double) =
            if (value in (0.0..1000.00)) Price(value).valid() else ValidationError("Must be between 0 and 1000.00").invalid()
    }
}

class BillingAmount private constructor(val value: Double) {
    companion object {
        operator fun invoke(value: Double) =
            if (value in (0.0..1000.00)) BillingAmount(value).valid() else ValidationError("Must be between 0 and 1000.00").invalid()
    }
}

data class PdfAttachment(val name: String, val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PdfAttachment

        if (name != other.name) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

inline class HtmlString(val value: String)

data class OrderAcknowledgment(val emailAddress: EmailAddress, val letter: HtmlString)

data class ValidatedOrderLine(
    val orderLineId: OrderLineId,
    val productCode: ProductCode,
    val quantity: OrderQuantity
)

data class ValidatedOrder(
    val orderId: OrderId,
    val customerInfo: CustomerInfo,
    val shippingAddress: Address,
    val billingAddress: Address,
    val lines: List<ValidatedOrderLine>
)

sealed class AddressValidationError {
    object InvalidFormat
    object AddressNotFound
}

enum class SendResult {
    SEND, NOTSENT
}

class CheckedAddress private constructor()

data class UnvalidatedCustomerInfo(val firstName: String, val lastName: String, val emailAddress: String)

data class UnvalidatedAddress(
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,
    val addressLine4: String?,
    val city: String,
    val zipCode: String
)

data class UnvalidatedOrderLine(
    val orderLineId: String,
    val productCode: String,
    val quantity: Double
)

data class UnvalidatedOrder(
    val orderId: String,
    val customerInfo: UnvalidatedCustomerInfo,
    val shippingAddress: UnvalidatedAddress,
    val billingAddress: UnvalidatedAddress,
    val lines: List<UnvalidatedOrderLine>
)

data class PersonalName(val firstName: String50, val lastName: String50)
data class CustomerInfo(val name: PersonalName, val emailAddress: EmailAddress)

data class Address(
    val addressLine1: String50,
    val addressLine2: String50? = null,
    val addressLine3: String50? = null,
    val addressLine4: String50? = null,
    val city: String50,
    val zipCode: ZipCode
)

data class PricedOrderLine(
    val orderLineId: OrderLineId,
    val productCode: ProductCode,
    val quantity: OrderQuantity,
    val linePrice: Price
)

sealed class PlaceOrderEvent {
    data class OrderAcknowledgmentSent(val orderId: OrderId, val emailAddress: EmailAddress) : PlaceOrderEvent()

    data class BillableOrderPlaced(
        val orderId: OrderId,
        val billingAddress: Address,
        val amountToBill: BillingAmount
    ) : PlaceOrderEvent()

    data class PricedOrder(
        val orderId: OrderId,
        val customerInfo: CustomerInfo,
        val shippingAddress: Address,
        val billingAddress: Address,
        val amountToBill: BillingAmount,
        val lines: List<PricedOrderLine>
    ) : PlaceOrderEvent()
}

typealias OrderPlaced = PlaceOrderEvent.PricedOrder

class ValidationError(val value: String) : PlaceOrderError()

sealed class PlaceOrderError {
    data class RemoteServiceError(val service: ServiceInfo, val exception: Exception) : PlaceOrderError()

    data class PricingError(val error: String) : PlaceOrderError()
}

data class ServiceInfo(val name: String, val endpoint: URI)

