package co.uk.domainmodelingmadefunktional

import arrow.core.*
import arrow.core.extensions.list.foldable.traverse_
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.core.extensions.validated.foldable.traverse_
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.syntax.function.curried
import arrow.syntax.function.partially1
import arrow.syntax.function.partially2

private val validated =
    UnvalidatedOrderLine("orderlineId", "W123", 1.0).toValidatedOrderLine()
        .traverse(ValidatedNel.applicative(Nel.semigroup<ValidationError>())) { it.validNel() }.fix()

private data class User(val id: String, val name: String)

private val listOfUsers = listOf(
    User("1", "Jimmy"),
    User("2", "Peter"),
    User("3", "Rob")
).k()

private fun method(a: String, b: String, c: String, d: String): String { return a + b + c + d }

fun main() {
    listOfUsers.traverse_(IO.monad()) { it.log() }
    val curried = IPlaceOrder::placeOrder.curried()

    val partially1 = ::method.partially1("Hello").partially1(" World").partially1(" again")
    println(partially1(" guys!"))
}


private fun User.log() = IO.unit.also { println(toString()) }
