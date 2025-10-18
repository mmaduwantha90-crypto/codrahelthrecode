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
import com.health.contracts.PatientContract.Commands.AddRecord
import com.health.states.PatientRecordState
import java.time.Instant

/**
 * A flow to add a new record to an existing patient.  The caller supplies the patient's
 * linearId as a string, along with a title and value for the record.  The flow
 * returns the unique identifier of the newly created record state.
 */
@StartableByRPC
class AddRecordFlow(
    private val patientId: String,
    private val title: String,
    private val value: String
) : FlowLogic<UniqueIdentifier>() {

    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction for new record.")
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
        // Step 1. Build the transaction.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val recordId = UniqueIdentifier()
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val patientLinearId = UniqueIdentifier.fromString(patientId)
        val outputState = PatientRecordState(
            recordId = recordId,
            patientId = patientLinearId,
            title = title,
            value = value,
            createdBy = ourIdentity,
            createdAt = Instant.now()
        )
        val command = Command(AddRecord(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(outputState, PatientContract.ID)
            .addCommand(command)

        // Step 2. Verify.
        txBuilder.verify(serviceHub)

        // Step 3. Sign.
        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Step 4. Finalise.
        progressTracker.currentStep = FINALISING_TRANSACTION
        subFlow(FinalityFlow(signedTx, emptyList()))

        return recordId
    }
}