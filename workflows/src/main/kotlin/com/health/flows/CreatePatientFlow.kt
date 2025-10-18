package com.health.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import com.health.contracts.PatientContract
import com.health.contracts.PatientContract.Commands.CreatePatient
import com.health.states.PatientState
import java.time.Instant

/**
 * A flow to create a new patient on the ledger.  The caller supplies a name and address,
 * and the flow returns the unique identifier of the newly created patient state.
 */
@StartableByRPC
class CreatePatientFlow(
    private val name: String,
    private val address: String
) : FlowLogic<UniqueIdentifier>() {

    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction for new patient.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : ProgressTracker.Step("Finalising transaction.")

        fun tracker() = ProgressTracker(
            GENERATING_TRANSACTION,
            SIGNING_TRANSACTION,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): UniqueIdentifier {
        // Step 1. Generate an unsigned transaction.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val patientId = UniqueIdentifier()
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = PatientState(
            patientId = patientId,
            name = name,
            address = address,
            owner = ourIdentity,
            createdAt = Instant.now()
        )
        val command = Command(CreatePatient(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(outputState, PatientContract.ID)
            .addCommand(command)

        // Step 2. Verify the transaction.
        txBuilder.verify(serviceHub)

        // Step 3. Sign the transaction.
        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Step 4. Finalise the transaction (no counterparty).
        progressTracker.currentStep = FINALISING_TRANSACTION
        subFlow(FinalityFlow(signedTx, emptyList()))

        return patientId
    }
}