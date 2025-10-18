package com.health.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.time.Instant
import com.health.contracts.PatientContract

/**
 * Represents a patient in the health records system.  Each patient is uniquely
 * identified by a [patientId] and contains basic attributes such as a name and
 * address.  The [owner] of the state is typically the node that created the patient.
 */
@BelongsToContract(PatientContract::class)
data class PatientState(
    val patientId: UniqueIdentifier,
    val name: String,
    val address: String,
    val owner: Party,
    val createdAt: Instant,
    override val participants: List<AbstractParty> = listOf(owner)
) : LinearState {
    override val linearId: UniqueIdentifier get() = patientId
}